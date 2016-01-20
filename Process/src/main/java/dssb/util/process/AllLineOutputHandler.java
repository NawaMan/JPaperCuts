package dssb.util.process;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

public class AllLineOutputHandler implements LineOutputHandler {

	private static final String NL = System.getProperty("line.separator");

	private final StringBuffer buffer = new StringBuffer();
	
	private final String newLine;
	
	public AllLineOutputHandler() {
		this(NL);
	}
	
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

	public List<String> getList() {
		return getList(newLine);
	}

	public List<String> getList(String delimiter) {
		return Arrays.asList(buffer.toString().split(Pattern.quote(delimiter)));
	}

	public String toString() {
		return buffer.toString();
	}

}
