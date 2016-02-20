package dssb.util.process;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.junit.Test;

import dssb.util.process.LineInputStream.NewlineType;

public class LineInputStreamTest {
	
	@Test
	public void unknownNewlineIsNotSupported() throws IOException {
		InputStream source = new ByteArrayInputStream("".getBytes());
		try {
			new LineInputStream(NewlineType.UNKNOWN, source);
			fail("Expect an exception!");
		} catch(IllegalArgumentException exception) {
			assertEquals(LineInputStream.UNKNOWN_NOT_SUPPORT, exception.getMessage());
		}
	}
	
	@Test
	public void nullSource() throws IOException {
		try {
			new LineInputStream(null);
			fail("Expect an exception!");
		} catch(NullPointerException exception) {
			assertEquals(LineInputStream.NULL_SOURCE, exception.getMessage());
		}
	}
	
	@Test
	public void readSimpleLine() throws IOException {
		// Given lines.
		String orgText = "Hello";
		
		// Create a text with LF as delimiter
		String orgLine = orgText + "\n";
		InputStream source = new ByteArrayInputStream(orgLine.getBytes());
		
		// Read it with LF as a new line.
		LineInputStream inStream = new LineInputStream(NewlineType.LINE_FEED, source);
		
		// We will get the original lines.
		assertEquals(orgText, inStream.readLine());
	}
	
	@Test
	public void readTwoLines() throws IOException {
		// Given lines.
		String orgText1 = "Hello";
		String orgText2 = "There";
		
		// Create a text with LF as delimiter
		String orgLines = orgText1 + "\n" + orgText2 + "\n";
		InputStream source = new ByteArrayInputStream(orgLines.getBytes());
		
		// Read it with LF as a new line.
		LineInputStream inStream = new LineInputStream(NewlineType.LINE_FEED, source);
		
		// We will get the original lines.
		assertEquals(orgText1, inStream.readLine());
		assertEquals(orgText2, inStream.readLine());
	}
	
	@Test
	public void readTwoLines_withLeftover() throws IOException {
		// Given lines with a leftover.
		String orgText1 = "Hello";
		String orgText2 = "There";
		String orgText3 = "!!!";
		
		// Create a text with LF as delimiter
		String orgLines = orgText1 + "\n" + orgText2 + "\n" + orgText3;
		InputStream source = new ByteArrayInputStream(orgLines.getBytes());
		
		// Read it with LF as a new line.
		LineInputStream inStream = new LineInputStream(NewlineType.LINE_FEED, source);
		
		// We will get the original lines.
		assertEquals(orgText1, inStream.readLine());
		assertEquals(orgText2, inStream.readLine());
		// ... ensure that is no more line.
		assertEquals(null, inStream.readLine());
		// ... check the left over.
		assertEquals(orgText3, inStream.peekLeftOver());
	}
	
	@Test
	public void readTwoLines_CR() throws IOException {
		// Given lines.
		String orgText1 = "Hello";
		String orgText2 = "There";
		
		// Create a text with CR as delimiter
		String orgLines = orgText1 + "\r" + orgText2 + "\r";
		InputStream source = new ByteArrayInputStream(orgLines.getBytes());
		
		// Read it with CR as a new line.
		LineInputStream inStream = new LineInputStream(NewlineType.CARRIAGE_RETURN, source);
		
		// We will get the original lines.
		assertEquals(orgText1, inStream.readLine());
		assertEquals(orgText2, inStream.readLine());
		// ... ensure that is no more line.
		assertEquals(null, inStream.readLine());
		// ... check that no more the left over.
		assertEquals("", inStream.peekLeftOver());
	}
	
	@Test
	public void readTwoLines_CR_withLF() throws IOException {
		// Given lines with LF in it.
		String orgText1 = "Hel\nlo";
		String orgText2 = "The\nre";
		// Create a text with CR as delimiter
		String orgLines = orgText1 + "\r" + orgText2 + "\r";
		InputStream source = new ByteArrayInputStream(orgLines.getBytes());
		
		// Read it with CR as a new line.
		LineInputStream inStream = new LineInputStream(NewlineType.CARRIAGE_RETURN, source);
		
		// We will get the original lines with the LFs inside them.
		assertEquals(orgText1, inStream.readLine());
		assertEquals(orgText2, inStream.readLine());
		// ... ensure that is no more line.
		assertEquals(null, inStream.readLine());
		// ... check that no more the left over.
		assertEquals("", inStream.peekLeftOver());
	}
	
	@Test
	public void readTwoLines_CRLF() throws IOException {
		// Given lines.
		String orgText1 = "Hello";
		String orgText2 = "There";
		
		// Create a text with CRLF as delimiter
		String orgLines = orgText1 + "\r\n" + orgText2 + "\r\n";
		InputStream source = new ByteArrayInputStream(orgLines.getBytes());
		
		// Read it with CRLF as a new line.
		LineInputStream inStream = new LineInputStream(NewlineType.CARRIAGE_RETURN_LINE_FEED, source);
		
		// We will get the original lines.
		assertEquals(orgText1, inStream.readLine());
		assertEquals(orgText2, inStream.readLine());
		// ... ensure that is no more line.
		assertEquals(null, inStream.readLine());
		// ... check that no more the left over.
		assertEquals("", inStream.peekLeftOver());
	}
	
	@Test
	public void readTwoLines_CRLF_withCR_orLF() throws IOException {
		// Given lines with either CR or LF in it.
		String orgText1 = "He\rl\nlo";
		String orgText2 = "Th\n\rere";
		
		// Create a text with CRLF as delimiter
		String orgLines = orgText1 + "\r\n" + orgText2 + "\r\n";
		InputStream source = new ByteArrayInputStream(orgLines.getBytes());
		
		// Read it with CRLF as a new line.
		LineInputStream inStream = new LineInputStream(NewlineType.CARRIAGE_RETURN_LINE_FEED, source);
		
		// We will get the original lines.
		assertEquals(orgText1, inStream.readLine());
		assertEquals(orgText2, inStream.readLine());
		// ... ensure that is no more line.
		assertEquals(null, inStream.readLine());
		// ... check that no more the left over.
		assertEquals("", inStream.peekLeftOver());
	}
	
	// == To be determined ============================================================================================
	
	@Test
	public void toBeDetermined_LF() throws IOException {
		// Given lines.
		String orgText1 = "Hello";
		String orgText2 = "There";
		
		// Create a text with LF as delimiter
		String orgLines = orgText1 + "\n" + orgText2 + "\n";
		InputStream source = new ByteArrayInputStream(orgLines.getBytes());
		
		// Read it with ToBeDetermine as a new line.
		LineInputStream inStream = new LineInputStream(NewlineType.TO_BE_DETERMINED, source);

		// We will get the original lines.
		assertEquals(orgText1, inStream.readLine());
		assertEquals(orgText2, inStream.readLine());
		// This become a newline line input stream
		assertEquals(NewlineType.LINE_FEED, inStream.getNewlineType());
	}
	
	@Test
	public void toBeDetermined_LF_followedByCR() throws IOException {
		// Given lines with CR in the second line.
		String orgText1 = "Hello";
		String orgText2 = "The\rre";
		
		// Create a text with LF as delimiter
		String orgLines = orgText1 + "\n" + orgText2 + "\n";
		InputStream source = new ByteArrayInputStream(orgLines.getBytes());
		
		// Read it with ToBeDetermine as a new line.
		LineInputStream inStream = new LineInputStream(NewlineType.TO_BE_DETERMINED, source);

		// We will get the original lines with CR in the second line as it was already determined that LF is the newline.
		assertEquals(orgText1, inStream.readLine());
		assertEquals(orgText2, inStream.readLine());
		// This become a newline line input stream
		assertEquals(NewlineType.LINE_FEED, inStream.getNewlineType());
	}

	@Test
	public void toBeDetermined_CR() throws IOException {
		// Given lines.
		String orgText1 = "Hello";
		String orgText2 = "There";
		
		// Create a text with LF as delimiter
		String orgLines = orgText1 + "\r" + orgText2 + "\r";
		InputStream source = new ByteArrayInputStream(orgLines.getBytes());
		
		// Read it with ToBeDetermine as a new line.
		LineInputStream inStream = new LineInputStream(NewlineType.TO_BE_DETERMINED, source);
		
		// We will get the original lines.
		assertEquals(orgText1, inStream.readLine());
		assertEquals(orgText2, inStream.readLine());
		// This become a newline line input stream
		assertEquals(NewlineType.CARRIAGE_RETURN, inStream.getNewlineType());
	}
	
	@Test
	public void toBeDetermined_CR_followedByLF() throws IOException {
		// Given lines with LF in the second line.
		String orgText1 = "Hello";
		String orgText2 = "The\nre";
		
		// Create a text with LF as delimiter
		String orgLines = orgText1 + "\r" + orgText2 + "\r";
		InputStream source = new ByteArrayInputStream(orgLines.getBytes());
		
		// Read it with ToBeDetermine as a new line.
		LineInputStream inStream = new LineInputStream(NewlineType.TO_BE_DETERMINED, source);
		
		// We will get the original lines with CR in the second line as it was already determined that LF is the newline.
		assertEquals(orgText1, inStream.readLine());
		assertEquals(orgText2, inStream.readLine());
		// This become a newline line input stream
		assertEquals(NewlineType.CARRIAGE_RETURN, inStream.getNewlineType());
	}

	@Test
	public void toBeDetermined_CRLF() throws IOException {
		// Given lines.
		String orgText1 = "Hello";
		String orgText2 = "There";
		
		// Create a text with LF as delimiter
		String orgLines = orgText1 + "\r\n" + orgText2 + "\r\n";
		InputStream source = new ByteArrayInputStream(orgLines.getBytes());
		
		// Read it with ToBeDetermine as a new line.
		LineInputStream inStream = new LineInputStream(NewlineType.TO_BE_DETERMINED, source);
		
		// We will get the original lines.
		assertEquals(orgText1, inStream.readLine());
		assertEquals(orgText2, inStream.readLine());
		// This become a newline line input stream
		assertEquals(NewlineType.CARRIAGE_RETURN_LINE_FEED, inStream.getNewlineType());
	}
	
	@Test
	public void toBeDetermined_CRLF_followedByCR_orLF() throws IOException {
		// Given lines with LF in the second line and CR in the third line.
		String orgText1 = "Hello";
		String orgText2 = "The\nre";
		String orgText3 = "The\rre";
		
		// Create a text with CRLF as delimiter
		String orgLines = orgText1 + "\r\n" + orgText2 + "\r\n" + orgText3 + "\r\n";
		InputStream source = new ByteArrayInputStream(orgLines.getBytes());
		
		// Read it with ToBeDetermine as a new line.
		LineInputStream inStream = new LineInputStream(NewlineType.TO_BE_DETERMINED, source);
		
		// We will get the original lines with CR in the second line as it was already determined that LF is the newline.
		assertEquals(orgText1, inStream.readLine());
		assertEquals(orgText2, inStream.readLine());
		assertEquals(orgText3, inStream.readLine());
		// This become a newline line input stream
		assertEquals(NewlineType.CARRIAGE_RETURN_LINE_FEED, inStream.getNewlineType());
	}
	
}
