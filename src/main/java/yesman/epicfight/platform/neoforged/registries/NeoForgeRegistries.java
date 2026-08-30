package yesman.epicfight.platform.neoforged.registries;

import net.minecraft.resources.ResourceKey;
import net.minecraft.core.Registry;
import com.mojang.serialization.MapCodec;

/// Stub for NeoForge's NeoForgeRegistries.
public class NeoForgeRegistries {
    public static class Keys {
        public static final ResourceKey<Registry<MapCodec<?>>> GLOBAL_LOOT_MODIFIER_SERIALIZERS = ResourceKey.createRegistryKey(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("neoforge", "global_loot_modifier_serializers"));
    }
}
