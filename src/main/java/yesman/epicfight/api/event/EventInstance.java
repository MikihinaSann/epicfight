package yesman.epicfight.api.event;

import org.jetbrains.annotations.ApiStatus;

import yesman.epicfight.api.client.camera.EpicFightCameraAPI;
import yesman.epicfight.api.event.subscriptions.ContextAwareEventSubscription;

/**
 * When {@link Event} is fired, the typed event instance should be created. Refer to
 * {@link EpicFightCameraAPI#onItemUseEvent} to see the usage
 */
public abstract class EventInstance {
	/**
	 * Holds information about whose called the event by event so far,
	 * and who canceled the event
	 * <p>
	 * the name of subscribers are specified as parameter in {@link Event#registerPassiveevent} and
	 * {@link CancelableEvent#registerCancelableevent} and {@link CancelableEvent#registerContextAwareevent}
	 */
	private EventContext eventContext;
	
	/**
	 * Returns whether the event event canceled
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
	 * Cancels the event event
	 * This method requires the class to inherit {@link CancelableEventInstance} to be used
	 */
	public void cancel() {
		if (!(this instanceof CancelableEventInstance)) {
			throw new IllegalStateException("Unable to cancel a non cancelable event");
		}
		
		this.eventContext.onCanceled();
	}
	
	/**
	 * Initialize {@link EventContext}, which is used by {@link ContextAwareEventSubscription}
	 * only called by {@link Event#post}
	 */
	@ApiStatus.Internal
	public EventContext initEventContext() {
		this.eventContext = new EventContext();
		return this.eventContext;
	}
}
