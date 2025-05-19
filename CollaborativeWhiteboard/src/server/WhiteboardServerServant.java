/**
* Author: Matthias Si En Ong
* Student Id: 1590392
* Email: matthiaso@student.unimelb.edu.au
*/
package server;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JOptionPane;
import remote.DrawEvent;
import remote.IWhiteboardClient;
import remote.IWhiteboardServer;
import whiteboardapp.Whiteboard.Drawable;

/**
 * This class contains the implementation of the server remote interface.
 * 
 * @version 1.0
 * @author Matthias Si En Ong
 */
public class WhiteboardServerServant extends UnicastRemoteObject implements IWhiteboardServer {

	/** The version identifier */
	private static final long serialVersionUID = 1L;
	
	/** HashMap of all clients and their usernames as keys */
	private Map<String, IWhiteboardClient> clients = new HashMap<>();
	
	/** Username of the manager of the whiteboard. */
	private String manager;
	
	/** Whiteboard state on the server. */
	private List<Drawable> drawHistory;

	/**
	 * Constructor of server servant program.
     * @param manager
     */
	public WhiteboardServerServant(String manager) throws RemoteException {
		this.manager = manager;
	}

	/**
	 * Broadcasts a draw event to all clients.
     * @param event Draw Event received from client to be broadcasted.
     */
	@Override
	public synchronized void broadcastDrawEvent(DrawEvent event) throws RemoteException {
		for (IWhiteboardClient client : clients.values()) {
			client.receiveDrawEvent(event);
		}
	}
	
	/**
	 * Broadcasts the entire whiteboard history to all clients.
     */
	@Override
	public synchronized void broadcastWhiteboardHistory() throws RemoteException {
		for (IWhiteboardClient client : clients.values()) {
			client.updateWhiteboard(drawHistory);
		}
	}

	/**
	 * Registers client on the server.
     * @param client
     * @param username
     */
	@Override
	public synchronized void registerClient(IWhiteboardClient client, String username) throws RemoteException {
		clients.put(username, client);
//		broadcastMessage(username + " joined.");

	}

	/**
	 * Remove a client from the server.
     * @param client
     * @param username
     */
	@Override
	public synchronized void removeClient(IWhiteboardClient client, String username) throws RemoteException {
		clients.remove(username);
		broadcastMessage(username + " left.");
		broadcastUserList();
	}

	/**
	 * Client requests to join server.
     * @param client
     * @param username
     */
	@Override
	public synchronized Boolean requestJoin(IWhiteboardClient client, String username) throws RemoteException {
		if (!clients.containsKey(username)) {
			int response = JOptionPane.showConfirmDialog(null, username + " wants to join your whiteboard. Approve?",
					"Join Request", JOptionPane.YES_NO_OPTION);
			if (response == JOptionPane.YES_OPTION) {
				registerClient(client, username);
				return true;
			}
			client.notify("Join request denied by manager.");
			return false;
		} else {
			client.notify("Username already exists! Please choose a different username.");
			return false; // failed
		}

	}

	/**
	 * Client is kicked from server.
     * @param username
     */
	@Override
	public synchronized Boolean kickUser(String username) throws RemoteException {
		if (username.equals(manager)) {
			// you cant kick the manager.
			return false;
		}
		IWhiteboardClient kicked = clients.remove(username);
		if (kicked != null) {
			kicked.notifyKicked();
			broadcastMessage(username + " was kicked.");
			broadcastUserList();
			return true; // success
		}
		return false;
	}
	
	/**
	 * Broadcasts the user list to all clients.
     */
	@Override
	public synchronized void broadcastUserList() throws RemoteException {
		List<String> userList = getUserList();
		for (IWhiteboardClient client : clients.values()) {
			client.updateUserList(userList);
		}
	}

	/**
	 * Broadcasts a message to all clients.
     * @param message
     */
	@Override
	public synchronized void broadcastMessage(String msg) throws RemoteException {
		for (IWhiteboardClient client : clients.values()) {
			client.notify(msg);
		}
	}
	
	/**
	 * Broadcasts a message on client's chat box to other clients.
     * @param username of the client that sends the message.
     * @param message
     */
	@Override
	public synchronized void broadcastChatMessage(String username, String msg) throws RemoteException {
		for (IWhiteboardClient client : clients.values()) {
			client.notify(username + ": " + msg);
		}
	}
	
	/**
	 * Not called over the network by clients but called by the host. Broadcasts to all 
	 * clients that the manager left.
     */
	public synchronized void broadcastManagerLeft() throws RemoteException {
		for (IWhiteboardClient client : clients.values()) {
			client.notifyManagerLeft();
		}
	}
	
	/**
	 * Getter for user list of the server.
     */
	@Override
	public synchronized List<String> getUserList() throws RemoteException {
		return new ArrayList<>(clients.keySet());
	}

	/**
	 * Getter for user list of the server.
     */
	@Override
	public List<Drawable> getDrawHistory() throws RemoteException {
		// get manager's whiteboard history, everybody syncs to manager
		IWhiteboardClient manager = clients.get(this.manager);
		return manager.getWhiteboard().getDrawHistory();
	}
	
	/**
	 * Setter for draw history of the server.
	 * @param drawHistory
     */
	@Override
	public void setDrawHistory(List<Drawable> drawHistory) throws RemoteException {
		this.drawHistory = drawHistory;
	}

}
