package yesman.epicfight.mixin.common;

import net.minecraft.core.Holder;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(AttributeSupplier.Builder.class)
public interface AttributeSupplierBuilderAccessor {
    @Invoker("hasAttribute")
    boolean epicfight$hasAttribute(Holder<Attribute> attribute);
}
