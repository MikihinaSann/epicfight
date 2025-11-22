package yesman.epicfight.api.hook;

import yesman.epicfight.api.hook.subscriptions.CancelableHookSubscription;
import yesman.epicfight.api.hook.subscriptions.ContextAwareHookSubscription;
import yesman.epicfight.api.hook.subscriptions.PassiveHookSubscription;

/**
 * Hook for {@link CancelableEventInstance}
 */
public class CancelableHook<T extends EventInstance & CancelableEventInstance> extends Hook<T> {
	/**
	 * Executes the subscribers' task by their priorities
	 * CancelableHookSubscription will be ignored if the event is canceled
	 * ContextAwareHookSubscription will ignore the canceled state and fired always, developers must
	 * validate whether fire the event or not by provided {@link EventContext}
	 * 
	 * @return whether the hook is canceled
	 */
	@Override
	public boolean post(T eventInstance) {
		EventContext hookContext = eventInstance.initEventContext();
		
		for (HookSubscriber<T> subscriber : this.subscriptions.values()) {
			hookContext.subscriptionStart(subscriber.name());
			
			if (subscriber.subscription() instanceof PassiveHookSubscription<T> passiveSubscription) {
				if (!eventInstance.hasCanceled()) {
					passiveSubscription.fire(eventInstance);
					hookContext.onCalled();
				}
			} else if (subscriber.subscription() instanceof CancelableHookSubscription<T> cancelableSubscription) {
				if (!eventInstance.hasCanceled()) {
					cancelableSubscription.fire(eventInstance);
					hookContext.onCalled();
				}
			} else if (subscriber.subscription() instanceof ContextAwareHookSubscription<T> contextAwareSubscription) {
				contextAwareSubscription.fire(eventInstance, hookContext);
				hookContext.onCalled();
			}
		}
		
		hookContext.subscriptionEnd();
		
		return eventInstance.hasCanceled();
	}
	
	/**
	 * Registers a hook with default name and priority
	 */
	public void registerCancelableHook(CancelableHookSubscription<T> subscription) {
		this.registerCancelableHook(subscription, getDefaultSubscriberName(), 0);
	}
	
	/**
	 * Registers a hook with default name
	 * @param priority determines the order of the hook in descending order
	 * 				   if a higher priority hook cancels the event, 
	 */
	public void registerCancelableHook(CancelableHookSubscription<T> subscription, int priority) {
		this.registerCancelableHook(subscription, getDefaultSubscriberName(), priority);
	}
	
	/**
	 * Registers a hook with default priority
	 * @param name you can specify the subscriber name to be referenced by other hooks, it will be stored
	 * 		  at {@link EventContext}
	 */
	public void registerCancelableHook(CancelableHookSubscription<T> subscription, String name) {
		this.registerCancelableHook(subscription, name, 0);
	}
	
	/**
	 * Registers a hook with full parameters
	 */
	public void registerCancelableHook(CancelableHookSubscription<T> subscription, String name, int priority) {
		this.subscriptions.put(priority, new HookSubscriber<>(name, subscription));
	}
	
	/**
	 * Registers a hook with default name and priority
	 */
	public void registerContextAwareHook(ContextAwareHookSubscription<T> subscription) {
		this.registerContextAwareHook(subscription, getDefaultSubscriberName(), 0);
	}
	
	/**
	 * Registers a hook with default name
	 * @param priority determines the order of the hook in descending order
	 */
	public void registerContextAwareHook(ContextAwareHookSubscription<T> subscription, int priority) {
		this.registerContextAwareHook(subscription, getDefaultSubscriberName(), priority);
	}
	
	/**
	 * Registers a hook with default priority
	 * @param name you can specify the subscriber name to be referenced by other hooks, it will be stored
	 * 		  at {@link EventContext}
	 */
	public void registerContextAwareHook(ContextAwareHookSubscription<T> subscription, String name) {
		this.registerContextAwareHook(subscription, name, 0);
	}
	
	/**
	 * Registers a hook with full parameters
	 */
	public void registerContextAwareHook(ContextAwareHookSubscription<T> subscription, String name, int priority) {
		this.subscriptions.put(priority, new HookSubscriber<>(name, subscription));
	}
	
	/**
	 * Defines a cancelable hook type
	 */
	public static <T extends EventInstance & CancelableEventInstance> CancelableHook<T> createCancelableHook() {
		return new CancelableHook<> ();
	}
}
