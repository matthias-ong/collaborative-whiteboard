package remote;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

import javax.swing.JList;

/**
 * RMI Remote interface - must be shared between client and server.
 * This is the interface that the server call on the clients.
 * All methods must throw RemoteException.
 * All parameters and return types must be either primitives or Serializable.
 *  
 * Any object that is a remote object must implement this interface.
 * Only those methods specified in a "remote interface" are available remotely.
 */
public interface IWhiteboardClient extends Remote {
    void receiveDrawEvent(DrawEvent event) throws RemoteException;
    void notify(String message) throws RemoteException;
    void notifyKicked() throws RemoteException;
	void notifyManagerLeft() throws RemoteException;
	void updateUserList(List<String> userList) throws RemoteException;
}
