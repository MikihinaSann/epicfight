package yesman.epicfight.api.data.reloader;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import yesman.epicfight.EpicFight;
import yesman.epicfight.client.online.cosmetics.Emote;
import yesman.epicfight.mixin.common.WritableRegistryAccessor;
import yesman.epicfight.registry.EpicFightRegistries;

import java.util.Map;

/// Reload listener for the emote datapack registry.
/// On NeoForge, this was handled by DataPackRegistryEvent which automatically loaded
/// JSON files from data/<namespace>/emote/ using Emote.CODEC.
/// On Fabric, we use a SimpleJsonResourceReloadListener to manually load and register emotes.
public class EmoteReloadListener extends SimpleJsonResourceReloadListener {
    public static final EmoteReloadListener INSTANCE = new EmoteReloadListener();

    private EmoteReloadListener() {
        super(new Gson(), "emote");
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> jsonMap, ResourceManager resourceManager, ProfilerFiller profiler) {
        EpicFight.LOGGER.info("Loading emote datapack registry: {} entries", jsonMap.size());

        // Temporarily unfreeze the registry to allow late registration from datapacks.
        // On NeoForge, DataPackRegistryEvent handles this automatically. On Fabric, the
        // registry is frozen during Bootstrap.bootStrap(), so we need to toggle the flag.
        var registry = EpicFightRegistries.EMOTE;
        var accessor = (WritableRegistryAccessor) registry;
        accessor.setFrozen(false);

        for (Map.Entry<ResourceLocation, JsonElement> entry : jsonMap.entrySet()) {
            ResourceLocation resourceId = entry.getKey();
            ResourceLocation registryKey = ResourceLocation.fromNamespaceAndPath(resourceId.getNamespace(), resourceId.getPath());

            try {
                JsonElement json = entry.getValue();
                Emote emote = Emote.CODEC.parse(JsonOps.INSTANCE, json).result().orElse(null);

                if (emote != null) {
                    Registry.register(registry, registryKey, emote);
                } else {
                    EpicFight.LOGGER.warn("Failed to parse emote {}: codec returned empty result", registryKey);
                }
            } catch (Throwable e) {
                EpicFight.LOGGER.warn("Failed to load emote {}: {}", registryKey, e.getMessage());
            }
        }

        // Refreeze the registry
        accessor.setFrozen(true);
    }
}
