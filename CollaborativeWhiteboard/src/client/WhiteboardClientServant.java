package client;

import java.awt.EventQueue;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;

import remote.DrawEvent;
import remote.IWhiteboardClient;
import remote.IWhiteboardServer;
import whiteboardapp.Whiteboard;
import whiteboardapp.Whiteboard.Drawable;

public class WhiteboardClientServant extends UnicastRemoteObject implements IWhiteboardClient {
	private IWhiteboardServer server;
    private String username;
    
    private Whiteboard whiteboard;
    private JTextArea chatArea;
    private DefaultListModel<String> userListModel;

    public void initialise(Whiteboard whiteboard, JTextArea chatArea, DefaultListModel<String> userListModel) {
    	this.whiteboard = whiteboard;
        this.chatArea = chatArea;
        this.userListModel = userListModel;
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
	public void updateWhiteboard(List<Drawable> drawHistory) throws RemoteException {
		if (this.whiteboard != null) {
			EventQueue.invokeLater(() -> {
				this.whiteboard.setDrawHistory(drawHistory);
	        });
		}
	}

	@Override
	public void notify(String message) throws RemoteException {
		System.out.println("Server Message: " + message);
		if (chatArea != null) {
	        EventQueue.invokeLater(() -> {
	            chatArea.append(message + "\n");
	        });
	    }
		
	}

	@Override
	public void notifyKicked() throws RemoteException {
		EventQueue.invokeLater(() -> {
			JOptionPane.showMessageDialog(null, "You have been kicked.");
			try {
                UnicastRemoteObject.unexportObject(this, true);
                System.out.println("Client shutdown cleanly.");
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            System.exit(0);
		});
	}
	
	@Override
    public void notifyManagerLeft() throws RemoteException {
		EventQueue.invokeLater(() -> {
			JOptionPane.showMessageDialog(null, "Manager has exited. The application will now close.");
			try {
                UnicastRemoteObject.unexportObject(this, true);
                System.out.println("Client shutdown cleanly.");
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            System.exit(0);
		});
        
        
    }
	
	@Override
	public void updateUserList(List<String> userList) throws RemoteException {
		EventQueue.invokeLater(() -> {
			this.userListModel.clear();
			for (String user : userList) {
				this.userListModel.addElement(user);
	        }
		});
	}

	public String getUsername() {
		return username;
	}
}
