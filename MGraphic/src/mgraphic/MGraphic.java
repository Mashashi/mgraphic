package mgraphic;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Point2D;
import java.awt.geom.Point2D.Double;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;

import javax.swing.JComponent;

import mgraphic.functions.Function;
import mgraphic.functions.ReservedNameException;

import jxl.Workbook;
import jxl.read.biff.BiffException;
import jxl.write.Label;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import jxl.write.biff.RowsExceededException;

/**
 * 
 * A graphic object that can be use to plot a sequence of coordinates may work as a bode or linear graphic.
 * 
 * @author Rafael Campos
 * @version 0.0.1
 * 
 */
@SuppressWarnings("serial")
public class MGraphic extends JComponent implements MouseMotionListener{
	
	/**
	 * The hash table where the functions will be recorded.
	 */
	private Hashtable<String, Function> functions; 
	
	/**
	 * The minimum abcises.
	 */
	private double xMin;
	/**
	 * The minimum ordinate.
	 */
	private double yMin;
	/**
	 * The maximum abcises.
	 */
	private double xMax;
	/**
	 * The maximum ordinate.
	 */
	private double yMax; 
	/**
	 * The abcises axis scale.
	 * E.g: If bode = true the abcises will be written at 10^(0*xScale), 10^(1*xScale), 10^(2*xScale)...
	 * E.g: If bode = false the abcises will be written at (0*xScale), (1*xScale), (2*xScale)...
	 */
	private double xScale;
	/**
	 * The ordinate scale.
	 */
	private double yScale;
	
	/**
	 * The format number used to mark the scale on a linear graphic
	 */
	private DecimalFormat numberFormatLinearScale;
	/**
	 * The format number used to mark the coordinate whene the mouse is houver the MGraphic element, if this option is enabled. 
	 */
	private DecimalFormat numberFormatMouseOver;
	/**
	 * The format number used to mark the scale on a bode graphic 
	 */
	private DecimalFormat numberFormatBodeScale;
	
	/**
	 * True if guidelines are visible false otherwise.
	 */
	private boolean guideLines; 
	/**
	 * True if the graphic should be ploted as a bode false if the graphic should be ploted as a linear
	 */
	private boolean bode; 
	/**
	 * If the function's names should be written at their end.
	 */
	private boolean functionName;
	/**
	 * The default guideLinesColor.
	 */
	private Color guideLinesColor;
	/**
	 * If the coordinates should be shown as the user passes the mouse over the MGraphic element. 
	 */
	private Point mousePosition;
	
	/**
	 * Constructs a graphic element.
	 * 
	 * @param xMin 
	 * If is a bode graphic this parameter corresponds to 10^xMin if not corresponds to xMin, this means the minimum abcises
	 * @param yMin 
	 * The minimum ordinate to show
	 * @param xMax 
	 * If is a bode graphic this parameter corresponds to 10^xMax if not corresponds to xMax, this means the maximum abcises
	 * @param yMax 
	 * The maximum ordinate to show
	 * @param xScale
	 * The abcises axis scale
	 * E.g: If bode = true the abcises will be written at 10^(0*xScale), 10^(1*xScale), 10^(2*xScale)...
	 * E.g: If bode = false the abcises will be written at (0*xScale), (1*xScale), (2*xScale)...
	 * @param yScale
	 * The ordinate axis scale
	 * @param bode
	 * True if the graphic should be ploted as a bode false if the graphic should be ploted as a linear 
	 * @param guideLines
	 * True if guidelines are visible false otherwise
	 * @param showMouseCoordinates 
	 * If the coordinates should be shown as the user passes the mouse over the MGraphic element
	 * @param functionName
	 * If the function's names should be written at their end
	 */
	public MGraphic(double xMin, double yMin, double xMax, double yMax, 
			double xScale, double yScale, 
			boolean bode, boolean guideLines, boolean showMouseCoordinates, boolean functionName) {
		
		this.functions = new Hashtable<String, Function>();
		
		setBounds(xMin, xMax, yMin, yMax);
		
		setScale(xScale, yScale);
		
		this.numberFormatLinearScale = new DecimalFormat("#.##");
		this.numberFormatMouseOver = new DecimalFormat("0.#######E0");
		this.numberFormatBodeScale = new DecimalFormat("0.#E0");
		
		this.guideLines = guideLines;
		this.guideLinesColor = Color.GRAY;
		this.guideLinesColor = this.guideLinesColor.brighter();
	
		this.bode = bode;
		
		if(showMouseCoordinates)
			this.addMouseMotionListener(this);
		
		this.functionName = functionName;
		
	}
	
	/**
	 * Adds a function to the graphic.
	 * If the points are not sorted these function will sort them.
	 * 
	 * @param name
	 * The function name
	 * @return
	 * The function object
	 */
	public Function getFunction(String name){ return functions.get(name); }
	
	/**
	 * Adds a function to the graphic.
	 * If the points are not sorted these function will sort them.
	 * 
	 * @param points
	 * Points covered by the function
	 * @param name
	 * It can not be complaint with the er ^F//d+$ or else ReservedNameException will be thrown
	 * @param color
	 * The function line color
	 * @return
	 * The function name
	 * @throws ReservedNameException
	 * It will be throw if a name complaint with the er ^F//d+$ is added, these names are reserved so that the user can
	 * add function with out specifing a name
	 */
	public String addFunction(Point2D.Double[] points, String name, Color color) throws ReservedNameException{
		
		if(name!=null && name.matches("F\\d+")) throw new ReservedNameException(name);
		
		class Sorter implements Comparator<Point2D.Double>{

			@Override
			public int compare(Double o1, Double o2) {
				double compare = o1.x-o2.x;
				return compare>0?1:compare<0?-1:0;
			}
			
		}
		
		Arrays.sort(points, new Sorter());

		synchronized(functions){
			String nameBuf = name==null?generateString():name;
			functions.put(nameBuf, new Function(points, nameBuf, color));
			return nameBuf;
		}
		
	}
	
	/**
	 * Adds a function to the graphic.
	 * If the points are not sorted these function will sort them.
	 * 
	 * @param points
	 * Points covered by the function
	 * @param name
	 * It can not be complaint with the er ^F//d+$ or else ReservedNameException will be thrown
	 * @return
	 * The function name
	 * @throws ReservedNameException
	 * It will be throw if a name complaint with the er ^F//d+$ is added, these names are reserved so that the user can
	 * add function with out specifing a name
	 */
	public String addFunction(Point2D.Double[] points, String name) throws ReservedNameException{ return addFunction(points, name, null); }
	
	/**
	 * Adds a function to the graphic.
	 * If the points are not sorted these function will sort them.
	 * 
	 * @param points
	 * Points covered by the function 
	 * @return 
	 * The function name
	 */
	public String addFunction(Point2D.Double[] points){ 
		try { return addFunction(points, null, null); } 
		catch (ReservedNameException e) { e.printStackTrace(); }
		
		return null;
		
	}
	
	/**
	 * 
	 * @return
	 */
	public boolean getBode(){
		return bode;
	}
	
	/**
	 * Generates a function based on the number of functions in the graphic.
	 * 
	 * @return
	 * The name of the function
	 */
	private String generateString(){return "F"+functions.size();}
	
	/**
	 * Remove a function.
	 * 
	 * @param name 
	 * Removes the specified name function
	 * @return
	 * The function removed
	 */
	public Function removeFunction(String name){ return functions.remove(name); }
	
	/**
	 * Set the scale.
	 * 
	 * @param xScale 
	 * Set the abcises write offset
	 * @param yScale 
	 * Set the ordinate write offset
	 */
	public void setScale(double xScale, double yScale){
		if(xScale<=0 || yScale<=0) throw new IllegalArgumentException();
		this.xScale = xScale;
		this.yScale = yScale;
	}
	
	/**
	 * 
	 * @return 
	 * The abcises scale
	 */
	public double getxScale() { return xScale; }
	
	/**
	 * 
	 * @return 
	 * The ordinate scale
	 */
	public double getyScale() { return yScale; }
	
	/**
	 * Set bounds.
	 * 
	 * @param xMin 
	 * The minimum abcises that will be shown
	 * @param xMax
	 * The maximum abcises that will be shown
	 * @param yMin
	 * The minimum ordinate that will be shown
	 * @param yMax
	 * The maximum ordinate that will be shown
	 */
	public void setBounds(double xMin, double xMax , double yMin, double yMax){
		if(xMin>xMax || yMin>yMax) throw new IllegalArgumentException();
		this.xMin = xMin;
		this.yMin = yMin;
		this.xMax = xMax;
		this.yMax = yMax;
	}
	
	/**
	 * 
	 * @return 
	 * The minimum abcises that will be shown
	 */
	public double getxMin() { return xMin; }
	
	/**
	 * 
	 * @return 
	 * The maximum abcises that will be shown
	 */
	public double getxMax() { return xMax; }
	
	/**
	 * 
	 * @return 
	 * The maximum ordinate that will be shown
	 */
	public double getyMax() { return yMax; }
	
	/**
	 * 
	 * @return 
	 * The minimum ordinate that will be shown
	 */
	public double getyMin() { return yMin; }
	
	/**
	 * 
	 * @param unitPixiesX
	 * @return
	 */
	public int getAxisWidth(int unitPixiesX){
		return (int) Math.round(getWidth()*(Math.abs(xMin)*unitPixiesX/getWidth()));
	}
	
	/**
	 * 
	 * @param show 
	 * True to show the guide lines of the both axis false hides it
	 */
	public void showGrid(boolean show){this.guideLines = show;}
	
	/**
	 * 
	 * @param bode 
	 * True and this graphic will be shown as a bode graphic false 
	 * and this grahic will be presented as a linear graphic
	 */
	public void setBode(boolean bode){ this.bode=bode; }
	
	/**
	 * 
	 * @return
	 */
	protected int getUnitPixeisX(){
		return (int) Math.round(getWidth()/(xMax-xMin));
	}
	
	/**
	 * 
	 * @return
	 */
	protected int getUnitPixeisY(){
		return (int) Math.round(getHeight()/(yMax-yMin));
	}
	
	/**
	 * 
	 */
	@Override
	public void paint(Graphics g) {
		
		super.paint(g);
		
		//Passa a ser unidades 10^unitPixiesX quando no gr�fico de bode
		int unitPixiesX = getUnitPixeisX(), unitPixiesY = getUnitPixeisY();
		int axisHeight = -1, axisWidth = -1;
		
		//Setting elements
		Font generalFont = new Font( "Arial", Font.PLAIN, 12 );
		g.setFont(generalFont);
		g.setColor(Color.BLACK);
		
		//Eixos
		if( isXAxisVisible() ){
			axisHeight = (int) Math.round(getHeight()*(yMax*unitPixiesY/getHeight()));
			g.drawLine(0, axisHeight, getWidth(), axisHeight );
			g.drawString("x", getWidth()-generalFont.getSize(), axisHeight+generalFont.getSize());
		}
		
		if( isYAxisVisible() ){
			axisWidth = (int) Math.round(getWidth()*(Math.abs(xMin)*unitPixiesX/getWidth()));
			g.drawLine(axisWidth, 0, axisWidth, getHeight() );
			g.drawString("y", axisWidth+generalFont.getSize(), generalFont.getSize());
		}
		
		writeXAxis(g, generalFont, unitPixiesX, axisHeight, axisWidth);
		writeYAxis(g, generalFont, unitPixiesY, axisHeight, axisWidth);
		
		drawFunctions( unitPixiesX, unitPixiesY, axisWidth, axisHeight, g);
		
		//Desenhar a legenda
		g.setColor(Color.BLACK);
		g.setFont(new Font("Arial",Font.BOLD,16));
		
		//Desenhar coordenadas no ponto em que o rato est�
		if(mousePosition != null){
			g.setFont(generalFont);
			
			g.drawString("("+
					(numberFormatMouseOver.format(insverseTransformX(axisWidth,unitPixiesX)).toString()+", "+
							numberFormatMouseOver.format(insverseTransformY(axisHeight,unitPixiesY)))+")", 
							mousePosition.x, 
							mousePosition.y);
		}
		
	}
	
	/**
	 * Function to draw.
	 * 
	 * @param unitPixiesX 
	 * The number of pixels that correspond to 1 unit or in the bode case the number of pixeis that correspond to 10^1
	 * @param unitPixiesY
	 * The number of pixels that correspond to 1 unit
	 * @param axisWidth
	 * Axis width in pixels
	 * @param axisHeight
	 * Axis height in pixels
	 * @param g
	 * The graphic variable to where the function will be written
	 */
	protected void drawFunctions(int unitPixiesX,int unitPixiesY,int axisWidth, int axisHeight, Graphics g){
		
		Iterator<Function> iteratorFunction = functions.values().iterator();
		
		while(iteratorFunction.hasNext()){
			
			Function function = iteratorFunction.next();
			Point2D[] points = function.getPoints();
			g.setColor(function.getColor());
			
			if(points.length==1){
				drawLine(g,points[0],points[0],unitPixiesX,unitPixiesY,axisWidth,axisHeight);
				g.drawString(function.getName(),transformX(points[0].getX(),unitPixiesX, axisWidth, bode), transformY(points[0].getY(), unitPixiesY, axisHeight));
			} else {
				for (int i = 0; i < points.length-1; i++) 
					drawLine(g,points[i],points[i+1],unitPixiesX,unitPixiesY,axisWidth,axisHeight);
				if(this.functionName && points.length!=0){
					g.drawString(function.getName(),transformX(points[points.length-1].getX(),unitPixiesX, axisWidth, bode), transformY(points[points.length-1].getY(), unitPixiesY, axisHeight));
				}
			}	
		
		}
		
	}
	
	/**
	 * 
	 * @param g
	 * The graphic variable to where the function will be written
	 * @param point1 
	 * First point
	 * @param point2 
	 * Second point
	 * @param unitPixiesX 
	 * The number of pixels that correspond to 1 unit or in the bode case the number of pixeis that correspond to 10^1
	 * @param unitPixiesY
	 * The number of pixels that correspond to 1 unit
	 * @param axisWidth
	 * Axis width in pixels
	 * @param axisHeight
	 * Axis height in pixels
	 */
	protected void drawLine(Graphics g,Point2D point1, Point2D point2,int unitPixiesX,int unitPixiesY,int axisWidth,int axisHeight){
		Point coord = transformCoordinate(point1, unitPixiesX, unitPixiesY, axisWidth, axisHeight);
		Point coordNext = transformCoordinate(point2, unitPixiesX, unitPixiesY, axisWidth, axisHeight);
		g.drawLine(coord.x, coord.y, coordNext.x, coordNext.y);
	}
	
	/**
	 * Writes the y axis. 
	 * 
	 * @param g
	 * The graphic variable to where the function will be written
	 * @param generalFont
	 * The font type that will be used to write the offset specified by the user
	 * @param unitPixiesY
	 * The number of pixels that correspond to 1 unit
	 * @param axisWidth
	 * Axis width in pixels
	 * @param axisHeight
	 * Axis height in pixels
	 */
	protected void writeYAxis(Graphics g, Font generalFont, double unitPixiesY, int axisHeight, int axisWidth){
		
		if(isYAxisVisible()){
			
			double yInc = isXAxisVisible()?axisHeight:0;
			double ordenada = isXAxisVisible()?0:yMax;
			
			if(isXAxisVisible()){
				
				while(yInc>0){
					g.drawString(""+numberFormatLinearScale.format(ordenada), axisWidth-3*generalFont.getSize(), (int)Math.round(yInc) );
					g.drawLine(axisWidth, (int)Math.round(yInc), axisWidth-2,  (int)Math.round(yInc) );
					yInc -= unitPixiesY*yScale;
					ordenada += yScale;
					if(this.guideLines){
						g.setColor(guideLinesColor);
						g.drawLine(0, (int)Math.round(yInc), getWidth(), (int)Math.round(yInc));
						g.setColor(Color.BLACK);
					}
				}
				
				yInc = axisHeight;
				ordenada = 0;	
			}
			
			while(yInc<getHeight()){
				g.drawString(""+numberFormatLinearScale.format(ordenada), axisWidth-3*generalFont.getSize(), (int)Math.round(yInc) );
				g.drawLine(axisWidth, (int)Math.round(yInc), axisWidth-2,  (int)Math.round(yInc) );
				yInc += unitPixiesY*yScale;
				ordenada -= yScale;
				if(this.guideLines){
					g.setColor(guideLinesColor);
					g.drawLine(0, (int)Math.round(yInc), getWidth(), (int)Math.round(yInc));
					g.setColor(Color.BLACK);
				}
			}	
		}
	}
	
	/**
	 * Write the x axis.
	 * 
	 * @param g
	 * The graphic variable to where the function will be written
	 * @param generalFont
	 * The font type that will be used to write the offset specified by the user
	 * @param unitPixiesX
	 * The number of pixels that correspond to 1 unit or in the bode case the number of pixeis that correspond to 10^1
	 * @param axisWidth
	 * Axis width in pixels
	 * @param axisHeight
	 * Axis height in pixels
	 */
	protected void writeXAxis(Graphics g, Font generalFont, double unitPixiesX, int axisHeight, int axisWidth){
		
		if(isXAxisVisible()){
			
			double xInc = isYAxisVisible()?axisWidth:0;
			double abcissa = isYAxisVisible()?0:xMin;
			
			double valueBode = 1;
			double counterBode = 0;
			
			if(isYAxisVisible()){
				
				while(xInc>0){
					g.drawString(""+(bode?numberFormatBodeScale.format(valueBode).toString():numberFormatLinearScale.format(abcissa)), (int)Math.round(xInc), axisHeight+generalFont.getSize());
					g.drawLine((int)Math.round(xInc), axisHeight, (int)Math.round(xInc),  axisHeight+2);
					xInc -= unitPixiesX*xScale;
					
					if(bode){
						counterBode -= xScale;
						valueBode = Math.pow(10, counterBode);
					}else 
						abcissa -= xScale;
					
					if(this.guideLines){
						g.setColor(guideLinesColor);
						g.drawLine((int)Math.round(xInc), 0, (int)Math.round(xInc), getHeight());
						g.setColor(Color.BLACK);
					}
					
				}
				
				xInc = axisWidth;
				abcissa = 0;	
				if(bode){ valueBode = 1; counterBode = 0;}
				
			}
			
			while(xInc<getWidth()){
				g.drawString(""+(bode?numberFormatMouseOver.format(valueBode).toString():numberFormatLinearScale.format(abcissa)), (int)Math.round(xInc), axisHeight+generalFont.getSize());
				g.drawLine((int)Math.round(xInc), axisHeight, (int)Math.round(xInc),  axisHeight+2);
				xInc += unitPixiesX*xScale;
				
				if(bode){
					counterBode += xScale;
					valueBode = Math.pow(10, counterBode);		
				}else{ abcissa += xScale; }
				
				if(this.guideLines){
					g.setColor(guideLinesColor);
					g.drawLine((int)Math.round(xInc), 0, (int)Math.round(xInc), getHeight());
					g.setColor(Color.BLACK);
				}
				
			}
		}
		
	}
	
	/**
	 * Transforms a certain coordinate into a point on the graphic.
	 * 
	 * @param coordinate 
	 * The coordinate to transform
	 * @param unitPixiesX 
	 * The number of pixels that correspond to 1 unit or in the bode case the number of pixeis that correspond to 10^1
	 * @param unitPixiesY
	 * The number of pixels that correspond to 1 unit
	 * @param axisWidth
	 * Axis width in pixels
	 * @param axisHeight
	 * Axis height in pixels
	 * @return The coordinate transformed into a point on the graphic
	 */
	protected Point transformCoordinate(Point2D coordinate, int unitPixiesX, int unitPixiesY, int axisWidth, int axisHeight){
		
		int x = transformX(coordinate.getX(), unitPixiesX, axisWidth,bode);
		int y = transformY(coordinate.getY(), unitPixiesY, axisHeight);
		
		return new Point(x,y);
		
	}
	
	/**
	 * Transfom an abcises into the correspondent pixels on the graphic.
	 * 
	 * @param x The abscises 
	 * @param unitPixiesX 
	 * The number of pixels that correspond to 1 unit or in the bode case the number of pixeis that correspond to 10^1
	 * @param axisWidth
	 * Axis width in pixels
	 * @param bode
	 * True if the abscises concerns a bode graphic false if it concernais a linear graphic
	 * @return
	 * The abscises transformed into the graphic position
	 */
	protected int transformX(double x, int unitPixiesX, int axisWidth, boolean bode){ 
		return  bode? transformX(Math.log10(x), unitPixiesX, axisWidth, false):(int)Math.round( axisWidth + x * unitPixiesX );
	}
	
	/**
	 * Inverse transform a y position into a ordiante.
	 * 
	 * @param y 
	 * The ordinate 
	 * @param unitPixiesY 
	 * The number of pixels that correspond to 1 unit
	 * @param axisHeight
	 * Axis height in pixels
	 * @return
	 * The ordinate transformed into the graphic position
	 */
	protected int transformY(double y, int unitPixiesY, int axisHeight) { 
		return (int) Math.round(axisHeight -y * unitPixiesY); 
	}
	
	/**
	 * Inverse transform the x position into an abcises given by the internal monitor variable of the mouse position into
	 * a valid abcises.
	 * 
	 * @param axisWidth
	 * The abcises axis width
	 * @param unitPixiesX
	 * The number of pixels that correspond to 1 unit or in the bode case the number of pixeis that correspond to 10^1
	 * @return
	 * The x position transformed into abcises
	 */
	protected float insverseTransformX(int axisWidth,int unitPixiesX){
		float linear =  (((float)(-axisWidth+mousePosition.x))/((float)unitPixiesX));	
		return (float) (bode?Math.pow(10, linear):linear);
	}
	
	/**
	 * Inverse transform the y position into an ordinate given by the internal monitor variable of the mouse position into
	 * a valid ordinate.
	 * 
	 * @param axisHeight
	 * The ordinate axis height
	 * @param unitPixiesY
	 * 
	 * @return
	 * The y position transformed into ordinate
	 */
	protected float insverseTransformY(int axisHeight, int unitPixiesY){
		return  (((float)(axisHeight-mousePosition.y))/((float)unitPixiesY));
	}
	
	/**
	 * 
	 * @return Se o eixo das abcissas est� vis�vel
	 */
	protected boolean isXAxisVisible(){ return yMin<=0 && yMax>=0; }
	
	/**
	 * 
	 * @return Se o eixo das ordenadas est� vis�vel
	 */
	protected boolean isYAxisVisible(){ return xMin<=0 && xMax>=0; }
	
	/**
	 * Loads the specified sheets that contains functions from a file into the graphic.
	 * Uncompliant format of functions in the xls may cause unpredictable results.
	 * 
	 * @param fileName
	 * File to load
	 * @param sheets
	 * Sheets to operate
	 * @param loadSelected
	 * If true loads the sheets expressed by sheets if false loads all other that not  expressed by sheets
	 * @throws IOException 
	 * Covers file not found exception, file in use by another process...
	 * @throws BiffException 
	 * Covers file format not recognizable
	 * @throws ReservedNameException
	 * It will be throw if a name complaint with the er ^F//d+$ is added, these names are reserved so that the user can
	 * add function with out specifing a name
	 */
	public void loadFromWorkBook(String fileName,Integer[] sheets, boolean loadSelected) throws IOException, BiffException, ReservedNameException {

		File file = new File(fileName);
		
		Workbook workbook = Workbook.getWorkbook(file);
		
		if(!loadSelected){
			
			ArrayList<Integer> loadSheets = new ArrayList<Integer>();
			
			for(int i=0;i<workbook.getNumberOfSheets();i++){
				boolean loadIt = true;
				for(int j=0; j<sheets.length;j++)
					if(i==sheets[j])
						loadIt = false;
				if(loadIt) loadSheets.add(i);
			}
			
			sheets = loadSheets.toArray(new Integer[loadSheets.size()]);
			
		}
		
		for(Integer sheet: sheets){
			
			LinkedList<Point2D.Double> points = new LinkedList<Point2D.Double>(); 
			
			try{
				for(int i=2;i<workbook.getSheet(sheet).getColumn(0).length && i<workbook.getSheet(sheet).getColumn(1).length;i++){
					points.add( new Point2D.Double( java.lang.Double.parseDouble( workbook.getSheet(sheet).getCell(0, i).getContents() ), java.lang.Double.parseDouble( workbook.getSheet(sheet).getCell(1, i).getContents() ) ) );
				}
			}catch(NumberFormatException e){}
			
			this.addFunction(points.toArray(new Point2D.Double[points.size()]), workbook.getSheet(sheet).getCell(1, 0).getContents(), new Color(Integer.parseInt(workbook.getSheet(sheet).getCell(3, 0).getContents())));
			
		}
		workbook.close();
		
	}
	
	/**
	 * 
	 * @param fileName
	 * @return
	 * @throws IOException 
	 * @throws BiffException 
	 */
	public int howManySheetsInFile(String fileName) throws BiffException, IOException{
		
		Workbook workbook = Workbook.getWorkbook(new File(fileName));
		int sheetsNumber = workbook.getNumberOfSheets();
		workbook.close();
		return sheetsNumber;
		
	}
	
	/**
	 * Saves the functions to a excel file 98-2003 compatible.
	 * 
	 * @param fileName 
	 * The file name that will be assigned to the file
	 * @throws RowsExceededException
	 * @throws WriteException
	 * @throws BiffException
	 * Covers file format not recognizable
	 * @throws IOException 
	 * Covers file not found, file in use by other process...
	 */
	public WritableWorkbook saveToWorkBook(String fileName) throws RowsExceededException, WriteException, IOException {

		File file = new File(fileName);
		
		WritableWorkbook workbook = Workbook.createWorkbook(file);
		
		Enumeration<Function> functionsIterator = functions.elements();
		
		while(functionsIterator.hasMoreElements()){
			
			Function function = functionsIterator.nextElement();
			
			WritableSheet sheet = workbook.createSheet(function.getName().length()<20?function.getName():function.getName().substring(0, 20).concat("..."), workbook.getNumberOfSheets()); 
			
			sheet.addCell(new Label(0, 0, "Nome da fun��o"));
			sheet.addCell(new Label(1, 0, function.getName()));
			sheet.addCell(new Label(2, 0, "Cor (RGB)"));
			sheet.addCell(new Label(3, 0, function.getColor().getRGB()+""));
			
			sheet.addCell(new Label(0, 1, "Abcissa"));
			sheet.addCell(new Label(1, 1, "Ordenada"));
			
			for(int i=0; i < function.getPoints().length; i++){
				sheet.addCell(new Label(0, i+2, function.getPoints()[i].getX()+""));
				sheet.addCell(new Label(1, i+2, function.getPoints()[i].getY()+""));
			}
			
		}
		
		return workbook;
		
	}
	
	/* **********************************************************
	 * **********************************************************
	 * MouseMotionListener **************************************
	 * **********************************************************
	 * **********************************************************
	 */
	
	@Override
	public void mouseDragged(MouseEvent arg) {}

	@Override
	public void mouseMoved(MouseEvent arg) { mousePosition = arg.getPoint();repaint(); }
	
}
