package dssb.util.process;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.junit.Test;

public class LineInputStreamTest {
	
	@Test
	public void unknownNewlineIsNotSupported() throws IOException {
		InputStream source = new ByteArrayInputStream("".getBytes());
		try {
			new LineInputStreamBuilder(source)
			        .newlineType(NewlineType.UNKNOWN)
			        .build();
			fail("Expect an exception!");
		} catch (IllegalArgumentException exception) {
			assertEquals(LineInputStream.UNKNOWN_NOT_SUPPORT, exception.getMessage());
		}
	}
	
	@Test
	public void nullSource() throws IOException {
		try {
			new LineInputStreamBuilder((CharIterator) null)
			        .build();
			fail("Expect an exception!");
		} catch (NullPointerException exception) {
			assertEquals(LineInputStream.NULL_SOURCE, exception.getMessage());
		}
	}
	
	@Test
	public void readNoLine_meansOneline() throws IOException {
		// Given lines.
		String orgText = "Hello";
		InputStream source = new ByteArrayInputStream(orgText.getBytes());
		
		// Read it with LF as a new line.
		LineInputStream inStream = new LineInputStreamBuilder(source)
		        .build();
		
		// We will get the original lines.
		assertEquals(orgText, inStream.readLine());
	}
	
	@Test
	public void readSimpleLine() throws IOException {
		// Given lines.
		String orgText = "Hello";
		
		// Create a text with LF as delimiter
		String orgLine = orgText + "\n";
		InputStream source = new ByteArrayInputStream(orgLine.getBytes());
		
		// Read it with LF as a new line.
		LineInputStream inStream = new LineInputStreamBuilder(source)
		        .linefeed()
		        .build();
		
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
		LineInputStream inStream = new LineInputStreamBuilder(source)
		        .linefeed()
		        .build();
		
		// We will get the original lines.
		assertEquals(orgText1, inStream.readLine());
		assertEquals(orgText2, inStream.readLine());
	}
	
	@Test
	public void readTwoLines_withLeftover_theLeftoverBecomeLastLine() throws IOException {
		// Given lines with a leftover.
		String orgText1 = "Hello";
		String orgText2 = "There";
		String orgText3 = "!!!";
		
		// Create a text with LF as delimiter
		String orgLines = orgText1 + "\n" + orgText2 + "\n" + orgText3;
		InputStream source = new ByteArrayInputStream(orgLines.getBytes());
		
		// Read it with LF as a new line.
		LineInputStream inStream = new LineInputStreamBuilder(source)
		        .linefeed()
		        .build();
		
		// We will get the original lines.
		assertEquals(orgText1, inStream.readLine());
		assertEquals(orgText2, inStream.readLine());
		// ... the left over becomes the last line
		assertEquals(orgText3, inStream.readLine());
		// ... no more left
		assertEquals(null, inStream.readLine());
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
		LineInputStream inStream = new LineInputStreamBuilder(source)
		        .carriageReturn()
		        .build();
		
		// We will get the original lines.
		assertEquals(orgText1, inStream.readLine());
		assertEquals(orgText2, inStream.readLine());
		// ... ensure that is no more line.
		assertEquals(null, inStream.readLine());
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
		LineInputStream inStream = new LineInputStreamBuilder(source)
		        .carriageReturn()
		        .build();
		
		// We will get the original lines with the LFs inside them.
		assertEquals(orgText1, inStream.readLine());
		assertEquals(orgText2, inStream.readLine());
		// ... ensure that is no more line.
		assertEquals(null, inStream.readLine());
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
		LineInputStream inStream = new LineInputStreamBuilder(source)
		        .carriageReturnThenLinefeed()
		        .build();
		
		// We will get the original lines.
		assertEquals(orgText1, inStream.readLine());
		assertEquals(orgText2, inStream.readLine());
		// ... ensure that is no more line.
		assertEquals(null, inStream.readLine());
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
		LineInputStream inStream = new LineInputStreamBuilder(source)
		        .carriageReturnThenLinefeed()
		        .build();
		
		// We will get the original lines.
		assertEquals(orgText1, inStream.readLine());
		assertEquals(orgText2, inStream.readLine());
		// ... ensure that is no more line.
		assertEquals(null, inStream.readLine());
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
		LineInputStream inStream = new LineInputStreamBuilder(source)
		        .toBeDetermined()
		        .build();
		
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
		LineInputStream inStream = new LineInputStreamBuilder(source)
		        .toBeDetermined()
		        .build();
		
		// We will get the original lines with CR in the second line as it was already determined that LF is the
		// newline.
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
		LineInputStream inStream = new LineInputStreamBuilder(source).newlineType(NewlineType.TO_BE_DETERMINED)
		        .build();
		
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
		LineInputStream inStream = new LineInputStreamBuilder(source)
		        .toBeDetermined()
		        .build();
		
		// We will get the original lines with CR in the second line as it was already determined that LF is the
		// newline.
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
		LineInputStream inStream = new LineInputStreamBuilder(source)
		        .toBeDetermined()
		        .build();
		
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
		LineInputStream inStream = new LineInputStreamBuilder(source)
		        .toBeDetermined()
		        .build();
		
		// We will get the original lines with CR in the second line as it was already determined that LF is the
		// newline.
		assertEquals(orgText1, inStream.readLine());
		assertEquals(orgText2, inStream.readLine());
		assertEquals(orgText3, inStream.readLine());
		// This become a newline line input stream
		assertEquals(NewlineType.CARRIAGE_RETURN_LINE_FEED, inStream.getNewlineType());
	}
	
	// == Non Roman ====================================================================================================
	
	@Test
	public void nonLatin() throws IOException {
		// Given lines with LF in the second line and CR in the third line.
		String orgText1 = "สวัสดี";
		String orgText2 = "สบ\rายดี\nไหม";
		String orgText3 = "แล้ว\n\rเจอกัน";
		
		// Create a text with CRLF as delimiter
		String orgLines = orgText1 + "\r\n" + orgText2 + "\r\n" + orgText3 + "\r\n";
		InputStream source = new ByteArrayInputStream(orgLines.getBytes());
		
		// Read it with ToBeDetermine as a new line.
		LineInputStream inStream = new LineInputStreamBuilder(source)
		        .newlineType(NewlineType.TO_BE_DETERMINED)
		        .build();
		
		// We will get the original lines with CR in the second line as it was already determined that LF is the
		// newline.
		assertEquals(orgText1, inStream.readLine());
		assertEquals(orgText2, inStream.readLine());
		assertEquals(orgText3, inStream.readLine());
		// This become a newline line input stream
		assertEquals(NewlineType.CARRIAGE_RETURN_LINE_FEED, inStream.getNewlineType());
	}
	
	// == Timeout ======================================================================================================
	
	@Test
	public void timeout_inTime() throws IOException, InterruptedException, ReadLineTimeoutException {
		// Give a text that is shorter than 5 characters with a newline at the end.
		String text = "123\n";
		
		// Create an input stream that give out a byte every 10 milliseconds.
		final InputStream slowSource = createSlowInputStream(text);
		
		// Read line using a LineInputStream.
		LineInputStream inStream = new LineInputStreamBuilder(slowSource).build();
		
		// Read it for 50 millisecond ... an we should the line in time.
		assertEquals("123", inStream.readLine(50));
	}
	
	@Test
	public void timeout_inTime_dontWaitLong() throws IOException, InterruptedException, ReadLineTimeoutException {
		// Give a text that is shorter than 5 characters with a newline at the end.
		String text = "123\n";
		
		// Create an input stream that give out a byte every 10 milliseconds.
		final InputStream slowSource = createSlowInputStream(text);
		
		// Read line using a LineInputStream.
		LineInputStream inStream = new LineInputStreamBuilder(slowSource).build();
		
		// Read it for 50 millisecond ... an we should the line in time without waiting for timeout.
		long startTime = System.currentTimeMillis();
		assertEquals("123", inStream.readLine(60_000));
		long processTime = System.currentTimeMillis() - startTime;
		assertTrue(processTime < 50);
	}
	
	@Test
	public void timeout_inTime_noLine() throws IOException, InterruptedException, ReadLineTimeoutException {
		// Give a text that is shorter than 5 characters with no newline at the end.
		String text = "123";
		
		// Create an input stream that give out a byte every 10 milliseconds.
		final InputStream slowSource = createSlowInputStream(text);
		
		// Read line using a LineInputStream.
		LineInputStream inStream = new LineInputStreamBuilder(slowSource).build();
		
		// Read it for 50 millisecond ... an we should get the line.
		assertEquals("123", inStream.readLine(50));
	}
	
	@Test
	public void timeout_tooLong_doneBeforeNextCall() throws IOException, InterruptedException {
		// Give a text that is longer than 5 characters with a new line at the end.
		String text = "1234567\n";
		
		// Create an input stream that give out a byte every 10 milliseconds.
		final InputStream slowSource = createSlowInputStream(text);
		
		// Read line using a LineInputStream.
		LineInputStream inStream = new LineInputStreamBuilder(slowSource).build();
		
		// Read it for 50 millisecond ... an we should get part of the line.
		try {
			inStream.readLine(55);
			fail("Expect an exception!");
		} catch (ReadLineTimeoutException exception) {
			assertEquals("12345", exception.getPart());
		}
		
		// Sleep some more to ensure the read was done.
		Thread.sleep(30);
		
		// Read again ... the previous read should be done by now.
		assertEquals("67", inStream.readLine());
		assertEquals(null, inStream.readLine());
	}
	
	@Test
	public void timeout_tooLong_doneBeforeNextReadTimeoutCall() throws IOException, InterruptedException,
	        ReadLineTimeoutException {
		// Give a text that is longer than 5 characters with a new line at the end.
		String text = "1234567\n";
		
		// Create an input stream that give out a byte every 10 milliseconds.
		final InputStream slowSource = createSlowInputStream(text);
		
		// Read line using a LineInputStream.
		LineInputStream inStream = new LineInputStreamBuilder(slowSource).build();
		
		// Read it for 50 millisecond ... an we should get part of the line.
		try {
			inStream.readLine(55);
			fail("Expect an exception!");
		} catch (ReadLineTimeoutException exception) {
			assertEquals("12345", exception.getPart());
		}
		
		// Sleep some more to ensure the read was done.
		Thread.sleep(30);
		
		// Read again ... the previous read should be done by now.
		assertEquals("67", inStream.readLine(100));
		assertEquals(null, inStream.readLine());
	}
	
	@Test
	public void timeout_tooLong_doneWithNextReadAllCall() throws IOException, InterruptedException {
		// Give a text that is longer than 5 characters with a new line at the end.
		String text = "1234567890123456\n";
		
		// Create an input stream that give out a byte every 10 milliseconds.
		final InputStream slowSource = createSlowInputStream(text);
		
		// Read line using a LineInputStream.
		LineInputStream inStream = new LineInputStreamBuilder(slowSource).build();
		
		// Read it for 50 millisecond ... an we should get part of the line.
		try {
			inStream.readLine(55);
			fail("Expect an exception!");
		} catch (ReadLineTimeoutException exception) {
			assertEquals("12345", exception.getPart());
		}
		
		// Read again ... the previous read should NOT be done by now.
		// Since this is a read all, it should be done with in this call.
		assertEquals("67890123456", inStream.readLine());
		assertEquals(null, inStream.readLine());
	}
	
	@Test
	public void timeout_tooLong_doneWithNextReadTimeoutCall()
	        throws IOException, InterruptedException, ReadLineTimeoutException {
		// Give a text that is longer than 5 characters with a new line at the end.
		String text = "12345678\n";
		
		// Create an input stream that give out a byte every 10 milliseconds.
		final InputStream slowSource = createSlowInputStream(text);
		
		// Read line using a LineInputStream.
		LineInputStream inStream = new LineInputStreamBuilder(slowSource).build();
		
		// Read it for 50 millisecond ... an we should get part of the line.
		try {
			inStream.readLine(55);
			fail("Expect an exception!");
		} catch (ReadLineTimeoutException exception) {
			assertEquals("12345", exception.getPart());
		}
		
		// Read again ... the previous read should NOT be done by now.
		assertEquals("678", inStream.readLine(100));
		assertEquals(null, inStream.readLine());
	}
	
	@Test
	public void timeout_tooLong_doneWithThreeNextReadTimeoutCall()
	        throws IOException, InterruptedException, ReadLineTimeoutException {
		// Give a text that is longer than 5 characters with a new line at the end.
		String text = "12345678\n";
		
		// Create an input stream that give out a byte every 10 milliseconds.
		final InputStream slowSource = createSlowInputStream(text);
		
		// Read line using a LineInputStream.
		LineInputStream inStream = new LineInputStreamBuilder(slowSource).build();
		
		// Read it for 50 millisecond ... an we should get part of the line.
		try {
			inStream.readLine(55);
			fail("Expect an exception!");
		} catch (ReadLineTimeoutException exception) {
			assertEquals("12345", exception.getPart());
		}
		
		// Read again ... get one more character but still not a line.
		try {
			inStream.readLine(10);
			fail("Expect an exception!");
		} catch (ReadLineTimeoutException exception) {
			assertEquals("6", exception.getPart());
		}
		
		// Read again ... get one more character but still not a line.
		try {
			inStream.readLine(10);
			fail("Expect an exception!");
		} catch (ReadLineTimeoutException exception) {
			assertEquals("7", exception.getPart());
		}
		
		// Read again ... the previous read should be done by now so get right away.
		long startTime = System.currentTimeMillis();
		assertEquals("8", inStream.readLine(60_0000));
		assertEquals(null, inStream.readLine());
		long processTime = System.currentTimeMillis() - startTime;
		assertTrue(processTime < 50);
	}
	
	@Test
	public void timeout_runtimeException()
	        throws IOException, InterruptedException, ReadLineTimeoutException {
		// Give a runtime exception.
		final RuntimeException theException = new RuntimeException();
		
		// Create an input stream that give out a byte every 10 milliseconds.
		final InputStream slowSource = new InputStream() {
			@Override
			public int read() throws IOException {
				try {
					Thread.sleep(25);
				} catch (InterruptedException e) {
					// Blank
				}
				throw theException;
			}
		};
		
		// Read line using a LineInputStream.
		LineInputStream inStream = new LineInputStreamBuilder(slowSource).build();
		
		// Read it for 50 millisecond
		try {
			inStream.readLine(50);
			fail("Expect an exception!");
		} catch (RuntimeException exception) {
			// Expect the exception
			assertEquals(theException, exception);
		}
	}
	
	@Test
	public void timeout_ioException()
	        throws IOException, InterruptedException, ReadLineTimeoutException {
		// Give an IO exception.
		final IOException theException = new IOException();
		
		// Create an input stream that give out a byte every 10 milliseconds.
		final InputStream slowSource = new InputStream() {
			@Override
			public int read() throws IOException {
				try {
					Thread.sleep(25);
				} catch (InterruptedException e) {
					// Blank
				}
				throw theException;
			}
		};
		
		// Read line using a LineInputStream.
		LineInputStream inStream = new LineInputStreamBuilder(slowSource).build();
		
		// Read it for 50 millisecond
		try {
			inStream.readLine(50);
			fail("Expect an exception!");
		} catch (IOException exception) {
			// Expect the exception
			assertEquals(theException, exception);
		}
	}
	
	private InputStream createSlowInputStream(String text) {
		final InputStream source = new ByteArrayInputStream(text.getBytes());
		final InputStream slowSource = new InputStream() {
			@Override
			public int read() throws IOException {
				try {
					Thread.sleep(10);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				return source.read();
			}
		};
		return slowSource;
	}
	
}
