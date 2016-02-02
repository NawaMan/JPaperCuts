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
		int inCount = remainer + bytes.length;
		if (remainer != 0) {
			if ((remainer + bytes.length) > leftover.length) {
				byte[] temp = new byte[remainer + bytes.length];
				System.arraycopy(leftover, 0, temp, 0,        remainer);
				System.arraycopy(bytes,    0, temp, remainer, bytes.length);
				bytes = temp;
			} else {
				System.arraycopy(bytes, 0, leftover, remainer, bytes.length);
				bytes = leftover;
			}
		}

		// Is it done this way because I am too stupid (and no time) to read how the buffer really works.
		// The buffer may already provide all I need to do this without the leftover and remainer.
		ByteBuffer bb = ByteBuffer.wrap(bytes, 0, inCount);
		CharBuffer cb = CharBuffer.allocate(bytes.length);
		charset.newDecoder().decode(bb, cb, true);
		char[] decodedChars = new char[cb.position()];
		cb.rewind();
		cb.get(decodedChars, 0, decodedChars.length);

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
				
		return decodedChars;
	}
	
	public boolean hasRemainer() {
		return remainer != 0;
	}
	
	public byte[] getRemainerBytes() {
		byte[] result = new byte[remainer];
		System.arraycopy(leftover, 0, result, 0, remainer);
		remainer = 0;
		return result;
	}
	
	
}
