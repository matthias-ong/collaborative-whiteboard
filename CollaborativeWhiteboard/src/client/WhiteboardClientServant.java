package client;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;

import javax.swing.JOptionPane;

import remote.DrawEvent;
import remote.IWhiteboardClient;
import remote.IWhiteboardServer;
import whiteboardapp.Whiteboard;

public class WhiteboardClientServant extends UnicastRemoteObject implements IWhiteboardClient {
	private IWhiteboardServer server;
    private String username;
    
    private Whiteboard whiteboard;

    public void setWhiteboard(Whiteboard whiteboard) {
        this.whiteboard = whiteboard;
    }

    
    public WhiteboardClientServant(String username) throws RemoteException {
    	this.username = username;
	}
    
    public void sendDrawEvent(DrawEvent event) throws RemoteException {
        server.broadcastDrawEvent(event);
    }

	@Override
	public void receiveDrawEvent(DrawEvent event) throws RemoteException {
		if (this.whiteboard != null) {
//			System.out.println("Received Draw Event from Server");
			this.whiteboard.addDrawableFromNetwork(event);
		}
	}

	@Override
	public void notify(String message) throws RemoteException {
		System.out.println("Server Message: " + message);
		
	}

	@Override
	public void notifyKicked() throws RemoteException {
		JOptionPane.showMessageDialog(null, "You have been kicked.");
        System.exit(0);
	}
	
	@Override
    public void notifyManagerLeft() throws RemoteException {
        JOptionPane.showMessageDialog(null, "Manager has exited. The application will now close.");
        System.exit(0);
    }
}
