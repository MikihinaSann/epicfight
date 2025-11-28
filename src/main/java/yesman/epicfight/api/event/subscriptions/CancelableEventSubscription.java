package yesman.epicfight.api.event.subscriptions;

import yesman.epicfight.api.event.CancelableEvent;
import yesman.epicfight.api.event.Event;

/**
 * A subscription type that developers can cancel the event
 * Events with lower priority won't execute unless it's {@link ContextAwareEventSubscription}
 */
@FunctionalInterface
public interface CancelableEventSubscription<T extends Event & CancelableEvent> extends EventSubscription<T> {
	void fire(T event);
}
