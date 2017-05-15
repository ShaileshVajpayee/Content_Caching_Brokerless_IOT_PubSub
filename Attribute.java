import java.util.Scanner;

/**
 * Created by shaileshvajpayee
 */
public class Attribute {
    public static Scanner s;
    String lhs;
    boolean less_than;
    boolean more_than;
    boolean equal_to;
    String rhs;

    public Attribute(){
        this.lhs = "";
        this.less_than = false;
        this.more_than = false;
        this.equal_to = false;
        this.rhs = "";
    }

    public void set_attr(String lhs, String operator, String rhs){
        this.lhs = lhs;
        if(operator.equals("=")){
            equal_to = true;
        }
        else if(operator.equals("<")){
            less_than = true;
        }
        else{
            more_than = true;
        }
        this.rhs = rhs;
    }

    public void get_attr() {
        s = new Scanner(System.in);
        System.out.println("Please enter your subscription(eg. a < 10)...\nEnter lhs: ");
        lhs = s.nextLine();
        System.out.println("Enter operator: ");
        char ch = s.nextLine().charAt(0);
        if (ch == '=') {
            equal_to = true;
        } else if (ch == '<') {
            less_than = true;
        } else if (ch == '>') {
            more_than = true;
        }
        System.out.println("Enter rhs: ");
        rhs = s.nextLine();
    }

    public String get_lhs() {
        return lhs;
    }

    public boolean compare_attr(Attribute attr_leader, Attribute attr_new){ //attr_new is published with =
        if(attr_leader.toString().equals(attr_new.toString())){
            return true;
        }
        int l_rhs = Integer.parseInt(attr_leader.rhs);
        int n_rhs = Integer.parseInt(attr_new.rhs);
        if(attr_leader.less_than){
            if(n_rhs < l_rhs){
                return true;
            }
        }
        else if(attr_leader.more_than){
            if(n_rhs > l_rhs){
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        String attr = lhs + " ";
        if (equal_to) {
            attr += "= ";
        } else if (less_than) {
            attr += "< ";
        } else if (more_than) {
            attr += "> ";
        }
        attr += rhs;
        return attr;
    }
}
