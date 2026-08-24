package yesman.epicfight.registry.entries;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import yesman.epicfight.EpicFight;

import com.mojang.serialization.MapCodec;


import yesman.epicfight.registry.deferred_shim.DeferredHolderShim;
import yesman.epicfight.registry.deferred_shim.DeferredRegisterShim;

import yesman.epicfight.data.loot.OnSkillBookDroppedByEntity;
import yesman.epicfight.main.EpicFightMod;

public final class EpicFightGlobalLootModifiers {
	private EpicFightGlobalLootModifiers() {}
	
	public static final DeferredRegisterShim<MapCodec<? extends Object>> REGISTRY = new DeferredRegisterShim<>(NeoForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, EpicFight.MODID);
	
	public static final DeferredHolderShim<MapCodec<? extends Object>, MapCodec<OnSkillBookDroppedByEntity>> SKILLS = REGISTRY.register("skillbook_loot_table_modifier", () ->  OnSkillBookDroppedByEntity.SKILL_CODEC);
}
