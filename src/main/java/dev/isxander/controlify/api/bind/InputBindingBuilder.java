package dev.isxander.controlify.api.bind;

import net.minecraft.network.chat.Component;

/// Stub for Controlify's InputBindingBuilder.
public class InputBindingBuilder {
    public InputBindingBuilder justPressed() { return this; }
    public InputBindingBuilder justReleased() { return this; }
    public InputBindingBuilder pressed() { return this; }
    public InputBindingBuilder analogue() { return this; }
    public InputBindingBuilder category(Component category) { return this; }
    public InputBindingBuilder id(net.minecraft.resources.ResourceLocation id) { return this; }
    public InputBindingBuilder id(String id) { return this; }
    public InputBinding build() { return new InputBinding(); }
}
