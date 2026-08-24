package net.neoforged.neoforge.common;

/// Stub for NeoForge's NeoForgeConfig.
public class NeoForgeConfig {
    public static class SERVER {
        public static Object fullBoundingBoxLadders = new Object() {
            public boolean get() { return false; }
        };
        public static Object removeErroringBlockEntities = new Object() {
            public boolean get() { return false; }
        };
        public static Object removeErroringEntities = new Object() {
            public boolean get() { return false; }
        };
    }
}
