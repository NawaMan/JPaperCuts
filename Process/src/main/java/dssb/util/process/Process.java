package dssb.util.process;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class Process {
	
	private final String   workingDir;
	private final String[] command;
	
	private final Object outHandler;
	
	private final Object errHandler;
	
	public Process(String workingDir, String[] command) {
		this(workingDir, command, null);
	}
	
	public Process(String workingDir, String[] command, LineOutputHandler outHandler) {
		this(workingDir, command, outHandler, null);
	}
	
	public Process(String workingDir, String[] command, LineOutputHandler outHandler, LineOutputHandler errHandler) {
		this.workingDir = workingDir;
		this.command = command;
		this.outHandler = outHandler;
		this.errHandler = errHandler;
	}
	
	public int run() throws IOException, InterruptedException {
		ProcessBuilder proBuilder = new ProcessBuilder(command);
		proBuilder.directory(new File(workingDir));
		
		final java.lang.Process process = proBuilder.start();
		
		InputStream[] inStreams = new InputStream[] { process.getInputStream(), process.getErrorStream() };
		Object[]      handlers  = new Object[]      { outHandler,               errHandler               };
		
		for (int i = 0; i < 2; i++) {
			InputStream inStream = (i == 0) ? process.getInputStream() : process.getErrorStream();
			Object      handler  = (i == 0) ? outHandler               : errHandler;
			if (handler instanceof LineOutputHandler) {
				final LineOutputHandler lineHandler = (LineOutputHandler)handler;
				Thread thread = prepareLineThread(inStream, lineHandler);
				thread.start();
			}
		}
		
		int exitCode = process.waitFor();
		return exitCode;
	}

	private Thread prepareLineThread(final InputStream inStream, final LineOutputHandler out) {
		return new Thread(new Runnable() {
			@Override
			public void run() {
				InputStreamReader reader  = new InputStreamReader(inStream);
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
