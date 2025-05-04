/**
* Author: Matthias Si En Ong
* Student Id: 1590392
* Email: matthiaso@student.unimelb.edu.au
*/
package WhiteboardApp;

import java.awt.Font;
import java.awt.Frame;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingWorker;

import constants.Constants;
import network.DictionaryClient;

import javax.swing.JPanel;
import javax.swing.JList;
import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;

/**
 * This class contains the front-end logic for the GUI in the WhiteBoard Application. It creates a Window
 * using the Java Swing GUI library.
 * @version 1.0
 * @author Matthias Si En Ong
*/
public class MainWindow {
	// key UI components
	private JFrame frame;

	/**
     * The constructor of the MainWindow.
     *
     * @param args Command line arguments, it should be in the order server-address, server-port.
     */
	public MainWindow(String args[]) {
		initialise();
		// run the connection in the background
	}

	/**
     * This function initialises the MainWindow with GUI elements.
     */
	private void initialise() {
		frame = new JFrame();
		frame.setTitle("Collaborative Whiteboard");
		frame.setVisible(true);
		frame.setSize(1000, 700);
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame.setResizable(false);
		frame.setLocationRelativeTo(null);
		frame.getContentPane().setLayout(null);
		frame.setIconImage(Toolkit.getDefaultToolkit().getImage(MainWindow.class.getResource("/WhiteboardApp/dictionary.png")));
		
		JMenuBar menuBar = new JMenuBar();
		menuBar.setBounds(0, 0, 600, 25);
		
		JMenu fileMenu = new JMenu("File");
		menuBar.add(fileMenu);
		
		JMenuItem newItem = new JMenuItem("New");
        JMenuItem openItem = new JMenuItem("Open");
        JMenuItem saveItem = new JMenuItem("Save");
        JMenuItem saveAsItem = new JMenuItem("Save As");
        JMenuItem closeItem = new JMenuItem("Close");

        fileMenu.add(newItem);
        fileMenu.add(openItem);
        fileMenu.add(saveItem);
        fileMenu.add(saveAsItem);
        fileMenu.addSeparator();
        fileMenu.add(closeItem);
        menuBar.add(fileMenu);
        
        JMenu manageMenu = new JMenu("Manage");
		menuBar.add(manageMenu);
        
        
        frame.setJMenuBar(menuBar);
        
        // Example action for "Close"
        closeItem.addActionListener(_ -> frame.dispose());
        
        Whiteboard whiteboard = new Whiteboard();
        whiteboard.setBounds(0, 25, 800, 600);
        frame.add(whiteboard);
	}
}
