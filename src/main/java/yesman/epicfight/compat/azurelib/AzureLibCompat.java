package yesman.epicfight.compat.azurelib;


import yesman.epicfight.api.client.event.EpicFightClientEventHooks;
import yesman.epicfight.api.client.model.transformer.HumanoidModelBaker;
import yesman.epicfight.compat.ICompatModule;
import yesman.epicfight.compat.azurelib.client.AzureModelTransformer;

public class AzureLibCompat implements ICompatModule {
	@Override
	public void onModEventBus(Object eventBus) {
	}

	@Override
	public void onGameEventBus(Object eventBus) {
	}

	@Override
	public void onModEventBusClient(Object eventBus) {
		HumanoidModelBaker.registerNewTransformer(new AzureModelTransformer());
	}

	@Override
	public void onGameEventBusClient(Object eventBus) {
		EpicFightClientEventHooks.Render.ANIMATED_ARMOR_TEXTURE.registerEvent(AzureModelTransformer::getGeoArmorTexturePath);
	}
}
