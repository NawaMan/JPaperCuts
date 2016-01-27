package dssb.util.process;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import dssb.util.process.CharStreamDecoder;
import dssb.util.process.CharStreamDecoder.IncommingCharacterListener;

public class CharStreamDecoderTest {

	@Test
	public void full() {
		final StringBuffer sb = new StringBuffer();
		CharStreamDecoder buffer = new CharStreamDecoder(new CharStreamDecoder.IncommingCharacterListener() {
			@Override
			public void on(String str) {
				sb.append(str);
			}
		});
		
		String original = "Hello World!";
		buffer.take(original.getBytes());
		assertEquals(original, sb.toString());
	}

	@Test
	public void half() {
		final StringBuffer sb = new StringBuffer();
		CharStreamDecoder buffer = new CharStreamDecoder(new CharStreamDecoder.IncommingCharacterListener() {
			@Override
			public void on(String str) {
				sb.append(str);
			}
		});
		
		String original = "ภาษาไทย";
		byte[] full = original.getBytes();
		byte[] part1 = new byte[full.length / 4];
		byte[] part2 = new byte[full.length / 4];
		byte[] part3 = new byte[full.length - part1.length - part2.length];
		System.arraycopy(full, 0,                           part1, 0, part1.length);
		System.arraycopy(full, part1.length,                part2, 0, part2.length);
		System.arraycopy(full, part1.length + part2.length, part3, 0, part3.length);
		buffer.take(part1);
		buffer.take(part2);
		buffer.take(part3);
		assertEquals(original, sb.toString());
	}

}
