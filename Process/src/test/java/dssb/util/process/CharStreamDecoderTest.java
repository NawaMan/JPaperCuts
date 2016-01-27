package dssb.util.process;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class CharStreamDecoderTest {

	@Test
	public void full() {
		CharStreamDecoder buffer = new CharStreamDecoder();
		
		String original = "Hello World!";
		String result = new String(buffer.take(original.getBytes()));
		assertEquals(original, result);
		assertTrue(buffer.hasRemainer());
	}

	@Test
	public void half() {
		CharStreamDecoder buffer = new CharStreamDecoder();
		
		String original = "ภาษาไทย";
		byte[] full = original.getBytes();
		byte[] part1 = new byte[full.length / 4];
		byte[] part2 = new byte[full.length / 4];
		byte[] part3 = new byte[full.length - part1.length - part2.length];
		System.arraycopy(full, 0,                           part1, 0, part1.length);
		System.arraycopy(full, part1.length,                part2, 0, part2.length);
		System.arraycopy(full, part1.length + part2.length, part3, 0, part3.length);
		StringBuffer sb = new StringBuffer();
		sb.append(buffer.take(part1));
		sb.append(buffer.take(part2));
		sb.append(buffer.take(part3));
		assertEquals(original, sb.toString());
		assertTrue(buffer.hasRemainer());
	}

}
