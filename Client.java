/* Client for the Tiny Bookstore server.
 *
 *
 * (c) 2016 modsoussi drs1
 */


import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;
import java.util.*;
import java.net.URL;



public class Client{

    private static XmlRpcClient client;

    public static void main(String[] args){
	XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
	client = null;

	try{
	    config.setServerURL(new URL("http://" + args[0] + ":" + (args.length > 0 ? args[1] : 8888)));
	    client = new XmlRpcClient();
	    client.setConfig(config);
	}catch(Exception e){System.err.println("Client problem: " + e);}

	Scanner s = new Scanner(System.in);
	System.out.print("> TinyBookStoreClient: ");
	while(s.hasNext()){
	    String cmd = s.nextLine();
	    String[] tokens = cmd.split(" ", 2);
	    switch(tokens[0]){
	    case "search":
		try{
		    search(tokens[1]);
		}catch(Exception e){
		    System.out.print("Missing params for search <topic>");
		}
		break;

	    case "lookup":
		try{
		    lookup(tokens[1]);
		}catch(Exception e){
		    System.out.print("Missing params for lookup <bookID>");
		}
		break;

	    case "buy":
		try{
		    buy(tokens[1]);
		}catch(Exception e){
		    System.out.print("Missing params for buy <bookID>");
		}
		break;

	    default:
		System.out.println("> TinyBookstoreClient: Command unsupported");
	    }
	    System.out.print("> TinyBookstoreClient: ");
	}
	s.close();
    }

    public static void search(String topic){
	String[] params = {topic};
	try {
	    String result = (String)client.execute("bookstore.search", params);
	    System.out.println(result);
	} catch(Exception e){System.out.println(e.getClass().getName() + ": " + e.getMessage());}
    }

    public static void buy(String bookNum){
	Integer[] params = {Integer.parseInt(bookNum)};
	String result = "";
	try{
	    result = (String)client.execute("bookstore.buy", params);
	    System.out.println(result);
	} catch(Exception e){System.out.println(e.getClass().getName() + ": " + e.getMessage());}
    }

    public static void lookup(String bookNum){
	String out = "";
	Integer[] params = {Integer.parseInt(bookNum)};
	try {
	    String result = (String)client.execute("bookstore.lookup", params);
	    System.out.print(result);
	} catch(Exception e){System.out.println(e.getClass().getName() + ": " + e.getMessage());}
    }
}
