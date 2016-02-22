package dssb.util.process;

import java.io.IOException;
import java.io.InputStream;

public class InputStreamCharIterator implements CharIterator {
	
	public static final String NULL_SOURCE = "The source input stream cannot be null.";
	
	private final InputStream source;
	
	private final CharStreamDecoder decoder;
	
	private char[] chars = new char[0];
	private int index = -1;
	private boolean isDone = false;
	
	public InputStreamCharIterator(InputStream source, CharStreamDecoder decoder) {
		if (source == null) {
			throw new NullPointerException(NULL_SOURCE);
		}
		
		this.source = source;
		this.decoder = (decoder != null) ? decoder : new CharStreamDecoder();
	}
	
	public synchronized char next() throws NoMoreCharException, IOException {
		if (isDone) {
			throw new NoMoreCharException();
		}
		
		index++;
		while (index >= chars.length) {
			int read = source.read();
			if (read == -1) {
				isDone = true;
				throw new NoMoreCharException();
			}
			chars = decoder.take((byte) read);
			index = 0;
		}
		return chars[index];
	}
	
	InputStream getSource() {
		return source;
	}
	
	public CharStreamDecoder getDecoder() {
		return decoder;
	}
	
}
