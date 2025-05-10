package whiteboardapp;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import constants.Constants.ShapeType;

public class Whiteboard extends JPanel {

    private List<List<Point>> strokes = new ArrayList<>();
    private List<ShapeInfo> shapes = new ArrayList<>();
    private List<Point> currentStroke = null;
    private Point startPoint;
    private Point endPoint;
    
    private ShapeType currentShape = ShapeType.TRIANGLE;
    
    public Whiteboard() {
        setBackground(Color.WHITE);
        
        addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e)) {
                	if (currentShape == ShapeType.FREEHAND) {
                		currentStroke = new ArrayList<>();
                        currentStroke.add(e.getPoint());
                        strokes.add(currentStroke);
                	}
                	else {
                		startPoint = e.getPoint();
                        endPoint = startPoint;
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
                if (currentShape != ShapeType.FREEHAND && startPoint != null) {
                    shapes.add(new ShapeInfo(currentShape, startPoint, endPoint));
                }
                startPoint = endPoint = null;
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
        g.setColor(Color.BLACK);
        
        // Draw current shape in progress
        if (startPoint != null && endPoint != null) {
            new ShapeInfo(currentShape, startPoint, endPoint).draw(g);
            
        }
        // shapes
        for (ShapeInfo shape : shapes) {
            shape.draw(g);
        }
        // free hand strokes
        for (java.util.List<Point> stroke : strokes) {
            for (int i = 1; i < stroke.size(); i++) {
                Point p1 = stroke.get(i - 1);
                Point p2 = stroke.get(i);
                g.drawLine(p1.x, p1.y, p2.x, p2.y);
            }
        }
    }
    
    public void setShapeSelection(ShapeType newShape) {
    	this.currentShape = newShape;
    }
}
