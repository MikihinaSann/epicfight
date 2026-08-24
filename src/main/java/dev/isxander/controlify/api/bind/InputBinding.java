package dev.isxander.controlify.api.bind;

import java.util.Optional;

/// Stub for Controlify's InputBinding.
public class InputBinding {
    public boolean digitalNow() { return false; }
    public float analogueNow() { return 0; }
    public boolean justPressed() { return false; }
    public boolean justReleased() { return false; }
    public boolean pressed() { return false; }
    public Optional<Boolean> guiPressed() { return Optional.empty(); }
    public net.minecraft.resources.ResourceLocation id() { return null; }
}
