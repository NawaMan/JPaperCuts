package dssb.util.process;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import org.junit.Test;

public class CharStreamDecoderTest {

	@Test
	public void full() {
		CharStreamDecoder buffer = new CharStreamDecoder();
		
		String original = "Hello World!";
		buffer.append(original.getBytes());
		String result = new String(buffer.take());
		assertEquals(original, result);
		assertFalse(buffer.hasRemainer());
	}

	@Test
	public void half() {
		CharStreamDecoder buffer = new CharStreamDecoder(StandardCharsets.UTF_8);
		
		String original = "ภาษาไทย";
		byte[] full = original.getBytes();
		byte[] part1 = new byte[full.length / 4];
		byte[] part2 = new byte[full.length / 4];
		byte[] part3 = new byte[full.length - part1.length - part2.length];
		System.arraycopy(full, 0,                           part1, 0, part1.length);
		System.arraycopy(full, part1.length,                part2, 0, part2.length);
		System.arraycopy(full, part1.length + part2.length, part3, 0, part3.length);
		StringBuffer sb = new StringBuffer();
		
		buffer.append(part1);
		sb.append(buffer.take());
		assertEquals("ภ", sb.toString());
		
		buffer.append(part2);
		sb.append(buffer.take());
		assertEquals("ภาษ", sb.toString());
		
		buffer.append(part3);
		sb.append(buffer.take());
		assertEquals("ภาษาไทย", sb.toString());
		
		assertEquals(original, sb.toString());
		assertFalse(buffer.hasRemainer());
	}

	@Test
	public void parts() {
		CharStreamDecoder decoder = new CharStreamDecoder(StandardCharsets.UTF_8);
		decoder.append(new byte[] { -32, -72, -124, -32 });
		assertEquals("[ค]", Arrays.toString(decoder.take()));
		assertEquals(
				"After take(): { 'remainder': 1, 'leftover': '[-32, -72, -124, -32]' }",
				"After take(): " + decoder.toDetail());
		
		decoder.append((byte)-72);

		System.out.println("Before take(): " + decoder.toDetail());
		decoder.append(new byte[] { -93, -32, -72, -79, -32, -72, -102 });
		System.out.println(Arrays.toString(decoder.take()));
		System.out.println(decoder.toDetail());

	}

}
