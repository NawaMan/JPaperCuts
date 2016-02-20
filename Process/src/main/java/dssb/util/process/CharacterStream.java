package dssb.util.process;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CoderResult;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class CharacterStream {

	private final InputStream inStream;
	
	private volatile byte[] byteBuffer = new byte[10];
	private final    char[] charBuffer = new char[10];
	
	private volatile int startIndex = 0;
	private volatile int endIndex = 0;
	private volatile int remainer = 0;
	private volatile boolean isDoneReading = false;

	private final CharsetDecoder decoder = StandardCharsets.UTF_8.newDecoder();
	
	public CharacterStream(InputStream inputStream) {
		this.inStream = inputStream;
		 
		Runnable runnable = new Runnable() {
			@Override
			public void run() {
				try {
					while (!isDoneReading) {
						
						synchronized (this) {
						if (startIndex <= endIndex) {
							if (endIndex != charBuffer.length) {
								System.out.println(Arrays.toString(byteBuffer));
								System.out.println("inStream.read: " + remainer + ","  + (byteBuffer.length - remainer));
								int count = inStream.read(byteBuffer, remainer, byteBuffer.length - remainer);
								System.out.println("F1" + Arrays.toString(byteBuffer));
								if (count == -1) {
									isDoneReading = true;
									return;
								}
		
								ByteBuffer bb = ByteBuffer.wrap(byteBuffer, 0, count);
								CharBuffer cb = CharBuffer.allocate(1024);
								CoderResult result = decoder.decode(bb, cb, false);
								if (!result.isOverflow()) {
									char[] chars = cb.array();
									int position = cb.position();
									System.arraycopy(chars, 0, charBuffer, endIndex, position);
									System.out.println(endIndex+ " vs " + position);
									endIndex = endIndex + position;
									
									printBuffer("FIL1");
									
									if (bb.hasRemaining()) {
										remainer = bb.remaining();
										byte[] bytes = new byte[10];
										System.arraycopy(byteBuffer, bb.position(), bytes, 0, remainer);
										byteBuffer = bytes;
									}
								}
							} else if (startIndex != 0) {
								int count = inStream.read(byteBuffer, remainer, byteBuffer.length - remainer);
								System.out.println("F2" + Arrays.toString(byteBuffer));
								if (count == -1) {
									isDoneReading = true;
									return;
								}
		
								ByteBuffer bb = ByteBuffer.wrap(byteBuffer, 0, count);
								CharBuffer cb = CharBuffer.allocate(1024);
								CoderResult result = decoder.decode(bb, cb, false);
								if (!result.isOverflow()) {
									char[] chars = cb.array();
									int position = cb.position();
									System.arraycopy(chars, 0, charBuffer, 0, position);
									endIndex = position;
									
									printBuffer("FIL2");
									
									if (bb.hasRemaining()) {
										remainer = bb.remaining();
										byte[] bytes = new byte[10];
										System.arraycopy(byteBuffer, bb.position(), bytes, 0, remainer);
										byteBuffer = bytes;
									}
								}
							}
						} else {
							throw new RuntimeException("Unimplemented branch: startIndex=" + startIndex + " endIndex=" + endIndex);
						}
						
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
		int total = 0;
		while (!isDoneReading) {
			synchronized (this) {
			if (startIndex != endIndex) {
				if (startIndex <= endIndex) {
					System.arraycopy(charBuffer, startIndex, charArray, start, length - total);
					int size = endIndex - startIndex;
					startIndex = endIndex;
					
					if (startIndex == charBuffer.length) {
						startIndex = endIndex = 0;
					}
					
					printBuffer("PUL1");
					total += size;
					if (total == length) {
						return total;
					}
				} else {
					int size1 = charBuffer.length - startIndex;
					int size2 = endIndex;
					System.arraycopy(charBuffer, startIndex, charArray, start, size1);
					System.arraycopy(charBuffer, 0,          charArray, size1, size2);
					startIndex = endIndex = 0;
					
					printBuffer("PUL2");
					total += size1 + size2;
					if (total == length) {
						return total;
					}
				}
			}
			
			}

			try {
				Thread.sleep(1);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		synchronized (this) {
		if (startIndex != endIndex) {
			if (startIndex <= endIndex) {
				System.arraycopy(charBuffer, startIndex, charArray, start, length - total);
				int size = endIndex - startIndex;
				startIndex = endIndex;
				
				printBuffer("PULL");
				if (startIndex == charBuffer.length) {
					startIndex = endIndex = 0;
				}
				return size;
			} else {
				int size1 = charBuffer.length - startIndex;
				int size2 = endIndex;
				System.arraycopy(charBuffer, startIndex, charArray, start, size1);
				System.arraycopy(charBuffer, 0,          charArray, size1, size2);
				startIndex = endIndex = 0;
				
				printBuffer("PULL");
				return size1 + size2;
			}
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
