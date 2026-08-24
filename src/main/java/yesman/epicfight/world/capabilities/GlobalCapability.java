package yesman.epicfight.world.capabilities;

import net.minecraft.resources.ResourceLocation;


public class GlobalCapability<T> extends Object<T, Void> {
	public GlobalCapability(ResourceLocation name, Class<T> type) {
		super(name, type, void.class);
	}
}
