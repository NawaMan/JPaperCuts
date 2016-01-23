package dssb.util.process;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import org.junit.Test;

public class CharacterStreamTest {

	private ByteArrayInputStream streamOf(String hello) {
		ByteArrayInputStream byteInputStream = new ByteArrayInputStream(hello.getBytes());
		return byteInputStream;
	}
	
	@Test
	public void emptyInputStream() throws IOException {
		assertReadTexts("", 0);
	}
	
	@Test
	public void textSmallerThanBuffer() throws IOException {
		assertReadTexts("01234", 5);
	}
	
	@Test
	public void textSameSizeAsBuffer() throws IOException {
		assertReadTexts("0123456789", 10);
	}
	
	@Test
	public void textBiggerSizeAsBuffer() throws IOException {
		assertReadTexts("0123456789ABCDE", 10, 5);
	}

	private void assertReadTexts(String text, int ... lengths) throws IOException {
		CharacterStream charStream = new CharacterStream(streamOf(text));
		
		int offset = 0;
		for (int length : lengths) {
			char[] chars = new char[10];
			assertEquals(length, charStream.read(chars, 0, 10));
			assertEquals(text.substring(offset, offset + length), new String(chars, 0, length));
			offset += length;
		}
	}

}
