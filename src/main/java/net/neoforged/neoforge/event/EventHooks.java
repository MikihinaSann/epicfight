package net.neoforged.neoforge.event;
import net.minecraft.client.Minecraft;

/// Stub for NeoForge's EventHooks.
public class EventHooks {
    public static boolean onLivingTick(net.minecraft.world.entity.LivingEntity entity) { return false; }
    public static void onLivingJump(net.minecraft.world.entity.LivingEntity entity) {}
    public static boolean onLivingUseItem(net.minecraft.world.entity.LivingEntity entity, net.minecraft.world.item.ItemStack stack, int duration) { return false; }
}
