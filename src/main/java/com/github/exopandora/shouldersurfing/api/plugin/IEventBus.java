package com.github.exopandora.shouldersurfing.api.plugin;

/// Stub for ShoulderSurfing v5 IEventBus.
public interface IEventBus {
    <T> void register(Class<T> eventType, java.util.function.Consumer<T> consumer);
}
