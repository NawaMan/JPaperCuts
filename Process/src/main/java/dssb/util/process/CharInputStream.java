package dssb.util.process;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class CharInputStream {
	
	private volatile InputStream inStream;
	private volatile CharStreamDecoder decoder;
	private volatile StringBuffer charBuffer = new StringBuffer();
	
	public CharInputStream(InputStream inStream) {
		this(null, inStream);
	}
	
	public CharInputStream(CharStreamDecoder decoder, InputStream inStream) {
		this.decoder = (decoder != null) ? decoder : new CharStreamDecoder();
		this.inStream = inStream;
	}
	
	public int read(char[] chars, int offset, int length) throws IOException {
		char[] cs = read();
		
		int count;
		if ((offset + cs.length) < length) {
			count = cs.length;
			System.arraycopy(cs, 0, chars, offset, count);
		} else {
			count = (length - offset);
			System.arraycopy(cs, 0, chars, offset, count);
			synchronized (charBuffer) {
				charBuffer.append(cs, offset + length, cs.length - count);
			}
		}
		return count;
	}

	public char[] read() throws IOException {
		return  read(-1, true);
	}
	
	public char[] read(long wait) throws IOException {
		return  read(wait, true);
	}
	
	private char[] read(long wait, boolean isAsyn) throws IOException {
		if (isAsyn && (wait < 0)) {
			return read(wait, false);
		}
		
		final AtomicReference<IOException> ioExceptionRef = new AtomicReference<IOException>();
		final AtomicReference<RuntimeException> runtimeExceptionRef = new AtomicReference<RuntimeException>();
		final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		final AtomicBoolean readStop = new AtomicBoolean(false);
		
		if (this.charBuffer.length() != 0) {
			synchronized (this.charBuffer) {
				if (this.charBuffer.length() != 0) {
					buffer.write(this.charBuffer.toString().getBytes());
					this.charBuffer.delete(0, this.charBuffer.length());
				}
			}
		}
		
		// TODO - Add conditional stop.
		Runnable runnable = new Runnable() {
			@Override
			public void run() {
				try {
					int read;
					while (!readStop.get() && ((read = inStream.read()) != -1)) {
						synchronized (buffer) {
							if (readStop.get()) {
								decoder.append((byte)read);
							} else {
								buffer.write(read);
							}
						}
					}
				} catch (IOException e) {
					ioExceptionRef.set(e);
				} catch (RuntimeException e) {
					runtimeExceptionRef.set(e);
				}
			}
		};
		runAsyn(runnable, wait, isAsyn);
		readStop.set(true);
		
		if (ioExceptionRef.get() != null) {
			throw ioExceptionRef.get();
		}
		if (runtimeExceptionRef.get() != null) {
			throw ioExceptionRef.get();
		}

		char[] cs;
		synchronized (buffer) {
			decoder.append(buffer.toByteArray());
			cs = decoder.take();
		}
		return cs;
	}

	// TODO - Move this elsewhere
	private void runAsyn(Runnable runnable, long wait, boolean isAsyn) {
		if (isAsyn) {
			new Thread(runnable).start();
			// This currently causes the read to wait for the "wait" time every time even when the char was read.
			// TODO - Think about what is the appropriate behavior for this.
			try {
				Thread.sleep(wait);
			} catch (InterruptedException e) {
			}
		} else {
			runnable.run();
		}
	}
	
}
