package bgu.spl.mics;

import java.util.concurrent.TimeUnit;

/**
 * A Future object represents a promised result - an object that will
 * eventually be resolved to hold a result of some operation. The class allows
 * Retrieving the result once it is available.
 * 
 * Only private methods may be added to this class.
 * No public constructor is allowed except for the empty constructor.
 */
public class Future<T> {
	private boolean completed;
	private T result;
	
	/**
	 * This should be the the only public constructor in this class.
	 */
	public Future() {
		completed = false;
		result = null;
	}
	
	/**
     * retrieves the result the Future object holds if it has been resolved.
     * This is a blocking method! It waits for the computation in case it has
     * not been completed.
     * <p>
     * @return return the result of type T if it is available, if not wait until it is available.
     * 	       
     */
	//@PRE: none
	//@POST: trivial
	public T get() {
		synchronized (this) {
			if (!completed) {
				try {
					wait();
				} catch (InterruptedException e) {}
			}
		}
		return result;
	}
	
	/**
     * Resolves the result of this Future object.
     */
	//@PRE: future.isDone() == false
	//@POST: future.isDone() == true
	public synchronized void resolve (T result) {
		this.result = result;
		completed = true;
		this.notifyAll();
	}
	
	/**
     * @return true if this object has been resolved, false otherwise
     */

	/**
	 *
	@PRE: none
	@POST: trivial
	 */
	public boolean isDone() {
		return completed;
	}
	
	/**
     * retrieves the result the Future object holds if it has been resolved,
     * This method is non-blocking, it has a limited amount of time determined
     * by {@code timeout}
     * <p>
     * @param timeout 	the maximal amount of time units to wait for the result.
     * @param unit		the {@link TimeUnit} time units to wait.
     * @return return the result of type T if it is available, if not, 
     * 	       wait for {@code timeout} TimeUnits {@code unit}. If time has
     *         elapsed, return null.
     */
	//@PRE: none
	//@POST: trivial
	public synchronized T get(long timeout, TimeUnit unit) {
		if (!completed) {
			try {
				unit.timedWait(this,timeout);
			} catch (InterruptedException e) {}
		}
		return result;
	}

}
