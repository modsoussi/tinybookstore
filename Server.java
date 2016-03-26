/* A Tiny Bookstore, containing 4 books. This is the front-end server. It allows users to lookup, search, and buy things in 
 * the database.
 *
 * (c) 2016. modsoussi. Danny Smith
 */

import org.apache.xmlrpc.server.XmlRpcServer;
import org.apache.xmlrpc.webserver.WebServer;
import org.apache.xmlrpc.server.PropertyHandlerMapping;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class Server {
	
    protected static String DATABASE = "bookstore";
    protected static int FAILURE = -1;
    protected static HashMap<Integer, Integer> hashlog;
    private static Object lock; /* provides locking mechanism for the buy method */
    
    /* prints a log of all purchases since the start of the server */
    private static void log(){
	for(Map.Entry<Integer, Integer> entry : hashlog.entrySet()){
	    System.out.println("Item " + entry.getKey() + ": "+ entry.getValue() + " purchases.");
	}
	return;
    }
    
    /* updates price for item with id itemNum */
    private static void update(int itemNum, float price){
	Connection c = null;
	try {
	    Class.forName("org.sqlite.JDBC");
	    c = DriverManager.getConnection("jdbc:sqlite:" + DATABASE + ".db");
	    c.setAutoCommit(false);
	    Statement stm;
	    stm = c.createStatement();
	    
	    String sql = "UPDATE BOOKS SET COST=" + price + " WHERE ID=" + itemNum + ";";
	    stm.executeUpdate(sql);
	    c.commit();
	    stm.close();
	    c.close();
	} catch(Exception e){
	    System.err.println(e.getClass().getName() + ": cannot perform query: " + e.getMessage());
	    System.exit(FAILURE);
	}
    }
    
    /* allows user to lookup books by their ids */
    public String lookup(int itemNum){
	String res = "";
	Connection c = null;
	try {
	    Class.forName("org.sqlite.JDBC");
	    c = DriverManager.getConnection("jdbc:sqlite:" + DATABASE + ".db");
	    c.setAutoCommit(false);
	    Statement stm;
	    stm = c.createStatement();
	    
	    String sql = "SELECT TITLE, STOCK, COST, TOPIC FROM BOOKS WHERE ID="+itemNum+";";
	    ResultSet set = stm.executeQuery(sql);
	    if(set.next()){ /* if result set not empty */
		res = res + (set.getInt("stock") > 0 ? "In Stock\n" : "Out of Stock\n");
		res = res + "Title: " + set.getString("title") + "\n";
		res = res + "Price: " + set.getFloat("cost") + "\n";
		res = res + "Topic: " + set.getString("topic") + "\n";
	    } 
	    set.close();
	    stm.close();
	    c.close();
	} catch(Exception e){
	    System.err.println(e.getClass().getName() + ": cannot perform query: " + e.getMessage());
	    System.exit(FAILURE);
	}
	return res;
    }
    
    /* allows people to search table by topic */
    public String search(String topic){
	String res = "";
	Connection c = null;
	try {
	    Class.forName("org.sqlite.JDBC");
	    c = DriverManager.getConnection("jdbc:sqlite:"+DATABASE+".db");
	    Statement stm;
	    stm = c.createStatement();
	    
	    /* if topic query is empty, return everything in database */
	    String sql =  topic.length() > 0 ?
		"SELECT ID, TITLE, STOCK FROM BOOKS WHERE TOPIC='"+topic.toLowerCase()+"';" :
		"SELECT * FROM BOOKS"; 
	    
	    ResultSet set = stm.executeQuery(sql);
	    int stock = -1;
	    while(set.next()){
		stock = set.getInt("stock");
		if(stock > 0){
		    res = res + "Item: " + set.getInt("id") + " | ";
		    res = res + "Title: " + set.getString("title") + "\n";
		}
	    }
	    set.close();
	    stm.close();
	    c.close();
	} catch(Exception e){
	    System.err.println(e.getClass().getName() + ": Cannot perform search: " + e.getMessage());
	    System.exit(FAILURE);
	}
	return res;
    }
    
    /* allows people to perform a purchase of a book */
    public String buy(int itemNum){
	synchronized(lock){
	    String s = "You have successfully bought 1 of ";
	    
	    Connection c = null;
	    try{
		Class.forName("org.sqlite.JDBC");
		c = DriverManager.getConnection("jdbc:sqlite:" + DATABASE +".db");
		c.setAutoCommit(false);
		Statement stm;
		stm = c.createStatement();
		
		/* getting stock */
		String sql = "SELECT TITLE, STOCK FROM BOOKS WHERE ID=" + itemNum+";";
		ResultSet res = stm.executeQuery(sql);
		int stock = -1;
		String title = "";
		if(res.next()){
		    stock = res.getInt("stock");
		    title = res.getString("title");
		}
		res.close();
		if(stock > 0){ /* item is in stock. buy underway. */
		    stock--;
		    
		    sql = "UPDATE BOOKS SET STOCK=" + stock + " WHERE ID=" + itemNum +";";
		    stm.executeUpdate(sql);
		    stm.close();
		    c.commit();
		    s = s + title;
		    
		    /* updating hashlog. Note: HashMaps need external synchronization only in case of structural modifications.
		     * not the case here, so no need to surround this block by synchornized.
		     */
		    int prev = hashlog.get(itemNum);
		    hashlog.put(itemNum, prev+1);
		    
		} else { /* item is out of stock */
		    s = "Unfortunately, " + title + " is out of stock. More is on the way.";
		}
		c.close();
	    } catch(Exception e){
		System.err.println("Server buy: " + e.getClass().getName() + ": " + e.getMessage());
		//TODO: catch when database is locked. Fix.
		s = "Unable to make purchase. Please try again.";
		System.exit(FAILURE);
	    }
	    return s;
	}
    }

    /* populates database */
    public static void fillDB(Connection c){
	Statement stm = null;
	try {
	    c.setAutoCommit(false);
	    stm = c.createStatement();
	    String sql = "INSERT INTO BOOKS (ID,TITLE,STOCK,COST,TOPIC)" +
		"VALUES (53477, 'Achieving Less Bugs with More Hugs in CSCI 339', 25, 39.99, 'distributed systems');";
	    stm.executeUpdate(sql);
	    
	    sql = "INSERT INTO BOOKS (ID,TITLE,STOCK,COST,TOPIC)" +
		"VALUES (53573, 'Distributed Systems for Dummies', 25, 59.99, 'distributed systems');";
	    stm.executeUpdate(sql);
	    
	    sql = "INSERT INTO BOOKS (ID,TITLE,STOCK,COST,TOPIC)" +
		"VALUES (12365, 'Surviving College', 25, 29.99, 'college life');";
	    stm.executeUpdate(sql);
	    
	    sql = "INSERT INTO BOOKS (ID,TITLE,STOCK,COST,TOPIC)" +
		"VALUES (12498, 'Cooking for the Impatient Undergraduate', 25, 12.95, 'college life');";
	    stm.executeUpdate(sql);
	    
	    stm.close();
	    c.commit();
	} catch(Exception e){
	    System.err.println(e.getClass().getName() + ": Cannot fill DB: " + e.getMessage());
	    /* No system.exit here. Error likely caused by already-filled out table. */
	}
    }
    
    public static void main(String[] args) {

	/* setting up lock */
	lock = new Object();

	/* setting up hashlog */
	hashlog = new HashMap<Integer, Integer>();
	hashlog.put(53477, 0);
	hashlog.put(53573, 0);
	hashlog.put(12365, 0);
	hashlog.put(12498, 0);
	
	/* setting up server */
	try {
	    PropertyHandlerMapping phm = new PropertyHandlerMapping();
	    WebServer server = new WebServer(args.length > 0 ? Integer.parseInt(args[0]) : 8888);
	    System.out.println(args.length > 0 ? Integer.parseInt(args[0]) : 8888);
	    XmlRpcServer xmlRpcServer = server.getXmlRpcServer();
	    phm.addHandler("bookstore", Server.class);
	    xmlRpcServer.setHandlerMapping(phm);
	    server.start();
	} catch (Exception e){
	    System.err.println("TinyBookstore: " + e.getMessage());
			System.exit(FAILURE);
	}
	
	/* opening database */
	Connection c = null;
	try {
	    Class.forName("org.sqlite.JDBC");
	    c = DriverManager.getConnection("jdbc:sqlite:"+ DATABASE +".db");
	} catch(Exception e) {
	    System.err.println(e.getClass().getName() + ": Cannot open database: " + e.getMessage());
	    System.exit(FAILURE);
	}
	System.out.println("Opened database successfully.");
	
	/* creating table */
	Statement stm = null;
	try {
	    stm = c.createStatement();
	    String sql = "CREATE TABLE IF NOT EXISTS BOOKS " +
		"(ID INT PRIMARY	KEY			NOT NULL," +
		"TITLE				CHAR(50)	NOT NULL," +
		"STOCK				INT			NOT NULL," +
		"COST				REAL		NOT NULL," +
		"TOPIC				CHAR(50)	NOT NULL)";
	    stm.executeUpdate(sql);
	    stm.close();
	} catch(Exception e){
	    System.err.println(e.getClass().getName() + ": Cannot create table: " + e.getMessage());
	}
		
	/* fill table if not already filled out*/
	try{
	    stm = c.createStatement();
	    String sql = "SELECT * FROM BOOKS WHERE ID=" + 53477 + ";";
	    ResultSet set = stm.executeQuery(sql);
	    if(!set.next()){
		fillDB(c);
	    }
	    set.close();
	    stm.close();
	} catch(Exception e){
	    System.err.println(e.getClass().getName() + ": Cannot open database: " + e.getMessage());
	    System.exit(FAILURE);
	}
	
	/* done with table opening, creating, and filling out. Close the connection to it. */
	try {
	    c.close();
	} catch(Exception e){
	    System.err.println(e.getClass().getName() + ": Cannot close connection: " + e.getMessage());
	    System.exit(FAILURE);
	}
	
	/* Now accepting commands */
	Scanner s = new Scanner(System.in);
	System.out.print("> TinyBookstoreAdmin: ");
	while(s.hasNext()){
	    String cmd = s.nextLine();
	    String[] tokens = cmd.split(" ");
	    switch(tokens[0]){
	    case "log":
		log();
		break;
	    case "update":
		try {
		    update(Integer.parseInt(tokens[1]), Float.parseFloat(tokens[2]));
		    System.out.printf("update: item: %s | price: $%s", tokens[1], tokens[2]);
		} catch (Exception e){
		    System.out.print("Missing params.");
		}
		break;
	    case "restock":
		/* spawning new thread to re-stock */
		try{
		    DBStockUpdater dsu = new DBStockUpdater(Integer.parseInt(tokens[1]), Integer.parseInt(tokens[2]), lock);
		    dsu.start();
		} catch(Exception e){
		    System.out.print("Missing params [updateInterval] [amount]");
		}
		break;
	    default:
		System.out.print("Command unsupported.\n> TinyBookstoreAdmin: ");
			}
	    System.out.print("\n> TinyBookstoreAdmin: ");
	}
	s.close();
    }
}
