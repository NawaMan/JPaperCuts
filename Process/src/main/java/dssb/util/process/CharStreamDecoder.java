package dssb.util.process;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class CharStreamDecoder {
	
	private byte[] leftover = null;
	private int remainer = 0;
	
	private Charset charset = StandardCharsets.UTF_8;
	
	public CharStreamDecoder() {
		this(null);
	}
	
	public CharStreamDecoder(Charset charset) {
		this.charset = (charset != null) ? charset : Charset.defaultCharset();
	}
	
	public char[] take(byte[] bytes) {
		CharBuffer cb = null;
		synchronized (this) {
			int inCount = remainer + bytes.length;
			if (remainer != 0) {
				bytes = prependLeftover(bytes);
			}
	
			// Is it done this way because I am too stupid (and no time) to read how the buffer really works.
			// The buffer may already provide all I need to do this without the leftover and remainer.
			ByteBuffer bb = ByteBuffer.wrap(bytes, 0, inCount);
			cb = CharBuffer.allocate(bytes.length);
			charset.newDecoder().decode(bb, cb, true);
	
			prepareLeftover(bytes, bb);
		}

		char[] decodedChars = new char[cb.position()];
		cb.rewind();
		cb.get(decodedChars, 0, decodedChars.length);
		return decodedChars;
	}

	private byte[] prependLeftover(byte[] bytes) {
		if ((remainer + bytes.length) > leftover.length) {
			// The leftover array is too small, a bigger one is created to hold bothe the leftoever and the bytes.
			byte[] temp = new byte[remainer + bytes.length];
			System.arraycopy(leftover, 0, temp, 0,        remainer);
			System.arraycopy(bytes,    0, temp, remainer, bytes.length);
			return temp;
		} else {
			// Enough space in the leftover array, so move the bytes to appends the leftoever.
			System.arraycopy(bytes, 0, leftover, remainer, bytes.length);
			return leftover;
		}
	}

	private void prepareLeftover(byte[] bytes, ByteBuffer bb) {
		if (bb.hasRemaining()) {
			int remainer = bb.remaining();
			if ((leftover == null) || (leftover.length < remainer)) {
				leftover = new byte[remainer + 4]; 	// Arbitrary now.
			}
			System.arraycopy(bytes, bytes.length - remainer, leftover, 0, remainer);
			this.remainer = remainer;
		} else {
			this.remainer = 0;
		}
	}
	
	public boolean hasRemainer() {
		synchronized (this) {
			return remainer != 0;
		}
	}
	
	public byte[] getRemainerBytes() {
		synchronized (this) {
			byte[] result = new byte[remainer];
			System.arraycopy(leftover, 0, result, 0, remainer);
			remainer = 0;
			return result;
		}
	}
	
	
}
