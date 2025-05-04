package WhiteboardApp;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;

public class Whiteboard extends JPanel {

    private java.util.List<java.util.List<Point>> strokes = new ArrayList<>();
    private java.util.List<Point> currentStroke = null;

    public Whiteboard() {
        setBackground(Color.WHITE);

        addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e)) {
                    currentStroke = new ArrayList<>();
                    currentStroke.add(e.getPoint());
                    strokes.add(currentStroke);
                    repaint();
                } else if (SwingUtilities.isRightMouseButton(e)) {
                    strokes.clear();
                    repaint();
                }
            }
        });

        addMouseMotionListener(new MouseMotionAdapter() {
            public void mouseDragged(MouseEvent e) {
                if (currentStroke != null) {
                    currentStroke.add(e.getPoint());
                    repaint();
                }
            }
        });
        
    }

    // Paint method to draw all strokes
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.setColor(Color.BLACK);

        for (java.util.List<Point> stroke : strokes) {
            for (int i = 1; i < stroke.size(); i++) {
                Point p1 = stroke.get(i - 1);
                Point p2 = stroke.get(i);
                g.drawLine(p1.x, p1.y, p2.x, p2.y);
            }
        }
    }
}
