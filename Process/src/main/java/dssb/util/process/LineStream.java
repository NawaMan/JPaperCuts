package dssb.util.process;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.atomic.AtomicInteger;

public class LineStream {
	
	private final InputStreamReader reader;
	
	private final char[] buffer = new char[10];//[4*1024];
	
	private volatile int startIndex = 0;
	private volatile int endIndex = 0;
	private volatile boolean isDone = false;
	
	private final Object mutex = new Object();
	private final AtomicInteger version = new AtomicInteger(0);
	
	public LineStream(InputStream inStream) {
		this.reader = new InputStreamReader(inStream);

		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					while(true){
						boolean isFull1;
						synchronized (LineStream.this) {
							isFull1 = ((endIndex - startIndex) == buffer.length);
						}
						if (isFull1) {
							continue;
						}
						
						boolean isFull2;
						synchronized (LineStream.this) {
							isFull2 = ((endIndex + 1) == startIndex);
						}
						if (isFull2) {
							continue;
						}
						
						boolean isEndOfBuffer = false;
						boolean isOverflowing = false;
						int accept = 0;
						int offset = 0;
						synchronized (LineStream.this) {
							if (endIndex == buffer.length) {
								isEndOfBuffer = true;
								offset = 0;
								accept = startIndex - 1;
							} else if (endIndex < startIndex) {
								isOverflowing = true;
								offset = endIndex;
								accept = startIndex - 1 - endIndex;
							} else {
								offset = endIndex;
								accept = buffer.length - endIndex;
							}
						}
						int count = reader.read(buffer, offset, accept);

						synchronized (LineStream.this) {
							if (count == -1) {
								isDone = true;
								break;
							}
							if (isEndOfBuffer) {
								endIndex = count;
								System.out.println("F1: " + LineStream.this);
							} else if (isOverflowing) {
								endIndex += count;
								System.out.println("F2: " + LineStream.this);
							} else {
								endIndex += count;
								System.out.println("F3: " + LineStream.this);
							}
							printIndexes();
							if (isEndOfBuffer && (startIndex != 0)) {
								continue;
							}
							synchronized (mutex) {
								System.out.println("notify");
								mutex.notifyAll();
								version.incrementAndGet();
							}
						}
					}
				} catch (IOException e) {
					// TODO - Pass this on.
					e.printStackTrace();
				}
			}
			
		}).start();
	}

	private void printIndexes() {
		System.out.print("     ");
		for (int i = 0; i <= buffer.length; i++) {
			if (startIndex == i) {
				if (endIndex == startIndex) {
					System.out.print("=");
					break;
				}
				System.out.print(">");
			} else if (endIndex == i) {
				System.out.print("<");
			} else {
				System.out.print(" ");
			}
		}
		System.out.println();
	}

	public String readLine() {
		return readLine(-1);
	}

	public String readLine(long timeout) {
		long expireTime = (timeout < 0) ? Long.MAX_VALUE : System.currentTimeMillis() + timeout;
		int lastVersion = 0;
		while (true) {
			boolean isEmpty = false;
			boolean isOverflow = false;
			synchronized (this) {
				isEmpty = (startIndex == endIndex);
				if (isEmpty && isDone) {
					return null;
				}
				isOverflow = (endIndex < startIndex);
			}
			
			if (isEmpty) {
				if (expireTime != Long.MAX_VALUE) {
					if (System.currentTimeMillis() > expireTime) {
						System.out.println("Expired");
						return "";
					}
				}
			} else {
				if (expireTime != Long.MAX_VALUE) {
					if (System.currentTimeMillis() > expireTime) {
						System.out.println("Expired");
						if (isOverflow) {
							synchronized (this) {
								System.out.println("OF: " + LineStream.this);
								printIndexes();
	
								String line
										= new String(buffer, startIndex, buffer.length - startIndex)
										+ new String(buffer, 0, endIndex + 1);
								startIndex = endIndex = 0;
								lastVersion = version.get();
								
								System.out.println("O3: " + LineStream.this);
								printIndexes();
								System.out.println("Return: " + line);
								return line;
							}
						} else {
							synchronized (this) {
								System.out.println("NM: " + LineStream.this);
								printIndexes();
								
								String line = new String(buffer, startIndex, endIndex - startIndex);
								startIndex = endIndex = 0;
								lastVersion = version.get();
								
								System.out.println("N2: " + LineStream.this);
								printIndexes();
								System.out.println("Return: " + line);
								return line;
							}
						}
					}
				}
				
				if (lastVersion != version.get()) {
					if (isOverflow) {
						synchronized (this) {
							System.out.println("OF: " + LineStream.this);
							printIndexes();
							for (int i = startIndex; i < buffer.length; i++) {
								char ch = buffer[i];
								if (ch != '\n') {
									continue;
								}
								
								String line = new String(buffer, startIndex, i - startIndex + 1);
								startIndex = i + 1;
								lastVersion = version.get();
								
								System.out.println("O1: " + LineStream.this);
								printIndexes();
								System.out.println("Return: " + line);
								return line;
							}
							for (int i = 0; i < endIndex; i++) {
								char ch = buffer[i];
								if (ch != '\n') {
									continue;
								}
								
								String line
										= new String(buffer, startIndex, buffer.length - startIndex)
										+ new String(buffer, 0, i + 1);
								startIndex = i + 1;
								lastVersion = version.get();
								
								System.out.println("O2: " + LineStream.this);
								printIndexes();
								System.out.println("Return: " + line);
								return line;
							}
						}
					} else {
						synchronized (this) {
							System.out.println("Nm: " + LineStream.this);
							printIndexes();
							for (int i = startIndex; i < endIndex; i++) {
								char ch = buffer[i];
								if (ch != '\n') {
									continue;
								}
								
								String line = new String(buffer, startIndex, i - startIndex + 1);
								startIndex = i + 1;
								lastVersion = version.get();
								
								System.out.println("N1: " + LineStream.this);
								printIndexes();
								System.out.println("Return: " + line);
								return line;
							}
						}
					}
				}
			}

			
			try {
				Thread.sleep(1);
				System.out.println("sleep");
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			if (expireTime != Long.MAX_VALUE) {
				try {
					synchronized (mutex) {
						System.out.println("wait");
						mutex.wait(timeout);
					}
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
	
	public String toString() {
		synchronized (this) {
			StringBuffer buff = new StringBuffer();
			buff.append('[');
			if (startIndex == endIndex) {
				for (int i = 0; i < buffer.length; i++) {
					buff.append(' ');
				}
			} else {
				for (int i = 0; i < buffer.length; i++) {
					char ch = buffer[i];
					if (startIndex < endIndex) {
						if ((i < startIndex) || (i >= endIndex)) {
							ch = ' ';
						}
					} else {
						if ((i < startIndex) && (i >= endIndex)) {
							ch = ' ';
						}
					}
					
					if ((ch < ' ') || (ch > '}')) {
						ch = '?';
					}
					buff.append(ch);
				}
			}
			buff.append(']');
			return buff.toString();
		}
	}
	
}
