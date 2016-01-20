package dssb.util.process;

import static org.junit.Assert.*;

import java.util.Arrays;

import org.junit.Test;

public class AllLineOutputHandlerTest {

	@Test
	public void allLinesAreCollected() {
		AllLineOutputHandler handler = new AllLineOutputHandler();
		handler.handleLine("Line1");
		handler.handleLine("Line2");
		handler.handleLine("Line3");
		assertEquals(Arrays.asList("Line1", "Line2", "Line3"), handler.getList());
		assertEquals(String.format("Line1%nLine2%nLine3%n"), handler.toString());
	}

	@Test
	public void allPartAreIncludedToLastLine() {
		AllLineOutputHandler handler = new AllLineOutputHandler();
		handler.handlePart("Part");
		handler.handlePart("Part");
		handler.handleLine("Line1");
		handler.handlePart("Part");
		assertEquals(Arrays.asList("PartPartLine1", "Part"), handler.getList());
		assertEquals(String.format("PartPartLine1%nPart"), handler.toString());
	}

	@Test
	public void newLineCanBeSpecified() {
		String newLine = "<+++++";
		AllLineOutputHandler handler = new AllLineOutputHandler(newLine);
		handler.handlePart("Part");
		handler.handleLine("Line1");
		handler.handlePart("Part");
		handler.handleLine("Line2");
		assertEquals(Arrays.asList("PartLine1", "PartLine2"), handler.getList());
		assertEquals(
				"PartLine1" + newLine +
				"PartLine2" + newLine,
				handler.toString());
	}

}
