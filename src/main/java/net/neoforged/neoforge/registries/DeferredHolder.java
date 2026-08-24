package net.neoforged.neoforge.registries;

/// Stub — use yesman.epicfight.registry.deferred_shim.DeferredHolderShim instead.
public class DeferredHolder<T, I extends T> extends yesman.epicfight.registry.deferred_shim.DeferredHolderShim<T, I> {
    public DeferredHolder(net.minecraft.resources.ResourceKey<? extends net.minecraft.core.Registry<T>> registryKey, net.minecraft.resources.ResourceLocation location, java.util.function.Supplier<I> supplier) {
        super(registryKey, location, supplier);
    }
}
