import java.awt.Color;

import javax.swing.JFrame;

import mgraphic.MGraphicFunctionPlot;
import mgraphic.MGraphicFunctionPlot.MathFunction;
import mgraphic.MGraphicFunctionPlot.Precision;
import mgraphic.functions.ReservedNameException;

/**
 * 
 */

/**
 * @author Rafael Campos
 *
 */
public class Tester {

	/**
	 * @param args
	 * @throws ReservedNameException 
	 */
	public static void main(String[] args) throws ReservedNameException {
		MGraphicFunctionPlot graphic = new MGraphicFunctionPlot(-0.1,-0.1,1,1,0.02,0.05,false,true,true,true);
		graphic.addMathFunction(new MathFunction(-1,1,-1,1, Color.green, Precision.MEDIUM,false){
			@Override
			public double calculateFunction(double... arg) {
				System.out.println("hello");
				if(arg.length!=1) throw new IllegalArgumentException();
				return arg[0]*Math.exp(-2*arg[0]);
			}
			@Override
			public String toString() {
				return "g*e^(-2*g)";
			}}, false);
		JFrame show = new JFrame();
		show.add(graphic);
		show.setSize(500, 500);
		show.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		show.setLocationRelativeTo(null);
		show.setVisible(true);

	}

}
