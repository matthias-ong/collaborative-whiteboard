package whiteboardapp;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Stroke;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import constants.Constants.ShapeType;
import remote.DrawEvent;
import remote.IWhiteboardServer;

public class Whiteboard extends JPanel {

	private List<Drawable> drawHistory = new ArrayList<>();
	private List<Point> currentStroke = null;
	private Point startPoint;
	private Point endPoint;

	private ShapeType currentShape = ShapeType.FREEHAND;
	private int toolSize = 1;
	private int fontSize = 12;
	private Color currColour = Color.BLACK;

	IWhiteboardServer rmiServer; // refer to the whiteboard server to synchronise information with

	abstract class Drawable {
		List<Point> points;
		Color color;
		int size;

		abstract void draw(Graphics2D g2);
	}

	class NormalStroke extends Drawable {
		public NormalStroke(List<Point> points, Color color, int size) {
			this.points = points;
			this.color = color;
			this.size = size;
		}

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
	}

	class TextField extends Drawable {
		String text;

		public TextField(String text, int x, int y, Color color, int size) {
			Point p1 = new Point(x, y);
			this.points = new ArrayList<Point>(1);
			this.points.add(p1);
			this.color = color;
			this.text = text;
			this.size = size;
		}

		@Override
		void draw(Graphics2D g2) {
			g2.setColor(color);
			g2.setFont(new Font("Arial", Font.PLAIN, this.size));
//        	g2.setFont(g2.getFont().deriveFont(this.size));
			Point p1 = this.points.get(0);
			g2.drawString(text, p1.x, p1.y);
		}
	}

	class EraserStroke extends Drawable {
		public EraserStroke(List<Point> points, int size) {
			this.points = points;
			this.size = size;
		}

		@Override
		void draw(Graphics2D g2) {
			g2.setColor(Color.WHITE);
//            Stroke originalStroke = g2.getStroke();
			g2.setStroke(new BasicStroke(this.size, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
			for (int i = 1; i < points.size(); i++) {
				Point p1 = points.get(i - 1);
				Point p2 = points.get(i);
				g2.drawLine(p1.x, p1.y, p2.x, p2.y);
			}
//            g2.setStroke(originalStroke);
		}
	}

	class ShapeInfo extends Drawable {
		ShapeType type;
		Color color;

		public ShapeInfo(ShapeType type, Point p1, Point p2, Color color, int size) {
			this.points = new ArrayList<Point>(2);
			this.points.add(p1);
			this.points.add(p2);
			this.type = type;
			this.color = color;
			this.size = size;
		}

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
	}

	public Whiteboard(IWhiteboardServer rmiServer) {
		this.rmiServer = rmiServer;
		setBackground(Color.WHITE);

		addMouseListener(new MouseAdapter() {
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

	public Color getColour() {
		return this.currColour;
	}

	public int getToolSize() {
		return this.toolSize;
	}

	public int getFontSize() {
		return this.fontSize;
	}

	public void setShapeSelection(ShapeType newShape) {
		this.currentShape = newShape;
	}

	public void setToolSize(int newSize) {
		this.toolSize = newSize;
	}

	public void setFontSize(int fontSize) {
		this.fontSize = fontSize;
	}

	public void setColour(Color colour) {
		this.currColour = colour;
	}
}
