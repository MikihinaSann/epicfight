package yesman.epicfight.api.hook.subscriptions;

import yesman.epicfight.api.hook.EventContext;
import yesman.epicfight.api.hook.EventInstance;
import yesman.epicfight.api.hook.CancelableEventInstance;

/**
 * A subscription type that developers can inspect hook, cancel history by {@link EventContext}
 * This hook subscription type called even after the hook is canceled
 */
@FunctionalInterface
public interface ContextAwareHookSubscription<T extends EventInstance & CancelableEventInstance> extends HookSubscription<T> {
	void fire(T hook, EventContext hookHistory);
}
