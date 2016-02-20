package dssb.util.process_;

public interface AsynComplete<R, T extends Throwable> {
	
	public void onComplete(R result, T throwable);
	
}