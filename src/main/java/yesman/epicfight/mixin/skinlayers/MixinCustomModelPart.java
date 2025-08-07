package yesman.epicfight.mixin.skinlayers;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import dev.tr7zw.skinlayers.versionless.render.CustomModelPart;

@Mixin(value = CustomModelPart.class)
public interface MixinCustomModelPart {
	@Accessor(remap = false)
	public float getX();

	@Accessor(remap = false)
	public float getY();

	@Accessor(remap = false)
	public float getZ();

	@Accessor(remap = false)
	public float getXRot();

	@Accessor(remap = false)
	public float getYRot();

	@Accessor(remap = false)
	public float getZRot();
}
