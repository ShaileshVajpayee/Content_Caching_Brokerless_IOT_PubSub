/**
 * Created by shaileshvajpayee on 4/20/17.
 */
public class Packet_tester {

    public static void main(String[] args){
        Attribute attr = new Attribute();
        attr.set_attr("A","=","5");
        Subscriber_Comm_Packet pkt = new Subscriber_Comm_Packet("192.168.1.2", 7777, attr, true, true, "1.2.3.4 500", "C = 4", false, false, true);
        Subscriber_Comm_Packet_Analyzer pkt_analyzer = new Subscriber_Comm_Packet_Analyzer(pkt);
//        System.out.println(pkt.toString());
        pkt_analyzer.print_packet();
        Subscriber_Comm_Packet_Analyzer pkt_analyzer2 = new Subscriber_Comm_Packet_Analyzer();
        pkt_analyzer2.init_packet(pkt.toString().split(" "));
        pkt_analyzer2.print_packet();
    }
}
