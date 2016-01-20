package dssb.util.process;

import java.io.File;
import java.io.IOException;

public class Process {
	
	private final String   workingDir;
	private final String[] command;
	
	public Process(String workingDir, String[] command) {
		this.workingDir = workingDir;
		this.command = command;
	}
	
	public int run() throws IOException, InterruptedException {
		ProcessBuilder proBuilder = new ProcessBuilder(command);
		proBuilder.directory(new File(workingDir));
		
		java.lang.Process process = proBuilder.start();
		
		int exitCode = process.waitFor();
		return exitCode;
	}
	
}
