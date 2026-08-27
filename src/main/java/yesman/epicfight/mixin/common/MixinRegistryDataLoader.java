package yesman.epicfight.mixin.common;

import net.minecraft.resources.RegistryDataLoader;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import yesman.epicfight.client.online.cosmetics.Emote;
import yesman.epicfight.registry.EpicFightRegistries;

import java.util.ArrayList;
import java.util.List;

/// Adds the EMOTE registry to the worldgen data pack registries.
/// On NeoForge, this is done via [DataPackRegistryEvent.dataPackRegistry].
/// On Fabric, we inject into the static initializer of [RegistryDataLoader] to add our registry
/// to [WORLDGEN_REGISTRIES], which is the list used by [WorldLoader] to load data pack registries.
/// This makes EMOTE a dynamic registry accessible via [RegistryAccess].
@Mixin(RegistryDataLoader.class)
public class MixinRegistryDataLoader {
    @Shadow @Final @Mutable
    private static List<RegistryDataLoader.RegistryData<?>> WORLDGEN_REGISTRIES;

    @Inject(method = "<clinit>", at = @At("TAIL"))
    private static void epicfight$addEmoteRegistry(CallbackInfo ci) {
        // Add EMOTE as a data pack registry.
        // This makes it accessible via RegistryAccess.registryOrThrow(EpicFightRegistries.Keys.EMOTE)
        // and vanilla will automatically load JSON files from data/<namespace>/emote/ using Emote.CODEC.
        List<RegistryDataLoader.RegistryData<?>> newList = new ArrayList<>(WORLDGEN_REGISTRIES);
        newList.add(new RegistryDataLoader.RegistryData<>(EpicFightRegistries.Keys.EMOTE, Emote.CODEC, false));
        WORLDGEN_REGISTRIES = newList;
    }
}
