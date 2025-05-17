package remote;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

/**
 * RMI Remote interface - must be shared between client and server.
 * This is the interface that clients call on the server.
 * All methods must throw RemoteException.
 * All parameters and return types must be either primitives or Serializable.
 *  
 * Any object that is a remote object must implement this interface.
 * Only those methods specified in a "remote interface" are available remotely.
 */
public interface IWhiteboardServer extends Remote {
	void broadcastDrawEvent(DrawEvent event) throws RemoteException;
    boolean registerClient(IWhiteboardClient client, String username) throws RemoteException;
    void removeClient(IWhiteboardClient client, String username) throws RemoteException;
    boolean requestJoin(IWhiteboardClient client, String username) throws RemoteException;
    List<String> getUserList() throws RemoteException;
    void kickUser(String username) throws RemoteException;
	
}