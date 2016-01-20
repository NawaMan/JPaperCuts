package dssb.util.process;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.io.IOException;

import org.junit.Test;

public class ProcessTest {
	
	private static final String OS = System.getProperty("os.name").toLowerCase();
	
	private static final boolean isWindows() {
		return (OS.indexOf("win") >= 0);
	}
	
	private static final String[] commands = isWindows()
			? new String[] { "", }
			: new String[] { "java", "-version"};

	@Test
	public void prcessCanReturnExitCode() throws IOException, InterruptedException {
		Process process = new Process(".", commands);
		assertEquals(0, process.run());
	}

	@Test
	public void prcessCanReturnLineOutput() throws IOException, InterruptedException {
		AllLineOutputHandler handler = new AllLineOutputHandler();
		// TODO - Find a better way to prove this.
		Process process = new Process(".", new String[] { "bash", "-c", "ls" }, handler);
		assertEquals(0, process.run());
		assertFalse("".equals(handler.toString()));
	}

}
