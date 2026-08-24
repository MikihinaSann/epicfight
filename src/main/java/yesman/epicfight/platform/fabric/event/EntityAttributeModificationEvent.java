package yesman.epicfight.platform.fabric.event;

import net.minecraft.core.Holder;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;

/// Stub for NeoForge's EntityAttributeModificationEvent.
public class EntityAttributeModificationEvent {

    public void add(EntityType<? extends LivingEntity> entityType, Holder<Attribute> attribute, double value) {
        // TODO: Implement via Fabric attribute registration
    }

    public void add(EntityType<? extends LivingEntity> entityType, Holder<Attribute> attribute) {
        // TODO: Implement via Fabric attribute registration
    }
}
