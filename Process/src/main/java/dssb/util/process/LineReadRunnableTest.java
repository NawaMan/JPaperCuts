package dssb.util.process;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.Test;

public class LineReadRunnableTest {

	@Test
	public void readOneLine() throws InterruptedException {
		final String line = "Hello\n";
		
		final InputStream inStream = new InputStream() {
			int index = 0;
			@Override
			public int read() throws IOException {
				if (index >= line.length()) {
					return -1;
				}
				
				if (index == 3) {
					try {
						Thread.sleep(20000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				
				return line.charAt(index++);
			}
		};
		

		final InputStream middle = new InputStream() {
			private final byte[] bytes = new byte[4*1024];
			private volatile int index = 0;
			@Override
			public int read() throws IOException {
				return inStream.read();
			}
		};
		
		LineOutputHandler lineHandle = new AllLineOutputHandler();
		
		LineReadRunnable readRunnable = new LineReadRunnable(middle, lineHandle);
		new Thread(readRunnable).start();
		
		Thread.sleep(10000);
		System.out.println(lineHandle);
		System.out.println("----");
	}

}
