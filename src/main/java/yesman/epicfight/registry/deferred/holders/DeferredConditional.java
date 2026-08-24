package yesman.epicfight.registry.deferred.holders;

import net.minecraft.resources.ResourceKey;
import yesman.epicfight.registry.deferred_shim.DeferredHolderShim;
import org.jetbrains.annotations.ApiStatus;
import yesman.epicfight.api.ex_cap.provider.ProviderConditional;

public final class DeferredConditional extends DeferredHolderShim<ProviderConditional.Builder, ProviderConditional.Builder> {
    @ApiStatus.Internal
    public DeferredConditional(ResourceKey<ProviderConditional.Builder> key) {
        super(key);
    }
}
