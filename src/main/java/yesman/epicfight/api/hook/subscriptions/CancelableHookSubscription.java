package yesman.epicfight.api.hook.subscriptions;

import yesman.epicfight.api.hook.CancelableEventInstance;
import yesman.epicfight.api.hook.EventInstance;

/**
 * A subscription type that developers can cancel the hook
 * Hooks with lower priority won't execute unless it's {@link ContextAwareHookSubscription}
 */
@FunctionalInterface
public interface CancelableHookSubscription<T extends EventInstance & CancelableEventInstance> extends HookSubscription<T> {
	void fire(T hook);
}
