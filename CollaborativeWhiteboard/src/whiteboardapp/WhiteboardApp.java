/**
* Author: Matthias Si En Ong
* Student Id: 1590392
* Email: matthiaso@student.unimelb.edu.au
*/
package whiteboardapp;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.filechooser.FileNameExtensionFilter;
import remote.IWhiteboardServer;
import whiteboardapp.WhiteboardConstants.ShapeType;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JList;
import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import java.awt.Color;
import javax.swing.JLabel;

/**
 * This class contains the front-end logic for the GUI in the WhiteBoard Application. It creates a Window
 * using the Java Swing GUI library.
 * @version 1.0
 * @author Matthias Si En Ong
*/
public class WhiteboardApp {
	
	/** Reference to key GUI component, used in Host application to render pop up. */
	private JFrame frame;
	
	/** Tool sizes allowed to be selected. */
	private String[] toolSizes = {"1", "4", "8", "16", "32", "64"};
	
	/** Font sizes allowed to be selected. */
	private String[] fontSizes = {"8", "10", "12", "14", "16", "18", "24", "32", "48", "64"};
	
	/** Reference to the whiteboard backend. */
	private Whiteboard whiteboard;

	/** List of colours allowed to be selected. */
	private Color[] colors = {
		    Color.BLACK, Color.DARK_GRAY, Color.GRAY, Color.LIGHT_GRAY,
		    Color.WHITE, Color.RED, Color.PINK, Color.ORANGE,
		    Color.YELLOW, Color.GREEN, Color.MAGENTA, Color.CYAN,
		    Color.BLUE, new Color(139, 69, 19),
		    new Color(128, 0, 128),
		    new Color(0, 128, 128)
		};
	
	/** Reference to the whiteboard server. */
	private IWhiteboardServer server;
	
	/** username of the whiteboard client. */
	private String username;
	
	/** Keep track of current colour selection. */
	private JButton selectedColourBtn = null; // track colour selection button
	
	/** Reference to the User List model GUI component. */
	private DefaultListModel<String> userListModel = new DefaultListModel<>();
	
	/** Reference to the Chat Area GUI component. */
	private JTextArea chatArea;
	
	/** Reference to the Chat Input GUI component. */
	private JTextField chatInput;
	
	/** Boolean for manager permissions. */
	private Boolean isManager;
	
	/** Reference to current file selected. */
	private File currentFile;


	/**
     * The constructor of the WhiteboardApp.
     *
     * @param args Command line arguments, it should be in the order server-address, server-port.
     */
	public WhiteboardApp(IWhiteboardServer rmiServer, String username, Boolean isManager) {
		this.server = rmiServer;
		this.username = username;
		this.whiteboard = new Whiteboard(rmiServer);
		this.isManager = isManager;
		initialise();
		// run the connection in the background
	}

	/**
     * This function initialises the WhiteboardApp with GUI elements and event listeners.
     */
	private void initialise() {
		frame = new JFrame();
		frame.setTitle("Collaborative Whiteboard");
		frame.setVisible(true);
		frame.setSize(1000, 650);
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame.setResizable(false);
		frame.setLocationRelativeTo(null);
		frame.getContentPane().setLayout(null);
		// Image Icon made by Freepik from @flaticon https://www.flaticon.com/free-icon/paint-brush_595570 
		frame.setIconImage(Toolkit.getDefaultToolkit().getImage(WhiteboardApp.class.getResource("/paint-brush.png")));
		
		if (this.isManager) {
			JMenuBar menuBar = new JMenuBar();
			menuBar.setBounds(0, 0, 600, 25);
			
			JMenu fileMenu = new JMenu("File");
			menuBar.add(fileMenu);
			
			JMenuItem newItem = new JMenuItem("New");
			newItem.addActionListener(_ -> {
				try {
					this.whiteboard.setDrawHistory(new ArrayList<>());
					server.setDrawHistory(this.whiteboard.getDrawHistory());
					server.broadcastWhiteboardHistory();
					this.currentFile = null;
				} catch (RemoteException e) {
					System.out.println("Error broadcasting whiteboard!");
				}
	        });
	        JMenuItem openItem = new JMenuItem("Open");
	        openItem.addActionListener(_ -> {
	        	JFileChooser fileChooser = new JFileChooser();
	        	fileChooser.setFileFilter(new FileNameExtensionFilter("Whiteboard Files (*.wbd)", "wbd"));
	        	if (fileChooser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
	        		File file = fileChooser.getSelectedFile();
	        		try {
	        			whiteboard.loadFromFile(file);
	        			server.setDrawHistory(this.whiteboard.getDrawHistory());
	        			server.broadcastWhiteboardHistory();
	        		} catch (Exception ex) {
	        			JOptionPane.showMessageDialog(frame, "Error loading file: " + ex.getMessage());
	        		}
	        	}
	        });
	        JMenuItem saveItem = new JMenuItem("Save");
	        saveItem.addActionListener(_ -> {
	        	if (currentFile != null) {
	        		try {
	        			whiteboard.saveToFile(currentFile);
	        		} catch (IOException ex) {
	        			JOptionPane.showMessageDialog(frame, "Error saving file: " + ex.getMessage());
	        		}
	            } else {
	                saveAs(); // fallback to Save As
	            }
	        	
	        });
	        JMenuItem saveAsItem = new JMenuItem("Save As");
	        saveAsItem.addActionListener(_ -> {
	        	saveAs();
	        });
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
			JMenuItem kickUserItem = new JMenuItem("Kick User");
			manageMenu.add(kickUserItem);
			
			kickUserItem.addActionListener(_ -> {
			    String usernameToKick = JOptionPane.showInputDialog(frame, "Enter username to kick:");
			    if (usernameToKick != null && !usernameToKick.trim().isEmpty()) {
			        try {
			            Boolean status = server.kickUser(usernameToKick.trim());
			            if (!status) {
			            	JOptionPane.showMessageDialog(frame, "Failed to kick!");
			            }
			        } catch (RemoteException ex) {
			            JOptionPane.showMessageDialog(frame, "Error kicking user: " + ex.getMessage());
			            ex.printStackTrace();
			        }
			    }
			});


	        
	        frame.setJMenuBar(menuBar);
	        
	        closeItem.addActionListener(_ -> frame.dispose());
		}		
        
        whiteboard.setBounds(0, 75, 750, 500);
        frame.getContentPane().add(whiteboard);
        
        JLabel shapesLabel = new JLabel("Tools");
        shapesLabel.setBounds(10, 0, 40, 24);
        frame.getContentPane().add(shapesLabel);
        
        JButton lineBtn = new JButton("Line");
        lineBtn.setBounds(50, 0, 100, 24);
        lineBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
            	whiteboard.setShapeSelection(ShapeType.LINE);
            }
        });
        frame.getContentPane().add(lineBtn);
        
        JButton rectangleBtn = new JButton("Rectangle");
        rectangleBtn.setBounds(150, 0, 100, 24);
        rectangleBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
            	whiteboard.setShapeSelection(ShapeType.RECTANGLE);
            }
        });
        frame.getContentPane().add(rectangleBtn);
        
        JButton ovalBtn = new JButton("Oval");
        ovalBtn.setBounds(250, 0, 100, 24);
        ovalBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
            	whiteboard.setShapeSelection(ShapeType.OVAL);
            }
        });
        frame.getContentPane().add(ovalBtn);
        
        JButton triangleBtn = new JButton("Triangle");
        triangleBtn.setBounds(350, 0, 100, 24);
        triangleBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
            	whiteboard.setShapeSelection(ShapeType.TRIANGLE);
            }
        });
        frame.getContentPane().add(triangleBtn);
        
        JButton freeHandBtn = new JButton("Free Hand");
        freeHandBtn.setBounds(450, 0, 100, 24);
        freeHandBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
            	whiteboard.setShapeSelection(ShapeType.FREEHAND);
            }
        });
        frame.getContentPane().add(freeHandBtn);
        
        JButton eraserBtn = new JButton("Eraser");
        eraserBtn.setBounds(550, 0, 100, 24);
        eraserBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
            	whiteboard.setShapeSelection(ShapeType.ERASER);
            }
        });
        frame.getContentPane().add(eraserBtn);
        
        JButton textBtn = new JButton("Text");
        textBtn.setBounds(650, 0, 100, 24);
        textBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                whiteboard.setShapeSelection(ShapeType.TEXT);
            }
        });
        frame.getContentPane().add(textBtn);
        
        int colorBtnStartX = 10;
        int btnSize = 24;
        int spacing = 24;

        
        for (int i = 0; i < colors.length; i++) {
        	JButton colorBtn = new JButton();
        	if (i == 0) { // black is the first colour
        		colorBtn.setBorder(BorderFactory.createLineBorder(Color.BLACK, 3));
                colorBtn.setBorderPainted(true);
                selectedColourBtn = colorBtn;
        	}
            colorBtn.setBackground(colors[i]);
            colorBtn.setOpaque(true);
            colorBtn.setBorderPainted(false);
            colorBtn.setBounds(colorBtnStartX + (i * spacing), 30, btnSize, btnSize);

            Color selectedColor = colors[i];
            colorBtn.addActionListener(_ -> {
                whiteboard.setColour(selectedColor);

                // Remove highlight from previous button
                if (selectedColourBtn != null) {
                	selectedColourBtn.setBorderPainted(false);
                    selectedColourBtn.setBorder(BorderFactory.createEmptyBorder());
                }

                // Highlight current button
                colorBtn.setBorder(BorderFactory.createLineBorder(Color.BLACK, 3));
                colorBtn.setBorderPainted(true);
                selectedColourBtn = colorBtn;
            });

            frame.getContentPane().add(colorBtn);
        }
        
        JButton colorPickerBtn = new JButton("Pick Color");
        colorPickerBtn.setBounds(colorBtnStartX + (colors.length * spacing), 30, btnSize, btnSize);
        frame.getContentPane().add(colorPickerBtn);
        colorPickerBtn.addActionListener(_ -> {
            Color selectedColor = JColorChooser.showDialog(frame, "Choose a Color", whiteboard.getColour());
            if (selectedColor != null) {
            	// Remove highlight from previous button
                if (selectedColourBtn != null) {
                	selectedColourBtn.setBorderPainted(false);
                    selectedColourBtn.setBorder(BorderFactory.createEmptyBorder());
                }
                whiteboard.setColour(selectedColor);
            }
        });
        
        
        JLabel sizeLabel = new JLabel("Tool Size");
        sizeLabel.setBounds(430, 30, 80, 24);
        frame.getContentPane().add(sizeLabel);
        
        JComboBox<String> sizesDropdown = new JComboBox<>(toolSizes);
        sizesDropdown.setBounds(500, 30, 60, 24);
        sizesDropdown.setSelectedItem(whiteboard.getToolSize()); // set default selection
        frame.getContentPane().add(sizesDropdown);
        sizesDropdown.addActionListener(_ -> {
            String selected = (String) sizesDropdown.getSelectedItem();
            int size = Integer.parseInt(selected);
            whiteboard.setToolSize(size);
        });
        
        JLabel fontSizeLabel = new JLabel("Font Size");
        fontSizeLabel.setBounds(575, 30, 80, 24);
        frame.getContentPane().add(fontSizeLabel);
        
        JComboBox<String> fontSizesDropdown = new JComboBox<>(fontSizes);
        fontSizesDropdown.setBounds(645, 30, 60, 24);
        fontSizesDropdown.setSelectedItem(String.valueOf(whiteboard.getFontSize())); // set default selection
        frame.getContentPane().add(fontSizesDropdown);
        fontSizesDropdown.addActionListener(_ -> {
            String selected = (String) fontSizesDropdown.getSelectedItem();
            int size = Integer.parseInt(selected);
            whiteboard.setFontSize(size);
        });
        
        // user list
        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(null);
        rightPanel.setBounds(750, 0, 230, 600);
        frame.getContentPane().add(rightPanel);
        
        JLabel userListLabel = new JLabel("Users:");
        userListLabel.setBounds(10, 10, 100, 20);
        rightPanel.add(userListLabel);
        
        JList<String> userList = new JList<>(userListModel);
        JScrollPane userScroll = new JScrollPane(userList);
        userScroll.setBounds(10, 30, 210, 125);
        rightPanel.add(userScroll);
        
        // chat
        JLabel chatLabel = new JLabel("Chat:");
        chatLabel.setBounds(10, 165, 100, 20);
        rightPanel.add(chatLabel);
        chatArea = new JTextArea();
        chatArea.setEditable(false);
        chatArea.setLineWrap(true);
        chatArea.setWrapStyleWord(true);
        JScrollPane chatScroll = new JScrollPane(chatArea);
        chatScroll.setBounds(10, 185, 210, 320);
//        chatScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        rightPanel.add(chatScroll);
        
        chatInput = new JTextField();
        chatInput.setBounds(10, 515, 140, 24);
        rightPanel.add(chatInput);

        JButton sendBtn = new JButton("Send");
        sendBtn.setBounds(150, 515, 70, 24);
        sendBtn.addActionListener(_ -> {
        	String message = chatInput.getText().trim();
        	try {
				this.server.broadcastChatMessage(this.username, message);
				chatInput.setText("");
			} catch (RemoteException e1) {
				System.out.println("Failed to broadcast message!");
			}
        });
        rightPanel.add(sendBtn);
	}
	
	/**
     * This function implements saveAs logic which opens a file chooser.
     */
	private void saveAs() {
		JFileChooser fileChooser = new JFileChooser();
    	fileChooser.setFileFilter(new FileNameExtensionFilter("Whiteboard Files (*.wbd)", "wbd"));
    	if (fileChooser.showSaveDialog(frame) == JFileChooser.APPROVE_OPTION) {
    		File file = fileChooser.getSelectedFile();
    		try {
    			whiteboard.saveToFile(file);
    			this.currentFile = file;
    		} catch (IOException ex) {
    			JOptionPane.showMessageDialog(frame, "Error saving file: " + ex.getMessage());
    		}
    	}
	}
	
	/**
     * Getter for the whiteboard.
     */
	public Whiteboard getWhiteBoard() {
		return this.whiteboard;
	}
	
	/**
     * Getter for the frame. Used in Host application.
     */
	public JFrame getFrame() {
		return this.frame;
	}
	
	/**
     * Getter for the user list model.
     */
	public DefaultListModel<String> getUserList() {
		return this.userListModel;
	}

	/**
     * Get chat area GUI
     */
	public JTextArea getChatArea() {
		return this.chatArea;
	}
}
