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

    public void make_me_leader(boolean leader_left) { // If no group or Leader left and is 1st coleader
        if (leader_left) {
            // get leader lists and update
        } else {

        }
    }

    public void find_node(Attribute attr) {
        //contact router, if not in router contact fog node
    }

    public void add_attr() {
        Attribute attr = new Attribute();
        attr.get_attr(); // take input from user
        attrs.add(attr); // add to list
        find_node(attr);
        logger.logMessage("Attribute added : " + attr.toString());
    }

    public void print_attr() {
        for (Attribute attribute : attrs) {
            System.out.println(attribute.toString());
        }
    }

    private static class Communicator implements Runnable { // will have separate port

        private void msgAnalyzer() {

        }

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

        private void parse_msg(DatagramPacket p) {
            String received_data = new String(Arrays.copyOfRange(p.getData(), 0, p.getLength()));
            String[] data = received_data.split(" ");
            Subscriber_Comm_Packet_Analyzer pkt_analyzer = new Subscriber_Comm_Packet_Analyzer();
            pkt_analyzer.init_packet(data);

            logger.logMessage("Received packet : " + pkt_analyzer.packet.print_packet());

            if (pkt_analyzer.packet.msg_from_fog && !pkt_analyzer.packet.you_are_leader) {
                if (pkt_analyzer.packet.attr.toString().equals(attrs.get(0).toString())) { // if attr matched
                    Leader_Group_List.put(pkt_analyzer.packet.sub_IP, pkt_analyzer.packet.sub_Port);
                    logger.logMessage("Received msg from: " + pkt_analyzer.packet.sub_IP + " " + pkt_analyzer.packet.sub_Port);
                    logger.logMessage("Added to group with leader (me): " + IP + " " + Port);
                    pkt_analyzer.packet.msg_from_leader = true;
                    pkt_analyzer.packet.msg_from_fog = false;
                    pkt_analyzer.packet.msg_from_sub = false;
                    sendMessage(pkt_analyzer.packet.sub_IP, pkt_analyzer.packet.sub_Port, pkt_analyzer.packet.toString());
                } else {
                    for (String key : Leader_succ.keySet()) {
                        sendMessage(key, Leader_succ.get(key), pkt_analyzer.packet.toString());
                        logger.logMessage("Fowarded add_sub req to successors" + key + " " + Leader_succ.get(key));
                    }
                }
            } else if (pkt_analyzer.packet.msg_from_fog && pkt_analyzer.packet.you_are_leader) {
                logger.logMessage("Received notif from fog");
                leader = pkt_analyzer.packet.you_are_leader;

            } else if (data[0].equals("leader")) { // other subscribers for group
                //add subscribers to the leader list
            } else if (data[0].equals("pub")) {
                System.out.println(received_data);
            }
        }

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