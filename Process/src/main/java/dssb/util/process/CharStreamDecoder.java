package dssb.util.process;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class CharStreamDecoder {
	
	public static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;

	public static final int DEFAULT_CAPACITY = 4*1024;

	private Charset charset = DEFAULT_CHARSET;
	
	private volatile ByteBuffer byteBuffer;
	
	public CharStreamDecoder() {
		this(null, -1);
	}
	
	public CharStreamDecoder(Charset charset) {
		this(charset, -1);
	}
	
	public CharStreamDecoder(int bufferCapacity) {
		this(null, bufferCapacity);
	}
	
	public CharStreamDecoder(Charset charset, int bufferCapacity) {
		this.charset = (charset != null) ? charset : Charset.defaultCharset();
		this.byteBuffer = ByteBuffer.allocate((bufferCapacity > 0) ? bufferCapacity : DEFAULT_CAPACITY);
	}
	
	public Charset getCharset() {
		return this.charset;
	}
	
	public int getCapacity() {
		return byteBuffer.capacity();
	}

	public void append(byte ... bytes) {
		if (bytes == null) {
			return;
		}
		if (bytes.length == 0) {
			return;
		}
		
		synchronized (this) {
			// TODO - Think about overflow.
			byteBuffer.put(bytes);
		}
	}
	
	public char[] take(byte ... bytes) {
		append(bytes);
		CharBuffer cb = decodeToChars();
		char[] decodedChars = toCharArray(cb);
		return decodedChars;
	}

	private CharBuffer decodeToChars() {
		CharBuffer cb = null;
		synchronized (this) {
			byteBuffer.flip();	// Change from write mode to read mode.
			cb = CharBuffer.allocate(byteBuffer.limit());
			charset.newDecoder().decode(byteBuffer, cb, true);
			byteBuffer.compact();	// Copy the leftover to the front making room for more bytes.
		}
		cb.flip();	// Change from write mode to read mode.
		return cb;
	}

	private char[] toCharArray(CharBuffer cb) {
		char[] decodedChars = new char[cb.limit()];
		cb.get(decodedChars);
		return decodedChars;
	}
	
	public boolean hasRemainer() {
		synchronized (this) {
			// From 0 to position is the remaining.
			return byteBuffer.position() != 0;
		}
	}
	
	public byte[] getRemainingBytes() {
		synchronized (this) {
			// From 0 to position is the remaining.
			// So we have to get from 0 to the position.
			// But when getting that, the position will change to the bytes we got,
			//   so there is no need to change the position back after the get.
			int thePosition = byteBuffer.position();
			byte[] remaining = new byte[thePosition];
			byteBuffer.position(0);
			byteBuffer.get(remaining, 0, thePosition);
			return remaining;
		}
	}
	
}
