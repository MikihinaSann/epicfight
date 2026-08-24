package yesman.epicfight.mixin.client;

import net.minecraft.client.renderer.entity.layers.VillagerProfessionLayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(VillagerProfessionLayer.class)
public interface VillagerProfessionLayerAccessor {
    @Accessor("typeHatCache")
    Map<String, net.minecraft.resources.ResourceLocation> epicfight$getTypeHatCache();
    @Accessor("professionHatCache")
    Map<String, net.minecraft.resources.ResourceLocation> epicfight$getProfessionHatCache();
}
