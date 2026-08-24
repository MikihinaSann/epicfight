package yesman.epicfight;

import net.fabricmc.api.ModInitializer;
import yesman.epicfight.platform.fabric.FabricModPlatform;

public class EpicFightFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        EpicFight.initialize(new FabricModPlatform());

        // TODO: Register all subsystems here
        // - DeferredRegisterShim.accept() for all registries
        // - Custom registries
        // - Networking
        // - Capabilities (item capability map)
        // - Configs (ForgeConfigAPIPort)
        // - Commands (CommandRegistrationCallback)
        // - Reload listeners
        // - Gamerules
        // - Compat modules
        // - Extensible enums
        // - Creative tab contents (ItemGroupEvents)
    }
}
