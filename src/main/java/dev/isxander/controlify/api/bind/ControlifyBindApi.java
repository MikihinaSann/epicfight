package dev.isxander.controlify.api.bind;

import dev.isxander.controlify.bindings.BindContext;
import net.minecraft.resources.ResourceLocation;
import java.util.function.Consumer;

/// Stub for Controlify's ControlifyBindApi.
public class ControlifyBindApi {
    public void registerBindContext(BindContext context) {}
    public InputBindingSupplier registerBinding(Consumer<InputBindingBuilder> builder) {
        return new InputBindingSupplier(new InputBinding());
    }
    public InputBindingSupplier registerBinding(ResourceLocation id, Consumer<InputBindingBuilder> builder) {
        return new InputBindingSupplier(new InputBinding());
    }
}
