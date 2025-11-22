package yesman.epicfight.api.hook;

import org.jetbrains.annotations.ApiStatus;

import yesman.epicfight.api.client.camera.EpicFightCameraAPI;
import yesman.epicfight.api.hook.subscriptions.ContextAwareHookSubscription;

/**
 * When {@link Hook} is fired, the typed event instance should be created. Refer to
 * {@link EpicFightCameraAPI#onItemUseHook} to see the usage
 */
public abstract class EventInstance {
	/**
	 * Holds information about whose called the event by hook so far,
	 * and who canceled the event
	 * <p>
	 * the name of subscribers are specified as parameter in {@link Hook#registerPassiveHook} and
	 * {@link CancelableHook#registerCancelableHook} and {@link CancelableHook#registerContextAwareHook}
	 */
	private EventContext eventContext;
	
	/**
	 * Returns whether the event hook canceled
	 * This method requires the class to inherit {@link CancelableEventInstance} to be used property,
	 * or it always returns false
	 */
	public boolean hasCanceled() {
		if (this instanceof CancelableEventInstance) {
			return this.eventContext.isCanceled();
		}
		
		return false;
	}
	
	/**
	 * Cancels the event hook
	 * This method requires the class to inherit {@link CancelableEventInstance} to be used
	 */
	public void cancel() {
		if (!(this instanceof CancelableEventInstance)) {
			throw new IllegalStateException("Unable to cancel a non cancelable hook");
		}
		
		this.eventContext.onCanceled();
	}
	
	/**
	 * Initialize {@link EventContext}, which is used by {@link ContextAwareHookSubscription}
	 * only called by {@link Hook#post}
	 */
	@ApiStatus.Internal
	public EventContext initEventContext() {
		this.eventContext = new EventContext();
		return this.eventContext;
	}
}
