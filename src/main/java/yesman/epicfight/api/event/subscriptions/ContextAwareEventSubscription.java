package yesman.epicfight.api.event.subscriptions;

import yesman.epicfight.api.event.CancelableEventInstance;
import yesman.epicfight.api.event.EventContext;
import yesman.epicfight.api.event.EventInstance;

/**
 * A subscription type that developers can inspect event, cancel history by {@link EventContext}
 * This event subscription type called even after the event is canceled
 */
@FunctionalInterface
public interface ContextAwareEventSubscription<T extends EventInstance & CancelableEventInstance> extends EventSubscription<T> {
	void fire(T event, EventContext eventContext);
}
