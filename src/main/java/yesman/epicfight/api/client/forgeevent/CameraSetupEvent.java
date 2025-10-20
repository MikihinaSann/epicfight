package yesman.epicfight.api.client.forgeevent;

import net.minecraft.client.Camera;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;
import yesman.epicfight.client.events.engine.RenderEngine;

/**
 * An event hook that you can setup camera without conflicts with epic fight camera setup
 */
@OnlyIn(Dist.CLIENT)
@Cancelable
public class CameraSetupEvent extends Event {
	private final Camera camera;
	private final float partialTick;
	private final boolean lockingOnTarget;
	private final RenderEngine renderEngine;
	
	/** This will prevent calling {@link Camera#setup} **/
	private boolean cancelVanillaSetup;
	
	public CameraSetupEvent(Camera camera, float partialTick, boolean lockingOnTarget, RenderEngine renderEngine) {
		this.camera = camera;
		this.partialTick = partialTick;
		this.lockingOnTarget = lockingOnTarget;
		this.renderEngine = renderEngine;
	}
	
	public Camera getCamera() {
		return this.camera;
	}
	
	public float getPartialTick() {
		return this.partialTick;
	}
	
	public boolean isLockingOnTarget() {
		return this.lockingOnTarget;
	}
	
	public RenderEngine getRenderEngine() {
		return this.renderEngine;
	}
	
	public void cancelVanillaSetup(boolean cancelVanillaSetup) {
		this.cancelVanillaSetup = cancelVanillaSetup;
	}
	
	public boolean shouldCancelVanillaSetup() {
		return this.cancelVanillaSetup;
	}
}
