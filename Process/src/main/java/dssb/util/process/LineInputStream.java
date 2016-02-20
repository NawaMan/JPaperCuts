package dssb.util.process;

import java.io.IOException;
import java.io.InputStream;

public class LineInputStream {
	
	public static final String NULL_SOURCE = "The source input stream cannot be null.";

	public static final String UNKNOWN_NOT_SUPPORT = "UNKNOWN newline type is not supported.";
	
	
	public static enum NewlineType {
		
		/** Linefeed ('\n') only. */
		LF,
		
		/** CarriageReturn ('\r') followed by Linefeed ('\n'). */
		CRLF,
		
		/** CarriageReturn ('\r') only. */
		CR,
		
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
			this.nlType = getSystemNewlineType();
		}
		
		if (this.nlType == NewlineType.UNKNOWN) {
			throw new IllegalArgumentException(UNKNOWN_NOT_SUPPORT);
		}
	}
	
	public static String getSystemNewline() {
		return System.getProperty("line.separator");
	}
	
	public static NewlineType getSystemNewlineType() {
		String systemNewline = System.getProperty("line.separator");
		if ("\n".equals(systemNewline)) {
			return NewlineType.LF;
		}
		if ("\r".equals(systemNewline)) {
			return NewlineType.CR;
		}
		if ("\r\n".equals(systemNewline)) {
			return NewlineType.CRLF;
		}
		
		return NewlineType.UNKNOWN;
	}
	
	public String readLine() throws IOException {
		if (nlType == NewlineType.LF) {
			return readLine('\n');
		}
		if (nlType == NewlineType.CR) {
			return readLine('\r');
		}
		if (nlType == NewlineType.CRLF) {
			return readLine_CRLF();
		}
		throw new UnsupportedOperationException();
	}
	
	private String readLine(char newLineChar) throws IOException {
		StringBuffer line = new StringBuffer();
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
	
	public String getLeftOver() {
		return leftOver;
	}
	
}
