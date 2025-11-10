package yesman.epicfight.client.input;

import com.mojang.blaze3d.platform.InputConstants;

import net.minecraft.client.KeyMapping;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import yesman.epicfight.main.EpicFightMod;

@EventBusSubscriber(value = Dist.CLIENT)
public class EpicFightKeyMappings {
	
	// Key mappings for GUI
	public static final KeyMapping WEAPON_INNATE_SKILL_TOOLTIP =
        new KeyMapping(
            EpicFightMod.format("key.%s.show_tooltip"),
            KeyConflictContext.GUI,
            InputConstants.Type.KEYSYM,
            InputConstants.KEY_LSHIFT,
            EpicFightMod.format("key.%s.gui")
        );

	public static final KeyMapping SKILL_EDIT =
        new KeyMapping(
            EpicFightMod.format("key.%s.skill_gui"),
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            InputConstants.KEY_K,
            EpicFightMod.format("key.%s.gui")
        );

	public static final KeyMapping OPEN_CONFIG_SCREEN =
        new KeyMapping(
            EpicFightMod.format("key.%s.config"),
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            -1,
            EpicFightMod.format("key.%s.gui")
        );
	
	// Ingame key mappings
	public static final KeyMapping DODGE =
        new CombatKeyMapping(
            EpicFightMod.format("key.%s.dodge"),
            InputConstants.KEY_LALT,
            EpicFightMod.format("key.%s.combat")
        );

	public static final KeyMapping GUARD =
        new CombatKeyMapping(
            EpicFightMod.format("key.%s.guard"),
            InputConstants.Type.MOUSE,
            InputConstants.MOUSE_BUTTON_RIGHT,
            EpicFightMod.format("key.%s.combat")
        );

	public static final KeyMapping ATTACK =
        new CombatKeyMapping(
            EpicFightMod.format("key.%s.attack"),
            InputConstants.Type.MOUSE,
            InputConstants.MOUSE_BUTTON_LEFT,
            EpicFightMod.format("key.%s.combat"));

	public static final KeyMapping WEAPON_INNATE_SKILL =
        new CombatKeyMapping(
            EpicFightMod.format("key.%s.weapon_innate_skill"),
            InputConstants.Type.MOUSE,
            InputConstants.MOUSE_BUTTON_LEFT,
            EpicFightMod.format("key.%s.combat")
        );

	public static final KeyMapping MOVER_SKILL =
        new CombatKeyMapping(
            EpicFightMod.format("key.%s.mover_skill"),
            InputConstants.KEY_SPACE,
            EpicFightMod.format("key.%s.combat")
        );

    public static final KeyMapping SWITCH_MODE =
        new KeyMapping(
            EpicFightMod.format("key.%s.switch_mode"),
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            InputConstants.KEY_R,
            EpicFightMod.format("key.%s.combat")
        );

	public static final KeyMapping LOCK_ON =
        new KeyMapping(
            EpicFightMod.format("key.%s.lock_on"),
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            InputConstants.KEY_G,
            EpicFightMod.format("key.%s.camera")
        );

    public static final KeyMapping LOCK_ON_SHIFT_LEFT =
        new KeyMapping(
            EpicFightMod.format("key.%s.lock_on_shift_left"),
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            InputConstants.KEY_LEFT,
            EpicFightMod.format("key.%s.camera")
        );

    public static final KeyMapping LOCK_ON_SHIFT_RIGHT =
        new KeyMapping(
            EpicFightMod.format("key.%s.lock_on_shift_right"),
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            InputConstants.KEY_RIGHT,
            EpicFightMod.format("key.%s.camera")
        );

    public static final KeyMapping LOCK_ON_SHIFT_FREELY =
        new KeyMapping(
            EpicFightMod.format("key.%s.lock_on_shift_freely"),
            KeyConflictContext.IN_GAME,
            InputConstants.Type.MOUSE,
            InputConstants.MOUSE_BUTTON_RIGHT,
            EpicFightMod.format("key.%s.camera")
        );

	// Systemical key mappings especially for debugging
	public static final KeyMapping SWITCH_VANILLA_MODEL_DEBUGGING =
        new KeyMapping(
            EpicFightMod.format("key.%s.switch_vanilla_model_debug"),
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            -1,
            EpicFightMod.format("key.%s.system")
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