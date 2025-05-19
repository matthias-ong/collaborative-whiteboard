/**
* Author: Matthias Si En Ong
* Student Id: 1590392
* Email: matthiaso@student.unimelb.edu.au
*/
package client;

import java.awt.EventQueue;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;

import remote.DrawEvent;
import remote.IWhiteboardClient;
import remote.IWhiteboardServer;
import whiteboardapp.Whiteboard;
import whiteboardapp.Whiteboard.Drawable;

/**
 * This class contains the implementation of the client interface. It implements client behavior and 
 * also acts as a server to receive updates from the server to update the local canvas.
 * 
 * @version 1.0
 * @author Matthias Si En Ong
 */
public class WhiteboardClientServant extends UnicastRemoteObject implements IWhiteboardClient {
	/** The version identifier */
	private static final long serialVersionUID = 1L;
	
	/** Keeps a reference of the server to make calls */
	private IWhiteboardServer server;
    
	/** Reference of the client whiteboard */
    private Whiteboard whiteboard;
    
    /** Reference of the client chat area */
    private JTextArea chatArea;
    
    /** Reference of the client user list model */
    private DefaultListModel<String> userListModel;

    /**
     * The function initialises the client servant program.
     *
     * @param whiteboard
     * @param chatArea
     * @param userListModel
     */
    public void initialise(Whiteboard whiteboard, JTextArea chatArea, DefaultListModel<String> userListModel) {
    	this.whiteboard = whiteboard;
        this.chatArea = chatArea;
        this.userListModel = userListModel;
    }
    
    /**
     * Constructor
     */
    public WhiteboardClientServant() throws RemoteException {
	}
    
    /**
     * This function sends a client's draw event to the server to broadcast.
     * @param event Client's DrawEvent 
     */
    public void sendDrawEvent(DrawEvent event) throws RemoteException {
        server.broadcastDrawEvent(event);
    }

    /**
     * This function receives a a draw event from the server and updates the whiteboard.
     * @param event
     */
	@Override
	public void receiveDrawEvent(DrawEvent event) throws RemoteException {
		if (this.whiteboard != null) {
//			System.out.println("Received Draw Event from Server");
			this.whiteboard.addDrawableFromNetwork(event);
		}
	}
	
	/**
     * This function updates the entire client's whiteboard with a list of draw history from the server.
     * @param drawHistory
     */
	@Override
	public void updateWhiteboard(List<Drawable> drawHistory) throws RemoteException {
		if (this.whiteboard != null) {
			EventQueue.invokeLater(() -> {
				// make a copy of the draw history
				List<Drawable> copyDrawHistory = new ArrayList<>();
				for (Drawable d : drawHistory) {
				    copyDrawHistory.add(d.copy());
				}
				this.whiteboard.setDrawHistory(drawHistory);
	        });
		}
	}

	/**
     * This function notifies the client with a message in the chat area.
     * @param message
     */
	@Override
	public void notify(String message) throws RemoteException {
		System.out.println("Server Message: " + message);
		if (chatArea != null) {
	        EventQueue.invokeLater(() -> {
	            chatArea.append(message + "\n");
	        });
	    }
		
	}

	/**
     * This function notifies the client with a message that they were kicked.
     */
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
	
	/**
     * This function notifies the client that the whiteboard manager left and will close the application cleanly.
     */
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
	
	/**
     * This function updates the user list of the whiteboard.
     * @param userList
     */
	@Override
	public void updateUserList(List<String> userList) throws RemoteException {
		EventQueue.invokeLater(() -> {
			this.userListModel.clear();
			for (String user : userList) {
				this.userListModel.addElement(user);
	        }
		});
	}

	/**
     * This is the getter for the client's whiteboard reference in this class.
     */
	public Whiteboard getWhiteboard() throws RemoteException {
		return this.whiteboard;
	}
}
