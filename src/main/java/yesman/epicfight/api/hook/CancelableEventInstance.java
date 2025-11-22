package yesman.epicfight.api.hook;

/**
 * An interface for event instances that represents
 * {@link CancelableHook}
 */
public interface CancelableEventInstance {
	/**
	 * Returns whether the hook is cancelled
	 */
	boolean hasCanceled();
	
	/**
	 * Cancel the hook
	 */
	void cancel();
}
