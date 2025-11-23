package yesman.epicfight.api.event;

import yesman.epicfight.api.event.subscriptions.CancelableEventSubscription;
import yesman.epicfight.api.event.subscriptions.ContextAwareEventSubscription;
import yesman.epicfight.api.event.subscriptions.PassiveEventSubscription;

/**
 * Event definition for {@link CancelableEventInstance}
 */
public class CancelableEvent<T extends EventInstance & CancelableEventInstance> extends Event<T> {
	/**
	 * Executes the subscribers' task by their priorities
	 * {@link CancelableEventSubscription} will be ignored if the event is canceled
	 * {@link ContextAwareEventSubscription} will ignore the canceled state and fired always, developers must
	 * validate whether fire the event or not by provided {@link EventContext}
	 * 
	 * @return whether the event is canceled
	 */
	@Override
	public boolean post(T eventInstance) {
		EventContext eventContext = eventInstance.initEventContext();
		
		for (EventSubscriber<T> subscriber : this.subscriptions.values()) {
			eventContext.subscriptionStart(subscriber.name());
			
			if (subscriber.subscription() instanceof PassiveEventSubscription<T> passiveSubscription) {
				if (!eventInstance.hasCanceled()) {
					passiveSubscription.fire(eventInstance);
					eventContext.onCalled();
				}
			} else if (subscriber.subscription() instanceof CancelableEventSubscription<T> cancelableSubscription) {
				if (!eventInstance.hasCanceled()) {
					cancelableSubscription.fire(eventInstance);
					eventContext.onCalled();
				}
			} else if (subscriber.subscription() instanceof ContextAwareEventSubscription<T> contextAwareSubscription) {
				contextAwareSubscription.fire(eventInstance, eventContext);
				eventContext.onCalled();
			}
		}
		
		eventContext.subscriptionEnd();
		
		return eventInstance.hasCanceled();
	}
	
	/**
	 * Registers an event with default name and priority
	 */
	public void registerCancelableEvent(CancelableEventSubscription<T> subscription) {
		this.registerCancelableEvent(subscription, getDefaultSubscriberName(), 0);
	}
	
	/**
	 * Registers an event with default name
	 * @param priority determines the order of the event in descending order
	 * 				   if a higher priority event cancels the event, 
	 */
	public void registerCancelableEvent(CancelableEventSubscription<T> subscription, int priority) {
		this.registerCancelableEvent(subscription, getDefaultSubscriberName(), priority);
	}
	
	/**
	 * Registers an event with default priority
	 * @param name you can specify the subscriber name to be referenced by other events, it will be stored
	 * 		  at {@link EventContext}
	 */
	public void registerCancelableEvent(CancelableEventSubscription<T> subscription, String name) {
		this.registerCancelableEvent(subscription, name, 0);
	}
	
	/**
	 * Registers an event with full parameters
	 */
	public void registerCancelableEvent(CancelableEventSubscription<T> subscription, String name, int priority) {
		this.subscriptions.put(priority, new EventSubscriber<>(name, subscription));
	}
	
	/**
	 * Registers an event with default name and priority
	 */
	public void registerContextAwareEvent(ContextAwareEventSubscription<T> subscription) {
		this.registerContextAwareEvent(subscription, getDefaultSubscriberName(), 0);
	}
	
	/**
	 * Registers an event with default name
	 * @param priority determines the order of the event in descending order
	 */
	public void registerContextAwareEvent(ContextAwareEventSubscription<T> subscription, int priority) {
		this.registerContextAwareEvent(subscription, getDefaultSubscriberName(), priority);
	}
	
	/**
	 * Registers an event with default priority
	 * @param name you can specify the subscriber name to be referenced by other events, it will be stored
	 * 		  at {@link EventContext}
	 */
	public void registerContextAwareEvent(ContextAwareEventSubscription<T> subscription, String name) {
		this.registerContextAwareEvent(subscription, name, 0);
	}
	
	/**
	 * Registers an event with full parameters
	 */
	public void registerContextAwareEvent(ContextAwareEventSubscription<T> subscription, String name, int priority) {
		this.subscriptions.put(priority, new EventSubscriber<>(name, subscription));
	}
	
	/**
	 * Defines a cancelable event type
	 */
	public static <T extends EventInstance & CancelableEventInstance> CancelableEvent<T> createCancelableEvent() {
		return new CancelableEvent<> ();
	}
}
