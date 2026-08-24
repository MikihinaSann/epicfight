package dev.isxander.controlify.api;

/// Stub for Controlify's ControlifyApi.
public class ControlifyApi {
    public static ControlifyApi INSTANCE = new ControlifyApi();

    public static ControlifyApi get() { return INSTANCE; }

    public dev.isxander.controlify.api.bind.ControlifyBindApi bindApi() {
        return new dev.isxander.controlify.api.bind.ControlifyBindApi();
    }
}
