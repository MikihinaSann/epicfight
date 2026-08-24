package fuzs.forgeconfigapiport.api.config.v3;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

/// Stub ModConfigSpec — minimal implementation to allow compilation.
public class ModConfigSpec {
    private final Builder builder;

    public ModConfigSpec(Builder builder) {
        this.builder = builder;
    }

    public static class Builder {
        private final List<Value<?>> values = new ArrayList<>();

        public Builder push(String category) { return this; }
        public Builder pop() { return this; }
        public Builder comment(String comment) { return this; }
        public Builder translation(String translationKey) { return this; }

        // Type-specific define methods — must come before generic to avoid ambiguity
        public BooleanValue define(String path, boolean defaultValue) {
            BooleanValue value = new BooleanValue(this, path, defaultValue);
            values.add(value);
            return value;
        }

        public BooleanValue define(String path, java.util.function.Supplier<Boolean> defaultValue) {
            BooleanValue value = new BooleanValue(this, path, defaultValue.get());
            values.add(value);
            return value;
        }

        public IntValue define(String path, int defaultValue) {
            IntValue value = new IntValue(this, path, defaultValue, Integer.MIN_VALUE, Integer.MAX_VALUE);
            values.add(value);
            return value;
        }

        public IntValue defineInRange(String path, int defaultValue, int min, int max) {
            IntValue value = new IntValue(this, path, defaultValue, min, max);
            values.add(value);
            return value;
        }

        public DoubleValue defineInRange(String path, double defaultValue, double min, double max) {
            DoubleValue value = new DoubleValue(this, path, defaultValue, min, max);
            values.add(value);
            return value;
        }

        public LongValue defineInRange(String path, long defaultValue, long min, long max) {
            LongValue value = new LongValue(this, path, defaultValue, min, max);
            values.add(value);
            return value;
        }

        // Generic define for non-primitive types — use define(String, Object) to avoid
        // conflicting with primitive overloads
        public ConfigValue<String> define(String path, String defaultValue) {
            ConfigValue<String> value = new ConfigValue<>(this, path, defaultValue);
            values.add(value);
            return value;
        }

        @SuppressWarnings("unchecked")
        public <T> ConfigValue<T> define(String path, T defaultValue) {
            if (defaultValue instanceof Boolean) {
                return (ConfigValue<T>) define(path, (Boolean) defaultValue);
            } else if (defaultValue instanceof Integer) {
                return (ConfigValue<T>) define(path, (Integer) defaultValue);
            }
            ConfigValue<T> value = new ConfigValue<>(this, path, defaultValue);
            values.add(value);
            return value;
        }

        public <T> ConfigValue<T> define(String path, T defaultValue, Predicate<T> validator) {
            ConfigValue<T> value = new ConfigValue<>(this, path, defaultValue);
            values.add(value);
            return value;
        }

        public <T extends Enum<T>> EnumValue<T> defineEnum(String path, T defaultValue) {
            EnumValue<T> value = new EnumValue<>(this, path, defaultValue);
            values.add(value);
            return value;
        }

        public <T> ConfigValue<List<? extends String>> defineList(String path, List<? extends String> defaultValue, Predicate<Object> validator) {
            ConfigValue<List<? extends String>> value = new ConfigValue<>(this, path, defaultValue);
            values.add(value);
            return value;
        }

        public ModConfigSpec build() {
            return new ModConfigSpec(this);
        }
    }

    public static class Value<T> {
        protected final Builder builder;
        protected final String path;
        protected T value;

        public Value(Builder builder, String path, T defaultValue) {
            this.builder = builder;
            this.path = path;
            this.value = defaultValue;
        }

        public T get() { return value; }
        public void set(T value) { this.value = value; }
        public String getPath() { return path; }
        public void save() {}
    }

    public static class ConfigValue<T> extends Value<T> {
        public ConfigValue(Builder builder, String path, T defaultValue) {
            super(builder, path, defaultValue);
        }
    }

    public static class EnumValue<T extends Enum<T>> extends ConfigValue<T> {
        public EnumValue(Builder builder, String path, T defaultValue) {
            super(builder, path, defaultValue);
        }
    }

    public static class BooleanValue extends ConfigValue<Boolean> {
        public BooleanValue(Builder builder, String path, boolean defaultValue) {
            super(builder, path, defaultValue);
        }
    }

    public static class IntValue extends ConfigValue<Integer> {
        private final int min, max;
        public IntValue(Builder builder, String path, int defaultValue, int min, int max) {
            super(builder, path, defaultValue);
            this.min = min;
            this.max = max;
        }
    }

    public static class DoubleValue extends ConfigValue<Double> {
        private final double min, max;
        public DoubleValue(Builder builder, String path, double defaultValue, double min, double max) {
            super(builder, path, defaultValue);
            this.min = min;
            this.max = max;
        }
    }

    public static class LongValue extends ConfigValue<Long> {
        private final long min, max;
        public LongValue(Builder builder, String path, long defaultValue, long min, long max) {
            super(builder, path, defaultValue);
            this.min = min;
            this.max = max;
        }
    }
}
