package dev.tr7zw.transition.mc;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.resources.ResourceLocation;
public class PlayerUtil {
    public CompoundTag getPersistentData() { return new CompoundTag(); }
    public static ResourceLocation getPlayerSkin(AbstractClientPlayer player) { return player.getSkin().texture(); }
}
