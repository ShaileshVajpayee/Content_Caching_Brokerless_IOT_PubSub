/**
 * Created by shaileshvajpayee on 4/17/17.
 */
public class Subscriber_Comm_Packet {
    String sub_IP;
    int sub_Port;
    Attribute attr;
    boolean you_are_leader;
    boolean add_group_member;
    String group_member;
    String pub_msg;
    boolean msg_from_fog;
    boolean msg_from_leader;
    boolean msg_from_sub;

    public Subscriber_Comm_Packet(String sub_IP,
                                  int sub_Port,
                                  Attribute attr,
                                  boolean you_are_leader,
                                  boolean add_group_member,
                                  String group_member,
                                  String pub_msg,
                                  boolean msg_from_fog,
                                  boolean msg_from_leader,
                                  boolean msg_from_sub) {
        this.sub_IP = sub_IP;
        this.sub_Port = sub_Port;
        this.attr = attr;
        this.you_are_leader = you_are_leader;
        this.add_group_member = add_group_member;
        this.group_member = group_member;
        this.pub_msg = pub_msg;
        this.msg_from_fog = msg_from_fog;
        this.msg_from_leader = msg_from_leader;
        this.msg_from_sub = msg_from_sub;
    }

    public String getSub_IP() {
        return sub_IP;
    }

    public int getSub_Port() {
        return sub_Port;
    }

    public Attribute getAttr() {
        return attr;
    }

    public boolean is_leader() {
        return you_are_leader;
    }

    public boolean isAdd_group_member() {
        return add_group_member;
    }

    public String getGroup_member() {
        return group_member;
    }

    public String getPub_msg() {
        return pub_msg;
    }

    public boolean isMsg_from_fog() {
        return msg_from_fog;
    }

    public boolean isMsg_from_leader() {
        return msg_from_leader;
    }

    public boolean isMsg_from_sub() {
        return msg_from_sub;
    }

    public Subscriber_Comm_Packet() {
        this.attr = null;
        this.you_are_leader = false;
        this.add_group_member = false;
        this.group_member = "";
        this.pub_msg = "";
    }

    public String print_packet(){
        return "\nSubscriber_Comm_Packet:-" +
                "\nsub_IP=" + sub_IP +
                "\nsub_Port=" + sub_Port +
                "\nattr=" + attr.toString() +
                "\nyou_are_leader=" + you_are_leader +
                "\nadd_group_member=" + add_group_member +
                "\ngroup_member=" + group_member +
                "\npub_msg=" + pub_msg +
                "\nmsg_from_fog=" + msg_from_fog +
                "\nmsg_from_leader=" + msg_from_leader +
                "\nmsg_from_sub=" + msg_from_sub + "\n";
    }

    @Override
    public String toString() {
        return  sub_IP + " " +
                sub_Port + " " +
                attr.toString() + " " +
                you_are_leader + " " +
                add_group_member + " " +
                group_member + " " +
                pub_msg + " " +
                msg_from_fog + " " +
                msg_from_leader + " " +
                msg_from_sub;
    }

}
