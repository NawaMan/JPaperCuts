package dssb.util.process;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.atomic.AtomicReference;

public class CharInputStream {
	
	private volatile InputStream inStream;
	private volatile CharStreamDecoder decoder = new CharStreamDecoder();
	private volatile StringBuffer buffer = new StringBuffer();
	
	public CharInputStream(InputStream inStream) {
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
			synchronized (buffer) {
				buffer.append(cs, offset + length, cs.length - count);
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
		
		if (this.buffer.length() != 0) {
			synchronized (this.buffer) {
				if (this.buffer.length() != 0) {
					buffer.write(this.buffer.toString().getBytes());
					this.buffer.delete(0, this.buffer.length());
				}
			}
		}
		
		Runnable runnable = new Runnable() {
			@Override
			public void run() {
				try {
					int read;
					while ((read = inStream.read()) != -1) {
						synchronized (buffer) {
							buffer.write(read);
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
		
		if (ioExceptionRef.get() != null) {
			throw ioExceptionRef.get();
		}
		if (runtimeExceptionRef.get() != null) {
			throw ioExceptionRef.get();
		}

		char[] cs;
		synchronized (buffer) {
			cs = decoder.take(buffer.toByteArray());
		}
		return cs;
	}

	private void runAsyn(Runnable runnable, long wait, boolean isAsyn) {
		if (isAsyn) {
			new Thread(runnable).start();
			try {
				Thread.sleep(wait);
			} catch (InterruptedException e) {
			}
		} else {
			runnable.run();
		}
	}
	
}
