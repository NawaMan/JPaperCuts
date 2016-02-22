package dssb.util.stringline.linereader;

import java.io.InputStream;
import java.nio.charset.Charset;

import dssb.util.stringline.chariterator.CharIterator;
import dssb.util.stringline.charstreamdecoder.CharStreamDecoder;

public class LineReaderBuilder {
	
	private NewlineType nlType = NewlineType.TO_BE_DETERMINED;
	private CharIterator charIterator;
	
	public LineReaderBuilder(InputStream source) {
		this(new InputStreamCharIterator(source, null));
	}
	
	public LineReaderBuilder(CharStreamDecoder decoder, InputStream source) {
		this(new InputStreamCharIterator(source, decoder));
	}
	
	public LineReaderBuilder(CharIterator charIterator) {
		this.charIterator = charIterator;
		if (this.charIterator == null) {
			throw new NullPointerException(LineReader.NULL_SOURCE);
		}
	}
	
	public LineReaderBuilder decoder(CharStreamDecoder decoder) {
		if (!(charIterator instanceof InputStreamCharIterator)) {
			String message = "The CharInterator was not an InputStreamCharIterator so its decoder can't be chagned.";
			throw new IllegalStateException(message);
		}
		
		InputStream source = ((InputStreamCharIterator) charIterator).getSource();
		this.charIterator = new InputStreamCharIterator(source, decoder);
		return this;
	}
	
	public LineReaderBuilder charset(Charset charset) {
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
	
	public LineReaderBuilder newlineType(NewlineType nlType) {
		this.nlType = nlType;
		return this;
	}
	
	public LineReaderBuilder linefeed() {
		newlineType(NewlineType.LINE_FEED);
		return this;
	}
	
	public LineReaderBuilder carriageReturn() {
		newlineType(NewlineType.CARRIAGE_RETURN);
		return this;
	}
	
	public LineReaderBuilder carriageReturnThenLinefeed() {
		newlineType(NewlineType.CARRIAGE_RETURN_LINE_FEED);
		return this;
	}
	
	public LineReaderBuilder toBeDetermined() {
		newlineType(NewlineType.TO_BE_DETERMINED);
		return this;
	}
	
	public LineReaderBuilder unix() {
		newlineType(NewlineType.LINE_FEED);
		return this;
	}
	
	public LineReaderBuilder mac() {
		newlineType(NewlineType.CARRIAGE_RETURN);
		return this;
	}
	
	public LineReaderBuilder windows() {
		newlineType(NewlineType.CARRIAGE_RETURN_LINE_FEED);
		return this;
	}
	
	public LineReaderBuilder lf() {
		newlineType(NewlineType.LINE_FEED);
		return this;
	}
	
	public LineReaderBuilder cr() {
		newlineType(NewlineType.CARRIAGE_RETURN);
		return this;
	}
	
	public LineReaderBuilder crlf() {
		newlineType(NewlineType.CARRIAGE_RETURN_LINE_FEED);
		return this;
	}
	
	public LineReader build() {
		return new LineReader(nlType, charIterator);
	}
	
}