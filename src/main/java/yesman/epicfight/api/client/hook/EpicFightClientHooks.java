package yesman.epicfight.api.client.hook;

import yesman.epicfight.api.client.hook.instances.BuildCameraTransform;
import yesman.epicfight.api.client.hook.instances.ItemUsedInDecoupledCamera;
import yesman.epicfight.api.hook.CancelableHook;
import yesman.epicfight.api.hook.Hook;

public final class EpicFightClientHooks {
	// Camera Hooks
	public static final class Camera {
		public static final CancelableHook<BuildCameraTransform.Pre> BUILD_TRANSFORM_PRE = CancelableHook.createCancelableHook();
		public static final Hook<BuildCameraTransform.Post> BUILD_TRANSFORM_POST = Hook.createHook();
		public static final Hook<ItemUsedInDecoupledCamera> ITEM_USED_WHEN_DECOUPLED = Hook.createHook();
	}
	
	private EpicFightClientHooks() {}
}