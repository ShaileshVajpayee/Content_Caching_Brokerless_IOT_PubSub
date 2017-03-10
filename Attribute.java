import java.util.Scanner;

/**
 * Created by shaileshvajpayee on 3/9/17.
 */
public class Attribute {
    String lhs;
    boolean less_than;
    boolean more_than;
    boolean equal_to;
    String rhs;

    public static Scanner s;

    public void get_attr(){
        s = new Scanner(System.in);
        System.out.println("Please enter your subscription(eg. a < 10)... Enter lhs: ");
        lhs = s.nextLine();
        System.out.println("Please enter your subscription(eg. a < 10)... Enter operator: ");
        char ch = s.nextLine().charAt(0);
        if(ch == '='){
            equal_to = true;
        }
        else if(ch == '<'){
            less_than = true;
        }
        else if(ch == '>'){
            more_than = true;
        }
    }
}
