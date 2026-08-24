package yesman.epicfight.mixin.common;
import net.minecraft.client.Minecraft;

import net.minecraft.world.level.dimension.end.EndDragonFight;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/// Accessor for EndDragonFight.dragonEvent (private field).
@Mixin(EndDragonFight.class)
public interface EndDragonFightAccessor {
    @Accessor("dragonEvent")
    Object epicfight$getDragonEvent();
}
