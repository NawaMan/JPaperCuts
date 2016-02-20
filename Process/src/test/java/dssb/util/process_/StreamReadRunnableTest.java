package dssb.util.process_;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.junit.Test;

import dssb.util.process_.LineHandler;
import dssb.util.process_.StreamReadRunnable;

public class StreamReadRunnableTest {

	@Test
	public void test() {
		final ByteArrayInputStream inStream = new ByteArrayInputStream("Line1\nLine2\nLine3".getBytes());
		final ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		StreamReadRunnable runnable = new StreamReadRunnable(inStream, new LineHandler() {
			@Override
			public void handler(String line) {
				try {
					outStream.write(line.getBytes());
				} catch (IOException exception) {
					throw new RuntimeException(exception);
				}
			}
		});
		
		runnable.run();
		assertEquals("Line1Line2Line3", new String(outStream.toByteArray()));
	}

}
