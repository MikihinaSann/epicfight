package yesman.epicfight.api.forgeevent;

import javax.annotation.Nullable;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;
import yesman.epicfight.world.capabilities.entitypatch.HurtableEntityPatch;

@Cancelable
public class EntityStunEvent extends Event {
	@Nullable
	private final DamageSource source;
	private final HurtableEntityPatch<?> stunned;
	
	public EntityStunEvent(DamageSource source, HurtableEntityPatch<?> stunned) {
		this.source = source;
		this.stunned = stunned;
	}
	
	public final DamageSource getDamageSource() {
		return this.source;
	}
	
	public final HurtableEntityPatch<?> getStunnedEntityPatch() {
		return this.stunned;
	}
}
