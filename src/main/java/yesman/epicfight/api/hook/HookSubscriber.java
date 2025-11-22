package yesman.epicfight.api.hook;

import yesman.epicfight.api.hook.subscriptions.HookSubscription;

/**
 * A hook subscription info
 * <p>
 * name: you can specify the name of subscriber, this will effect {@link EventContext} to inspect who called,
 * and who canceled the event. (default is a class name called {@link Hook#registerPassiveHook},
 * {@link CancelableHook#registerCancelableHook}, and {@link CancelableHook#registerContextAwareHook}
 * <p>
 * subscription: a task provided as a lambda expression
 */
public record HookSubscriber<T extends EventInstance> (String name, HookSubscription<T> subscription) {
}
