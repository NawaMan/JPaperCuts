package dssb.util.process;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.StandardCharsets;

import org.junit.Test;

public class CharacterStreamTest {

	private ByteArrayInputStream streamOf(String hello) {
		ByteArrayInputStream byteInputStream = new ByteArrayInputStream(hello.getBytes());
		return byteInputStream;
	}
	
	@Test
	public void emptyInputStream() throws IOException {
		String text = "";
		char[] chars;
		
		CharacterStream charStream = new CharacterStream(streamOf(text));
		
		chars = new char[10];
		assertEquals(0, charStream.read(chars, 0, 10));
		assertEquals(text, new String(chars, 0, 0));
	}
	
	@Test
	public void textSmallerThanBuffer() throws IOException {
		String text = "01234";
		char[] chars;
		
		CharacterStream charStream = new CharacterStream(streamOf(text));
		
		chars = new char[10];
		assertEquals(5, charStream.read(chars, 0, 10));
		assertEquals(text, new String(chars, 0, 5));
	}
	
	@Test
	public void textSameSizeAsBuffer() throws IOException {
		String text = "0123456789";
		char[] chars;
		
		CharacterStream charStream = new CharacterStream(streamOf(text));
		
		chars = new char[10];
		assertEquals(10, charStream.read(chars, 0, 10));
		assertEquals(text, new String(chars, 0, 10));
	}
	
	@Test
	public void textBiggerSizeAsBuffer() throws IOException {
		String text = "0123456789ABCDE";
		char[] chars;
		
		CharacterStream charStream = new CharacterStream(streamOf(text));
		
		chars = new char[10];
		assertEquals(10, charStream.read(chars, 0, 10));
		assertEquals(text.substring(0, 10), new String(chars, 0, 10));
		
		chars = new char[10];
		assertEquals(5, charStream.read(chars, 0, 10));
		assertEquals(text.substring(10, 10 + 5), new String(chars, 0, 5));
	}

}
