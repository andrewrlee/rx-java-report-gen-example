package uk.co.optimisticpanda.reportgen;

import java.util.concurrent.Callable;

import com.google.common.base.Throwables;

public class Utils {

	
	public static <T> T propagateAnyError(Callable<T> callable) {
		try {
			return callable.call();
		} catch (Exception e) {
			throw Throwables.propagate(e);
		}
	}

	public static void propagateAnyError(ThrowingRunnable runnable) {
		try {
			runnable.run();
		} catch (Exception e) {
			throw Throwables.propagate(e);
		}
	}

	
	public interface ThrowingRunnable {
		void run() throws Exception;
	}
	
}
