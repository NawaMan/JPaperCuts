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
	
	private final CharIterator charIterator = new CharIterator();
	
	private volatile NewLineStrategy nlStrategy;
	
	public LineInputStream(InputStream source) {
		this(null, null, source);
	}
	
	public LineInputStream(NewlineType nlType, InputStream source) {
		this(null, nlType, source);
	}
	
	public LineInputStream(CharStreamDecoder decoder, NewlineType nlType, InputStream source) {
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
		
		updateNewlineStrategy();
	}
	
	private void updateNewlineStrategy() {
		if (nlType == NewlineType.LINE_FEED) {
			nlStrategy = new LineFeed();
		} else if (nlType == NewlineType.CARRIAGE_RETURN) {
			nlStrategy = new CarriageReturn();
		} else if (nlType == NewlineType.CARRIAGE_RETURN_LINE_FEED) {
			nlStrategy = new CarriageReturnThenLineFeed();
		} else if (nlType == NewlineType.TO_BE_DETERMINED) {
			nlStrategy = new ToBeDetermined();
		} else {
			throw new IllegalStateException(UNKNOWN_NOT_SUPPORT);
		}
	}
	
	public synchronized NewlineType getNewlineType() {
		return this.nlType;
	}
	
	private StringBuffer absorbLeftOver() {
		StringBuffer line = new StringBuffer();
		line.append(leftOver);
		leftOver = "";
		return line;
	}
	
	public synchronized String readLine() throws IOException {
		StringBuffer line = absorbLeftOver();
		
		nlStrategy.reset();
		
		try {
			while (true) {
				char ch = charIterator.next();
				String readLine = nlStrategy.processChar(ch, line);
				if (readLine != null) {
					return readLine;
				}
			}
		} catch (EndOfStreamException exception) {
			// This block is intentionally left blank.
		}
		
		leftOver = line.toString();
		return null;
	}
	
	public synchronized String peekLeftOver() {
		return leftOver;
	}
	
	public synchronized String takeLeftOver() {
		String theLeftOver = leftOver;
		leftOver = "";
		return theLeftOver;
	}
	
	// == Utilities ====================================================================================================
	
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
	
	// == Helper classes ===============================================================================================
	
	private class CharIterator {
		char[] chars = new char[0];
		int index = -1;
		boolean isDone = false;
		
		public synchronized char next() throws EndOfStreamException, IOException {
			if (isDone) {
				throw new EndOfStreamException();
			}
			
			index++;
			while (index >= chars.length) {
				int read = source.read();
				if (read == -1) {
					isDone = true;
					throw new EndOfStreamException();
				}
				chars = decoder.take((byte) read);
				index = 0;
			}
			return chars[index];
		}
	}
	
	private interface NewLineStrategy {
		
		void reset();
		
		String processChar(char ch, StringBuffer line);
		
	}
	
	private class SingleCharNewLine implements NewLineStrategy {
		
		private final char newLineChar;
		
		SingleCharNewLine(char newLineChar) {
			this.newLineChar = newLineChar;
		}
		
		@Override
		public void reset() {
		}
		
		public String processChar(char ch, StringBuffer line) {
			if (ch == newLineChar) {
				return line.toString();
			} else {
				line.append(ch);
				return null;
			}
		}
		
	}
	
	private class LineFeed extends SingleCharNewLine {
		LineFeed() {
			super('\n');
		}
	}
	
	private class CarriageReturn extends SingleCharNewLine {
		CarriageReturn() {
			super('\r');
		}
	}
	
	private class CarriageReturnThenLineFeed implements NewLineStrategy {
		
		private volatile boolean wasCR = false;
		
		@Override
		public void reset() {
			wasCR = false;
		}
		
		public String processChar(char ch, StringBuffer line) {
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
			return null;
		}
		
	}
	
	private class ToBeDetermined implements NewLineStrategy {
		
		private volatile boolean wasCR = false;
		
		@Override
		public void reset() {
			wasCR = false;
		}
		
		public String processChar(char ch, StringBuffer line) {
			if (ch == '\r') {
				wasCR = true;
			} else {
				if (wasCR) {
					if (ch == '\n') {
						nlType = NewlineType.CARRIAGE_RETURN_LINE_FEED;
						updateNewlineStrategy();
						return line.toString();
					} else {
						nlType = NewlineType.CARRIAGE_RETURN;
						updateNewlineStrategy();
						leftOver = "" + ch;
						return line.toString();
					}
				} else {
					if (ch == '\n') {
						nlType = NewlineType.LINE_FEED;
						updateNewlineStrategy();
						return line.toString();
					} else {
						line.append(ch);
					}
				}
				wasCR = false;
			}
			return null;
		}
		
	}
	
	static class EndOfStreamException extends Throwable {
		
		/** */
		private static final long serialVersionUID = 1L;
		
	}
	
}
