/**
 * Created by shaileshvajpayee on 4/17/17.
 */
public class Subscriber_Comm_Packet_Analyzer {
    Subscriber_Comm_Packet packet;

    public Subscriber_Comm_Packet_Analyzer(Subscriber_Comm_Packet packet) {
        this.packet = packet;
    }

    public Subscriber_Comm_Packet_Analyzer() {
        this.packet = new Subscriber_Comm_Packet();
    }

    public boolean is_msg_from_fog() {
        return packet.msg_from_fog;
    }

    public String get_sub_IP() {
        return packet.sub_IP;
    }

    public int get_sub_Port() {
        return packet.sub_Port;
    }

    public String get_sub_attr_lhs() {
        return packet.attr.get_lhs();
    }

    public void init_packet(String[] data) {
        int i = 0;

        i = 0;
        if (data[0].equals("0")) {
            i = 1;
        }

        packet.sub_IP = data[i];
        i++;
        packet.sub_Port = Integer.parseInt(data[i]);
        i++;
        if (!data[i].equals("none")) {
            Attribute attr = new Attribute();
            attr.set_attr(data[i], data[i + 1], data[i + 2]);
            packet.attr = attr;
            i = i + 3;
        } else {
            i++;
        }
        packet.you_are_leader = data[i].equals("true");
        i++;
        packet.add_group_member = data[i].equals("true");
        i++;
        if (packet.add_group_member) {
            packet.group_member = data[i] + " " + data[i + 1];
            i = i + 2;
        } else {
            packet.group_member = data[i];
            i++;
        }
        if (!data[i].equals("none")) {
            packet.pub_msg = data[i] + " " + data[i + 1] + " " + data[i + 2];
            i = i + 3;
        } else {
            packet.pub_msg = data[i];
            i++;
        }
        packet.msg_from_fog = data[i].equals("true");
        i++;
        packet.msg_from_leader = data[i].equals("true");
        i++;
        packet.msg_from_sub = data[i].equals("true");

    }

    public void print_packet() {
        System.out.println(packet.print_packet());
    }

}
