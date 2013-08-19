package mgraphic.functions;

/**
 * The exception that specifies a function name reservation violation.
 * @author Rafael Campos
 * @version 0.0.1
 */
@SuppressWarnings("serial")
public class ReservedNameException extends Exception {
	
	/**
	 * 
	 * @param name 
	 * The violated name
	 */
	public ReservedNameException(String name){
		super("The function name "+name+" is reserved.");
	}
	
}
