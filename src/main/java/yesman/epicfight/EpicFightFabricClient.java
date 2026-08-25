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

        // Register CLIENT config via ForgeConfigAPIPort
        try {
            fuzs.forgeconfigapiport.fabric.api.neoforge.v4.NeoForgeConfigRegistry.INSTANCE.register(
                EpicFight.MODID, net.neoforged.fml.config.ModConfig.Type.CLIENT,
                (net.neoforged.fml.config.IConfigSpec) yesman.epicfight.config.ClientConfig.SPEC);

            fuzs.forgeconfigapiport.fabric.api.neoforge.v4.NeoForgeModConfigEvents.loading(EpicFight.MODID).register(config -> {
                if (config.getType() == net.neoforged.fml.config.ModConfig.Type.CLIENT) {
                    // Inline config value reads — v3 ModConfigEvent types are remapped by Loom
                    yesman.epicfight.config.ClientConfig.maxStuckProjectiles = yesman.epicfight.config.ClientConfig.MAX_STUCK_PROJECTILES.get();
                    yesman.epicfight.config.ClientConfig.bloodEffects = yesman.epicfight.config.ClientConfig.BLOOD_EFFECTS.get();
                    yesman.epicfight.config.ClientConfig.tpsType = yesman.epicfight.config.ClientConfig.TPS_TYPE.get();
                    yesman.epicfight.config.ClientConfig.cameraHorizontalLocation = yesman.epicfight.config.ClientConfig.CAMERA_HORIZONTAL_LOCATION.get();
                    yesman.epicfight.config.ClientConfig.cameraVerticalLocation = yesman.epicfight.config.ClientConfig.CAMERA_VERTICAL_LOCATION.get();
                    yesman.epicfight.config.ClientConfig.cameraZoom = yesman.epicfight.config.ClientConfig.CAMERA_ZOOM.get();
                    yesman.epicfight.config.ClientConfig.entityFocusingRange = yesman.epicfight.config.ClientConfig.ENTITY_FOCUSING_RANGE.get();
                    yesman.epicfight.config.ClientConfig.holdingThreshold = yesman.epicfight.config.ClientConfig.HOLDING_THRESHOLD.get();
                    yesman.epicfight.config.ClientConfig.autoPerspectiveSwithing = yesman.epicfight.config.ClientConfig.AUTO_PERSPECTIVE_SWITCHING.get();
                    yesman.epicfight.config.ClientConfig.lockOnSnapping = yesman.epicfight.config.ClientConfig.LOCK_ON_SNAPPING.get();
                    yesman.epicfight.config.ClientConfig.enableAnimatedFirstPersonModel = yesman.epicfight.config.ClientConfig.ENABLE_ANIMATED_FIRST_PERSON_MODEL.get();
                    yesman.epicfight.config.ClientConfig.enableOriginalModel = yesman.epicfight.config.ClientConfig.ENABLE_PLAYER_VANILLA_MODEL.get();
                    yesman.epicfight.config.ClientConfig.enableCosmetics = yesman.epicfight.config.ClientConfig.ENABLE_COSMETICS.get();
                    yesman.epicfight.config.ClientConfig.enableFirstPersonCameraMove = yesman.epicfight.config.ClientConfig.ENABLE_FIRST_PERSON_CAMERA_MOVE.get();
                    yesman.epicfight.config.ClientConfig.showTargetIndicator = yesman.epicfight.config.ClientConfig.SHOW_TARGET_INDICATOR.get();
                    yesman.epicfight.config.ClientConfig.healthBarVisibility = yesman.epicfight.config.ClientConfig.HEALTH_BAR_VISIBILITY.get();
                    yesman.epicfight.config.ClientConfig.mineBlockGuideOption = yesman.epicfight.config.ClientConfig.MINE_BLOCK_GUIDE_OPTION.get();
                    yesman.epicfight.config.ClientConfig.enableTargetEntityGuide = yesman.epicfight.config.ClientConfig.ENABLE_TARGET_ENTITY_GUIDE.get();
                    yesman.epicfight.config.ClientConfig.activateComputeShader = yesman.epicfight.config.ClientConfig.ACTIVATE_COMPUTE_SHADER.get();
                    yesman.epicfight.config.ClientConfig.activatePersistentBuffer = yesman.epicfight.config.ClientConfig.ACTIVATE_PERSISTENT_BUFFER.get();
                    yesman.epicfight.config.ClientConfig.groundSlams = yesman.epicfight.config.ClientConfig.GROUND_SLAMS.get();
                    EpicFight.LOGGER.info("EpicFight client config loaded");
                }
            });
        } catch (Throwable e) {
            EpicFight.LOGGER.warn("Failed to register client config: " + e.getMessage());
        }

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
