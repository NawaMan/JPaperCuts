package dssb.util.process;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

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

	public void append(final byte ... bytes) {
		if (bytes == null) {
			return;
		}
		if (bytes.length == 0) {
			return;
		}
		
		synchronized (this) {
			int byteLength = bytes.length;
			int leftoverLength = (leftover != null) ? leftover.length : 0;
			if ((remainer + byteLength) > leftoverLength) {
				// The leftover array is too small, a bigger one is created.
				byte[] temp = new byte[remainer + byteLength];
				if (remainer != 0) {
					System.arraycopy(leftover, 0, temp, 0, remainer);
				}
				leftover = temp;
			}

			if (byteLength == 1) {
				leftover[remainer] = bytes[0];
			} else {
				System.arraycopy(bytes, 0, leftover, remainer, byteLength);
			}
			remainer += byteLength;
		}
	}
	
	public char[] take() {
		CharBuffer cb = null;
		synchronized (this) {
			// Is it done this way because I am too stupid (and no time) to read how the buffer really works.
			// The buffer may already provide all I need to do this without the leftover and remainer.
			ByteBuffer bb = ByteBuffer.wrap(leftover, 0, remainer);
			cb = CharBuffer.allocate(leftover.length);
			charset.newDecoder().decode(bb, cb, true);
	
			prepareLeftover(bb);
		}

		char[] decodedChars = new char[cb.position()];
		cb.rewind();
		cb.get(decodedChars, 0, decodedChars.length);
		return decodedChars;
	}

	private void prepareLeftover(ByteBuffer bb) {
		if (bb.hasRemaining()) {
			int remainer = bb.remaining();
			if ((leftover == null) || (leftover.length < remainer)) {
				leftover = new byte[remainer + 4]; 	// Arbitrary now.
			}
			System.arraycopy(leftover, leftover.length - remainer, leftover, 0, remainer);
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
	
	public String toDetail() {
		return "{ 'remainder': " + remainer +", 'leftover': '" + Arrays.toString(leftover) +"' }";
	}
	
	
}
