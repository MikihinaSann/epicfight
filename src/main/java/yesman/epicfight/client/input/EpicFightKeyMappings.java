package yesman.epicfight.client.input;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.ApiStatus;
import yesman.epicfight.main.EpicFightMod;

@Mod.EventBusSubscriber(modid = EpicFightMod.MODID, value = Dist.CLIENT, bus=Mod.EventBusSubscriber.Bus.MOD)
public class EpicFightKeyMappings {

    @ApiStatus.Internal
    public static class InputCategories {
        public static final String COMBAT = EpicFightMod.format("key.%s.combat");
        public static final String GUI = EpicFightMod.format("key.%s.gui");
        public static final String SYSTEM = EpicFightMod.format("key.%s.system");
        public static final String CAMERA = EpicFightMod.format("key.%s.camera");
    }

	// Key mappings for GUI
	public static final KeyMapping WEAPON_INNATE_SKILL_TOOLTIP =
        new KeyMapping(
            EpicFightMod.format("key.%s.show_tooltip"),
            KeyConflictContext.GUI,
            InputConstants.Type.KEYSYM,
            InputConstants.KEY_LSHIFT,
            InputCategories.GUI
        );

	public static final KeyMapping SKILL_EDIT =
        new KeyMapping(
            EpicFightMod.format("key.%s.skill_gui"),
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            InputConstants.KEY_K,
            InputCategories.GUI
        );

	public static final KeyMapping OPEN_CONFIG_SCREEN =
        new KeyMapping(
            EpicFightMod.format("key.%s.config"),
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            -1,
            InputCategories.GUI
        );
	
	// Ingame key mappings
	public static final KeyMapping DODGE =
        new CombatKeyMapping(
            EpicFightMod.format("key.%s.dodge"),
            InputConstants.KEY_LALT,
            InputCategories.COMBAT
        );

	public static final KeyMapping GUARD =
        new CombatKeyMapping(
            EpicFightMod.format("key.%s.guard"),
            InputConstants.Type.MOUSE,
            InputConstants.MOUSE_BUTTON_RIGHT,
            InputCategories.COMBAT
        );

	public static final KeyMapping ATTACK =
        new CombatKeyMapping(
            EpicFightMod.format("key.%s.attack"),
            InputConstants.Type.MOUSE,
            InputConstants.MOUSE_BUTTON_LEFT,
            InputCategories.COMBAT
        );

	public static final KeyMapping WEAPON_INNATE_SKILL =
        new CombatKeyMapping(
            EpicFightMod.format("key.%s.weapon_innate_skill"),
            InputConstants.Type.MOUSE,
            InputConstants.MOUSE_BUTTON_LEFT,
            InputCategories.COMBAT
        );

	public static final KeyMapping MOVER_SKILL =
        new CombatKeyMapping(
            EpicFightMod.format("key.%s.mover_skill"),
            InputConstants.KEY_SPACE,
            InputCategories.COMBAT
        );

    public static final KeyMapping SWITCH_MODE =
        new KeyMapping(
            EpicFightMod.format("key.%s.switch_mode"),
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            InputConstants.KEY_R,
            InputCategories.COMBAT
        );

	public static final KeyMapping LOCK_ON =
        new KeyMapping(
            EpicFightMod.format("key.%s.lock_on"),
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            InputConstants.KEY_G,
            InputCategories.CAMERA
        );

    public static final KeyMapping LOCK_ON_SHIFT_LEFT =
        new KeyMapping(
            EpicFightMod.format("key.%s.lock_on_shift_left"),
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            InputConstants.KEY_LEFT,
            InputCategories.CAMERA
        );

    public static final KeyMapping LOCK_ON_SHIFT_RIGHT =
        new KeyMapping(
            EpicFightMod.format("key.%s.lock_on_shift_right"),
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            InputConstants.KEY_RIGHT,
            InputCategories.CAMERA
        );

    public static final KeyMapping LOCK_ON_SHIFT_FREELY =
        new KeyMapping(
            EpicFightMod.format("key.%s.lock_on_shift_freely"),
            KeyConflictContext.IN_GAME,
            InputConstants.Type.MOUSE,
            InputConstants.MOUSE_BUTTON_RIGHT,
            InputCategories.CAMERA
        );

	// Systemical key mappings especially for debugging
	public static final KeyMapping SWITCH_VANILLA_MODEL_DEBUGGING =
        new KeyMapping(
            EpicFightMod.format("key.%s.switch_vanilla_model_debug"),
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            -1,
            InputCategories.SYSTEM
        );
	
	@SubscribeEvent
	public static void registerKeys(RegisterKeyMappingsEvent event) {
		event.register(WEAPON_INNATE_SKILL_TOOLTIP);
		event.register(SWITCH_MODE);
		event.register(DODGE);
		event.register(GUARD);
		event.register(ATTACK);
		event.register(WEAPON_INNATE_SKILL);
		event.register(MOVER_SKILL);
		event.register(SKILL_EDIT);
		event.register(LOCK_ON);
		event.register(LOCK_ON_SHIFT_LEFT);
		event.register(LOCK_ON_SHIFT_RIGHT);
		event.register(LOCK_ON_SHIFT_FREELY);
		event.register(OPEN_CONFIG_SCREEN);
		event.register(SWITCH_VANILLA_MODEL_DEBUGGING);
	}
}
