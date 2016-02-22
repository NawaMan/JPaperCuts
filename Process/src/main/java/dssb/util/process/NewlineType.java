package dssb.util.process;

public enum NewlineType {
	
	/** Linefeed ('\n') only. */
	LINE_FEED,
	
	/** CarriageReturn ('\r') followed by Linefeed ('\n'). */
	CARRIAGE_RETURN_LINE_FEED,
	
	/** CarriageReturn ('\r') only. */
	CARRIAGE_RETURN,
	
	/** To be determine */
	TO_BE_DETERMINED,
	
	/** Unknown */
	UNKNOWN;
	
	public static String getSystemNewline() {
		return System.getProperty("line.separator");
	}
	
	public static NewlineType getSystemNewlineType() {
		String systemNewline = System.getProperty("line.separator");
		if ("\n".equals(systemNewline)) {
			return NewlineType.LINE_FEED;
		}
		if ("\r".equals(systemNewline)) {
			return NewlineType.CARRIAGE_RETURN;
		}
		if ("\r\n".equals(systemNewline)) {
			return NewlineType.CARRIAGE_RETURN_LINE_FEED;
		}
		
		return NewlineType.UNKNOWN;
	}
	
}
