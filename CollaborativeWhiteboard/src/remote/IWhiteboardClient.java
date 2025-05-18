/**
* Author: Matthias Si En Ong
* Student Id: 1590392
* Email: matthiaso@student.unimelb.edu.au
*/
package remote;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;
import whiteboardapp.Whiteboard;
import whiteboardapp.Whiteboard.Drawable;

/**
 * RMI Remote interface - must be shared between client and server.
 * This is the interface that the server call on the clients to update the local whiteboard.
 * All methods must throw RemoteException.
 * All parameters and return types must be either primitives or Serializable.
 *  
 * Any object that is a remote object must implement this interface.
 * Only those methods specified in a "remote interface" are available remotely.
 */
public interface IWhiteboardClient extends Remote {
	
	/**
     * @param event Draw Event received from server
     */
    void receiveDrawEvent(DrawEvent event) throws RemoteException;
    
    /**
     * @param message received from server
     */
    void notify(String message) throws RemoteException;
    
    /**
     * Notifies the client that they were kicked.
     */
    void notifyKicked() throws RemoteException;
    
    /**
     * Notifies the client that the manager left.
     */
	void notifyManagerLeft() throws RemoteException;
	
	/**
     * Updates the user list of the client.
     * @param userList new user list sent over by the server.
     */
	void updateUserList(List<String> userList) throws RemoteException;
	
	/**
     * Updates the whole client's whiteboard.
     * @param drawHistory from the server.
     */
	void updateWhiteboard(List<Drawable> drawHistory) throws RemoteException;
	
	/**
     * Getter for the whiteboard reference.
     */
	Whiteboard getWhiteboard() throws RemoteException;
}
