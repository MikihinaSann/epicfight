package yesman.epicfight.registry;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import yesman.epicfight.registry.deferred_shim.DeferredRegisterShim;
import yesman.epicfight.EpicFight;
import yesman.epicfight.api.animation.SynchedAnimationVariableKey;
import yesman.epicfight.api.ex_cap.data.Moveset;
import yesman.epicfight.api.ex_cap.data.modifier.WeaponModifier;
import yesman.epicfight.api.ex_cap.provider.ProviderConditional;
import yesman.epicfight.client.online.cosmetics.Emote;
import yesman.epicfight.data.conditions.Condition;
import yesman.epicfight.registry.callbacks.SkillCallbacks;
import yesman.epicfight.registry.callbacks.SkillDataKeyCallbacks;
import yesman.epicfight.registry.callbacks.SynchedAnimationVariableKeyCallbacks;
import yesman.epicfight.registry.entries.*;
import yesman.epicfight.skill.Skill;
import yesman.epicfight.skill.SkillDataKey;
import yesman.epicfight.world.capabilities.item.CapabilityItem;
import yesman.epicfight.world.capabilities.item.custom.CustomData;
import yesman.epicfight.world.entity.data.ExpandedEntityDataAccessor;

import java.util.List;
import java.util.function.Supplier;

public abstract class EpicFightRegistries {
    // Custom registries — created via Registry.registerSimple / Registry.register
    public static final Registry<Supplier<Condition<?>>> CONDITION = Registry.registerSimple(Keys.CONDITION);
    public static final Registry<ExpandedEntityDataAccessor<?>> EXPANDED_ENTITY_DATA_ACCESSOR = Registry.registerSimple(Keys.EXPANDED_ENTITY_DATA_ACCESSOR);
    public static final Registry<Skill> SKILL = Registry.registerSimple(Keys.SKILL, SkillCallbacks.getSkillCallback());
    public static final Registry<SkillDataKey<?>> SKILL_DATA_KEY = Registry.registerSimple(Keys.SKILL_DATA_KEY, SkillDataKeyCallbacks.getRegistryCallback());
    public static final Registry<SynchedAnimationVariableKey<?>> SYNCHED_ANIMATION_VARIABLE = Registry.registerSimple(Keys.SYNCHED_ANIMATION_VARIABLE_KEY, SynchedAnimationVariableKeyCallbacks.getRegistryCallback());
    public static final Registry<CapabilityItem.Builder<?>> BUILDERS = Registry.registerSimple(Keys.BUILDERS);
    public static final Registry<Moveset.Builder> MOVESETS = Registry.registerSimple(Keys.MOVESETS);
    public static final Registry<ProviderConditional.Builder> PROVIDER_CONDITIONALS = Registry.registerSimple(Keys.PROVIDER_CONDITIONALS);
    public static final Registry<WeaponModifier.Builder> MODIFIERS = Registry.registerSimple(Keys.MODIFIERS);
    public static final Registry<CustomData<?>> WEAPON_DATA = Registry.registerSimple(Keys.WEAPON_DATA);
    public static final Registry<CustomData<?>> MOVESET_DATA = Registry.registerSimple(Keys.MOVESET_DATA);

    // Deferred Registries
    public static final List<DeferredRegisterShim<?>> DEFERRED_REGISTRIES = List.of(
        EpicFightArmorMaterials.REGISTRY,
        EpicFightAttributes.REGISTRY,
        EpicFightBlockEntities.REGISTRY,
        EpicFightBlocks.REGISTRY,
        EpicFightCommandArgumentTypes.REGISTRY,
        EpicFightConditions.REGISTRY,
        EpicFightCreativeTabs.REGISTRY,
        EpicFightDataComponentTypes.REGISTRY,
        EpicFightEntityTypes.REGISTRY,
        EpicFightExpandedEntityDataAccessors.REGISTRY,
        EpicFightItems.REGISTRY,
        EpicFightLootItemFunctions.REGISTRY,
        EpicFightMobEffects.REGISTRY,
        EpicFightParticles.REGISTRY,
        EpicFightPotions.REGISTRY,
        EpicFightSkillDataKeys.REGISTRY,
        EpicFightSkills.REGISTRY,
        EpicFightSounds.REGISTRY,
        EpicFightSynchedAnimationVariableKeys.REGISTRY,
        EpicFightMovesets.REGISTRY,
        EpicFightProviderConditionals.REGISTRY,
        EpicFightItemCapabilityPresets.REGISTRY,
        EpicFightModifiers.REGISTRY,
        EpicFightMovesetData.REGISTER
    );

    public interface Keys {
        ResourceKey<Registry<CapabilityItem.Builder<?>>> BUILDERS = key("item_capability_builder");
        ResourceKey<Registry<Moveset.Builder>> MOVESETS = key("moveset");
        ResourceKey<Registry<ProviderConditional.Builder>> PROVIDER_CONDITIONALS = key("provider_conditional");
        ResourceKey<Registry<WeaponModifier.Builder>> MODIFIERS = key("modifiers");
        ResourceKey<Registry<CustomData<?>>> WEAPON_DATA = key("weapon_data");
        ResourceKey<Registry<CustomData<?>>> MOVESET_DATA = key("moveset_data");
        ResourceKey<Registry<Supplier<Condition<?>>>> CONDITION = key("condition");
        ResourceKey<Registry<ExpandedEntityDataAccessor<?>>> EXPANDED_ENTITY_DATA_ACCESSOR = key("expanded_entity_data_accessor");
        ResourceKey<Registry<SynchedAnimationVariableKey<?>>> SYNCHED_ANIMATION_VARIABLE_KEY = key("synched_animation_variable_key");
        ResourceKey<Registry<Skill>> SKILL = key("skill");
        ResourceKey<Registry<SkillDataKey<?>>> SKILL_DATA_KEY = key("skill_data_key");

        // Data Pack Registries
        ResourceKey<Registry<Emote>> EMOTE = key("emote");

        private static <T> ResourceKey<Registry<T>> key(String name) {
            return ResourceKey.createRegistryKey(EpicFight.identifier(name));
        }
    }
}
