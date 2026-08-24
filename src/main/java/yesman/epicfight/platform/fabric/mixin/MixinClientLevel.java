package yesman.epicfight.platform.fabric.mixin;

import net.minecraft.client.multiplayer.ClientLevel;
import org.spongepowered.asm.mixin.Mixin;

/// On NeoForge, this mixin prevented crashes when joining the world with certain mods
/// by intercepting the Object.post() call in ClientLevel.<init>.
/// On Fabric, there is no Object call in ClientLevel.<init>, so this mixin is a no-op.
/// Kept for mixin config compatibility.
@Mixin(value = ClientLevel.class)
public abstract class MixinClientLevel {
    // No-op on Fabric
}
