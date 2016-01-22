package dssb.util.process;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.io.ByteArrayInputStream;

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
		String laKrup = "laKrup\n";
		String one    = "one\n";
		String two    = "two\n";
		String three  = "three\n";
		String four   = "four\n";
		String theLine = hello + world + laKrup + one + two + three + four;
		
		ByteArrayInputStream bais = new ByteArrayInputStream(theLine.getBytes());
		LineStream lineStream = new LineStream(bais);
		String line = lineStream.readLine();
		assertEquals(hello, line);
		line = lineStream.readLine();
		assertEquals(world, line);
		line = lineStream.readLine();
		assertEquals(laKrup, line);
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

}
