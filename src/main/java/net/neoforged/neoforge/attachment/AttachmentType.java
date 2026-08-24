package net.neoforged.neoforge.attachment;

/// Stub for NeoForge's AttachmentType — used by compat modules.
/// On Fabric, attachments are handled differently (mixin-injected fields).
public class AttachmentType<T> {
    public static <T> Builder<T> builder(java.util.function.Supplier<T> supplier) {
        return new Builder<>();
    }

    public static class Builder<T> {
        public AttachmentType<T> build() {
            return new AttachmentType<>();
        }
    }
}
