package WhiteboardApp;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;

public class Whiteboard extends JPanel {

    private java.util.List<Point> points = new ArrayList<>();

    public Whiteboard() {
        setBackground(Color.WHITE);

        // Mouse drag listener to add points
        addMouseMotionListener(new MouseMotionAdapter() {
            public void mouseDragged(MouseEvent e) {
                points.add(e.getPoint());
                repaint(); // Redraw with the new point
            }
        });

        // Optional: Clear on right-click
        addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    points.clear();
                    repaint();
                }
            }
        });
    }

    // Paint method to draw all points
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.setColor(Color.BLACK);

        for (int i = 1; i < points.size(); i++) {
            Point p1 = points.get(i - 1);
            Point p2 = points.get(i);
            if (p1 != null && p2 != null) {
                g.drawLine(p1.x, p1.y, p2.x, p2.y);
            }
        }
    }
}
