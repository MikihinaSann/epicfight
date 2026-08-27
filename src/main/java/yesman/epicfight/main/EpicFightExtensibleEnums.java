package yesman.epicfight.main;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Style;
import net.minecraft.world.item.Rarity;

import java.util.function.UnaryOperator;

public class EpicFightExtensibleEnums {

    /// The style modifier for the UNIQUE rarity.
    public static final UnaryOperator<Style> UNIQUE_STYLE = style -> style.withColor(ChatFormatting.GREEN);

    /// The UNIQUE rarity value. Set during initExtensibleEnums().
    public static Rarity UNIQUE;

    /// Creates the UNIQUE rarity enum value.
    /// On Fabric, this is done via MixinRarity which injects a new enum constant
    /// at class init time. The field is set by the mixin's <clinit> injection.
    @SuppressWarnings("unchecked")
    public static void initExtensibleEnums() {
        // MixinRarity adds the UNIQUE constant and sets this field during <clinit>.
        // If the mixin didn't run for some reason, fall back to EPIC.
        if (UNIQUE == null) {
            UNIQUE = Rarity.EPIC;
        }
    }
}
