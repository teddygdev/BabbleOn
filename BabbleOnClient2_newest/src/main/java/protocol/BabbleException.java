/**
 * the SpRTException class throws a particular type of exception for any
 * error requiring a thrown exception from a SpRT.protocol class
 * @author Brandy
 *  Assignment: Program 0
 */

package protocol;


public class BabbleException extends Exception {

	/**
	 * SpRTException constructor containing a message parameter
	 * @param string the message for the exception
	 */
	public BabbleException(String string) {
		super(string);
	}

	/**
	 * SpRTException constructor containing a message and cause parameters
	 * @param string the message for the exception
	 * @param cause throwable cause to create the exception cause 
	 */
	public BabbleException(String string, Throwable cause){
		super(string, cause);
	}
	
	/**
	 * Required for serialization implementation
	 */
	private static final long serialVersionUID = 1L;	

}
