package dssb.util.process;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

public class Process {
	
	private final String   workingDir;
	private final String[] command;
	
	private final Object outHandler;
	
	public Process(String workingDir, String[] command) {
		this(workingDir, command, null);
	}
	
	public Process(String workingDir, String[] command, LineOutputHandler outHandler) {
		this.workingDir = workingDir;
		this.command = command;
		this.outHandler = outHandler;
	}
	
	public int run() throws IOException, InterruptedException {
		ProcessBuilder proBuilder = new ProcessBuilder(command);
		proBuilder.directory(new File(workingDir));
		
		final java.lang.Process process = proBuilder.start();
		
		if (this.outHandler instanceof LineOutputHandler) {
			final LineOutputHandler handler = (LineOutputHandler)this.outHandler;
			
			Thread thread = prepareLineThread(process, handler);
			thread.start();
		}
		
		int exitCode = process.waitFor();
		return exitCode;
	}

	private Thread prepareLineThread(final java.lang.Process process, final LineOutputHandler out) {
		return new Thread(new Runnable() {
			@Override
			public void run() {
				InputStreamReader reader  = new InputStreamReader(process.getInputStream());
				BufferedReader    bufferd = new BufferedReader(reader);
				
				while (true) {
					String line = null;
					try {
						line = bufferd.readLine();
					} catch (IOException e) {
						// TODO - Deal with this
						e.printStackTrace();
					}
					if (line == null) {
						break;
					}
					
					out.handleLine(line);
				}
			}
		});
	}
	
}
