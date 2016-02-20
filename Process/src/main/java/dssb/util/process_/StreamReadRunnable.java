package dssb.util.process_;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class StreamReadRunnable implements Runnable {
	
	private final InputStream inStream;
	
	private final LineHandler handler;
	
	public StreamReadRunnable(final InputStream inStream, LineHandler handler) {
		this.inStream = inStream;
		this.handler = handler;
	}
	
	public void run() {
		InputStreamReader reader  = new InputStreamReader(inStream);
		BufferedReader    bufferd = new BufferedReader(reader);
		try {
			String line;
			while ((line = bufferd.readLine()) != null) {
				handler.handler(line);
			}
		} catch (IOException exception) {
			throw new RuntimeException(exception);
		}
	}
}
