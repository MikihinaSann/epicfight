package yesman.epicfight.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.mojang.authlib.GameProfile;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.phys.Vec3;
import yesman.epicfight.client.ClientEngine;
import yesman.epicfight.api.client.camera.EpicFightCameraAPI;
import yesman.epicfight.client.world.capabilites.entitypatch.player.LocalPlayerPatch;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;

@Mixin(value = LocalPlayer.class)
public abstract class MixinLocalPlayer extends AbstractClientPlayer {
	// Dummy constructor
	public MixinLocalPlayer(ClientLevel arg1, GameProfile arg2) {
		super(arg1, arg2);
	}

	@Unique private float epicfight$lastXxa = Float.NaN;
	@Unique private float epicfight$lastZza = Float.NaN;

	@Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;sendPosition()V", shift = At.Shift.BEFORE), method = "tick()V")
	private void epicfight$tick(CallbackInfo callbackInfo) {
		LocalPlayer epicfight$entity = (LocalPlayer)(Object)this;
		LocalPlayerPatch localPlayerPatch = EpicFightCapabilities.getEntityPatch(epicfight$entity, LocalPlayerPatch.class);

		if (localPlayerPatch != null) {
			localPlayerPatch.dx = epicfight$entity.xxa;
			localPlayerPatch.dz = epicfight$entity.zza;
			// ponytail: only stream input on change. Steady/stationary input (the common case) was re-sent identical 20x/s,
			// each re-broadcast to every tracker and flushed — this chain dominated the server-thread profile (eventfd_write).
			// NaN init forces the first tick to send, so initial state is always synced. Ceiling: a player who *starts*
			// tracking mid-hold gets dx/dz on the next input edge; upgrade path = sync dx/dz on start-tracking if it matters.
			// (zza, xxa) matches sendPlayerInput's (forward, strafe): the relay handlers assign
			// dx = strafe, dz = forward, so this keeps remote patches' dx/dz aligned with local
			if (epicfight$entity.xxa != this.epicfight$lastXxa || epicfight$entity.zza != this.epicfight$lastZza) {
				this.epicfight$lastXxa = epicfight$entity.xxa;
				this.epicfight$lastZza = epicfight$entity.zza;
				localPlayerPatch.sendPlayerInput(epicfight$entity.zza, epicfight$entity.xxa);
			}
		}
	}
	
    @Inject(method = "drop", at = @At("HEAD"), cancellable = true)
    private void onDrop(boolean fullStack, CallbackInfoReturnable<Boolean> cir) {
        if (ClientEngine.getInstance().controlEngine.isSwitchOrDropBlocked()) {
            // Prevents the player from accidentally dropping the item while attacking in Epic Fight mode.
            cir.cancel();
        }
    }
	
	@Override
	public void moveRelative(float amount, Vec3 relative) {
		Vec3 vec3 = EpicFightCameraAPI.getInstance().getRelativeMove(relative, amount);
		this.setDeltaMovement(this.getDeltaMovement().add(vec3));
	}
}