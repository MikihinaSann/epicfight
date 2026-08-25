package yesman.epicfight;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import yesman.epicfight.api.client.input.action.EpicFightInputAction;
import yesman.epicfight.api.client.input.action.InputAction;
import yesman.epicfight.api.client.input.action.MinecraftInputAction;
import yesman.epicfight.client.gui.screen.SkillBookScreen;
import yesman.epicfight.client.gui.widgets.AnchoredButton;
import yesman.epicfight.client.gui.widgets.ColorDeterminator;
import yesman.epicfight.client.gui.widgets.common.WidgetTheme;
import yesman.epicfight.client.input.EpicFightKeyMappings;
import yesman.epicfight.client.renderer.patched.item.EpicFightItemProperties;
import yesman.epicfight.client.renderer.shader.compute.loader.ComputeShaderProvider;
import yesman.epicfight.network.EpicFightClientPayloadRegistration;
import yesman.epicfight.platform.fabric.client.FabricClientModPlatform;
import yesman.epicfight.world.capabilities.provider.CommonEntityPatchProvider;

public class EpicFightFabricClient implements ClientModInitializer {

    private static boolean computeShaderChecked = false;

    @Override
    public void onInitializeClient() {
        EpicFightClient.initialize(new FabricClientModPlatform());

        // Register client-bound payload handlers with Fabric networking
        try {
            EpicFightClientPayloadRegistration.registerClientHandlers();
        } catch (Throwable e) {
            EpicFight.LOGGER.warn("Failed to register client payload handlers: " + e.getMessage());
        }

        // Register client-side extensible enums
        try {
            InputAction.ENUM_MANAGER.registerEnumCls(EpicFight.MODID, EpicFightInputAction.class);
            InputAction.ENUM_MANAGER.registerEnumCls("minecraft", MinecraftInputAction.class);
            InputAction.ENUM_MANAGER.loadEnum();
            WidgetTheme.ENUM_MANAGER.loadEnum();
        } catch (Throwable e) {
            EpicFight.LOGGER.warn("Client enum registration failed: {}", e.getMessage());
        }

        // Register key mappings
        try {
            EpicFightKeyMappings.registerKeys();
        } catch (Throwable e) {
            EpicFight.LOGGER.warn("Key mapping registration failed: {}", e.getMessage());
        }

        // Register client player patches
        try {
            CommonEntityPatchProvider.ClientModule.registerClientPlayerPatches();
        } catch (Throwable e) {
            EpicFight.LOGGER.warn("Client player patch registration failed: {}", e.getMessage());
        }

        // Register skill book icons
        try {
            SkillBookScreen.registerIconItems();
        } catch (Throwable e) {
            EpicFight.LOGGER.warn("Skill book icon registration failed: {}", e.getMessage());
        }

        // Register item properties
        try {
            EpicFightItemProperties.registerItemProperties();
        } catch (Throwable e) {
            EpicFight.LOGGER.warn("Item properties registration failed: {}", e.getMessage());
        }

        // Defer compute shader check to first client tick (OpenGL context not ready during init)
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (!computeShaderChecked) {
                computeShaderChecked = true;
                try {
                    ComputeShaderProvider.checkIfSupports();
                } catch (Throwable e) {
                    EpicFight.LOGGER.warn("Compute shader check failed: {}", e.getMessage());
                }
            }
        });

        EpicFight.LOGGER.info("Epic Fight Fabric client initialized successfully!");
    }
}
