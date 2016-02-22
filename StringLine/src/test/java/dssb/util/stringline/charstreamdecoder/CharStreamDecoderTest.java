package dssb.util.stringline.charstreamdecoder;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import org.junit.Test;

import dssb.util.stringline.charstreamdecoder.CharStreamDecoder;

public class CharStreamDecoderTest {

	@Test
	public void absentDefaultCapacity() {
		CharStreamDecoder buffer = new CharStreamDecoder();
		assertEquals(CharStreamDecoder.DEFAULT_CAPACITY, buffer.getCapacity());
	}
	
	@Test
	public void specifyDefaultCapacity() {
		CharStreamDecoder buffer = new CharStreamDecoder(-1);
		assertEquals(CharStreamDecoder.DEFAULT_CAPACITY, buffer.getCapacity());
	}
	
	@Test
	public void specifyCapacity() {
		int theCapacity = 123;
		CharStreamDecoder buffer = new CharStreamDecoder(theCapacity);
		assertEquals(theCapacity, buffer.getCapacity());
	}

	@Test
	public void absentDefaultCharset() {
		CharStreamDecoder buffer = new CharStreamDecoder();
		assertEquals(CharStreamDecoder.DEFAULT_CHARSET, buffer.getCharset());
	}
	
	@Test
	public void specifyDefaultCharset() {
		CharStreamDecoder buffer = new CharStreamDecoder(-1);
		assertEquals(CharStreamDecoder.DEFAULT_CHARSET, buffer.getCharset());
	}
	
	@Test
	public void specifyCharset() {
		Charset theCharset = StandardCharsets.ISO_8859_1;
		CharStreamDecoder buffer = new CharStreamDecoder(theCharset);
		assertEquals(theCharset, buffer.getCharset());
	}
	
	@Test
	public void specifyCapacityCharset() {
		Charset theCharset = StandardCharsets.ISO_8859_1;
		int theCapacity = 123;
		
		CharStreamDecoder buffer = new CharStreamDecoder(theCharset, theCapacity);
		assertEquals(theCharset, buffer.getCharset());
		assertEquals(theCapacity, buffer.getCapacity());
	}
	
	@Test
	public void full() {
		CharStreamDecoder buffer = new CharStreamDecoder();
		
		String original = "Hello World!";
		buffer.append(original.getBytes());
		String result = new String(buffer.take());
		assertEquals(original, result);
		assertFalse(buffer.hasRemainer());
		assertEquals("[]", Arrays.toString(buffer.getRemainingBytes()));
	}

	@Test
	public void half() {
		CharStreamDecoder buffer = new CharStreamDecoder(StandardCharsets.UTF_8);
		
		String original = "ภาษาไทย";
		byte[] full = original.getBytes();
		assertEquals("[-32, -72, -96, -32, -72, -78, -32, -72, -87, -32, -72, -78, -32, -71, -124, -32, -72, -105, -32, -72, -94]", Arrays.toString(full));
		
		byte[] part1 = new byte[full.length / 4];
		byte[] part2 = new byte[full.length / 4];
		byte[] part3 = new byte[full.length - part1.length - part2.length];
		System.arraycopy(full, 0,                           part1, 0, part1.length);
		System.arraycopy(full, part1.length,                part2, 0, part2.length);
		System.arraycopy(full, part1.length + part2.length, part3, 0, part3.length);
		
		assertEquals("[-32, -72, -96, -32, -72]", Arrays.toString(part1));
		assertEquals("[-78, -32, -72, -87, -32]", Arrays.toString(part2));
		assertEquals("[-72, -78, -32, -71, -124, -32, -72, -105, -32, -72, -94]", Arrays.toString(part3));
		
		StringBuffer sb = new StringBuffer();
		
		buffer.append(part1);
		sb.append(buffer.take());
		assertEquals("ภ", sb.toString());
		assertEquals("[-32, -72]", Arrays.toString(buffer.getRemainingBytes()));
		
		buffer.append(part2);
		sb.append(buffer.take());
		assertEquals("ภาษ", sb.toString());
		assertEquals("[-32]", Arrays.toString(buffer.getRemainingBytes()));
		
		buffer.append(part3);
		sb.append(buffer.take());
		assertEquals("ภาษาไทย", sb.toString());
		assertEquals("[]", Arrays.toString(buffer.getRemainingBytes()));
		
		assertEquals(original, sb.toString());
		assertFalse(buffer.hasRemainer());
		assertEquals("[]", Arrays.toString(buffer.getRemainingBytes()));
	}

	@Test
	public void parts() {
		CharStreamDecoder decoder = new CharStreamDecoder(StandardCharsets.UTF_8);
		decoder.append(new byte[] { -32, -72, -124, -32 });
		assertEquals("[ค]", Arrays.toString(decoder.take()));
		assertEquals("[-32]", Arrays.toString(decoder.getRemainingBytes()));
		
		decoder.append((byte)-72);

		decoder.append(new byte[] { -93, -32, -72, -79, -32, -72, -102 });
		assertEquals("[ร, ั, บ]", Arrays.toString(decoder.take()));
		assertEquals("[]", Arrays.toString(decoder.getRemainingBytes()));

	}

}
