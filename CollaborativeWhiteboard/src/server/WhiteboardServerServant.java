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

public class WhiteboardServerServant extends UnicastRemoteObject implements IWhiteboardServer {
	private Map<String, IWhiteboardClient> clients = new HashMap<>();
	private String manager;
	
	public WhiteboardServerServant(String manager) throws RemoteException {
		this.manager = manager;
    }

	@Override
	public void broadcastDrawEvent(DrawEvent event) throws RemoteException {
		for (IWhiteboardClient client : clients.values()) {
            client.receiveDrawEvent(event);
        }	
	}

	@Override
	public synchronized boolean registerClient(IWhiteboardClient client, String username) throws RemoteException {
		if (!clients.containsValue(client)) {
            clients.put(username, client);
            broadcastMessage(username + " joined.");
            return true;
        }
		else {
			client.notify("Username already exists! Please choose a different username.");
			return false; // failed
		}
	}

	@Override
	public synchronized void removeClient(IWhiteboardClient client, String username) throws RemoteException {
		clients.remove(username);
        broadcastMessage(username + " left.");
	}

	@Override
	public synchronized boolean requestJoin(IWhiteboardClient client, String username) throws RemoteException {
		int response = JOptionPane.showConfirmDialog(null, username + " wants to join your whiteboard. Approve?", "Join Request", JOptionPane.YES_NO_OPTION);
        if (response == JOptionPane.YES_OPTION) {
        	if (registerClient(client, username))
        		return true;
        	else
        		return false;
        }
        client.notify("Join request denied by manager.");
        return false;
	}

	@Override
	public synchronized void kickUser(String username) throws RemoteException {
		IWhiteboardClient kicked = clients.remove(username);
        if (kicked != null) {
            kicked.notifyKicked();
            broadcastMessage(username + " was kicked.");
        }
	}
	
	@Override
	public synchronized List<String> getUserList() throws RemoteException {
		return new ArrayList<>(clients.keySet());
	}

	private void broadcastMessage(String msg) throws RemoteException {
		for (IWhiteboardClient client : clients.values()) {
            client.notify(msg);
        }
	}
	
	public String getManager() {
		return this.manager;
	}
	
	
}
