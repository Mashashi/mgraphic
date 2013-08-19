package mgraphic;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.util.LinkedList;

import mgraphic.functions.ReservedNameException;

/**
 * A graphic element that plots mathematical functions.
 * 
 * @author Rafael Campos
 * @version 0.0.1
 */
@SuppressWarnings("serial")
public class MGraphicFunctionPlot extends MGraphic{
	
	
	/**
	 * Constructs a MGraphicFunctionPlot able of ploting mathematical expressions.
	 * 
	 * @param xMin Minimum abcises 10^xMax if bode xMax if linear
	 * @param yMin The Minimum ordinate
	 * @param xMax Maximum abcises 10^xMax if bode xMax if linear
	 * @param yMax The maximum ordinate
	 * @param xScale Abcises scale
	 * @param yScale Ordinate scale
	 * @param bode True if bode false if linear
	 * @param guideLines True ef the guidelines should be shown
	 * @param showMouseCoordinates True if the mouse coordinates should be shown
	 * @param functionName The function name
	 */
	public MGraphicFunctionPlot(double xMin, double yMin, double xMax,
			double yMax, double xScale, double yScale, boolean bode,
			boolean guideLines, boolean showMouseCoordinates,
			boolean functionName) {
		super(xMin, yMin, xMax, yMax, xScale, yScale, bode, guideLines, showMouseCoordinates, functionName);
	}
	
	/**
	 * Defines the precision used to plot a graphic of a mathematical expression.
	 * 
	 * @author Rafael Campos
	 * @since 0.0.1
	 */
	public static enum Precision{
		/**
		 * Defines a high precision plot. This means that will be taken samples of the function by an offset of 10^(-5).
		 */
		VERY_HIGH(100000),
		/**
		 * Defines a high precision plot. This means that will be taken samples of the function by an offset of 10^(-4).
		 */
		HIGH(10000),
		/**
		 * Defines a medium precision plot. This means that will be taken samples of the function by an offset of 10^(-3).
		 */
		MEDIUM(1000),
		/**
		 * Defines a low precision plot. This means that will be taken samples of the function by an offset of 10^(-2).
		 */
		LOW(100);
		
		private int precision;
		
		/**
		 * Constructs a precision element.
		 * @param precision The default samples taken by unit
		 * 
		 */
		private Precision(final int precision){
			this.precision = precision;
		}
		
		/**
		 * 
		 * @return The precision offset
		 */
		public int getPrecision() { return precision; }
		
	}
	
	/**
	 * Defines a mathematical function.
	 * 
	 * @author Rafael Campos
	 * @version 0.0.1
	 */
	public static abstract class MathFunction{
		
		private double offSet;
		private double xMin;
		private double xMax;
		private double yMax;
		private double yMin;
		private Color color;
		private boolean bode;
		/**
		 * 
		 * @param xMin Minimum abcises 10^xMax if bode xMax if linear
		 * @param xMax Maximum abcises 10^xMax if bode xMax if linear
		 * @param yMin Minimum ordinate
		 * @param yMax Maximum ordinate
		 * @param color Color which is going to be used to plot the function if null black
		 * @param bode True if the graphic is an bode false if linear
		 * @param precision The precision that will be used whene plotting the graphic
		 */
		public MathFunction(double xMin, double xMax, double yMin, double yMax,Color color, Precision precision,boolean bode){
			
			this.bode = bode;
			
			if(this.bode)
			{ this.xMin = Math.pow(10, xMin); this.xMax = Math.pow(10, xMax);}
			else
			{ this.xMin = xMin; this.xMax = xMax; }
			
			this.offSet = (xMax-xMin)/precision.getPrecision();
			this.yMax = yMax;
			this.yMin = yMin;
			this.color = color;
		}
		
		/**
		 * Calculate the function for the given parameters.
		 * 
		 * @param parameters The parameters used to preform the calculus
		 * @return The correspondent result of the calculus preformed with parameters
		 */
		public abstract double calculateFunction(double... parameters);
		
		@Override
		public abstract String toString();
	
		/**
		 * 
		 * @return Gets the offset that the object MathFunction uses to take the samples of the function
		 */
		public double getOffSet(){ return offSet; }
		
		/**
		 * 
		 * @return Gets the minimum abcises
		 */
		public double getxMin() { return xMin; }
		
		/**
		 * 
		 * @return Gets the maximum abcises
		 */
		public double getxMax() { return xMax; }
		
		/**
		 * 
		 * @return Gets the maximum ordinate
		 */
		public double getyMax() { return yMax; }
		
		/**
		 * 
		 * @return Gets the minimum ordinate
		 */
		public double getyMin() { return yMin; }
		
		/**
		 * 
		 * @return Color which is going to be used to plot the function if null black
		 */
		public Color getColor() { return color; }
	
		/**
		 * 
		 * @return True if the graphic is an bode false if linear
		 */
		public boolean getBode() { return bode; }
		
	}
	
	/**
	 * Adds a mathematical function to the graphic.
	 * 
	 * @param mathFunction 
	 * The mathematical function to be plot
	 * @param bode
	 * @throws ReservedNameException
	 * It will be throw if a name complaint with the er ^F//d+$ is added, these names are reserved so that the user can
	 * add function with out specifing a name
	 */
	public void addMathFunction(MathFunction mathFunction, boolean bode) throws ReservedNameException{
		
		LinkedList<Point2D.Double> points = new LinkedList<Point2D.Double>(); 
		System.out.println(mathFunction.getxMin()+" "+mathFunction.getxMax());
		for (double offSet = mathFunction.getxMin(); offSet < mathFunction.getxMax(); offSet+=mathFunction.getOffSet()) {
			
			double image = mathFunction.calculateFunction(offSet);
			
			if(image<=mathFunction.getyMax() && image>=mathFunction.getyMin()){
				points.add(new Point2D.Double(offSet, image));
			}
		}
		
		super.addFunction(points.toArray(new Point2D.Double[points.size()]), mathFunction.toString(), mathFunction!=null?mathFunction.getColor():Color.BLACK);
	
	}
	
}
