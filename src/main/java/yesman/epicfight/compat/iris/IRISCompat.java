package yesman.epicfight.compat.iris;



import yesman.epicfight.client.events.engine.RenderEngine;
import yesman.epicfight.client.renderer.shader.compute.loader.ComputeShaderProvider;
import yesman.epicfight.compat.ICompatModule;
import yesman.epicfight.compat.sodium.client.SodiumFakeBlockRenderer;

public class IRISCompat implements ICompatModule {
	@Override
	public void onModEventBus(Object eventBus) {
	}
	
	@Override
	public void onGameEventBus(Object eventBus) {
	}
	
	@Override
	public void onModEventBusClient(Object eventBus) {
		eventBus.<Object>addListener(event -> {
			ComputeShaderProvider.initIris();
			event.enqueueWork(() -> RenderEngine.getInstance().reloadFakeBlockRenderer(new SodiumFakeBlockRenderer()));
		});
	}
	
	@Override
	public void onGameEventBusClient(Object eventBus) {
	}
}
