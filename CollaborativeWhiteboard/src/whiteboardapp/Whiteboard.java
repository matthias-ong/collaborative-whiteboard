/**
* Author: Matthias Si En Ong
* Student Id: 1590392
* Email: matthiaso@student.unimelb.edu.au
*/
package whiteboardapp;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import remote.DrawEvent;
import remote.IWhiteboardServer;
import whiteboardapp.WhiteboardConstants.ShapeType;

/**
 * This class contains the main Whiteboard back end functionality.
 * 
 * @version 1.0
 * @author Matthias Si En Ong
 */
public class Whiteboard extends JPanel {

	/** The version identifier */
	private static final long serialVersionUID = 1L;
	
	/** Draw history of the whiteboard. */
	private List<Drawable> drawHistory;
	
	/** Current stroke */
	private List<Point> currentStroke = null;
	
	/** Starting point */
	private Point startPoint;
	
	/** End point */
	private Point endPoint;

	/** Current shape that is being drawn. */
	private ShapeType currentShape = ShapeType.FREEHAND;
	
	/** Current tool size that is being used. */
	private int toolSize = 1;
	
	/** Current tool size that is being used. */
	private int fontSize = 12;
	
	/** Current colour that is being used. */
	private Color currColour = Color.BLACK;

	/** Reference to the whiteboard server to synchronise information with */
	IWhiteboardServer rmiServer;

	/**
	 * An abstract base class representing a drawable shape or stroke on the whiteboard.
	 * This class holds common attributes like the list of points, color, and stroke size,
	 * and defines an abstract method for rendering the object. Subclasses should implement how
	 * to render the drawable object.
	 * 
	 * @version 1.0
	 * @author Matthias Si En Ong
	 */
	public abstract class Drawable implements Serializable {
		/** The version identifier */
		private static final long serialVersionUID = 1L;

		/** The sequence of points that make up the drawable shape or stroke. */
		List<Point> points;
		
		/** Current colour that is being used. */
		Color color;
		
		/** Current size that is being used. */
		int size;

		/** Draws the shape on the provided. */
		abstract void draw(Graphics2D g2);
		
		/** Makes a deep copy of Drawable to prevent side effects across clients. */
		public abstract Drawable copy();

	}

	/**
	 * An class extends Drawable to implement a freehand stroke.
	 * 
	 * @version 1.0
	 * @author Matthias Si En Ong
	 */
	class NormalStroke extends Drawable {
		/** The version identifier */
		private static final long serialVersionUID = 1L;

		/**
		 * Constructor of NormalStroke.
	     * @param points
	     * @param color
	     * @param size
	     */
		public NormalStroke(List<Point> points, Color color, int size) {
			this.points = points;
			this.color = color;
			this.size = size;
		}

		/**
		 * Implements the rendering of a normal stroke.
	     * @param g2 Graphics2D to provide 2D rendering functionality.
	     */
		@Override
		void draw(Graphics2D g2) {
			g2.setStroke(new BasicStroke(this.size, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
			g2.setColor(color);
			for (int i = 1; i < points.size(); i++) {
				Point p1 = points.get(i - 1);
				Point p2 = points.get(i);
				g2.drawLine(p1.x, p1.y, p2.x, p2.y);
			}
		}
		
		/**
		 * Implements the copying.
	     */
		@Override
		public Drawable copy() {
		    List<Point> copiedPoints = new ArrayList<>();
		    for (Point p : this.points) {
		        copiedPoints.add(new Point(p));
		    }
		    return new NormalStroke(copiedPoints, new Color(color.getRGB()), size);
		}
	}

	/**
	 * An class extends Drawable to implement a text drawable.
	 * 
	 * @version 1.0
	 * @author Matthias Si En Ong
	 */
	class TextField extends Drawable {
		
		/** The version identifier */
		private static final long serialVersionUID = 1L;
		
		/** The text for the text field. */
		String text;

		/**
		 * Constructor of TextField.
		 * @param text
	     * @param x
	     * @param y
	     * @param color
	     * @param size
	     */
		public TextField(String text, int x, int y, Color color, int size) {
			Point p1 = new Point(x, y);
			this.points = new ArrayList<Point>(1);
			this.points.add(p1);
			this.color = color;
			this.text = text;
			this.size = size;
		}

		/**
		 * Implements the rendering of a text field.
	     * @param g2 Graphics2D to provide 2D rendering functionality.
	     */
		@Override
		void draw(Graphics2D g2) {
			g2.setColor(color);
			g2.setFont(new Font("Arial", Font.PLAIN, this.size));
			Point p1 = this.points.get(0);
			g2.drawString(text, p1.x, p1.y);
		}
		
		/**
		 * Implements the copying.
	     */
		@Override
		public Drawable copy() {
		    Point p = this.points.get(0);
		    return new TextField(new String(text), p.x, p.y, new Color(color.getRGB()), size);
		}
	}

	/**
	 * An class extends drawable to implement an eraser stroke.
	 * 
	 * @version 1.0
	 * @author Matthias Si En Ong
	 */
	class EraserStroke extends Drawable {
		
		/** The version identifier */
		private static final long serialVersionUID = 1L;

		/**
		 * Constructor of EraserStroke.
		 * @param points
	     * @param size
	     */
		public EraserStroke(List<Point> points, int size) {
			this.points = points;
			this.size = size;
		}

		/**
		 * Implements the rendering of an eraser stroke.
	     * @param g2 Graphics2D to provide 2D rendering functionality.
	     */
		@Override
		void draw(Graphics2D g2) {
			g2.setColor(Color.WHITE);
			g2.setStroke(new BasicStroke(this.size, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
			for (int i = 1; i < points.size(); i++) {
				Point p1 = points.get(i - 1);
				Point p2 = points.get(i);
				g2.drawLine(p1.x, p1.y, p2.x, p2.y);
			}
		}
		
		/**
		 * Implements the copying.
	     */
		@Override
		public Drawable copy() {
		    List<Point> copiedPoints = new ArrayList<>();
		    for (Point p : this.points) {
		        copiedPoints.add(new Point(p));
		    }
		    return new EraserStroke(copiedPoints, size);
		}
	}

	/**
	 * An class extends drawable to implement different shape types.
	 * 
	 * @version 1.0
	 * @author Matthias Si En Ong
	 */
	class ShapeInfo extends Drawable {
		
		/** The version identifier */
		private static final long serialVersionUID = 1L;
		
		/** The type of shape. */
		ShapeType type;

		/**
		 * Constructor of ShapeInfo.
		 * @param type Type of shape
	     * @param p1
	     * @param p2
	     * @param color
	     * @param size
	     */
		public ShapeInfo(ShapeType type, Point p1, Point p2, Color color, int size) {
			this.points = new ArrayList<Point>(2);
			this.points.add(p1);
			this.points.add(p2);
			this.type = type;
			this.color = color;
			this.size = size;
		}

		/**
		 * Implements the rendering of a shape.
	     * @param g Graphics2D to provide 2D rendering functionality.
	     */
		@Override
		void draw(Graphics2D g) {
			g.setStroke(new BasicStroke(this.size));
			Point p1 = this.points.get(0);
			Point p2 = this.points.get(1);
			int x = Math.min(p1.x, p2.x);
			int y = Math.min(p1.y, p2.y);
			int w = Math.abs(p1.x - p2.x);
			int h = Math.abs(p1.y - p2.y);
			g.setColor(color);
			switch (type) {
			case LINE:
				g.drawLine(p1.x, p1.y, p2.x, p2.y);
				break;
			case RECTANGLE:
				g.drawRect(x, y, w, h);
				break;
			case OVAL:
				g.drawOval(x, y, w, h);
				break;
			case TRIANGLE:
				int midX = (p1.x + p2.x) / 2;
				int[] xPoints = { midX, p1.x, p2.x };
				int[] yPoints = { p1.y, p2.y, p2.y };
				g.drawPolygon(xPoints, yPoints, 3);
				break;
			default:
				break;
			}
		}
		
		/**
		 * Implements the copying.
	     */
		@Override
		public Drawable copy() {
		    Point p1 = this.points.get(0);
		    Point p2 = this.points.get(1);
		    return new ShapeInfo(type, new Point(p1), new Point(p2), new Color(color.getRGB()), size);
		}
	}

	/**
	 * Constructor of Whiteboard. Initialises the whiteboard.
     * @param rmiServer
     */
	public Whiteboard(IWhiteboardServer rmiServer) {
		this.rmiServer = rmiServer;
		this.drawHistory = new ArrayList<>();
		setBackground(Color.WHITE);

		addMouseListener(new MouseAdapter() {
			
			// Upon mouse pressed listener
			public void mousePressed(MouseEvent e) {
				if (SwingUtilities.isLeftMouseButton(e)) {
					Drawable drawable = null;
					if (currentShape == ShapeType.FREEHAND) {
						currentStroke = new ArrayList<>();
						currentStroke.add(e.getPoint());
						drawable = new NormalStroke(currentStroke, currColour, toolSize);
						drawHistory.add(drawable);
					} else if (currentShape == ShapeType.ERASER) {
						currentStroke = new ArrayList<>();
						currentStroke.add(e.getPoint());
						drawable = new EraserStroke(currentStroke, toolSize);
						drawHistory.add(drawable);
					} else if (currentShape == ShapeType.TEXT) {
						String inputText = JOptionPane.showInputDialog("Enter text:");
						if (inputText != null && !inputText.trim().isEmpty()) {
							drawable = new TextField(inputText, e.getX(), e.getY(), currColour, fontSize);
							drawHistory.add(drawable);
						}
					} else {
						currentStroke = null;
						startPoint = e.getPoint();
						endPoint = startPoint;
					}
					if (drawable != null && rmiServer != null) {
						DrawEvent drawEvent = convertToDrawableData(drawable);
						try {
							rmiServer.broadcastDrawEvent(drawEvent);
							System.out.println("Broadcast Draw Event!");
						} catch (RemoteException e1) {
							System.out.println("Failed to broadcast draw event!");
						}
					}
					repaint();

				} else if (SwingUtilities.isRightMouseButton(e)) {
					if (currentStroke != null) {
						currentStroke.clear();
					}
					startPoint = endPoint = null;
					repaint();
				}
			}

			// Upon mouse released listener
			public void mouseReleased(MouseEvent e) {
				endPoint = e.getPoint();
				Drawable drawable = null;
				if (currentShape == ShapeType.FREEHAND && currentStroke != null && !currentStroke.isEmpty()) {
					drawable = new NormalStroke(new ArrayList<>(currentStroke), currColour, toolSize);
					drawHistory.add(drawable);
					currentStroke = null;
				}
				else if (currentShape == ShapeType.ERASER && currentStroke != null && !currentStroke.isEmpty()) {
					drawable = new EraserStroke(new ArrayList<>(currentStroke), toolSize);
					drawHistory.add(drawable);
					currentStroke = null;
				}
				else if (currentShape != ShapeType.FREEHAND && startPoint != null) {
					drawable = new ShapeInfo(currentShape, startPoint, endPoint, currColour, toolSize);
					drawHistory.add(drawable);
					
				}
				startPoint = endPoint = null;
				if (drawable != null && rmiServer != null) {
					DrawEvent drawEvent = convertToDrawableData(drawable);
					try {
						rmiServer.broadcastDrawEvent(drawEvent);
						System.out.println("Broadcast Draw Event!");
					} catch (RemoteException e1) {
						System.out.println("Failed to broadcast draw event!");
					}
				}
				repaint();
			}
		});

		// Upon mouse moved listener
		addMouseMotionListener(new MouseMotionAdapter() {
			public void mouseDragged(MouseEvent e) {
				endPoint = e.getPoint();
				if (currentStroke != null) {
					currentStroke.add(e.getPoint());
				}
				repaint();
			}
		});
	}

	/**
	 * Renders the whiteboard and its elements.
	 * @param g Graphics for rendering functionality.
     */
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2 = (Graphics2D)g.create();

		for (Drawable item : drawHistory) {
			item.draw(g2);
		}

		// draw incomplete shape
		if (startPoint != null && endPoint != null) {
			new ShapeInfo(currentShape, startPoint, endPoint, currColour, toolSize).draw(g2);
		}
	}

	/**
	 * Add a drawable obtained from the whiteboard server to the client's whiteboard.
	 * @param event
     */
	public void addDrawableFromNetwork(DrawEvent event) {
		System.out.println("Received Draw Event from Server");
		Drawable drawable = null;
		switch (event.type) {
		case ShapeType.FREEHAND:
			drawable = new NormalStroke(event.points, event.colour, event.size);
			break;
		case ShapeType.ERASER:
			drawable = new EraserStroke(event.points, event.size);
			break;
		case ShapeType.TEXT:
			Point textPoint = event.points.get(0);
			drawable = new TextField(event.text, textPoint.x, textPoint.y, event.colour, event.size);
			break;
		case LINE: // all other shapes
		case RECTANGLE:
		case OVAL:
		case TRIANGLE:
			Point p1 = event.points.get(0);
			Point p2 = event.points.get(1);
			drawable = new ShapeInfo(event.type, p1, p2, event.colour, event.size);
			break;
		default:
			System.err.println("Unknown shape type from network: " + event.type);
			return;
		}

		drawHistory.add(drawable);
		repaint();
	}

	/**
	 * Convert Drawable to DrawEvent to send over the network.
	 * @param d Drawable
     */
	public DrawEvent convertToDrawableData(Drawable d) {

		if (d instanceof NormalStroke) {
			NormalStroke ns = (NormalStroke) d;
			return new DrawEvent(ShapeType.FREEHAND, ns.points, null, ns.size, ns.color);
		} else if (d instanceof EraserStroke) {
			EraserStroke es = (EraserStroke) d;
			return new DrawEvent(ShapeType.ERASER, es.points, null, es.size, Color.WHITE);
		} else if (d instanceof TextField) {
			TextField tf = (TextField) d;
			return new DrawEvent(ShapeType.TEXT, d.points, tf.text, tf.size, tf.color);
		} else if (d instanceof ShapeInfo) {
			ShapeInfo si = (ShapeInfo) d;
			return new DrawEvent(si.type, si.points, null, si.size, si.color);
		}
		return null;
	}
	
	/**
	 * Saves whiteboard to file with .wbd
	 * @param file
     */
	public void saveToFile(File file) throws IOException {
		if (!file.getName().toLowerCase().endsWith(".wbd")) {
	        file = new File(file.getAbsolutePath() + ".wbd");
	    }
		
		try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(file))) {
			out.writeObject(drawHistory);
		}
	}

	/**
	 * Loads a saved .wbd file to the local whiteboard and renders it.
	 * @param file
     */
	@SuppressWarnings("unchecked")
	public void loadFromFile(File file) throws Exception {
		try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(file))) {
			this.drawHistory = (List<Drawable>)in.readObject();
			repaint();
		}
	}

	/**
	 * Getter for current colour.
     */
	public Color getColour() {
		return this.currColour;
	}

	/**
	 * Getter for current tool size.
     */
	public int getToolSize() {
		return this.toolSize;
	}

	/**
	 * Getter for current font size.
     */
	public int getFontSize() {
		return this.fontSize;
	}
	
	/**
	 * Getter for entire draw history/whiteboard state.
     */
	public List<Drawable> getDrawHistory() {
		return this.drawHistory;
	}

	/**
	 * Setter for current shape selection.
     */
	public void setShapeSelection(ShapeType newShape) {
		this.currentShape = newShape;
	}

	/**
	 * Setter for current tool size.
     */
	public void setToolSize(int newSize) {
		this.toolSize = newSize;
	}

	/**
	 * Setter for current font size.
     */
	public void setFontSize(int fontSize) {
		this.fontSize = fontSize;
	}

	/**
	 * Setter for current colour.
     */
	public void setColour(Color colour) {
		this.currColour = colour;
	}
	
	/**
	 * Setter for entire draw history/whiteboard state.
     */
	public void setDrawHistory(List<Drawable> newHistory) {
		this.drawHistory = newHistory;
		currentStroke = null;
		startPoint = null;
		endPoint = null;
		repaint();
	}
	
}
