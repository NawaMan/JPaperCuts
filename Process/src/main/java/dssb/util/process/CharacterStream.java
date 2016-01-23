package dssb.util.process;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CoderResult;
import java.nio.charset.StandardCharsets;

public class CharacterStream {

	private final InputStream inStream;
	
	//private final byte[] byteBuffer = new byte[10];
	private final char[] charBuffer = new char[10];
	
	private volatile int startIndex = 0;
	private volatile int endIndex = 0;
	private volatile boolean isDoneReading = false;

	private final CharsetDecoder decoder = StandardCharsets.UTF_8.newDecoder();
	
	public CharacterStream(InputStream inputStream) {
		this.inStream = inputStream;
		 
		Runnable runnable = new Runnable() {
			@Override
			public void run() {
				try {
					while (!isDoneReading) {
						if (startIndex <= endIndex) {
							if (endIndex != charBuffer.length) {
								byte[] bytes = new byte[10];
								int count = inStream.read(bytes, startIndex, 10 - endIndex);
								if (count == -1) {
									isDoneReading = true;
									return;
								}
		
								ByteBuffer bb = ByteBuffer.wrap(bytes, 0, count);
								CharBuffer cb = CharBuffer.allocate(1024);
								CoderResult result = decoder.decode(bb, cb, false);
								if (!result.isOverflow()) {
									char[] chars = cb.array();
									int position = cb.position();
									System.arraycopy(chars, 0, charBuffer, endIndex, position);
									endIndex += position;
									
									printBuffer("FILL");
								}
							} else {
								byte[] bytes = new byte[10];
								int count = inStream.read(bytes, 0, startIndex);
								if (count == -1) {
									isDoneReading = true;
									return;
								}
		
								ByteBuffer bb = ByteBuffer.wrap(bytes, 0, count);
								CharBuffer cb = CharBuffer.allocate(1024);
								CoderResult result = decoder.decode(bb, cb, false);
								if (!result.isOverflow()) {
									char[] chars = cb.array();
									int position = cb.position();
									System.arraycopy(chars, 0, charBuffer, 0, position);
									endIndex = position;
									
									printBuffer("FILL");
								}
							}
						} else {
							throw new RuntimeException("Unimplemented branch.");
						}

						try {
							Thread.sleep(1);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
		};
		Thread thread = new Thread(runnable, "ReadThread");
		thread.start();
	}

	public int read(char[] charArray, int start, int length) throws IOException {
		while (!isDoneReading) {
			if (startIndex != endIndex) {
				if (startIndex <= endIndex) {
					System.arraycopy(charBuffer, startIndex, charArray, start, length);
					int size = endIndex - startIndex;
					startIndex = endIndex;
					
					printBuffer("PULL");
					if (startIndex == charBuffer.length) {
						startIndex = endIndex = 0;
					}
					return size;
				}
			}

			try {
				Thread.sleep(1);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return 0;
	}

	private void printBuffer(String action) {
		System.out.println(System.currentTimeMillis() + " @ " + action + ": " + CharacterStream.this);
		System.out.print(System.currentTimeMillis() + " @        ");
		for (int i = 0; i <= charBuffer.length; i++) {
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
	
	public String toString() {
		synchronized (this) {
			StringBuffer buff = new StringBuffer();
			buff.append('[');
			if (startIndex == endIndex) {
				for (int i = 0; i < charBuffer.length; i++) {
					buff.append(" ");
				}
			} else {
				for (int i = 0; i < charBuffer.length; i++) {
					char ch = charBuffer[i];
					if (startIndex < endIndex) {
						if ((i < startIndex) || (i >= endIndex)) {
							ch = ' ';
						}
					} else {
						if ((i < startIndex) && (i >= endIndex)) {
							ch = ' ';
						}
					}
					
					buff.append(ch);
				}
			}
			buff.append(']');
			return buff.toString();
		}
	}

}
