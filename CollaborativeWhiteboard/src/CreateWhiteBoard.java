
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
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import javax.swing.JFrame;

import client.WhiteboardClientServant;
import remote.IWhiteboardServer;
import server.WhiteboardServerServant;
import whiteboardapp.WhiteboardApp;
/**
 * This class contains the entry point of the Whiteboard host.
 * 
 * @version 1.0
 * @author Matthias Si En Ong
 */
public class CreateWhiteBoard {
	private String serverAddress;
    private int port;
    private String username;
	
	/**
	 * The entry point of the Whiteboard host.
	 *
	 * @param args Command line arguments.
	 */
	public static void main(String[] args) {
		CreateWhiteBoard createWB = new CreateWhiteBoard();
		if (!createWB.isValidArgs(args)) {
			System.exit(1);
		}
		
		try {
			Registry registry = LocateRegistry.createRegistry(createWB.port);
			WhiteboardServerServant server = new WhiteboardServerServant(createWB.username);
			registry.rebind("WhiteboardService", server);
			
			// Host is a client to itself too!
			WhiteboardClientServant client = new WhiteboardClientServant(createWB.username);
			server.registerClient(client, createWB.username);
			// WHITEBOARD GUI! Run this on Event dispatch thread, Swing code is run on the same thread.
			EventQueue.invokeLater(new Runnable() {
				public void run() {
					try {
						WhiteboardApp app = new WhiteboardApp(server);
						client.setWhiteboard(app.getWhiteBoard());
						JFrame frame = app.getFrame();
						frame.addWindowListener(new WindowAdapter() {
				            @Override
				            public void windowClosed(WindowEvent e) {
				                try {
				                	server.broadcastManagerLeft();
				                    registry.unbind("WhiteboardService");
				                    UnicastRemoteObject.unexportObject(server, true);
				                    System.out.println("Server shut down.");
				                } catch (Exception ex) {
				                    ex.printStackTrace();
				                }
				                System.exit(0);
				            }
				        });
						System.out.println("Create Whiteboard App!");
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		System.out.println("Whiteboard server started by " + createWB.username);

	}
	
	private Boolean isValidArgs(String[] args) {
		if (args.length != 3) {
			System.out.println("java –jar CreateWhiteBoard.jar <server-IP-address> <server-port> <username>");
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
