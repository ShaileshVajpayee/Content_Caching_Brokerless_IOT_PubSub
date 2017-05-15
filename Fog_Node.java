import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.HashMap;

/**
 * Created by shaileshvajpayee
 */
public class Fog_Node {
    private static HashMap<String, String> attr_to_owners;
    private static DatagramSocket Send_socket;
    private static DatagramSocket Listen_socket;
    private static Logger logger;
    private static String my_IP;

    public Fog_Node() {
        attr_to_owners = new HashMap<>();
        logger = new Logger("Fog_logs.txt");
    }

    /**
     * Used to begin the threads
     */
    private static void begin_threads() {
        Communicator comm = new Communicator();
        new Thread(comm).start();
    }

    /**
     * The main function of this class
     * @param args: ListenPort
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            System.out.println("Please enter arguments:$ ListenPort : 9000");
            System.exit(0);
        }
        Fog_Node fog = new Fog_Node();
        Listen_socket = new DatagramSocket(Integer.parseInt(args[0]));
        Send_socket = new DatagramSocket();
        my_IP = InetAddress.getLocalHost().getHostAddress();
        logger.logMessage("Logging to file Fog_logs.txt");
        logger.logMessage("Fog Node initialized at " + my_IP + " " + args[0]);
        begin_threads();
    }


    /**
     * This is a Thread class which is the communicator at the Fog Node
     */
    private static class Communicator implements Runnable {

        /**
         * This function is used to send a message
         * @param IP Destination IP
         * @param Port Destination Port
         * @param msg Message to be sent
         */
        private void sendMessage(String IP, int Port, String msg) {
            byte[] byte_stream = msg.getBytes();
            InetAddress inetAddress;
            try {
                inetAddress = InetAddress.getByName(IP);
                DatagramPacket p = new DatagramPacket(byte_stream, byte_stream.length, inetAddress, Port);
                Send_socket.send(p);
                logger.logMessage(msg + " -> sent to " + IP + " " + Port);
            } catch (Exception e) {
                e.printStackTrace();
                logger.logMessage(e.toString());
            }
        }

        /**
         * This function controls the communication with subscribers
         * @param data The data received from subscriber
         */
        private void subscriber_communication(String[] data) { //ADD MEMBER TO GROUP PACKET
            // analyze the sub_comm_packet
            Subscriber_Comm_Packet_Analyzer pkt_analyzer = new Subscriber_Comm_Packet_Analyzer();
            pkt_analyzer.init_packet(data);

//            String attr = data[1];// + " " + data[2] + " " + data[3];
            logger.logMessage("Received msg from sub --> " + pkt_analyzer.packet.print_packet());
            logger.logMessage("Received subscriber req for " + pkt_analyzer.packet.attr.get_lhs() + " from subscriber " + pkt_analyzer.packet.sub_IP + " " + pkt_analyzer.packet.sub_Port);

            if (attr_to_owners.containsKey(pkt_analyzer.packet.attr.lhs)) { // if attr owner available
                pkt_analyzer.packet.add_group_member = true;
                pkt_analyzer.packet.group_member = pkt_analyzer.packet.sub_IP + " " + pkt_analyzer.packet.sub_Port;
                pkt_analyzer.packet.msg_from_fog = true;
                pkt_analyzer.packet.msg_from_leader = false;
                pkt_analyzer.packet.msg_from_sub = false;

                String[] owner = attr_to_owners.get(pkt_analyzer.packet.attr.lhs).split(" ");

                sendMessage(owner[0], Integer.parseInt(owner[1]), pkt_analyzer.packet.toString());
                logger.logMessage("msg : " + pkt_analyzer.packet.print_packet() + " -> sent to " + owner[0] + " " + owner[1]);
            } else { // else make subscriber the owner
                logger.logMessage("No owner at fog for : " + pkt_analyzer.packet.attr.get_lhs() + "\n-- adding owner now!");
                pkt_analyzer.packet.you_are_leader = false;
                pkt_analyzer.packet.msg_from_fog = true;
                pkt_analyzer.packet.msg_from_sub = false;
                sendMessage(pkt_analyzer.packet.sub_IP, pkt_analyzer.packet.sub_Port, pkt_analyzer.packet.toString());
                logger.logMessage("msg sent -->" + pkt_analyzer.packet.print_packet());
                attr_to_owners.put(pkt_analyzer.packet.attr.get_lhs(), pkt_analyzer.packet.sub_IP + " " + pkt_analyzer.packet.sub_Port);
                logger.logMessage("sub made owner notif sent to ->" + pkt_analyzer.packet.sub_IP + " " + pkt_analyzer.packet.sub_Port);
            }
        }

        /**
         * This function controls communication with publishers.
         * @param data
         * @param IP
         */
        private void publisher_communication(String[] data, String IP) {
            logger.logMessage("Received published info " + data[1] + " " + data[2] + " " + data[3] + " from publisher " + IP);
            if (attr_to_owners.get(data[1]) != null) {
                String[] IP_Port = attr_to_owners.get(data[1]).split(" ");
                Attribute attrib = new Attribute();
                attrib.set_attr(data[1], data[2],data[3]);
                Subscriber_Comm_Packet pkt = new Subscriber_Comm_Packet(
                        IP_Port[0],
                        Integer.parseInt(IP_Port[1]),
                        attrib,
                        true,
                        false,
                        "none",
                        attrib.toString(),
                        true,
                        false,
                        false);
//                System.out.println(pkt.print_packet());
                sendMessage(IP_Port[0], Integer.parseInt(IP_Port[1]), pkt.toString());
                logger.logMessage(data[1] + " " + data[2] + " " + data[3] + " pub msg forwared to owner " + IP_Port[0] + " " + IP_Port[1]);
            }
        }

        /**
         * Parse the received msg
         * @param p
         */
        private void parse_msg(DatagramPacket p) {
            String received_data = new String(Arrays.copyOfRange(p.getData(), 0, p.getLength()));
            String[] data = received_data.split(" ");
//            System.out.println(received_data);
            if (data[0].equals("0")) { // 0 means subscriber
                subscriber_communication(data);
            } else {
                publisher_communication(data, p.getAddress().getHostAddress());
            }
        }

        @Override
        public void run() {
            try {
                byte[] bytes;
                while (true) {
                    bytes = new byte[1024];
                    DatagramPacket p = new DatagramPacket(bytes, bytes.length);
                    Listen_socket.receive(p);
                    parse_msg(p);
                }
            } catch (Exception e) {
                e.printStackTrace();
                logger.logMessage(e.toString());
            }
        }
    }
}