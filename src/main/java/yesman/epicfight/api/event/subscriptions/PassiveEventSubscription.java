package yesman.epicfight.api.event.subscriptions;

import yesman.epicfight.api.event.EventInstance;

/**
 * A default event subscription type
 * If you want to cancel the event, use {@link CancelableEventSubscription}
 * If you want to fire the event with custom validation, {@link ContextAwareEventSubscription}
 */
@FunctionalInterface
public interface PassiveEventSubscription<T extends EventInstance> extends EventSubscription<T> {
	void fire(T event);
}
