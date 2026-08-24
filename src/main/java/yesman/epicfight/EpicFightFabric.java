package yesman.epicfight;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.resource.event.v1.ServerResourceReloadEvents;
import net.fabricmc.loader.api.FabricLoader;
import yesman.epicfight.platform.ModPlatformProvider;
import yesman.epicfight.platform.fabric.FabricModPlatform;

public class EpicFightFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        EpicFight.initialize(new FabricModPlatform());

        // Registration, networking, capabilities, configs, etc. are handled here
        // This will be filled in as we port each subsystem
    }
}
