package dssb.util.process;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

/**
 * This output handler collect all output a lines.
 * 
 * @author dssb
 */
public class AllLineOutputHandler implements LineOutputHandler {

	private static final String NL = System.getProperty("line.separator");

	private final StringBuffer buffer = new StringBuffer();
	
	private final String newLine;
	
	/**
	 * Default constructor.
	 */
	public AllLineOutputHandler() {
		this(NL);
	}

	/**
	 * Constructor with new line delimiter.
	 */
	public AllLineOutputHandler(String newLine) {
		this.newLine = newLine;
	}

	@Override
	public void handlePart(String textOutput) {
		buffer.append(textOutput);
	}

	@Override
	public void handleLine(String lineOutput) {
		buffer.append(lineOutput).append(newLine);
	}

	/**
	 * Returns the list of lines using system new line.
	 **/
	public List<String> getList() {
		return getList(newLine);
	}

	/**
	 * Returns the list of lines using the given delimiter.
	 **/
	public List<String> getList(String delimiter) {
		return Arrays.asList(buffer.toString().split(Pattern.quote(delimiter)));
	}

	/**
	 * Returns the collected string.
	 */
	public String toString() {
		return buffer.toString();
	}

}
