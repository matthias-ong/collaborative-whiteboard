/**
* Author: Matthias Si En Ong
* Student Id: 1590392
* Email: matthiaso@student.unimelb.edu.au
*/
package remote;

import java.io.Serializable;
import java.util.List;
import whiteboardapp.WhiteboardConstants.ShapeType;
import java.awt.Color;
import java.awt.Point;

/**
 * This class contains the serializable class for the Draw Event which will be sent across the network
 * using RMI.
 * 
 * @version 1.0
 * @author Matthias Si En Ong
 */
public class DrawEvent implements Serializable {
	
	/** The version identifier */
	private static final long serialVersionUID = 1L;

	/** type of shape of the draw event */
    public ShapeType type;
    
    /** list of points associated with the draw event */
    public List<Point> points;
    
    /** text if it is a text type draw event */
    public String text;
    
    /** colour of the draw event */
    public Color colour;
    
    /** size of the draw event */
    public int size;
    
    /** font size if it is a text type draw event */
    public int fontSize;
    
    /**
     * Constructor
     * 
     * @param type ShapeType sent
     * @param points
     * @param text
     * @param size
     * @param color
     */
    public DrawEvent(ShapeType type, List<Point> points, String text, int size, Color color) {
        this.type = type;
        this.points = points;
        this.text = text;
        this.size = size;
        this.colour = color;
    }
}
