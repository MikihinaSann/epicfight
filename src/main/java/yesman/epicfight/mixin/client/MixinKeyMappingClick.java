package yesman.epicfight.mixin.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import yesman.epicfight.client.input.CombatKeyMapping;

import java.util.HashSet;
import java.util.Set;

/// Distributes click events to all [CombatKeyMapping] instances that share the same key,
/// EXCEPT the one that vanilla's [KeyMapping#click] already handled (the one in the static MAP).
///
/// In vanilla Minecraft, [KeyMapping#click] only increments the click count for the first
/// key mapping registered for a given key (stored in the static MAP). This means
/// [CombatKeyMapping] instances (like Epic Fight's ATTACK, GUARD, etc.) never receive
/// clicks when they share a key with a vanilla mapping (e.g., MOUSE_BUTTON_LEFT).
///
/// On NeoForge, [KeyConflictContext#IN_GAME] solves this by making the key mapping system
/// distribute clicks to all matching mappings. Fabric has no equivalent, so this mixin
/// replicates that behavior for [CombatKeyMapping] instances specifically.
///
/// IMPORTANT: We must NOT double-increment the click count for the CombatKeyMapping that
/// is already in MAP (i.e., the one vanilla already clicked). This happens when a
/// CombatKeyMapping is the only mapping for a key (e.g., SWITCH_MODE on R key).
/// Double-incrementing causes toggleMode() to fire twice, immediately reverting the mode.
///
/// REPEAT filtering: GLFW sends PRESS (action=1) then REPEAT (action=2) events while a
/// key is held. Vanilla [KeyboardHandler.keyPress] calls [KeyMapping.set(key, true)] BEFORE
/// [KeyMapping.click(key)], so checking [KeyMapping#isDown] in a HEAD injection cannot
/// distinguish PRESS from REPEAT (isDown is always true when click is called).
/// Instead, we track pressed keys in a Set and filter REPEAT events in the TAIL injection.
@Mixin(KeyMapping.class)
public abstract class MixinKeyMappingClick {

    /// Tracks keys that have been pressed (PRESS) and not yet released.
    /// Used to distinguish PRESS from REPEAT events.
    private static final Set<InputConstants.Key> EPICFIGHT_PRESSED_KEYS = new HashSet<>();

    @Inject(method = "click(Lcom/mojang/blaze3d/platform/InputConstants$Key;)V", at = @At("TAIL"))
    private static void epicfight$distributeClick(InputConstants.Key key, CallbackInfo ci) {
        KeyMapping vanillaClicked = KeyMappingAccessor.epicfight$getMap().get(key);
        boolean isRepeat = EPICFIGHT_PRESSED_KEYS.contains(key);

        for (KeyMapping mapping : CombatKeyMapping.getCombatKeyMappings()) {
            KeyMappingAccessor accessor = (KeyMappingAccessor) mapping;
            // Use the current bound key (field "key"), not getDefaultKey().
            // MAP is never updated when keys are rebound (setKey only updates the field),
            // so getDefaultKey() or MAP lookup would match the wrong key after rebind.
            boolean currentKeyMatches = accessor.epicfight$getKey().equals(key);

            if (mapping == vanillaClicked) {
                // Vanilla already incremented this mapping's click count in click().
                if (!currentKeyMatches) {
                    // Stale MAP entry — this mapping was rebound away from this key.
                    // Undo vanilla's erroneous increment so the old key no longer triggers it.
                    accessor.epicfight$setClickCount(accessor.epicfight$getClickCount() - 1);
                } else if (isRepeat) {
                    // REPEAT event — undo vanilla's increment to prevent duplicate triggers.
                    accessor.epicfight$setClickCount(accessor.epicfight$getClickCount() - 1);
                }
                // else: PRESS and current key matches — vanilla's increment is correct.
            } else {
                // Mapping was NOT handled by vanilla (not in MAP for this key).
                // This happens when another mod (e.g., JEI) registered a mapping for the
                // same key AFTER Epic Fight, overwriting our CombatKeyMapping in MAP.
                if (currentKeyMatches && !isRepeat) {
                    // Manually increment for PRESS only.
                    accessor.epicfight$setClickCount(accessor.epicfight$getClickCount() + 1);
                }
            }
        }

        if (!isRepeat) {
            EPICFIGHT_PRESSED_KEYS.add(key);
        }
    }

    /// Clear pressed keys on RELEASE so the next PRESS is detected correctly.
    /// [KeyMapping.set] is called by [KeyboardHandler.keyPress] with pressed=false on RELEASE.
    @Inject(method = "set(Lcom/mojang/blaze3d/platform/InputConstants$Key;Z)V", at = @At("HEAD"))
    private static void epicfight$clearReleasedKey(InputConstants.Key key, boolean pressed, CallbackInfo ci) {
        if (!pressed) {
            EPICFIGHT_PRESSED_KEYS.remove(key);
        }
    }
}
