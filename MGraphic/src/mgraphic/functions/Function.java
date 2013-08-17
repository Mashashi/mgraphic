package mgraphic.functions;
/**
 * 
 */


import java.awt.Color;
import java.awt.geom.Point2D;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
/**
 * 
 * @author Rafael Campos
 * @version 0.0.1
 */
public class Function{
	
	private Point2D[] points;
	private Color color;
	private String name;
	
	/**
	 * 
	 * @param points The points covered by the function
	 * @param name The function name
	 * @param color The function color null will generate a random color
	 */
	public Function(Point2D[] points, String name, Color color) {
		if(points == null || name == null) throw new IllegalArgumentException();
		this.points = points;
		this.name = name;
		this.color = color==null?generateColor():color;
	}
	
	/**
	 * 
	 * @return A random color
	 */
	private Color generateColor() {
		return new Color((int)(Math.random()*255), (int)(Math.random()*255), (int)(Math.random()*255));
	}

	/**
	 * 
	 * @return Gets the points covered by the function
	 */
	public Point2D[] getPoints() { return points; }
	
	/**
	 * 
	 * @return Gets the function name
	 */
	public String getName() { return name; }
	
	/**
	 * 
	 * @return Gets the color of the function
	 */
	public Color getColor() { return color; }
	
	@Override
	public String toString() {
		
		StringBuilder buffer = new StringBuilder();
		buffer.append("Name:\n"+name);
		buffer.append("\nColor:\nr"+color.getRed()+"g"+color.getGreen()+"b"+color.getBlue());
		buffer.append("\nPoints:");
		for(Point2D point : points)
			buffer.append("\n"+point.toString());
		
		return buffer.toString();
		
	}
	
	/**
	 * Save the function to the file fileName.sim.
	 * Will create a file if the file is not found.
	 * Will append the function to the file if it already exists.
	 * 
	 * @param fileName The fileName to which the function will be saved
	 * @throws IOException Covers file not found, file in use by another process...
	 */
	public void appendToFile(String fileName) throws IOException{
		
		if(fileName==null) throw new IllegalArgumentException();
		
		File file = new File(fileName+".sim");
		if(!file.exists())
			file.createNewFile();
		PrintWriter writer = new PrintWriter(file);
		writer.append(this.toString());
		writer.close();
		
	}
	
}