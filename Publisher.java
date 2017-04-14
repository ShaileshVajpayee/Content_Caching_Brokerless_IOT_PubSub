import java.io.IOException;
import java.net.*;
import java.util.Arrays;
import java.util.HashMap;

/**
 * Created by shaileshvajpayee
 */
public class Publisher {
    // to decide this
    // Fog based, root based or generic ?

    // will send publisher message with field for publisher! Router will route this to the Fog Node.
    // Fog Node will have owners of attribute trees in its sub network. It will choose appropriate owner and the
    // information traverses to the right group.
    // If no attribute tree it means it has no subscribers in its sub network.
    // Then we must send this to other Fog Nodes.... (not decided how to do that! depends on architecture of fog nodes)

    // Not much problem as data is small and constitutes of just notifications


    private static HashMap<String, Integer> nearest_Fog_Nodes;
    private static String my_IP;
    private static Logger logger;
    private static DatagramSocket Send_socket;
    private static DatagramSocket Listen_socket;
    private static String RouterIP;
    private static int RouterPort;
    private static String my_attribute;
    private static String attr_value;
    private static String FogIP;
    private static int FogPort;


    public Publisher() {
        nearest_Fog_Nodes = new HashMap<>();
        my_IP = "";
        logger = new Logger("Publisher_logs.txt");
    }

    public static void begin_threads() {
        Communicator comm = new Communicator();
        new Thread(comm).start();
    }

    public static void main(String[] args) throws UnknownHostException, SocketException {
        if (args.length < 4) {
            System.out.println("Please enter arguments:$ ListenPort RouterIP RouterPort attribute_name");
            System.exit(0);
        }
        Publisher p = new Publisher();
        my_attribute = args[3];
        attr_value = "10"; // generate randomly in a given range
        Listen_socket = new DatagramSocket(Integer.parseInt(args[0]));
        Send_socket = new DatagramSocket();
        my_IP = InetAddress.getLocalHost().getHostAddress().toString();
        RouterIP = args[1];
        RouterPort = Integer.parseInt(args[2]);
        logger.logMessage("Publisher initialized with IP " + my_IP + " and port " + args[0]);
        logger.logMessage("Logging to file Publisher_logs.txt");
        begin_threads();
    }

    static class Communicator implements Runnable { // will have separate port

        private void sendMessage(String IP, int Port, String msg) {
            byte[] byte_stream = msg.getBytes();
            InetAddress inetAddress;
            try {
                inetAddress = InetAddress.getByName(IP);
                DatagramPacket p = new DatagramPacket(byte_stream, byte_stream.length, inetAddress, Port);
                Send_socket.send(p);
                logger.logMessage("Sent to " + IP + " " + Port);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private void get_FogAddr() {
            try {
                System.out.println(Listen_socket.getLocalPort());
                sendMessage(RouterIP, RouterPort, "1 " + Listen_socket.getLocalPort());
                logger.logMessage("Sent FogIP req: " + "1 " + Listen_socket.getLocalPort() + " -> to Router: " + RouterIP + " " + RouterPort);
                byte[] bytes = new byte[1024];
                DatagramPacket p = new DatagramPacket(bytes, bytes.length);
                Listen_socket.receive(p);
                String[] received_data = (new String(Arrays.copyOfRange(p.getData(), 0, p.getLength()))).split(" ");
                FogIP = received_data[0];
                FogPort = Integer.parseInt(received_data[1]);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void publish_to_fog(){
            sendMessage(FogIP,FogPort,"1 " + my_attribute + " = " + attr_value);
        }

        @Override
        public void run() {
            get_FogAddr();
            try{
                while(true){
                    publish_to_fog();
                    Thread.sleep(5000);
                }
            } catch(Exception e){
                e.printStackTrace();
                logger.logMessage(e.toString());
            }
        }
    }
}