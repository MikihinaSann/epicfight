package yesman.epicfight.platform.neoforged.attachment;

import java.util.function.Supplier;

/// Stub for NeoForge's AttachmentType.
public class AttachmentType<T> {
    public static <T> Builder<T> builder(Supplier<T> supplier) {
        return new Builder<>();
    }

    public static class Builder<T> {
        public AttachmentType<T> build() {
            return new AttachmentType<>();
        }
    }
}
