package yesman.epicfight.main;
import yesman.epicfight.EpicFight;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Style;
import net.minecraft.world.item.Rarity;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.function.UnaryOperator;

public class EpicFightExtensibleEnums {

    /// The style modifier for the UNIQUE rarity.
    public static final UnaryOperator<Style> UNIQUE_STYLE = style -> style.withColor(ChatFormatting.GREEN);

    /// The UNIQUE rarity value. Set during initExtensibleEnums().
    public static Rarity UNIQUE;

    /// Creates the UNIQUE rarity enum value via reflection.
    /// This adds a new constant to the Rarity enum at runtime.
    /// Requires the Rarity constructor to be accessible (via access widener).
    @SuppressWarnings("unchecked")
    public static void initExtensibleEnums() {
        try {
            // Get the Rarity class
            Class<Rarity> rarityClass = Rarity.class;

            // Get all current enum constants
            Rarity[] currentValues = rarityClass.getEnumConstants();
            int newOrdinal = currentValues.length;

            // Access the constructor via reflection
            // Rarity(String name, int ordinal, UnaryOperator<Style> styleModifier)
            Constructor<Rarity> constructor = rarityClass.getDeclaredConstructor(
                String.class, int.class, UnaryOperator.class
            );
            constructor.setAccessible(true);

            // Create the new enum value
            // Note: This uses the internal EnumConstructor to create a new constant
            // This is the standard Fabric approach for enum extension
            UNIQUE = (Rarity) sun.reflect.ReflectionFactory.getReflectionFactory()
                .newConstructorAccessor(constructor)
                .newInstance(new Object[]{"EPICFIGHT_UNIQUE", newOrdinal, UNIQUE_STYLE});

            // Add the new value to the $VALUES array
            Field valuesField = rarityClass.getDeclaredField("$VALUES");
            valuesField.setAccessible(true);

            Rarity[] newValues = new Rarity[newOrdinal + 1];
            System.arraycopy(currentValues, 0, newValues, 0, newOrdinal);
            newValues[newOrdinal] = UNIQUE;
            valuesField.set(null, newValues);

        } catch (Exception e) {
            EpicFight.LOGGER.error("Failed to create EPICFIGHT_UNIQUE rarity", e);
            // Fallback: use an existing rarity
            UNIQUE = Rarity.EPIC;
        }
    }
}
