package yesman.epicfight.api.hook;

import java.util.TreeMap;

import yesman.epicfight.api.client.hook.EpicFightClientHooks;
import yesman.epicfight.api.hook.subscriptions.PassiveHookSubscription;

/**
 * This class handles event subscription in Epic Fight API, inspired by
 * Forge/NeoForge's Event, and Fabric's Callback/Event
 * <p>
 * Note this object isn't created each time a event is occur. it only
 * defines event type and its subscriptions
 * <p>
 * To create custom hook, follow these codebase: {@link EpicFightHooks}
 * and {@link EpicFightClientHooks} for client-side only events
 */
public class Hook<T extends EventInstance> {
	/**
	 * Treemap to order subscribers in descending order
	 */
	final TreeMap<Integer, HookSubscriber<T>> subscriptions = new TreeMap<> ((i1, i2) -> Integer.compare(i2, i1));
	
	/**
	 * Executes the subscribers' task by their priorities
	 * @return whether the hook is canceled. Always returns false since the hook is not cancelable
	 */
	public boolean post(T eventInstance) {
		EventContext hookContext = new EventContext();
		
		for (HookSubscriber<T> subscriber : this.subscriptions.values()) {
			hookContext.subscriptionStart(subscriber.name());
			
			if (subscriber.subscription() instanceof PassiveHookSubscription<T> passiveSubscription) {
				passiveSubscription.fire(eventInstance);
				hookContext.onCalled();
			}
		}
		
		hookContext.subscriptionEnd();
		
		return false;
	}
	
	/**
	 * Register a hook with default name and priority
	 */
	public void registerPassiveHook(PassiveHookSubscription<T> subscription) {
		this.registerPassiveHook(subscription, getDefaultSubscriberName(), 0);
	}
	
	/**
	 * Register a hook with default name
	 * @param priority determines the order of the hook in descending order
	 */
	public void registerPassiveHook(PassiveHookSubscription<T> subscription, int priority) {
		this.registerPassiveHook(subscription, getDefaultSubscriberName(), priority);
	}
	
	/**
	 * Register a hook with default priority
	 * @param name you can specify the subscriber name to be referenced by other hooks, it will be stored
	 * 			   at {@link EventContext}
	 */
	public void registerPassiveHook(PassiveHookSubscription<T> subscription, String name) {
		this.registerPassiveHook(subscription, name, 0);
	}
	
	/**
	 * Register a hook with full parameters
	 */
	public void registerPassiveHook(PassiveHookSubscription<T> subscription, String name, int priority) {
		this.subscriptions.put(priority, new HookSubscriber<>(name, subscription));
	}
	
	/**
	 * Returns a class name who called register_Hook methods
	 */
	protected static String getDefaultSubscriberName() {
		StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
        StackTraceElement caller = stackTraceElements[2];
        return caller.getClassName();
	}
	
	/**
	 * Defines a default hook type
	 */
	public static <T extends EventInstance> Hook<T> createHook() {
		return new Hook<> ();
	}
}
