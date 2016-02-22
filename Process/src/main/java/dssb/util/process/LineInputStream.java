package dssb.util.process;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

public class LineInputStream {
	
	public static final String NULL_SOURCE = "The source input stream cannot be null.";
	
	public static final String UNKNOWN_NOT_SUPPORT = "UNKNOWN newline type is not supported.";
	
	public static final long NO_TIMEOUT = -1;
	
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
	
	private volatile String leftLine = null;
	
	private final CharIterator charIterator = new CharIterator();
	
	private volatile NewLineStrategy nlStrategy;
	
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
	
	private StringBuffer absorbLeftOver(StringBuffer line) {
		line.append(leftOver);
		leftOver = "";
		return line;
	}
	
	public String readLine() throws IOException {
		StringBuffer lineBuffer = new StringBuffer();
		String line = readLine(lineBuffer);
		return line;
	}
	
	private static class ReadThread extends Thread {
		
		private final StringBuffer lineBuffer;
		
		private volatile Thread mainThread;
		
		private volatile boolean isDone;
		
		private volatile String line = null;
		
		private volatile RuntimeException runtimeException = null;
		
		private volatile IOException ioException = null;
		
		ReadThread(Thread mainThread, StringBuffer lineBuffer, Runnable runnable) {
			super(runnable, "ReadThread");
			this.isDone = false;
			this.mainThread = mainThread;
			this.lineBuffer = (lineBuffer != null) ? lineBuffer : new StringBuffer();
		}
		
	}
	
	private volatile ReadThread readThread = null;
	
	public String readLine(long timeout) throws IOException, ReadLineTimeoutException, InterruptedException {
		if (leftLine != null) {
			synchronized (this) {
				String line = leftLine;
				leftLine = null;
				return line;
			}
		}
		
		if ((readThread == null) || readThread.isDone) {
			final StringBuffer lineBuffer = new StringBuffer();
			readThread = new ReadThread(Thread.currentThread(), lineBuffer, new Runnable() {
				@Override
				public void run() {
					final ReadThread theReadThread = readThread;
					try {
						String line = readLine(lineBuffer);
						theReadThread.line = line;
					} catch (IOException e) {
						theReadThread.ioException = e;
					} catch (RuntimeException e) {
						theReadThread.runtimeException = e;
					} finally {
						synchronized (theReadThread) {
							theReadThread.isDone = true;
							if (theReadThread.mainThread != null) {
								// Attached with a mainThread ... so interrupted it.
								theReadThread.mainThread.interrupt();
							} else {
								// is not yet attach to other main thread so added to leftLine.
								synchronized (LineInputStream.this) {
									String line = theReadThread.line;
									leftLine = ((line != null) ? line : "");
								}
							}
						}
					}
				}
			});
			readThread.start();
		} else {
			readThread.mainThread = Thread.currentThread();
		}
		
		InterruptedException interruptedException = null;
		if (!readThread.isDone) {
			try {
				Thread.sleep(timeout);
			} catch (InterruptedException exception) {
				interruptedException = exception;
			}
		}
		
		boolean isDone = false;
		synchronized (readThread) {
			isDone = readThread.isDone;
			readThread.mainThread = null;
			
			if (isDone) {
				RuntimeException runtimeException = readThread.runtimeException;
				if (runtimeException != null) {
					throw runtimeException;
				}
				
				IOException ioException = readThread.ioException;
				if (ioException != null) {
					throw ioException;
				}
				String readLine = readThread.line;
				return readLine;
			}
		}
		
		if (interruptedException != null) {
			// Sleep was interrupted because of another reason
			throw interruptedException;
		}
		
		String readPart = readThread.lineBuffer.toString();
		readThread.lineBuffer.delete(0, readPart.length());
		throw new ReadLineTimeoutException(readPart);
	}
	
	private synchronized String readLine(StringBuffer lineBuffer) throws IOException {
		if (leftLine != null) {
			lineBuffer.append(leftLine);
			leftLine = null;
			return lineBuffer.toString();
		}
		
		absorbLeftOver(lineBuffer);
		
		nlStrategy.reset();
		
		try {
			while (true) {
				char ch = charIterator.next();
				String readLine = nlStrategy.processChar(ch, lineBuffer);
				if (readLine != null) {
					return readLine;
				}
			}
		} catch (EndOfStreamException exception) {
			// This block is intentionally left blank.
			String readLine = lineBuffer.toString();
			if (readLine.isEmpty()) {
				return null;
			} else {
				return readLine;
			}
		}
	}
	
	// == Creation =====================================================================================================
	
	public static class Builder {
		
		private CharStreamDecoder decoder = new CharStreamDecoder();
		private NewlineType nlType = NewlineType.TO_BE_DETERMINED;
		private final InputStream source;
		
		public Builder(InputStream source) {
			this.source = source;
			if (this.source == null) {
				throw new NullPointerException(NULL_SOURCE);
			}
		}
		
		public Builder decoder(CharStreamDecoder decoder) {
			this.decoder = decoder;
			return this;
		}
		
		public Builder charset(Charset charset) {
			int capacity = (this.decoder != null) ? this.decoder.getCapacity() : CharStreamDecoder.DEFAULT_CAPACITY;
			this.decoder = new CharStreamDecoder(charset, capacity);
			return this;
		}
		
		public Builder newlineType(NewlineType nlType) {
			this.nlType = nlType;
			return this;
		}
		
		public Builder linefeed() {
			newlineType(NewlineType.LINE_FEED);
			return this;
		}
		
		public Builder carriageReturn() {
			newlineType(NewlineType.CARRIAGE_RETURN);
			return this;
		}
		
		public Builder carriageReturnThenLinefeed() {
			newlineType(NewlineType.CARRIAGE_RETURN_LINE_FEED);
			return this;
		}
		
		public Builder toBeDetermined() {
			newlineType(NewlineType.TO_BE_DETERMINED);
			return this;
		}
		
		public Builder unix() {
			newlineType(NewlineType.LINE_FEED);
			return this;
		}
		
		public Builder mac() {
			newlineType(NewlineType.CARRIAGE_RETURN);
			return this;
		}
		
		public Builder windows() {
			newlineType(NewlineType.CARRIAGE_RETURN_LINE_FEED);
			return this;
		}
		
		public Builder lf() {
			newlineType(NewlineType.LINE_FEED);
			return this;
		}
		
		public Builder cr() {
			newlineType(NewlineType.CARRIAGE_RETURN);
			return this;
		}
		
		public Builder crlf() {
			newlineType(NewlineType.CARRIAGE_RETURN_LINE_FEED);
			return this;
		}
		
		public LineInputStream build() {
			return new LineInputStream(decoder, nlType, source);
		}
		
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
	
	private static class EndOfStreamException extends Throwable {
		
		/** */
		private static final long serialVersionUID = 1L;
		
	}
	
}
