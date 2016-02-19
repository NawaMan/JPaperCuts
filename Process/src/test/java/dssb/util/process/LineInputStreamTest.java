package dssb.util.process;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.junit.Test;

public class LineInputStreamTest {

	@Test
	public void readSimpleLine() throws IOException {
		String orgText = "Hello";
		String orgLine = orgText + "\n";
		
		InputStream source = new ByteArrayInputStream(orgLine.getBytes());
		LineInputStream inStream = new LineInputStream(source);
		
		assertEquals(orgText, inStream.readLine());
	}

	@Test
	public void readTwoLines() throws IOException {
		String orgText1 = "Hello";
		String orgText2 = "There";
		String orgLines = orgText1 + "\n" + orgText2 + "\n";
		
		InputStream source = new ByteArrayInputStream(orgLines.getBytes());
		LineInputStream inStream = new LineInputStream(source);
		
		assertEquals(orgText1, inStream.readLine());
		assertEquals(orgText2, inStream.readLine());
	}

	@Test
	public void readTwoLines_withLeftover() throws IOException {
		String orgText1 = "Hello";
		String orgText2 = "There";
		String orgText3 = "!!!";
		String orgLines = orgText1 + "\n" + orgText2 + "\n" + orgText3;
		
		InputStream source = new ByteArrayInputStream(orgLines.getBytes());
		LineInputStream inStream = new LineInputStream(source);
		
		assertEquals(orgText1, inStream.readLine());
		assertEquals(orgText2, inStream.readLine());
		assertEquals(null,     inStream.readLine());
		assertEquals(orgText3, inStream.getLeftOver());
	}

	@Test
	public void readTwoLines_CR() throws IOException {
		String orgText1 = "Hello";
		String orgText2 = "There";
		String orgLines = orgText1 + "\r" + orgText2 + "\r";
		
		InputStream source = new ByteArrayInputStream(orgLines.getBytes());
		LineInputStream inStream = new LineInputStream(source);
		
		assertEquals(orgText1, inStream.readLine());
		assertEquals(orgText2, inStream.readLine());
	}

}
