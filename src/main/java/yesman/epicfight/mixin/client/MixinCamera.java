package yesman.epicfight.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.Camera;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import yesman.epicfight.api.client.camera.EpicFightCameraAPI;

@Mixin(value = Camera.class)
public abstract class MixinCamera {
	@Shadow
	private boolean initialized;
	@Shadow
	private BlockGetter level;
	@Shadow
	private Entity entity;
	@Shadow
	private boolean detached;
	
	@Inject(at = @At(value = "HEAD"), method = "setup(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/world/entity/Entity;ZZF)V", cancellable = true)
	public void epicfight$setup(BlockGetter level, Entity entity, boolean detached, boolean thirdPersonReverse, float partialTick, CallbackInfo callbackInfo) {
		this.initialized = true;
		this.level = level;
		this.entity = entity;
		this.detached = detached;
		
		EpicFightCameraAPI cameraApi = EpicFightCameraAPI.getInstance();
		Camera camera = (Camera)(Object)this;
		
		if (cameraApi.setupCamera(camera, partialTick)) {
			callbackInfo.cancel();
		}
		
		cameraApi.fireCameraBuildPost(camera, partialTick);
	}
}
