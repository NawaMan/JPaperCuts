package dssb.util.process;

/**
 * Classes implements this interface can handle line output.
 * 
 * @author dssb
 */
public interface LineOutputHandler {

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
