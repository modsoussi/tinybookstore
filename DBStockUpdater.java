/* A DBStockUpdater re-stocks the bookstore every updateInterval seconds.
 * (c) 2016. modsoussi. Danny Smith.
 */

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class DBStockUpdater implements Runnable  {
	
    private Thread t;
    private static String threadName = "DBStockUpdate";
    private int updateInterval; /* chosen by the admin */
    private int amount; /* amount of update */
    Object lock;
	
    DBStockUpdater(int updateInterval, int amount, Object lock) {
	/* don't really need to do anything here */
	this.updateInterval = updateInterval;
	this.amount = amount;
	this.lock = lock;
    }
	
	/* implementing the runnable interface */
	/* 1. run() */
	public void run(){
		while(true){
		    /* wait updateInterval, then re-stock */
		    try {
			Thread.sleep(updateInterval);
		    } catch (InterruptedException e) {
			System.err.println(e.getClass().getName() + ": " + e.getMessage());
		    }	
		    
		    Connection c = null;
		    synchronized(lock) {
			try{
			    Class.forName("org.sqlite.JDBC");
			    c = DriverManager.getConnection("jdbc:sqlite:" + Server.DATABASE + ".db");
			    c.setAutoCommit(false);
			    Statement stm = c.createStatement();
			    
			    String sql = "UPDATE BOOKS SET STOCK=STOCK+" + amount + ";";
			    stm.executeUpdate(sql);
			    c.commit();
			    stm.close();
			    c.close();
			} catch(Exception e){
			    System.err.println("DBSU: " + e.getClass().getName() + ": " + e.getMessage());
			    System.exit(Server.FAILURE);
			}
		    }
		}
	}
    
    /* 2. start() */
    public void start(){
	if(t == null){
	    t = new Thread(this, threadName);
	    t.start();
	}
    }
}
