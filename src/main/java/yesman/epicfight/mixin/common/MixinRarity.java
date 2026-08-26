package yesman.epicfight.mixin.common;

import net.minecraft.network.chat.Style;
import net.minecraft.world.item.Rarity;
import org.spongepowered.asm.mixin.Mixin;
import yesman.epicfight.main.EpicFightExtensibleEnums;

import java.util.function.UnaryOperator;

/// Replaces NeoForge's enum_extensions.json.
/// On Fabric, we use a simple approach: the Rarity enum is extended via
/// the Fabric access widener to expose the constructor, then we create
/// a synthetic enum value using reflection in EpicFightExtensibleEnums.
///
/// Note: True enum extension (adding values to the values() array) requires
/// bytecode manipulation. For now, we use a workaround where EPICFIGHT_UNIQUE
/// is stored as a static field and used directly, not via Rarity.values().
@Mixin(Rarity.class)
public class MixinRarity {
    // No injection needed — the enum value is created via reflection in
    // EpicFightExtensibleEnums.initExtensibleEnums()
}
