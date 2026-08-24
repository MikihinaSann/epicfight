package yesman.epicfight.registry.deferred_shim;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import yesman.epicfight.EpicFight;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/// Fabric-compatible replacement for NeoForge's [DeferredRegister].
///
/// Queues registrations and processes them when [#accept()] is called
/// (typically from the mod initializer).
///
/// @param <T> the registry element type
public class DeferredRegisterShim<T> {
    private final ResourceKey<Registry<T>> registryKey;
    private final String modId;
    private final List<DeferredHolderShim<T>> entries = new ArrayList<>();
    private boolean accepted = false;

    public DeferredRegisterShim(ResourceKey<Registry<T>> registryKey, String modId) {
        this.registryKey = registryKey;
        this.modId = modId;
    }

    public <I extends T> DeferredHolderShim<T, I> register(String name, Supplier<I> supplier) {
        if (accepted) {
            throw new IllegalStateException("Cannot register after accept() has been called");
        }
        DeferredHolderShim<T, I> holder = new DeferredHolderShim<>(registryKey, ResourceLocation.fromNamespaceAndPath(modId, name), supplier);
        entries.add(holder);
        return holder;
    }

    public void accept() {
        if (accepted) {
            return;
        }
        accepted = true;

        Registry<T> registry = getRegistry();
        for (DeferredHolderShim<T> entry : entries) {
            entry.bind(registry);
        }
    }

    @SuppressWarnings("unchecked")
    private Registry<T> getRegistry() {
        // Try BuiltInRegistries first, then fall back to custom registries
        Registry<?> registry = BuiltInRegistries.REGISTRY.get(registryKey.location());
        if (registry == null) {
            // For custom registries, they should already be registered
            registry = (Registry<T>) Registry.class.cast(BuiltInRegistries.REGISTRY.get(registryKey.location()));
        }
        return (Registry<T>) registry;
    }
}
