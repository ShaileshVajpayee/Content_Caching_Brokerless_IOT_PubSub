import java.io.IOException;
import java.net.*;
import java.util.Arrays;
import java.util.HashMap;

/**
 * Created by shaileshvajpayee
 */
public class Router {
    private static HashMap<Attribute, Subscriber> attr_map; // attribute to owner(if available) of subnet. routes subs to owners
    private static HashMap<String, Integer> nearest_Fog_Nodes;
    private static String my_IP;
    private static Logger logger;
    private static DatagramSocket Send_socket;
    private static DatagramSocket Listen_socket;

    public Router() {
        attr_map = new HashMap<>();
        nearest_Fog_Nodes = new HashMap<>();
        my_IP = "";
        logger = new Logger("Router_logs.txt");
    }

    public void insert_Fog_Node_info(String IP, int port) { // fog listen thread
        nearest_Fog_Nodes.put(IP, port);
    }

    public void get_entry_from_owner(Attribute attr, Subscriber s) { // owners listen thread
        attr_map.put(attr, s);
    }

    public HashMap<String, Integer> provide_fog_table() { // request from subscriber
        return nearest_Fog_Nodes;
    }

    public static String print_Fog(){
        String table = "";
        for(String i:nearest_Fog_Nodes.keySet()){
            table += i + " " + nearest_Fog_Nodes.get(i);
        }
        return table;
    }

    public static void begin_threads(){
        Communicator comm = new Communicator();
        new Thread(comm).start();
    }

    /**
     * main function
     * @param args ListenPort FogIP FogPort
     * @throws UnknownHostException
     * @throws SocketException
     */
    public static void main(String[] args) throws UnknownHostException, SocketException {
        if(args.length < 3){
            System.out.println("Please enter arguments:$ ListenPort FogIP FogPort :");
            System.exit(0);
        }
        Router r = new Router();
        nearest_Fog_Nodes.put(args[1], Integer.parseInt(args[2]));
        Listen_socket = new DatagramSocket(Integer.parseInt(args[0]));
        Send_socket = new DatagramSocket();
        my_IP = InetAddress.getLocalHost().getHostAddress().toString();
        logger.logMessage("Logging to file Router_logs.txt");
        logger.logMessage("Router initialized at " + my_IP + " " + args[0]);
        begin_threads();
    }

    /**
     * Communicator class
     */
    private static class Communicator implements Runnable { // will have separate port

        private void sendMessage(String IP, int Port, String msg) {
            byte[] byte_stream = msg.getBytes();
            InetAddress inetAddress;
            try {
                inetAddress = InetAddress.getByName(IP);
                DatagramPacket p = new DatagramPacket(byte_stream, byte_stream.length, inetAddress, Port);
                Send_socket.send(p);
                System.out.println("Fog Address " + msg + " sent to " + IP + " " + Port);
            } catch (Exception e) {
                e.printStackTrace();
                logger.logMessage(e.toString());
            }
        }

        private void send_Fog_addr(String IP, int Port, String msg){
            String log_line = "Received request for Fog address from " + IP + " " + Port;
            logger.logMessage(log_line);
            sendMessage(IP, Port, msg);
        }

        private void parse_msg(DatagramPacket p){
            String received_data = new String(Arrays.copyOfRange(p.getData(), 0, p.getLength()));
            String[] data = received_data.split(" ");
            if (data[0].equals("1")) { // 1 means request for fog node IP address nearest to subnet
                send_Fog_addr(p.getAddress().getHostAddress(), Integer.parseInt(data[1]), print_Fog());
            }
        }


        @Override
        public void run() {
            try {
                while(true) {
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
