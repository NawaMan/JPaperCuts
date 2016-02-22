package dssb.util.stringline.chariterator;

import java.io.IOException;

public interface CharIterator {
	
	public char next() throws NoMoreCharException, IOException;
	
}
