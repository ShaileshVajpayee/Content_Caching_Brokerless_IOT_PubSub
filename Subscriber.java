import org.w3c.dom.ls.LSException;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;

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

    public Subscriber() {
        attrs = new LinkedList<>();
        logger = new Logger("Subscriber_Logs.txt");
        attrs = new LinkedList<>();
        add_attr();
    }

    public void make_me_owner() { // If no attr tree ie no successor to a leader.

    }

    public void make_me_leader(boolean leader_left) { // If no group or Leader left and is 1st coleader
        if (leader_left) {
            // get leader lists and update
        } else {
            Leader_Group_List = new LinkedHashMap<>();
            Leader_pred = new LinkedHashMap<>();
            Leader_succ = new LinkedHashMap<>();
        }
    }

    public void find_node(Attribute attr) {
        //contact router, if not in router contact fog node
    }

    public void add_attr() {
        Attribute attr = new Attribute();
        attr.get_attr();
        attrs.add(attr);
        find_node(attr);
        logger.logMessage("Attribute added : " + attr.toString());
    }

    public static void main(String[] args) throws SocketException {
        if (args.length < 3) {
            System.out.println("Please enter arguments: ListenPort RouterIP RouterPort");
            System.exit(0);
        }
        Subscriber sb = new Subscriber();
        Listen_socket = new DatagramSocket(Integer.parseInt(args[0]));
        Send_socket = new DatagramSocket();
        RouterIP = args[1];
        RouterPort = Integer.parseInt(args[2]);
        Communicator comm = new Communicator();
        new Thread(comm).start();
    }

    public void print_attr() {
        for (Attribute attribute : attrs) {
            System.out.println(attribute.toString());
        }
    }

    private static class Communicator implements Runnable { // will have separate port

        private void sendMessage(String IP, int Port, String msg) {
            byte[] byte_stream = msg.getBytes();
            InetAddress inetAddress;
            try {
                inetAddress = InetAddress.getByName(IP);
                DatagramPacket p = new DatagramPacket(byte_stream, byte_stream.length, inetAddress, Port);
                Send_socket.send(p);
                System.out.println("Address " + msg + " sent to " + IP + " " + Port);
            } catch (Exception e) {
                e.printStackTrace();
                logger.logMessage(e.toString());
            }
        }

        private void get_FogAddr() {
            try { // 1 means request for fog node address device in subnet
                sendMessage(RouterIP, RouterPort, "1 " + Listen_socket.getLocalPort());
                logger.logMessage("Sent FogIP req to Router: " + RouterIP + " " + RouterPort);
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

        private void parse_msg(DatagramPacket p){
            String received_data = new String(Arrays.copyOfRange(p.getData(), 0, p.getLength()));
            String[] data = received_data.split(" ");
            if (data[0].equals("1")) {
            }
        }

        private void fog_communication() throws IOException {
            sendMessage(FogIP,FogPort, "0 " + attrs.get(0).lhs);
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
                while(true) {
                    fog_communication();
                }
            } catch (IOException e) {
                e.printStackTrace();
                logger.logMessage(e.toString());
            }
        }
    }


}
// Leader structure (can be subscriber or Leader)