import java.io.IOException;
import java.net.*;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedList;

/**
 * Created by shaileshvajpayee
 */
public class Subscriber {
    private static LinkedList<Attribute> attrs;
    private static LinkedHashMap<String, Integer> Leader_Group_List;
    private static LinkedHashMap<String, Integer> Leader_succ;
    private static LinkedHashMap<String, Integer> Leader_pred;
    private static boolean leader;
    private static boolean co_leader;
    private static int co_leader_number;
    private static Logger logger;
    private static String RouterIP;
    private static int RouterPort;
    private static String FogIP;
    private static int FogPort;
    private static DatagramSocket Listen_socket;
    private static DatagramSocket Send_socket;
    private static String IP;
    private static int Port;

    public Subscriber() {
        attrs = new LinkedList<>();
        logger = new Logger("Subscriber_Logs.txt");
        attrs = new LinkedList<>();
        Leader_Group_List = new LinkedHashMap<>();
        Leader_pred = new LinkedHashMap<>();
        Leader_succ = new LinkedHashMap<>();
        add_attr();
        try {
            IP = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    /**
     * The main function of the class
     * @param args ListenPort RouterIP RouterPort
     * @throws SocketException
     */
    public static void main(String[] args) throws SocketException {
        if (args.length < 3) {
            System.out.println("Please enter arguments: ListenPort RouterIP RouterPort");
            System.exit(0);
        }
        Port = Integer.parseInt(args[0]);
        Subscriber sb = new Subscriber();
        Listen_socket = new DatagramSocket(Port);
        Send_socket = new DatagramSocket();
        RouterIP = args[1];
        RouterPort = Integer.parseInt(args[2]);
        Communicator comm = new Communicator();
        new Thread(comm).start();
    }


    /**
     * add subscription
     */
    public void add_attr() {
        Attribute attr = new Attribute();
        attr.get_attr(); // take input from user
        attrs.add(attr); // add to list
        find_node(attr);
        logger.logMessage("Attribute added : " + attr.toString());
    }

    /**
     * Print subscription
     */
    public void print_attr() {
        for (Attribute attribute : attrs) {
            System.out.println(attribute.toString());
        }
    }

    /**
     * This Thread class is responsible for communication at subscriber
     */
    private static class Communicator implements Runnable {

        /**
         * Used to send message
         * @param IP destination IP
         * @param Port destination Port
         * @param msg message to be sent
         */
        private void sendMessage(String IP, int Port, String msg) {
            System.out.println("the msg is: " + msg);
            byte[] byte_stream = msg.getBytes();
            InetAddress inetAddress;
            try {
                inetAddress = InetAddress.getByName(IP);
                DatagramPacket p = new DatagramPacket(byte_stream, byte_stream.length, inetAddress, Port);
                Send_socket.send(p);
                logger.logMessage("Message: " + msg + " -> sent to " + IP + " " + Port);
            } catch (Exception e) {
                e.printStackTrace();
                logger.logMessage(e.toString());
            }
        }

        /**
         * Get the fog address from Router
         */
        private void get_FogAddr() {
            try { // 1 means request for fog node address device in subnet
                sendMessage(RouterIP, RouterPort, "1 " + Listen_socket.getLocalPort());
                logger.logMessage("Sent FogIP req: " + "1 " + Listen_socket.getLocalPort() + " -> to Router: " + RouterIP + " " + RouterPort);
                byte[] bytes = new byte[1024];
                DatagramPacket p = new DatagramPacket(bytes, bytes.length);
                Listen_socket.receive(p);
                String[] received_data = (new String(Arrays.copyOfRange(p.getData(), 0, p.getLength()))).split(" ");
                FogIP = received_data[0];
                FogPort = Integer.parseInt(received_data[1]);
                logger.logMessage("Received Fog info : " + FogIP + " " + FogPort);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        /**
         * Parse the received message
         * @param p the packet
         */
        private void parse_msg(DatagramPacket p) {
            String received_data = new String(Arrays.copyOfRange(p.getData(), 0, p.getLength()));
            String[] data = received_data.split(" ");
//            System.out.println(received_data);
            Subscriber_Comm_Packet_Analyzer pkt_analyzer = new Subscriber_Comm_Packet_Analyzer();
            pkt_analyzer.init_packet(data);

            logger.logMessage("Received packet : " + pkt_analyzer.packet.print_packet());

            if(pkt_analyzer.packet.msg_from_fog) {// if msg from fog
                if(pkt_analyzer.packet.add_group_member){
                    // if attr matches we add member to current group
                    // else forward this msg to successors... if no successors, add member as successor make you are leader true in packet1
                    if(pkt_analyzer.packet.attr.compare_attr(attrs.get(0), pkt_analyzer.packet.attr)){
                        String[] mem_addr = pkt_analyzer.packet.group_member.split(" ");
                        Leader_Group_List.put(mem_addr[0], Integer.parseInt(mem_addr[1]));
                        return;
                    }
                    else{
                        if(Leader_succ.size() > 0){
                            for (String key : Leader_succ.keySet()) {
                                sendMessage(key, Leader_succ.get(key), pkt_analyzer.packet.toString());
                                logger.logMessage("Forwarded add_sub req to successors: " + key + " " + Leader_succ.get(key));
                            }
                        }
                        else{
                            Leader_succ.put(pkt_analyzer.packet.sub_IP, pkt_analyzer.packet.sub_Port);
                            logger.logMessage("Added to successors: " + pkt_analyzer.packet.sub_IP + " " + pkt_analyzer.packet.sub_Port);
                        }
                    }
                    return;
                }

                if(pkt_analyzer.packet.you_are_leader && !leader)
                    leader = true; // first msg comm with fog, get to know if leader only if no owner at fog
                else if(pkt_analyzer.packet.pub_msg.length() > 0 && leader){ // if already leader and pub_msg, match attr and forward accordingly
                    // compare attributes
                    String[] msg = pkt_analyzer.packet.pub_msg.split(" ");
                    Attribute pub_attr = new Attribute();
                    pub_attr.set_attr(msg[0], msg[1], msg[2]);
                    if(pub_attr.compare_attr(attrs.get(0), pub_attr)){// if attr matched, send to all group members
                        for (String key : Leader_succ.keySet()) {
                            sendMessage(key, Leader_succ.get(key), pkt_analyzer.packet.toString());
                            logger.logMessage("Forwarded add_sub req to successors: " + key + " " + Leader_succ.get(key));
                        }
                        for(String IP:Leader_Group_List.keySet()){ // modify packet accordingly
                            pkt_analyzer.packet.msg_from_fog = false;
                            pkt_analyzer.packet.msg_from_leader = true;
                            pkt_analyzer.packet.msg_from_sub = false;
                            sendMessage(IP,Leader_Group_List.get(IP), pkt_analyzer.packet.toString());
                        }
                        logger.logMessage("Fowarded published msg group members: " + Leader_Group_List.toString());
                    }
                    else{ // if not matched, forward to successors.
                        for (String key : Leader_succ.keySet()) {
                            sendMessage(key, Leader_succ.get(key), pkt_analyzer.packet.toString());
                            logger.logMessage("Forwarded add_sub req to successors: " + key + " " + Leader_succ.get(key));
                        }
                    }
                }
                return;
            }

            if(pkt_analyzer.packet.msg_from_leader){ // if msg from leader ie owner already exists
                if(pkt_analyzer.packet.pub_msg.length() > 0) {// if pub_msg
                    logger.logMessage("Received published msg: " + pkt_analyzer.packet.pub_msg);
                }
                else{
                    logger.logMessage("Added to group with leader: " + pkt_analyzer.packet.sub_IP);

                }
                return;
            }

        }

        /**
         * Control fog communication
         * @param attribute subscription
         * @throws IOException
         */
        private void fog_communication(Attribute attribute) throws IOException {
            Subscriber_Comm_Packet pkt = new Subscriber_Comm_Packet(
                    IP,
                    Port,
                    attribute,
                    false,
                    false,
                    "none",
                    "none",
                    false,
                    false,
                    true);

            sendMessage(FogIP, FogPort, "0 " + pkt.toString());
            byte[] bytes = new byte[1024];
            DatagramPacket p = new DatagramPacket(bytes, bytes.length);
            Listen_socket.receive(p);
            parse_msg(p);
            // would like to subscribe to x attribute.
            // if no attr fog will create a new attribute with sub as owner and tell the subscriber
            // else it will send addr of owner in which case sub must contact owner.
        }

        @Override
        public void run() {
            try {
                get_FogAddr();
                for (int i = 0; i < attrs.size(); i++) {
                    fog_communication(attrs.get(i));
                } // finished registering for subscriptions... if leader of any group must forward publisher data which it receives.
                while (true) {
                    byte[] bytes = new byte[1024];
                    DatagramPacket p = new DatagramPacket(bytes, bytes.length);
                    Listen_socket.receive(p);
                    parse_msg(p);
                }
            } catch (IOException e) {
                e.printStackTrace();
                logger.logMessage(e.toString());
            }
        }
    }
}