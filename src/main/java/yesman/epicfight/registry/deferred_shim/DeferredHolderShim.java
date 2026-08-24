package yesman.epicfight.registry.deferred_shim;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

/// Fabric-compatible replacement for NeoForge's [DeferredHolder].
///
/// Wraps a lazy reference to a registry entry. The entry is registered
/// when [#bind()] is called by the parent [DeferredRegisterShim].
///
/// @param <T> the registry element type
/// @param <I> the specific implementation type (may equal T)
///
public class DeferredHolderShim<T, I extends T> {
    private final ResourceKey<T> key;
    private final Supplier<I> supplier;
    private I value;
    private Holder<T> holder;

    @SuppressWarnings("unchecked")
    public DeferredHolderShim(ResourceKey<? extends Registry<T>> registryKey, ResourceLocation location, Supplier<I> supplier) {
        this.key = ResourceKey.create((ResourceKey<Registry<T>>) (Object) registryKey, location);
        this.supplier = supplier;
    }

    @SuppressWarnings("unchecked")
    void bind(Registry<T> registry) {
        I supplied = supplier.get();
        this.value = supplied;
        Registry.register(registry, key.location(), supplied);
        this.holder = registry.getOrCreateHolder(key);
    }

    public I get() {
        if (value == null) {
            throw new IllegalStateException("Accessed before registration: " + key.location());
        }
        return value;
    }

    public boolean isPresent() {
        return value != null;
    }

    public ResourceKey<T> getKey() {
        return key;
    }

    public ResourceLocation getId() {
        return key.location();
    }

    public Optional<Holder<T>> getHolder() {
        return Optional.ofNullable(holder);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DeferredHolderShim<?, ?> that)) return false;
        return Objects.equals(key, that.key);
    }

    @Override
    public int hashCode() {
        return key.hashCode();
    }

    @Override
    public String toString() {
        return "DeferredHolderShim[" + key.location() + "]";
    }
}
