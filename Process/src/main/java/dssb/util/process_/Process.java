package dssb.util.process_;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;

public class Process extends AsynTask<ProcessOutput, Throwable> {
	
	private final String   workingDir;
	private final String[] command;

	public Process(String theWorkingDir, String ... theCommand) throws IOException, InterruptedException {
		this.workingDir = (theWorkingDir != null) ? theWorkingDir : ".";
		
		int length = theCommand.length;
		this.command = new String[length];
		System.arraycopy(theCommand, 0, command, 0, length);
	}

	@Override
	protected void run() {
		ProcessBuilder proBuilder = new ProcessBuilder(command);
		proBuilder.directory(new File(workingDir));
		
		java.lang.Process process;
		try {
			process = proBuilder.start();
		} catch (IOException throwable) {
			onComplete(null, throwable);
			return;
		}

		final AtomicReference<String>    outStr = new AtomicReference<String>(null);
		final AtomicReference<String>    errStr = new AtomicReference<String>(null);
		final AtomicReference<Throwable> outThw = new AtomicReference<Throwable>(null);
		final AtomicReference<Throwable> errThw = new AtomicReference<Throwable>(null);
		
		StreamReadThread outThread = new StreamReadThread(process.getInputStream());
		StreamReadThread errThread = new StreamReadThread(process.getErrorStream());
		
		outThread.whenComplete(new AsynComplete<String, Throwable>() {
			@Override
			public void onComplete(String result, Throwable throwable) {
				outStr.set(result);
				outThw.set(throwable);
			}
		});
		errThread.whenComplete(new AsynComplete<String, Throwable>() {
			@Override
			public void onComplete(String result, Throwable throwable) {
				errStr.set(result);
				errThw.set(throwable);
			}
		});
		
		Throwable     throwable = null;
		ProcessOutput output    = null;
		try {
			int exitCode = process.waitFor();
			output = new ProcessOutput(exitCode, outStr.get(), errStr.get());
		} catch(Throwable t) {
			throwable = t;
		}
		
		onComplete(output, throwable);
	}

}
