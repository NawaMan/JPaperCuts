package dssb.util.process_;

public class ProcessOutput {
	
	private final int exitCode;
	
	private final String output;
	
	private final String error;
	
	public ProcessOutput(
			int exitCode,
			String output,
			String error) {
		this.exitCode = exitCode;
		this.output   = output;
		this.error    = error;
	}

	public int getExitCode() {
		return exitCode;
	}

	public String getOutput() {
		return output;
	}

	public String getError() {
		return error;
	}
	
}
