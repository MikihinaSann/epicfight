package yesman.epicfight.compat.azurelib;


import yesman.epicfight.api.client.event.EpicFightClientEventHooks;
import yesman.epicfight.api.client.model.transformer.HumanoidModelBaker;
import yesman.epicfight.compat.ICompatModule;
import yesman.epicfight.compat.azurelib.client.AzureArmorTransformer;

public class AzureLibArmorCompat implements ICompatModule {
	public void onModEventBusClient(Object eventBus) {
		HumanoidModelBaker.registerNewTransformer(new AzureArmorTransformer());
	}
	
	public void onGameEventBusClient(Object eventBus) {
        EpicFightClientEventHooks.Render.ANIMATED_ARMOR_TEXTURE.registerEvent(AzureArmorTransformer::getGeoArmorTexturePath);
	}
	
	public void onModEventBus(Object eventBus) {
	}
	
	public void onGameEventBus(Object eventBus) {
	}
}