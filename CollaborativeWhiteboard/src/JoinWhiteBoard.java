/**
* Author: Matthias Si En Ong
* Student Id: 1590392
* Email: matthiaso@student.unimelb.edu.au
*/
import java.awt.EventQueue;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import client.WhiteboardClientServant;
import remote.IWhiteboardClient;
import remote.IWhiteboardServer;
import server.WhiteboardServerServant;
import whiteboardapp.WhiteboardApp;

/**
 * This class contains the entry point of the Dictionary Client.
 * @version 1.0
 * @author Matthias Si En Ong
*/
public class JoinWhiteBoard {
	private String serverAddress;
    private int port;
    private String username;
	/**
     * The entry point of a Whiteboard Client.
     *
     * @param args Command line arguments, it should be in the order server-address, server-port.
     */
	public static void main(String[] args) {
		JoinWhiteBoard joinWB = new JoinWhiteBoard();
		if (!joinWB.isValidArgs(args)) {
			System.exit(1);
		}
		
		Registry registry;
		try {
			registry = LocateRegistry.getRegistry(joinWB.serverAddress, joinWB.port);
			IWhiteboardServer server = (IWhiteboardServer) registry.lookup("WhiteboardService");
			
			WhiteboardClientServant client = new WhiteboardClientServant(joinWB.username);
			
			boolean approved = server.requestJoin(client, joinWB.username);
            if (!approved) {
                System.out.println("Join request denied by manager.");
                return;
            }
            
            System.out.println("Join request approved.");
            
            // Run this on Event dispatch thread, Swing code is run on the same thread.
    		EventQueue.invokeLater(new Runnable() {
    			public void run() {
    				try {
    					WhiteboardApp app = new WhiteboardApp(server);
    					System.out.println("Join Whiteboard!");
    					client.setWhiteboard(app.getWhiteBoard());
    					
    				} catch (Exception e) {
    					e.printStackTrace();
    				}
    			}
    		});
            
		} catch (Exception e) {
			System.out.println("Server not found! Make sure a whiteboard is created first.");
		}
	}
	
	private Boolean isValidArgs(String[] args) {
		if (args.length != 3) {
			System.out.println("java –jar JoinWhiteBoard.jar <server-IP-address> <server-port> <username>");
			return false;
		}
		
		try {
			InetAddress.getByName(args[0]);
			this.serverAddress = args[0];
			this.port = Integer.parseInt(args[1]);
		} catch (NumberFormatException e) {
			System.out.println("Invalid Port Number");
			return false;
		}
		catch (UnknownHostException e) {
			System.out.println("Invalid IP address" + args[0]);
			return false;
		}
		this.username = args[2];
		return true;
	}

}
