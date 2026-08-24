package dev.isxander.controlify.api.event;
import dev.isxander.controlify.controller.ControllerEntity;
import java.util.function.Consumer;
public class ControlifyEvents {
    public static Object LOOK_INPUT_MODIFIER = new Object() {
        public void register(Consumer<?> consumer) {}
    };
    public static Object on(Consumer<ControllerEntity> consumer) { return new Object(); }
    public static Object onOrNull(Consumer<ControllerEntity> consumer) { return new Object(); }
}
