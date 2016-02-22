package dssb.util.process;

import java.io.InputStream;
import java.nio.charset.Charset;

public class LineInputStreamBuilder {
	
	private NewlineType nlType = NewlineType.TO_BE_DETERMINED;
	private CharIterator charIterator;
	
	public LineInputStreamBuilder(InputStream source) {
		this(new InputStreamCharIterator(source, null));
	}
	
	public LineInputStreamBuilder(CharStreamDecoder decoder, InputStream source) {
		this(new InputStreamCharIterator(source, decoder));
	}
	
	public LineInputStreamBuilder(CharIterator charIterator) {
		this.charIterator = charIterator;
		if (this.charIterator == null) {
			throw new NullPointerException(LineInputStream.NULL_SOURCE);
		}
	}
	
	public LineInputStreamBuilder decoder(CharStreamDecoder decoder) {
		if (!(charIterator instanceof InputStreamCharIterator)) {
			String message = "The CharInterator was not an InputStreamCharIterator so its decoder can't be chagned.";
			throw new IllegalStateException(message);
		}
		
		InputStream source = ((InputStreamCharIterator) charIterator).getSource();
		this.charIterator = new InputStreamCharIterator(source, decoder);
		return this;
	}
	
	public LineInputStreamBuilder charset(Charset charset) {
		if (!(charIterator instanceof InputStreamCharIterator)) {
			String message = "The CharInterator was not an InputStreamCharIterator so its decoder charset can't be chagned.";
			throw new IllegalStateException(message);
		}
		
		InputStream source = ((InputStreamCharIterator) charIterator).getSource();
		CharStreamDecoder oldDecoder = ((InputStreamCharIterator) charIterator).getDecoder();
		int capacity = (oldDecoder != null) ? oldDecoder.getCapacity() : CharStreamDecoder.DEFAULT_CAPACITY;
		CharStreamDecoder newDecoder = new CharStreamDecoder(charset, capacity);
		this.charIterator = new InputStreamCharIterator(source, newDecoder);
		return this;
	}
	
	public LineInputStreamBuilder newlineType(NewlineType nlType) {
		this.nlType = nlType;
		return this;
	}
	
	public LineInputStreamBuilder linefeed() {
		newlineType(NewlineType.LINE_FEED);
		return this;
	}
	
	public LineInputStreamBuilder carriageReturn() {
		newlineType(NewlineType.CARRIAGE_RETURN);
		return this;
	}
	
	public LineInputStreamBuilder carriageReturnThenLinefeed() {
		newlineType(NewlineType.CARRIAGE_RETURN_LINE_FEED);
		return this;
	}
	
	public LineInputStreamBuilder toBeDetermined() {
		newlineType(NewlineType.TO_BE_DETERMINED);
		return this;
	}
	
	public LineInputStreamBuilder unix() {
		newlineType(NewlineType.LINE_FEED);
		return this;
	}
	
	public LineInputStreamBuilder mac() {
		newlineType(NewlineType.CARRIAGE_RETURN);
		return this;
	}
	
	public LineInputStreamBuilder windows() {
		newlineType(NewlineType.CARRIAGE_RETURN_LINE_FEED);
		return this;
	}
	
	public LineInputStreamBuilder lf() {
		newlineType(NewlineType.LINE_FEED);
		return this;
	}
	
	public LineInputStreamBuilder cr() {
		newlineType(NewlineType.CARRIAGE_RETURN);
		return this;
	}
	
	public LineInputStreamBuilder crlf() {
		newlineType(NewlineType.CARRIAGE_RETURN_LINE_FEED);
		return this;
	}
	
	public LineInputStream build() {
		return new LineInputStream(nlType, charIterator);
	}
	
}