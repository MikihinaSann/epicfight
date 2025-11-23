package yesman.epicfight.api.client.event.instances;

import net.minecraft.client.Camera;
import yesman.epicfight.api.client.camera.EpicFightCameraAPI;
import yesman.epicfight.api.event.CancelableEventInstance;
import yesman.epicfight.api.event.EventInstance;

public abstract class BuildCameraTransform extends EventInstance {
	private final EpicFightCameraAPI cameraApi;
	private final Camera camera;
	private final float partialTick;
	
	public BuildCameraTransform(EpicFightCameraAPI cameraApi, Camera camera, float partialTick) {
		this.cameraApi = cameraApi;
		this.camera = camera;
		this.partialTick = partialTick;
	}
	
	public EpicFightCameraAPI getEpicFightCameraAPI() {
		return this.cameraApi;
	}
	
	public Camera getCamera() {
		return this.camera;
	}
	
	public float getPartialTick() {
		return this.partialTick;
	}
	
	public static final class Pre extends BuildCameraTransform implements CancelableEventInstance {
		public Pre(EpicFightCameraAPI cameraApi, Camera camera, float partialTick) {
			super(cameraApi, camera, partialTick);
		}
	}
	
	public static final class Post extends BuildCameraTransform {
		public Post(EpicFightCameraAPI cameraApi, Camera camera, float partialTick) {
			super(cameraApi, camera, partialTick);
		}
	}
}
