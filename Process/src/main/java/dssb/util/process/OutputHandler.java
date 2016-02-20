package dssb.util.process;

/**
 * Classes implementing this interface can handle the output.
 * 
 * @author dssb
 */
public interface OutputHandler {

	/**
	 * Handle the output that is not a complete line. These output is given
	 * after the timeout is encountered.
	 * 
	 * @param partOutput
	 *            the part output.
	 */
	public void handlePart(String textOutput);

	/**
	 * Handle the line output.
	 * 
	 * @param lineOutput
	 *            the line output.
	 */
	public void handleLine(String lineOutput);

}
