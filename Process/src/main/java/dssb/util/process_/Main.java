package dssb.util.process_;

import java.io.IOException;

public class Main {

	public static void main(String... args) throws IOException, InterruptedException {
		System.out.println("Hello!!!");

		final String[] command = {
				"/usr/lib/jvm/java-8-oracle/bin/java",
				"-Dfile.encoding=UTF-8",
				"-classpath",
				"/home/dssb/git/DirectInterceptor/Examples/Log/LogExampleApplication/target/classes:"+
				"/home/dssb/.m2/repository/net/bytebuddy/byte-buddy/0.7.7/byte-buddy-0.7.7.jar:"+
				"/home/dssb/git/DirectInterceptor/Handler/target/classes:"+
				"/home/dssb/git/DirectInterceptor/Examples/Log/LogExampleCore/target/classes:"+
				"/home/dssb/.m2/repository/junit/junit/4.12/junit-4.12.jar:"+
				"/home/dssb/.m2/repository/org/hamcrest/hamcrest-core/1.3/hamcrest-core-1.3.jar:"+
				"/home/dssb/git/DirectInterceptor/Agent/target/classes:"+
				"/usr/lib/jvm/java-8-oracle/lib/tools.jar",
				"di.example.log.app.DynamicMain",
				"--with-agent"
		};
		
		String workingDir = "/home/dssb";
		
		Process process = new Process(workingDir, command);
		process.start();
		process.whenComplete(new AsynComplete<ProcessOutput, Throwable>() {
			@Override
			public void onComplete(ProcessOutput result, Throwable throwable) {
				if (result != null) {
					System.out.println("OUT: " + result.getOutput());
					System.out.println("ERR: " + result.getError());
					System.out.println("COD: " + result.getExitCode());
				} else {
					throwable.printStackTrace();
				}
			}
		});
	}
	
}
