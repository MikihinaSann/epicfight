package yesman.epicfight.api.event.subscriptions;

import yesman.epicfight.api.event.CancelableEventInstance;
import yesman.epicfight.api.event.EventInstance;

/**
 * A subscription type that developers can cancel the event
 * Events with lower priority won't execute unless it's {@link ContextAwareEventSubscription}
 */
@FunctionalInterface
public interface CancelableEventSubscription<T extends EventInstance & CancelableEventInstance> extends EventSubscription<T> {
	void fire(T event);
}
