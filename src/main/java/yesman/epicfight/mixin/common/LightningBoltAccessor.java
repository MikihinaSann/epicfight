package yesman.epicfight.mixin.common;

import net.minecraft.world.entity.LightningBolt;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.Map;
import java.util.WeakHashMap;

/// NeoForge adds a `damage` field to LightningBolt that can be set to 0 to prevent
/// vanilla lightning damage. On Fabric, we store the override in a static WeakHashMap
/// and MixinLightningBolt checks it during tick() to skip the hurt calls.
@Mixin(LightningBolt.class)
public interface LightningBoltAccessor {
    Map<LightningBolt, Float> DAMAGE_OVERRIDES = new WeakHashMap<>();

    @Unique
    default void epicfight$setDamage(float damage) {
        DAMAGE_OVERRIDES.put((LightningBolt)(Object)this, damage);
    }

    @Unique
    default float epicfight$getDamage() {
        return DAMAGE_OVERRIDES.getOrDefault((LightningBolt)(Object)this, -1.0F);
    }
}
