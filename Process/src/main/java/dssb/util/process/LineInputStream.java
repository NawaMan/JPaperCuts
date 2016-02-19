package dssb.util.process;

import java.io.IOException;
import java.io.InputStream;

public class LineInputStream {
	
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
		this(null, null, source);
	}
	
	public LineInputStream(CharStreamDecoder decoder, NewlineType nlType, InputStream source) {
		if (source == null) {
			throw new NullPointerException();
		}
		
		this.decoder = (decoder != null) ? decoder : new CharStreamDecoder();
		this.nlType = nlType;
		this.source = source;
		
		if (this.nlType == null) {
			this.nlType = getSystemNewlineType();
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
			return readLine_LF();
		}
		throw new UnsupportedOperationException();
	}
	
	private String readLine_LF() throws IOException {
		StringBuffer line = new StringBuffer();
		int read;
		while ((read = source.read()) != -1) {
			char[] chars = decoder.take((byte)read);
			for (int i = 0; i < chars.length; i++) {
				char ch = chars[i];
				if (ch == '\n') {
					return line.toString();
				} else {
					line.append(ch);
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
