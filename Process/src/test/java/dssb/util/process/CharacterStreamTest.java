package dssb.util.process;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;

import org.junit.Test;

public class CharacterStreamTest {

	private ByteArrayInputStream streamOf(String hello) {
		ByteArrayInputStream byteInputStream = new ByteArrayInputStream(hello.getBytes());
		return byteInputStream;
	}
	
	@Test
	public void emptyInputStream() throws Exception {
		assertReadTexts("", 0);
	}
	
	@Test
	public void textSmallerThanBuffer() throws Exception {
		assertReadTexts("01234", 5);
	}
	
	@Test
	public void textSameSizeAsBuffer() throws Exception {
		assertReadTexts("0123456789", 10);
	}
	
	@Test
	public void textBiggerSizeAsBuffer() throws Exception {
		assertReadTexts("0123456789ABCDE", 10, 5);
	}
	
	@Test
	public void textBiggerSizeAsBuffer_withDelay() throws Exception {
		assertReadTextsBy10(500, "0123456789ABCDE", 10, 5);
	}
	
	@Test
	public void textMuchBiggerSizeAsBuffer() throws Exception {
		assertReadTexts("0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ", 10, 10, 10, 6);
	}
	
	@Test
	public void textMuchBiggerSizeAsBuffer_withDelay() throws Exception {
		assertReadTextsBy10(500, "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ", 10, 10, 10, 6);
	}
	
	@Test
	public void nonLatin_small() throws Exception {
		assertReadTexts("งง", 2);
	}

	private void assertReadTexts(String text, int ... lengths) throws Exception {
		assertReadTextsBy10(0, text, lengths);
	}
	
	private void assertReadTextsBy10(long delay, String text, int ... lengths) throws Exception {
		CharacterStream charStream = new CharacterStream(streamOf(text));
		
		int offset = 0;
		for (int i = 0; i < lengths.length; i++) {
			int    length = lengths[i];
			char[] chars  = new char[10];
			String label  = "#" + i + ": ";
			int    count  = charStream.read(chars, 0, 10);
			assertEquals(label, text.substring(offset, offset + length), new String(chars, 0, length));
			assertEquals(label, length, count);
			offset += length;
			Thread.sleep(delay);
		}
	}

}
