package yesman.epicfight.api.utils;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.enchantment.Enchantment;

/// Helper methods for registry access on Fabric.
public class RegistryHelper {
    /// Gets the Holder for an enchantment from the registry access.
    public static Holder<Enchantment> getEnchantmentHolder(RegistryAccess access, ResourceKey<Enchantment> key) {
        Registry<Enchantment> registry = access.registryOrThrow(Registries.ENCHANTMENT);
        return registry.getHolder(key).orElseThrow();
    }
}
