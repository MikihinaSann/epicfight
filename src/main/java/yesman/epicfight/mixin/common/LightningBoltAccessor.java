package yesman.epicfight.mixin.common;

import net.minecraft.world.entity.LightningBolt;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(LightningBolt.class)
public interface LightningBoltAccessor {
	@Accessor("damage")
	void epicfight$setDamage(float damage);
}
