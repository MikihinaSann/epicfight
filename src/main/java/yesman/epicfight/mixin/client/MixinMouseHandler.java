package yesman.epicfight.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.minecraft.client.MouseHandler;
import net.minecraft.client.player.LocalPlayer;
import yesman.epicfight.client.ClientEngine;

@Mixin(value = MouseHandler.class)
public abstract class MixinMouseHandler {
	@Redirect(
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/player/LocalPlayer;turn(DD)V"
		),
		method = "turnPlayer()V"
	)
	private void epicfight_turnPlayer(LocalPlayer player, double yRot, double xRot) {
		if (!ClientEngine.getInstance().renderEngine.turnPlayer(yRot, xRot)) player.turn(yRot, xRot);
	}
}
