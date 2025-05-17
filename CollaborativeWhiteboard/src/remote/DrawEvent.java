package remote;

import java.io.Serializable;
import java.util.List;
import java.awt.Color;
import java.awt.Point;

import constants.Constants.ShapeType;

public class DrawEvent implements Serializable {
    public ShapeType type;
    public List<Point> points;
    public String text;
    public Color colour;
    public int size;
    public int fontSize;
    
    public DrawEvent(ShapeType type, List<Point> points, String text, int size, Color color) {
        this.type = type;
        this.points = points;
        this.text = text;
        this.size = size;
        this.colour = color;
    }
}
