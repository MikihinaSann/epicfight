package net.neoforged.neoforge.common;

/// Stub for NeoForge's NeoForgeConfig.
public class NeoForgeConfig {
    public static class SERVER {
        public static ConfigValue fullBoundingBoxLadders = new ConfigValue(false);
        public static ConfigValue removeErroringBlockEntities = new ConfigValue(false);
        public static ConfigValue removeErroringEntities = new ConfigValue(false);
    }

    public static class ConfigValue {
        private final boolean value;
        public ConfigValue(boolean value) { this.value = value; }
        public boolean get() { return value; }
    }
}
