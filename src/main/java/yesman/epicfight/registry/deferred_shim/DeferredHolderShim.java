package yesman.epicfight.registry.deferred_shim;
import net.minecraft.client.Minecraft;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

/// Fabric-compatible replacement for NeoForge's [DeferredHolder].
/// Implements [Holder<T>] so it can be used wherever a Holder is expected.
public class DeferredHolderShim<T, I extends T> implements Holder<T> {
    private final ResourceKey<T> key;
    private final Supplier<I> supplier;
    private I value;
    private Holder<T> holder;

    @SuppressWarnings("unchecked")
    public DeferredHolderShim(Object registryKey, ResourceLocation location, Supplier<I> supplier) {
        this.key = ResourceKey.create((ResourceKey<Registry<T>>) (Object) registryKey, location);
        this.supplier = supplier;
    }

    @SuppressWarnings("unchecked")
    void bind(Registry<T> registry) {
        I supplied = supplier.get();
        this.value = supplied;
        Registry.register(registry, key.location(), supplied);
        this.holder = registry.getHolder((net.minecraft.resources.ResourceKey<T>) key).orElse(null);
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

    public Holder<T> asHolder() {
        if (holder == null) {
            throw new IllegalStateException("Accessed before registration: " + key.location());
        }
        return holder;
    }

    // Holder<T> interface methods
    @Override
    public T value() {
        return get();
    }

    @Override
    public boolean isBound() {
        return value != null;
    }

    @Override
    public boolean is(ResourceKey<T> key) {
        return this.key == key;
    }

    @Override
    public boolean is(Holder<T> holder) {
        return this.holder != null && this.holder.is(holder);
    }

    @Override
    public Optional<ResourceKey<T>> unwrapKey() {
        return Optional.of(key);
    }

    @Override
    public com.mojang.datafixers.util.Either<ResourceKey<T>, T> unwrap() {
        if (value != null) {
            return com.mojang.datafixers.util.Either.right(value);
        }
        return com.mojang.datafixers.util.Either.left(key);
    }

    @Override
    public Kind kind() {
        return Kind.REFERENCE;
    }

    @Override
    public boolean canSerializeIn(net.minecraft.core.HolderOwner<T> owner) {
        return true;
    }

    @Override
    public java.util.stream.Stream<net.minecraft.tags.TagKey<T>> tags() {
        return holder != null ? holder.tags() : java.util.stream.Stream.empty();
    }

    @Override
    public boolean is(net.minecraft.tags.TagKey<T> tag) {
        return holder != null && holder.is(tag);
    }

    @Override
    public boolean is(java.util.function.Predicate<ResourceKey<T>> predicate) {
        return predicate.test(key);
    }

    @Override
    public boolean is(ResourceLocation location) {
        return this.key.location().equals(location);
    }

    @Override
    public String toString() {
        return "DeferredHolderShim[" + key.location() + "]";
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
}
