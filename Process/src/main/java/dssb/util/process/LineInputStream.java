package dssb.util.process;

import java.io.IOException;
import java.io.InputStream;

public class LineInputStream {
	
	public static final String NULL_SOURCE = "The source input stream cannot be null.";

	public static final String UNKNOWN_NOT_SUPPORT = "UNKNOWN newline type is not supported.";
	
	
	public static enum NewlineType {
		
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
		
	}
	
	private final InputStream source;
	
	private final CharStreamDecoder decoder;
	
	private volatile NewlineType nlType;
	
	private volatile String leftOver = "";
	
	public LineInputStream(InputStream source) {
		this(null, null, source);
	}
	
	public LineInputStream(NewlineType nlType, InputStream source) {
		this(null, nlType, source);
	}
	
	public LineInputStream(CharStreamDecoder decoder, NewlineType nlType,
	        InputStream source) {
		if (source == null) {
			throw new NullPointerException(NULL_SOURCE);
		}
		
		this.decoder = (decoder != null) ? decoder : new CharStreamDecoder();
		this.nlType = nlType;
		this.source = source;
		
		if (this.nlType == null) {
			this.nlType = NewlineType.TO_BE_DETERMINED;
		}
		
		if (this.nlType == NewlineType.UNKNOWN) {
			throw new IllegalArgumentException(UNKNOWN_NOT_SUPPORT);
		}
	}
	
	public NewlineType getNewlineType() {
		return this.nlType;
	}
	
	public String readLine() throws IOException {
		if (nlType == NewlineType.LINE_FEED) {
			return readLine('\n');
		}
		if (nlType == NewlineType.CARRIAGE_RETURN) {
			return readLine('\r');
		}
		if (nlType == NewlineType.CARRIAGE_RETURN_LINE_FEED) {
			return readLine_CRLF();
		}
		if (nlType == NewlineType.TO_BE_DETERMINED) {
			return readLine_TBD();
		}
		throw new UnsupportedOperationException();
	}
	
	private String readLine(char newLineChar) throws IOException {
		StringBuffer line = new StringBuffer();
		line.append(leftOver);
		leftOver = "";
		
		int read;
		while ((read = source.read()) != -1) {
			char[] chars = decoder.take((byte) read);
			for (int i = 0; i < chars.length; i++) {
				char ch = chars[i];
				if (ch == newLineChar) {
					return line.toString();
				} else {
					line.append(ch);
				}
			}
		}
		
		leftOver = line.toString();
		return null;
	}
	
	private String readLine_CRLF() throws IOException {
		StringBuffer line = new StringBuffer();
		line.append(leftOver);
		leftOver = "";
		
		int read;
		boolean wasCR = false;
		while ((read = source.read()) != -1) {
			char[] chars = decoder.take((byte) read);
			for (int i = 0; i < chars.length; i++) {
				char ch = chars[i];
				if (ch == '\r') {
					wasCR = true;
				} else {
					if (wasCR) {
						if (ch == '\n') {
							return line.toString();
						} else {
							line.append('\r').append(ch);
						}
					} else {
						line.append(ch);
					}
					wasCR = false;
				}
			}
		}
		
		leftOver = line.toString();
		return null;
	}
	
	private String readLine_TBD() throws IOException {
		StringBuffer line = new StringBuffer();
		line.append(leftOver);
		leftOver = "";
		
		int read;
		boolean wasCR = false;
		while ((read = source.read()) != -1) {
			char[] chars = decoder.take((byte) read);
			for (int i = 0; i < chars.length; i++) {
				char ch = chars[i];
				if (ch == '\r') {
					wasCR = true;
				} else {
					if (wasCR) {
						if (ch == '\n') {
							nlType = NewlineType.CARRIAGE_RETURN_LINE_FEED;
							return line.toString();
						} else {
							nlType = NewlineType.CARRIAGE_RETURN;
							leftOver = "" + ch;
							return line.toString();
						}
					} else {
						if (ch == '\n') {
							nlType = NewlineType.LINE_FEED;
							return line.toString();
						} else {
							line.append(ch);
						}
					}
					wasCR = false;
				}
			}
		}
		
		leftOver = line.toString();
		return null;
	}
	
	public String getLeftOver() {
		return leftOver;
	}
	
	//== Utilities =====================================================================================================
	
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
