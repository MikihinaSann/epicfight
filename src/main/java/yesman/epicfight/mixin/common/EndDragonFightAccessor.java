package yesman.epicfight.mixin.common;

import net.minecraft.server.bossevents.ServerBossEvent;
import net.minecraft.world.level.dimension.end.EndDragonFight;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/// Accessor for EndDragonFight.dragonEvent (private field).
@Mixin(EndDragonFight.class)
public interface EndDragonFightAccessor {
    @Accessor("dragonEvent")
    ServerBossEvent epicfight$getDragonEvent();
}
