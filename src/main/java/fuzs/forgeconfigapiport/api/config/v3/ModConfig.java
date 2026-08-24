package fuzs.forgeconfigapiport.api.config.v3;

/// Stub ModConfig class.
public class ModConfig {
    public enum Type {
        COMMON, CLIENT, SERVER
    }

    private final Type type;

    public ModConfig(Type type) {
        this.type = type;
    }

    public Type getType() {
        return type;
    }

    public java.nio.file.Path getFullPath() {
        return null;
    }
}
