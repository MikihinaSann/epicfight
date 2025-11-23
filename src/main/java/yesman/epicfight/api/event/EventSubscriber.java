package yesman.epicfight.api.event;

import yesman.epicfight.api.event.subscriptions.EventSubscription;

/**
 * An event subscription info
 * <p>
 * name: you can specify the name of subscriber, this will effect {@link EventContext} to inspect who called,
 * and who canceled the event. (default is a class name called {@link Event#registerPassiveEvent},
 * {@link CancelableEvent#registerCancelableEvent}, and {@link CancelableEvent#registerContextAwareEvent}
 * <p>
 * subscription: a task provided as a lambda expression
 */
public record EventSubscriber<T extends EventInstance> (String name, EventSubscription<T> subscription) {
}
