package whiteboardapp;

import java.awt.Graphics;
import java.awt.Point;

import constants.Constants.ShapeType;

public class ShapeInfo {
	ShapeType type;
    Point p1, p2;

    public ShapeInfo(ShapeType type, Point p1, Point p2) {
        this.type = type;
        this.p1 = p1;
        this.p2 = p2;
    }

    void draw(Graphics g) {
        int x = Math.min(p1.x, p2.x);
        int y = Math.min(p1.y, p2.y);
        int w = Math.abs(p1.x - p2.x);
        int h = Math.abs(p1.y - p2.y);

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
                int[] xPoints = {midX, p1.x, p2.x};
                int[] yPoints = {p1.y, p2.y, p2.y};
                g.drawPolygon(xPoints, yPoints, 3);
                break;
		default:
			break;
        }
    }
}
