/**
* Author: Matthias Si En Ong
* Student Id: 1590392
* Email: matthiaso@student.unimelb.edu.au
*/
package remote;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

import whiteboardapp.Whiteboard.Drawable;

/**
 * RMI Remote interface - must be shared between client and server.
 * This is the interface that clients call on the whiteboard server.
 * All methods must throw RemoteException.
 * All parameters and return types must be either primitives or Serializable.
 *  
 * Any object that is a remote object must implement this interface.
 * Only those methods specified in a "remote interface" are available remotely.
 */
public interface IWhiteboardServer extends Remote {
	
	/**
	 * Broadcasts a draw event to all clients.
     * @param event Draw Event received from client to be broadcasted.
     */
	void broadcastDrawEvent(DrawEvent event) throws RemoteException;
	
	/**
	 * Registers client on the server.
     * @param client
     * @param username
     */
    void registerClient(IWhiteboardClient client, String username) throws RemoteException;
    
    /**
	 * Remove a client from the server.
     * @param client
     * @param username
     */
    void removeClient(IWhiteboardClient client, String username) throws RemoteException;
    
    /**
	 * Client requests to join server.
     * @param client
     * @param username
     */
    boolean requestJoin(IWhiteboardClient client, String username) throws RemoteException;
    
    /**
	 * Client is kicked from server.
     * @param username
     */
    Boolean kickUser(String username) throws RemoteException;
    
    /**
	 * Broadcasts a message on client's chatbox to other clients.
     * @param username of the client that sends the message.
     * @param message
     */
	void broadcastChatMessage(String username, String msg) throws RemoteException;
	
	/**
	 * Broadcasts a message to all clients.
     * @param message
     */
	void broadcastMessage(String msg) throws RemoteException;
	
	/**
	 * Broadcasts the user list to all clients.
     */
	void broadcastUserList() throws RemoteException;
	
	/**
	 * Broadcasts the entire whiteboard history to all clients.
     */
	void broadcastWhiteboardHistory() throws RemoteException;
	
	/**
	 * Getter for the entire draw history of the server.
     */
	List<Drawable> getDrawHistory() throws RemoteException;
	
	/**
	 * Getter for user list of the server.
     */
    List<String> getUserList() throws RemoteException;
    
    /**
	 * Setter for draw history of the server.
	 * @param drawHistory
     */
	void setDrawHistory(List<Drawable> drawHistory) throws RemoteException;
}