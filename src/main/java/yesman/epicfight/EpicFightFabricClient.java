package yesman.epicfight;

import net.fabricmc.api.ClientModInitializer;
import yesman.epicfight.platform.client.ClientModPlatformProvider;
import yesman.epicfight.platform.fabric.client.FabricClientModPlatform;

public class EpicFightFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        EpicFightClient.initialize(new FabricClientModPlatform());

        // Client-side registration: key mappings, shaders, renderers, etc.
        // This will be filled in as we port each subsystem
    }
}
