package dssb.util.process_;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;

public class StreamReadThread extends AsynTask<String, Throwable> {
	
	private final InputStream inStream;
	
	public StreamReadThread(final InputStream inStream) {
		this.inStream = inStream;
		start();
	}
	
	public void run() {
		InputStreamReader reader  = new InputStreamReader(inStream);
		BufferedReader    bufferd = new BufferedReader(reader);
		
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		PrintStream out = new PrintStream(buffer);
		try {
			String line;
			while ((line = bufferd.readLine()) != null) {
				out.println(line);
			}

			onComplete(buffer.toString(), null);
		} catch (IOException e1) {
			onComplete(buffer.toString(), e1);
		}
	}
}