package dssb.util.process;

import static org.junit.Assert.assertEquals;

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

}
