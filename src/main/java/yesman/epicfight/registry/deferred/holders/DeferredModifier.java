package yesman.epicfight.registry.deferred.holders;

import net.minecraft.resources.ResourceKey;
import yesman.epicfight.registry.deferred_shim.DeferredHolderShim;
import yesman.epicfight.api.ex_cap.data.modifier.WeaponModifier;

public final class DeferredModifier extends DeferredHolderShim<WeaponModifier.Builder, WeaponModifier.Builder>
{
    public DeferredModifier(ResourceKey<WeaponModifier.Builder> key) {
        super(key);
    }
}
