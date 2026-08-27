package yesman.epicfight.mixin.common;

import net.minecraft.ChatFormatting;
import net.minecraft.world.item.Rarity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;

/// Adds Rarity.UNIQUE enum constant at runtime, matching NeoForge's EnumProxy behavior.
/// The new constant has a green chat color, matching Epic Fight's original intent.
@Mixin(Rarity.class)
public abstract class MixinRarity {
    @Shadow
    @Final
    @Mutable
    private static Rarity[] $VALUES;

    @Invoker("<init>")
    private static Rarity epicfight$invokeConstructor(int ordinal, String name, ChatFormatting color) {
        throw new AssertionError();
    }

    @Inject(method = "<clinit>", at = @At("TAIL"))
    private static void epicfight$addUniqueRarity(CallbackInfo ci) {
        // Create the new UNIQUE rarity with green color
        Rarity unique = epicfight$invokeConstructor($VALUES.length, "UNIQUE", ChatFormatting.GREEN);

        // Add to $VALUES array
        ArrayList<Rarity> values = new ArrayList<>(Arrays.asList($VALUES));
        values.add(unique);
        $VALUES = values.toArray(new Rarity[0]);

        // Set the static field on EpicFightExtensibleEnums
        try {
            Class<?> clazz = Class.forName("yesman.epicfight.main.EpicFightExtensibleEnums");
            Field field = clazz.getDeclaredField("UNIQUE");
            field.setAccessible(true);
            // Remove final modifier if present
            field.set(null, unique);
        } catch (Exception e) {
            // Fallback: try to find via reflection on the mod's classloader
        }
    }
}
