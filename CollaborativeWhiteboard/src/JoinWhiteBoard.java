/**
* Author: Matthias Si En Ong
* Student Id: 1590392
* Email: matthiaso@student.unimelb.edu.au
*/
import java.awt.EventQueue;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import javax.swing.JFrame;

import client.WhiteboardClientServant;
import remote.IWhiteboardServer;
import whiteboardapp.WhiteboardApp;

/**
 * This class contains the entry point of the Dictionary Client.
 * @version 1.0
 * @author Matthias Si En Ong
*/
public class JoinWhiteBoard {
	
	/** The IP Address of the server to connect to. */
	private String serverAddress;
	
	/** The port number of the server to connect to. */
    private int port;
    
    /** The username of the client to connect to the server as. */
    private String username;
	/**
     * The entry point of a Whiteboard Client, it connects to a server registered on the RMI registry.
     *
     * @param args Command line arguments, it should be in the order server-address, server-port, username.
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
			
			WhiteboardClientServant client = new WhiteboardClientServant();
			
			boolean approved = server.requestJoin(client, joinWB.username);
            if (!approved) {
                System.out.println("Join request denied.");
                try {
                    UnicastRemoteObject.unexportObject(client, true);
                    System.out.println("Client shutdown cleanly.");
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                System.exit(0);
            }
            
            System.out.println("Join request approved.");
            
            // Run this on Event dispatch thread, Swing code is run on the same thread.
    		EventQueue.invokeLater(new Runnable() {
    			public void run() {
    				try {
    					WhiteboardApp app = new WhiteboardApp(server, joinWB.username, false);
    					System.out.println("Join Whiteboard!");
    					client.initialise(app.getWhiteBoard(), app.getChatArea(), app.getUserList());
    					app.getWhiteBoard().setDrawHistory(server.getDrawHistory());
    					server.broadcastUserList();
    					server.broadcastMessage(joinWB.username + " joined.");
    					JFrame frame = app.getFrame();
    					frame.addWindowListener(new WindowAdapter() {
    	                    @Override
    	                    public void windowClosed(WindowEvent e) {
    	                        try {
    	                            server.removeClient(client, joinWB.username);
    	                            UnicastRemoteObject.unexportObject(client, true);
    	                            System.out.println("Client shutdown cleanly.");
    	                        } catch (Exception ex) {
    	                            ex.printStackTrace();
    	                        }
    	                        System.exit(0);
    	                    }
    	                });
    					
    				} catch (Exception e) {
    					e.printStackTrace();
    				}
    			}
    		});
            
		} catch (Exception e) {
			System.out.println("Server not found! Make sure a whiteboard is created first.");
		}
	}
	
	/**
     * The function checks for valid arguments.
     *
     * @param args Command line arguments, it should be in the order server-IP, port number, client username.
     */
	private Boolean isValidArgs(String[] args) {
		if (args.length != 3) {
			System.out.println("java -jar JoinWhiteBoard.jar <server-IP-address> <server-port> <username>");
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
			System.out.println("Invalid IP address: " + args[0]);
			return false;
		}
		this.username = args[2];
		return true;
	}

}
