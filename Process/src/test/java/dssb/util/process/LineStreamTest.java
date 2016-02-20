package dssb.util.process;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.junit.Ignore;
import org.junit.Test;

public class LineStreamTest {

	@Test
	public void emptyStreamHasNoLine() {
		ByteArrayInputStream bais = new ByteArrayInputStream("".getBytes());
		LineStream lineStream = new LineStream(bais);
		String line = lineStream.readLine();
		assertNull(line);
	}

	@Test
	public void oneLineStreamHasOneLine() {
		String theLine = "Hello\n";
		
		ByteArrayInputStream bais = new ByteArrayInputStream(theLine.getBytes());
		LineStream lineStream = new LineStream(bais);
		String line = lineStream.readLine();
		assertEquals(theLine, line);
	}

	@Test
	public void multipleLines_Once() {
		String hello = "Hello\n";
		String world = "World\n";;
		String theLine = hello + world;
		
		ByteArrayInputStream bais = new ByteArrayInputStream(theLine.getBytes());
		LineStream lineStream = new LineStream(bais);
		String line = lineStream.readLine();
		assertEquals(hello, line);
		line = lineStream.readLine();
		assertEquals(world, line);
	}

	@Test
	public void multipleLines() {
		String hello  = "Hello\n";
		String world  = "World\n";
		String one    = "one\n";
		String two    = "two\n";
		String three  = "three\n";
		String four   = "four\n";
		String theLine = hello + world + one + two + three + four;
		
		ByteArrayInputStream bais = new ByteArrayInputStream(theLine.getBytes());
		LineStream lineStream = new LineStream(bais);
		String line = lineStream.readLine();
		assertEquals(hello, line);
		line = lineStream.readLine();
		assertEquals(world, line);
		line = lineStream.readLine();
		assertEquals(one, line);
		line = lineStream.readLine();
		assertEquals(two, line);
		line = lineStream.readLine();
		assertEquals(three, line);
		line = lineStream.readLine();
		assertEquals(four, line);
	}

	@Test
	public void lastNonLineFinishWithNoWait() {
		String theLine = "Hello";
		
		ByteArrayInputStream bais = new ByteArrayInputStream(theLine.getBytes());
		LineStream lineStream = new LineStream(bais);
		long startTime = System.currentTimeMillis();
		String line = lineStream.readLine();
		assertEquals(theLine, line);
		System.out.println("---: " + (System.currentTimeMillis() - startTime));
		long time = System.currentTimeMillis() - startTime;
		assertEquals(0, time/100);
	}

	@Test
	public void delayCompleteLineStream() {
		String hello  = "Hello\n";
		String world  = "World\n";
		String one    = "one\n";
		String last   = "last\n";
		final String whole = hello + world + one +last;
		
		InputStream inStream = new InputStream() {
			private int i = 0;
			@Override
			public int read() throws IOException {
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				try {
					return whole.charAt(i++);
				} catch (StringIndexOutOfBoundsException e) {
					return -1;
				}
			}
		};
		
		
		LineStream lineStream = new LineStream(inStream);
		long startTime = System.currentTimeMillis();
		System.out.println("Start" + " -- " + System.currentTimeMillis());
		
		String line;
		long time;
		
		line = lineStream.readLine();
		assertEquals(hello, line);
		System.out.println("---: " + (System.currentTimeMillis() - startTime));
		time = System.currentTimeMillis() - startTime;
		assertTrue(0 != time/100);
		
		startTime = System.currentTimeMillis();
		
		line = lineStream.readLine();
		assertEquals(world, line);
		System.out.println("---: " + (System.currentTimeMillis() - startTime));
		time = System.currentTimeMillis() - startTime;
		assertEquals(0, time/100);
		
		line = lineStream.readLine();
		assertEquals(one, line);
		System.out.println("---: " + (System.currentTimeMillis() - startTime));
		time = System.currentTimeMillis() - startTime;
		assertEquals(0, time/100);
		
		line = lineStream.readLine();
		assertEquals(last, line);
		System.out.println("---: " + (System.currentTimeMillis() - startTime));
		time = System.currentTimeMillis() - startTime;
		assertEquals(0, time/100);
	}

	@Test
	public void delayUncompleteLineStream() {
		String hello  = "Hello\n";
		String world  = "World\n";
		String one    = "one\n";
		String last   = "last";
		final String whole = hello + world + one +last;
		
		InputStream inStream = new InputStream() {
			private int i = 0;
			@Override
			public int read() throws IOException {
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				try {
					return whole.charAt(i++);
				} catch (StringIndexOutOfBoundsException e) {
					return -1;
				}
			}
		};
		
		
		LineStream lineStream = new LineStream(inStream);
		long startTime = System.currentTimeMillis();
		String line;
		long time;
		
		line = lineStream.readLine();
		assertEquals(hello, line);
		time = System.currentTimeMillis() - startTime;
		assertTrue(0 != time/100);
		
		line = lineStream.readLine();
		assertEquals(world, line);
		time = System.currentTimeMillis() - startTime;
		assertEquals(0, time/100);
		
		line = lineStream.readLine();
		assertEquals(one, line);
		time = System.currentTimeMillis() - startTime;
		assertEquals(0, time/100);
		
		line = lineStream.readLine();
		assertEquals(last, line);
		time = System.currentTimeMillis() - startTime;
		assertEquals(0, time/100);
	}

}
