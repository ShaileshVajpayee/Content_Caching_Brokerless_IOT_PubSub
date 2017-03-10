import java.util.HashMap;

/**
 * Created by shaileshvajpayee on 3/9/17.
 */
public class Router {
    HashMap<Attribute,Subscriber> attr_map; // attribute to owner(if available) of subnet. routes subs to owners
    HashMap<String, Integer> nearest_Fog_Nodes;

    public Router() {
        attr_map = new HashMap<>();
    }

    public void insert_Fog_Node_info(String IP, int port){ // fog listen thread
        nearest_Fog_Nodes.put(IP,port);
    }

    public void get_entry_from_owner(Attribute attr, Subscriber s){ // owners listen thread
        attr_map.put(attr,s);
    }

    public HashMap<String, Integer> provide_fog_table(){ // request from subscriber
        return nearest_Fog_Nodes;
    }
}
