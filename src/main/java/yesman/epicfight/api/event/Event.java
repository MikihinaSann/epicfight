package yesman.epicfight.api.event;

import java.util.TreeMap;

import yesman.epicfight.api.client.event.EpicFightClientEvents;
import yesman.epicfight.api.event.subscriptions.PassiveEventSubscription;

/**
 * This class handles event subscription in Epic Fight API, inspired by
 * Forge/NeoForge's Event, and Fabric's Callback/Event
 * <p>
 * Note this object isn't created each time a event is occur. it only
 * defines event type and its subscriptions
 * <p>
 * To create custom evens, follow these codebase: {@link EpicFightEvents}
 * and {@link EpicFightClientEvents} for client-side only events
 */
public class Event<T extends EventInstance> {
	/**
	 * Treemap to order subscribers in descending order
	 */
	final TreeMap<Integer, EventSubscriber<T>> subscriptions = new TreeMap<> ((i1, i2) -> Integer.compare(i2, i1));
	
	/**
	 * Executes the subscribers' task by their priorities
	 * @return whether the event is canceled. Always returns false since the event is not cancelable
	 */
	public boolean post(T eventInstance) {
		EventContext eventContext = new EventContext();
		
		for (EventSubscriber<T> subscriber : this.subscriptions.values()) {
			eventContext.subscriptionStart(subscriber.name());
			
			if (subscriber.subscription() instanceof PassiveEventSubscription<T> passiveSubscription) {
				passiveSubscription.fire(eventInstance);
				eventContext.onCalled();
			}
		}
		
		eventContext.subscriptionEnd();
		
		return false;
	}
	
	/**
	 * Register an event with default name and priority
	 */
	public void registerPassiveEvent(PassiveEventSubscription<T> subscription) {
		this.registerPassiveEvent(subscription, getDefaultSubscriberName(), 0);
	}
	
	/**
	 * Register an event with default name
	 * @param priority determines the order of the event in descending order
	 */
	public void registerPassiveEvent(PassiveEventSubscription<T> subscription, int priority) {
		this.registerPassiveEvent(subscription, getDefaultSubscriberName(), priority);
	}
	
	/**
	 * Register an event with default priority
	 * @param name you can specify the subscriber name to be referenced by other events, it will be stored
	 * 			   at {@link EventContext}
	 */
	public void registerPassiveEvent(PassiveEventSubscription<T> subscription, String name) {
		this.registerPassiveEvent(subscription, name, 0);
	}
	
	/**
	 * Register an event with full parameters
	 */
	public void registerPassiveEvent(PassiveEventSubscription<T> subscription, String name, int priority) {
		this.subscriptions.put(priority, new EventSubscriber<>(name, subscription));
	}
	
	/**
	 * Returns a class name who called register_Event methods
	 */
	protected static String getDefaultSubscriberName() {
		StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
        StackTraceElement caller = stackTraceElements[2];
        return caller.getClassName();
	}
	
	/**
	 * Defines a default event type
	 */
	public static <T extends EventInstance> Event<T> createEvent() {
		return new Event<> ();
	}
}
