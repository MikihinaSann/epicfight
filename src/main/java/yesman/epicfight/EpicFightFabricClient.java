package yesman.epicfight;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.CoreShaderRegistrationCallback;
import yesman.epicfight.api.client.animation.property.JointMaskReloadListener;
import yesman.epicfight.api.client.input.action.EpicFightInputAction;
import yesman.epicfight.api.client.input.action.InputAction;
import yesman.epicfight.api.client.input.action.MinecraftInputAction;
import yesman.epicfight.api.client.model.ItemSkinsReloadListener;
import yesman.epicfight.api.client.model.Meshes;
import yesman.epicfight.client.gui.screen.SkillBookScreen;
import yesman.epicfight.client.gui.widgets.AnchoredButton;
import yesman.epicfight.client.gui.widgets.ColorDeterminator;
import yesman.epicfight.client.gui.widgets.common.WidgetTheme;
import yesman.epicfight.client.input.EpicFightKeyMappings;
import yesman.epicfight.client.renderer.patched.item.EpicFightItemProperties;
import yesman.epicfight.client.renderer.shader.compute.loader.ComputeShaderProvider;
import yesman.epicfight.config.ClientConfig;
import yesman.epicfight.main.EpicFightSharedConstants;
import yesman.epicfight.platform.client.ClientModPlatformProvider;
import yesman.epicfight.platform.fabric.client.FabricClientModPlatform;
import yesman.epicfight.world.capabilities.provider.CommonEntityPatchProvider;

public class EpicFightFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        EpicFightClient.initialize(new FabricClientModPlatform());

        // Register client-side extensible enums
        InputAction.ENUM_MANAGER.registerEnumCls(EpicFight.MODID, EpicFightInputAction.class);
        InputAction.ENUM_MANAGER.registerEnumCls("minecraft", MinecraftInputAction.class);

        // Load client enums
        InputAction.ENUM_MANAGER.loadEnum();
        WidgetTheme.ENUM_MANAGER.loadEnum();

        // Register key mappings
        EpicFightKeyMappings.registerKeys();

        // Register client player patches
        CommonEntityPatchProvider.ClientModule.registerClientPlayerPatches();

        // Register skill book icons
        SkillBookScreen.registerIconItems();

        // Register item properties
        EpicFightItemProperties.registerItemProperties();

        // Check compute shader support
        ComputeShaderProvider.checkIfSupports();

        // Initialize config screen
        // TODO: Register config screen via Fabric

        // Reset items preference if empty
        if (ClientConfig.combatCategorizedItems.isEmpty() && ClientConfig.miningCategorizedItems.isEmpty()) {
            // ItemsPreferenceScreen.resetItems(); // TODO: Uncomment when config works
        }

        EpicFight.LOGGER.info("Epic Fight Fabric client initialized successfully!");
    }
}
