package dssb.util.process;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class LineReadRunnable implements Runnable {
	
	private final InputStream inStream;
	
	private final LineOutputHandler out;
	
	public LineReadRunnable(InputStream inStream, LineOutputHandler out) {
		this.inStream = inStream;
		this.out      = out;
	}

	@Override
	public void run() {
		InputStreamReader reader = new InputStreamReader(inStream);
		BufferedReader bufferd = new BufferedReader(reader);

		while (true) {
			String line = null;
			try {
				line = bufferd.readLine();
			} catch (IOException e) {
				// TODO - Deal with this
				e.printStackTrace();
			}
			if (line == null) {
				break;
			}

			out.handleLine(line);
		}
	}
	
}