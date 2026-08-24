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
    /// On Fabric, we can't add enum values at runtime without bytecode manipulation.
    /// Use a mixin @ExtendEnum or just fallback to EPIC with green color.
    @SuppressWarnings("unchecked")
    public static void initExtensibleEnums() {
        // Fallback: use EPIC rarity with custom style
        // The actual enum extension should be done via mixin @ExtendEnum
        UNIQUE = Rarity.EPIC;
    }
}
