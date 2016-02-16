package dssb.util.process;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.Ignore;
import org.junit.Test;

public class CharInputStreamTest {

	@Test
	public void readLessThanAvailable() throws IOException {
		InputStream inStream = new ByteArrayInputStream("Hello".getBytes());
		CharInputStream stream = new CharInputStream(inStream);
		char[] chars = new char[4];
		stream.read(chars, 0, 4);
		
		assertEquals("Hell", new String(chars));
	}

	@Test
	public void readLessThanAvailableThenReadTheRest() throws IOException {
		InputStream inStream = new ByteArrayInputStream("Hello".getBytes());
		CharInputStream stream = new CharInputStream(inStream);
		char[] chars = new char[3];
		
		stream.read(chars, 0, 3);
		assertEquals("Hel", new String(chars));
		
		assertEquals(2, stream.read(chars, 0, 3));
		assertEquals("lo", new String(chars, 0, 2));
	}

	@Test
	public void readMoreThanAvailable() throws IOException {
		InputStream inStream = new ByteArrayInputStream("Hello".getBytes());
		CharInputStream stream = new CharInputStream(inStream);
		char[] chars = new char[10];
		int count = stream.read(chars, 0, 10);
		
		assertEquals("Hello", new String(chars, 0, count));
	}

	@Test
	public void readAll() throws IOException {
		InputStream inStream = new ByteArrayInputStream("Hello".getBytes());
		CharInputStream stream = new CharInputStream(inStream);
		char[] chars = stream.read();
		
		assertEquals("Hello", new String(chars));
	}

	@Test
	public void slowStream() throws IOException {
		final byte[] bytes = "Hello".getBytes();
		InputStream inStream = new InputStream() {
			int index = 0;
			@Override
			public int read() throws IOException {
				try {
					Thread.sleep(20);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				try {
					return bytes[index++];
				} catch (ArrayIndexOutOfBoundsException e) {
					return -1;
				}
			}
		};
		CharInputStream stream = new CharInputStream(inStream);
		char[] chars = stream.read(50);
		
		assertEquals("He", new String(chars));
	}

	@Test
	public void nonLatin_readAll() throws IOException {
		InputStream inStream = new ByteArrayInputStream("ครับ".getBytes());
		CharInputStream stream = new CharInputStream(inStream);
		char[] chars = stream.read();
		
		assertEquals("ครับ", new String(chars));
	}

	@Test
	public void nonLatin_slow() throws IOException {
		final byte[] bytes = "ครับ".getBytes();
		System.out.println("First: " + Arrays.toString("ครับ".toCharArray()));
		System.out.println("First: " + Arrays.toString("ครับ".getBytes()));

		InputStream inStream = new InputStream() {
			int index = 0;
			@Override
			public int read() throws IOException {
				try {
					Thread.sleep(10);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				
				try {
					return bytes[index++];
				} catch (ArrayIndexOutOfBoundsException e) {
					return -1;
				}
			}
		};
		
		CharInputStream stream = new CharInputStream(inStream);
		char[] chars = stream.read(50);
		
		assertEquals("ค", new String(chars));
		assertEquals(1, chars.length);
		
		chars = stream.read();
		assertEquals("รับ", new String(chars));
	}

}

