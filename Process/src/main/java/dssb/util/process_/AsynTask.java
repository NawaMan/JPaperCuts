package dssb.util.process_;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class AsynTask<R, T extends Throwable> {
	
	private volatile R result = null;
	
	private volatile T throwable = null;
	
	private AtomicBoolean isDone = new AtomicBoolean(false);
	
	private List<AsynComplete<R, T>> listeners = new ArrayList<>();
	
	private Thread thread;
	
	protected AsynTask() {
		this.thread = new Thread() {
			public void run() {
				AsynTask.this.run();
			}
		};
	}
	
	public void start() {
		this.thread.start();
	}
	
	protected abstract void run();
	
	protected final void onComplete(R result, T throwable) {
		if (isDone.compareAndSet(false, true)) {
			this.result    = result;
			this.throwable = throwable;
		}
		synchronized (this) {
			if (isDone.get()) {
				for (AsynComplete<R, T> complete : listeners) {
					notifyCompletion(complete);
				}
			}
		}
	}
	
	public final void whenComplete(AsynComplete<R, T> complete) {
		if (complete == null) {
			return;
		}
		
		if (isDone.get()) {
			notifyCompletion(complete);
		}
		synchronized (this) {
			if (!isDone.get()) {
				listeners.add(complete);
			}
		}
	}

	private void notifyCompletion(AsynComplete<R, T> complete) {
		try {
			complete.onComplete(result, throwable);
		} catch(Throwable problem) {
			// This block is intentionally left black
		}
	}
	
}