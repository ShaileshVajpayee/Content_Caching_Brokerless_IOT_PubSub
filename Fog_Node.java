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

    private static void begin_threads() {
        Communicator comm = new Communicator();
        new Thread(comm).start();
    }

    public static void main(String[] args) throws Exception{
        if(args.length < 1){
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


    private static class Communicator implements Runnable{

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

        private void subscriber_communication(String[] data, String IP, int Port){
            String attr = data[1] + " " + data[2] + " " + data[3];
            logger.logMessage("Received subscriber req " + attr + " from subscriber " + IP);
            if(attr_to_owners.containsKey(attr)){ // if attr owner available
                sendMessage(IP, Port, attr_to_owners.get(attr));
                logger.logMessage("owner : " + attr_to_owners.get(attr) + " -> sent to " + IP + " " + Port);
            }
            else{ // else make subscriber the owner
                sendMessage(IP, Port, "l");
                logger.logMessage("l -> sent to " + IP + " " + Port);
                attr_to_owners.put(attr,IP + " " + Port);
            }
        }

        private void publisher_communication(String[] data, String IP){
            logger.logMessage("Received published info " + data[1] + " " + data[2] + " " + data[3] + " from publisher " + IP);
        }

        private void parse_msg(DatagramPacket p){
            String received_data = new String(Arrays.copyOfRange(p.getData(), 0, p.getLength()));
            String[] data = received_data.split(" ");
//            System.out.println(received_data);
            if (data[0].equals("0")) { // 0 means subscriber
                subscriber_communication(data, p.getAddress().toString(), Integer.parseInt(data[4]));
            }
            else{
                publisher_communication(data, p.getAddress().toString());
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
            } catch(Exception e){
                e.printStackTrace();
                logger.logMessage(e.toString());
            }
        }
    }
}