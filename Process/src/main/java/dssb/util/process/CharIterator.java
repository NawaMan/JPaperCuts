package dssb.util.process;

import java.io.IOException;

public interface CharIterator {
	
	public char next() throws NoMoreCharException, IOException;
	
}
