/**
* Author: Matthias Si En Ong
* Student Id: 1590392
* Email: matthiaso@student.unimelb.edu.au
*/
package whiteboardapp;

import java.awt.Font;
import java.awt.Frame;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingWorker;

import constants.Constants;
import constants.Constants.ShapeType;
import network.DictionaryClient;

import javax.swing.JPanel;
import javax.swing.JList;
import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import javax.swing.JLabel;

/**
 * This class contains the front-end logic for the GUI in the WhiteBoard Application. It creates a Window
 * using the Java Swing GUI library.
 * @version 1.0
 * @author Matthias Si En Ong
*/
public class MainWindow {
	// key UI components
	private JFrame frame;
	
	private String[] eraserSizes = {"4", "8", "16", "32", "64"};
	Color[] colors = {
		    Color.BLACK, Color.DARK_GRAY, Color.GRAY, Color.LIGHT_GRAY,
		    Color.WHITE, Color.RED, Color.PINK, Color.ORANGE,
		    Color.YELLOW, Color.GREEN, Color.MAGENTA, Color.CYAN,
		    Color.BLUE, new Color(139, 69, 19),
		    new Color(128, 0, 128),
		    new Color(0, 128, 128)
		};


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
        whiteboard.setBounds(0, 75, 800, 550);
        frame.getContentPane().add(whiteboard);
        
        JLabel shapesLabel = new JLabel("Tools");
        shapesLabel.setBounds(10, 0, 50, 24);
        frame.getContentPane().add(shapesLabel);
        
        JButton lineBtn = new JButton("Line");
        lineBtn.setBounds(75, 0, 100, 24);
        lineBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
            	whiteboard.setShapeSelection(ShapeType.LINE);
            }
        });
        frame.getContentPane().add(lineBtn);
        
        JButton rectangleBtn = new JButton("Rectangle");
        rectangleBtn.setBounds(175, 0, 100, 24);
        rectangleBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
            	whiteboard.setShapeSelection(ShapeType.RECTANGLE);
            }
        });
        frame.getContentPane().add(rectangleBtn);
        
        JButton ovalBtn = new JButton("Oval");
        ovalBtn.setBounds(275, 0, 100, 24);
        ovalBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
            	whiteboard.setShapeSelection(ShapeType.OVAL);
            }
        });
        frame.getContentPane().add(ovalBtn);
        
        JButton triangleBtn = new JButton("Triangle");
        triangleBtn.setBounds(375, 0, 100, 24);
        triangleBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
            	whiteboard.setShapeSelection(ShapeType.TRIANGLE);
            }
        });
        frame.getContentPane().add(triangleBtn);
        
        JButton freeHandBtn = new JButton("Free Hand");
        freeHandBtn.setBounds(475, 0, 100, 24);
        freeHandBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
            	whiteboard.setShapeSelection(ShapeType.FREEHAND);
            }
        });
        frame.getContentPane().add(freeHandBtn);
        
        JButton eraserBtn = new JButton("Eraser");
        eraserBtn.setBounds(575, 0, 100, 24);
        eraserBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
            	whiteboard.setShapeSelection(ShapeType.ERASER);
            }
        });
        frame.getContentPane().add(eraserBtn);
        
        JComboBox<String> eraserSizeDropdown = new JComboBox<>(eraserSizes);
        eraserSizeDropdown.setBounds(675, 0, 60, 24); // Positioned next to Eraser button
        frame.getContentPane().add(eraserSizeDropdown);
        eraserSizeDropdown.addActionListener(_ -> {
            String selected = (String) eraserSizeDropdown.getSelectedItem();
            int size = Integer.parseInt(selected);
            whiteboard.setEraserSize(size);
        });
        
        int colorBtnStartX = 10;
        int btnSize = 30;
        int spacing = 32;

        for (int i = 0; i < colors.length; i++) {
            JButton colorBtn = new JButton();
            colorBtn.setBackground(colors[i]);
            colorBtn.setOpaque(true);
            colorBtn.setBorderPainted(false);
            colorBtn.setBounds(colorBtnStartX + (i * spacing), 30, btnSize, btnSize);

            Color selectedColor = colors[i]; // effectively final
            colorBtn.addActionListener(e -> whiteboard.setColour(selectedColor));

            frame.getContentPane().add(colorBtn);
        }
        
        JButton colorPickerBtn = new JButton("Pick Color");
        colorPickerBtn.setBounds(colorBtnStartX + (colors.length * spacing), 30, btnSize, btnSize);
        frame.getContentPane().add(colorPickerBtn);
        colorPickerBtn.addActionListener(_ -> {
            Color selectedColor = JColorChooser.showDialog(frame, "Choose a Color", whiteboard.getColour());
            if (selectedColor != null) {
                whiteboard.setColour(selectedColor);
            }
        });
	}
}
