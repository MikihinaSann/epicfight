package yesman.epicfight.api.hook.subscriptions;

import yesman.epicfight.api.hook.EventInstance;

/**
 * A default hook subscription type
 * If you want to cancel the hook, use {@link CancelableHookSubscription}
 * If you want to fire the hook for custom validation, {@link ContextAwareHookSubscription}
 */
@FunctionalInterface
public interface PassiveHookSubscription<T extends EventInstance> extends HookSubscription<T> {
	void fire(T hook);
}
