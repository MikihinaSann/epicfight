package yesman.epicfight.mixin.common;

import net.minecraft.world.entity.AreaEffectCloud;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(AreaEffectCloud.class)
public interface AreaEffectCloudAccessor {
    @Accessor("victims")
    List<net.minecraft.world.entity.LivingEntity> epicfight$getVictims();
}
