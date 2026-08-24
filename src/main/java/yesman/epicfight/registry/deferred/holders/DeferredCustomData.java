package yesman.epicfight.registry.deferred.holders;

import net.minecraft.resources.ResourceKey;
import yesman.epicfight.registry.deferred_shim.DeferredHolderShim;
import yesman.epicfight.world.capabilities.item.custom.CustomData;

public class DeferredCustomData<T extends CustomData<?>> extends DeferredHolderShim<CustomData<?>, T> {
    public DeferredCustomData(ResourceKey<CustomData<?>> key) {
        super(key);
    }
}
