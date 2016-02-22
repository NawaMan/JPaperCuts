package dssb.util.stringline.linereader;

import java.io.IOException;

import dssb.util.stringline.chariterator.CharIterator;
import dssb.util.stringline.chariterator.NoMoreCharException;

public class LineReader {
	
	public static final String NULL_SOURCE = "The source character iterator cannot be null.";
	
	public static final String UNKNOWN_NOT_SUPPORT = "UNKNOWN newline type is not supported.";
	
	public static final long NO_TIMEOUT = -1;
	
	private final CharIterator charIterator;
	
	private volatile NewlineType nlType;
	
	private volatile NewLineStrategy nlStrategy;
	
	private volatile String leftOver = "";
	
	private volatile String leftLine = null;
	
	private volatile ReadThread readThread = null;
	
	public LineReader(NewlineType nlType, CharIterator charIterator) {
		this.charIterator = charIterator;
		this.nlType = nlType;
		
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
	
	public String readLine() throws IOException {
		StringBuffer lineBuffer = new StringBuffer();
		String line = readLine(lineBuffer);
		return line;
	}
	
	public String readLine(long timeout) throws IOException, ReadLineTimeoutException, InterruptedException {
		String leftLine = checkLeftLineFirst();
		if (leftLine != null) {
			return leftLine;
		}
		
		try {
			setUpReadThread();
			letsRead(timeout);
			
			String line = processDone();
			if (line != null) {
				return line;
			} else {
				String readPart = extractAlreadyReadPart();
				throw new ReadLineTimeoutException(readPart);
			}
		} finally {
			detachReadThread();
		}
	}
	
	private String checkLeftLineFirst() {
		if (leftLine == null) {
			return null;
		}
		
		synchronized (this) {
			if (leftLine == null) {
				return null;
			}
			
			String line = leftLine;
			leftLine = null;
			return line;
		}
	}
	
	private void setUpReadThread() {
		if ((readThread == null) || readThread.isDone) {
			useNewReadThread();
		} else {
			attachExistingReadThread();
		}
	}
	
	private void useNewReadThread() {
		StringBuffer lineBuffer = new StringBuffer();
		readThread = new ReadThread(lineBuffer);
		readThread.mainThread = Thread.currentThread();
		readThread.start();
	}
	
	private void attachExistingReadThread() {
		readThread.mainThread = Thread.currentThread();
	}
	
	private void letsRead(long timeout) throws InterruptedException {
		synchronized (readThread) {
			if (readThread.isDone) {
				return;
			}
		}
		
		try {
			Thread.sleep(timeout);
		} catch (InterruptedException exception) {
			synchronized (readThread) {
				readThread.mainThread = null;
				if (!readThread.isDone) {
					// Interrupted because of other reason.
					throw exception;
				}
			}
		}
	}
	
	private String processDone() throws IOException {
		synchronized (readThread) {
			readThread.mainThread = null;
			if (!readThread.isDone) {
				return null;
			}
			
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
	
	private String extractAlreadyReadPart() {
		String readPart = readThread.lineBuffer.toString();
		readThread.lineBuffer.delete(0, readPart.length());
		return readPart;
	}
	
	private void detachReadThread() {
		if (readThread != null) {
			readThread.mainThread = null;
		}
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
		} catch (NoMoreCharException exception) {
			// This block is intentionally left blank.
			String readLine = lineBuffer.toString();
			if (readLine.isEmpty()) {
				return null;
			} else {
				return readLine;
			}
		}
	}
	
	private StringBuffer absorbLeftOver(StringBuffer line) {
		line.append(leftOver);
		leftOver = "";
		return line;
	}
	
	// == Helper classes ===============================================================================================
	
	private interface NewLineStrategy {
		
		public void reset();
		
		public String processChar(char ch, StringBuffer line);
		
	}
	
	private class SingleCharNewLine implements NewLineStrategy {
		
		private final char newLineChar;
		
		SingleCharNewLine(char newLineChar) {
			this.newLineChar = newLineChar;
		}
		
		@Override
		public void reset() {
		}
		
		@Override
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
	
	private class ReadThread extends Thread {
		
		private final StringBuffer lineBuffer;
		
		private volatile Thread mainThread = null;
		
		private volatile boolean isDone = false;
		
		private volatile String line = null;
		
		private volatile RuntimeException runtimeException = null;
		
		private volatile IOException ioException = null;
		
		ReadThread(StringBuffer lineBuffer) {
			super("ReadThread");
			this.isDone = false;
			this.lineBuffer = (lineBuffer != null) ? lineBuffer : new StringBuffer();
		}
		
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
						// is not yet attached to other main thread so added to leftLine.
						synchronized (LineReader.this) {
							String line = theReadThread.line;
							leftLine = ((line != null) ? line : "");
						}
					}
				}
			}
		}
		
	}
	
}
