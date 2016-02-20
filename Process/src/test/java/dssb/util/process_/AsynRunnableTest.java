package dssb.util.process_;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.junit.Assert;
import org.junit.Test;

import dssb.util.process_.AsynComplete;
import dssb.util.process_.AsynTask;

public class AsynRunnableTest {

	@Test
	public void test() throws InterruptedException {
		final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		final PrintStream out = new PrintStream(buffer);
		
		AsynTask<String, Throwable> runnable = new AsynTask<String, Throwable>(){
			protected void run() {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				onComplete("Hello!", null);
			}
		};
		runnable.start();
		
		runnable.whenComplete(new AsynComplete<String, Throwable>() {
			@Override
			public void onComplete(String result, Throwable throwable) {
				out.println("2: " + result);
				if (throwable != null) {
					throwable.printStackTrace();
				}
			}
		});
		
		Thread.sleep(100);
		out.println("1");
		Thread.sleep(2000);
		out.println("3");

		runnable.whenComplete(new AsynComplete<String, Throwable>() {
			@Override
			public void onComplete(String result, Throwable throwable) {
				out.println("4: " + result);
				if (throwable != null) {
					throwable.printStackTrace();
				}
			}
		});
		
		Assert.assertEquals(
				"1\n" +
				"2: Hello!\n" +
				"3\n" +
				"4: Hello!\n",
				buffer.toString());
	}

}
