package fuzs.forgeconfigapiport.api.config.v3;

/// Stub for NeoForge's ModConfigEvent.
public class ModConfigEvent {
    public static class Loading extends ModConfigEvent {
        private final ModConfig config;
        public Loading(ModConfig config) { this.config = config; }
        public ModConfig getConfig() { return config; }
    }

    public static class Reloading extends ModConfigEvent {
        private final ModConfig config;
        public Reloading(ModConfig config) { this.config = config; }
        public ModConfig getConfig() { return config; }
    }
}
