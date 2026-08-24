package yesman.epicfight;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.loader.api.FabricLoader;
import yesman.epicfight.api.animation.AnimationManager;
import yesman.epicfight.api.animation.LivingMotion;
import yesman.epicfight.api.animation.LivingMotions;
import yesman.epicfight.api.data.reloader.ItemCapabilityReloadListener;
import yesman.epicfight.api.data.reloader.MobPatchReloadListener;
import yesman.epicfight.api.data.reloader.SkillReloadListener;
import yesman.epicfight.api.event.EpicFightEventHooks;
import yesman.epicfight.api.ex_cap.listeners.ConditionalReloadListener;
import yesman.epicfight.api.ex_cap.listeners.ItemPresetReloadListener;
import yesman.epicfight.api.ex_cap.listeners.MovesetReloadListener;
import yesman.epicfight.api.ex_cap.listeners.WeaponModifierReloadListener;
import yesman.epicfight.client.online.cosmetics.Emote;
import yesman.epicfight.compat.ICompatModule;
import yesman.epicfight.compat.MinecraftMod;
import yesman.epicfight.compat.mcreator.MCreatorPlayerAnimationsCompat;
import yesman.epicfight.compat.simplytooltips.EpicFightTooltipProvider;
import yesman.epicfight.config.CommonConfig;
import yesman.epicfight.config.ServerConfig;
import yesman.epicfight.data.loot.EpicFightLootTables;
import yesman.epicfight.gameasset.Armatures;
import yesman.epicfight.gameasset.ColliderPreset;
import yesman.epicfight.main.EpicFightExtensibleEnums;
import yesman.epicfight.main.EpicFightSharedConstants;
import yesman.epicfight.network.EntityPairingPacketType;
import yesman.epicfight.network.EntityPairingPacketTypes;
import yesman.epicfight.platform.fabric.FabricModPlatform;
import yesman.epicfight.registry.EpicFightRegistries;
import yesman.epicfight.registry.entries.*;
import yesman.epicfight.server.commands.AnimatorCommand;
import yesman.epicfight.server.commands.PlayerModeCommand;
import yesman.epicfight.server.commands.PlayerSkillCommand;
import yesman.epicfight.server.commands.PlayerStaminaCommand;
import yesman.epicfight.skill.SkillCategories;
import yesman.epicfight.skill.SkillCategory;
import yesman.epicfight.skill.SkillSlot;
import yesman.epicfight.skill.SkillSlots;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.Faction;
import yesman.epicfight.world.capabilities.entitypatch.Factions;
import yesman.epicfight.world.capabilities.item.CapabilityItem;
import yesman.epicfight.world.capabilities.item.WeaponTypeReloadListener;
import yesman.epicfight.world.capabilities.provider.CommonEntityPatchProvider;
import yesman.epicfight.world.gamerule.EpicFightGameRules;
import yesman.epicfight.world.item.SkillBookItem;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;
import net.sweenus.simplytooltips.api.TooltipProviderRegistry;

public class EpicFightFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        EpicFight.initialize(new FabricModPlatform());

        // Register SimplyTooltips provider
        try {
            TooltipProviderRegistry.register(new EpicFightTooltipProvider(), 2);
        } catch (Throwable ignored) {}

        // Register configs
        // TODO: Register configs via ForgeConfigAPIPort when available

        // Register extensible enums
        LivingMotion.ENUM_MANAGER.registerEnumCls(EpicFight.MODID, LivingMotions.class);
        SkillCategory.ENUM_MANAGER.registerEnumCls(EpicFight.MODID, SkillCategories.class);
        SkillSlot.ENUM_MANAGER.registerEnumCls(EpicFight.MODID, SkillSlots.class);
        CapabilityItem.Styles.ENUM_MANAGER.registerEnumCls(EpicFight.MODID, CapabilityItem.Styles.class);
        CapabilityItem.WeaponCategories.ENUM_MANAGER.registerEnumCls(EpicFight.MODID, CapabilityItem.WeaponCategories.class);
        Faction.ENUM_MANAGER.registerEnumCls(EpicFight.MODID, Factions.class);
        EntityPairingPacketType.ENUM_MANAGER.registerEnumCls(EpicFight.MODID, EntityPairingPacketTypes.class);

        // Load enums
        LivingMotion.ENUM_MANAGER.loadEnum();
        SkillCategory.ENUM_MANAGER.loadEnum();
        SkillSlot.ENUM_MANAGER.loadEnum();
        CapabilityItem.Styles.ENUM_MANAGER.loadEnum();
        CapabilityItem.WeaponCategories.ENUM_MANAGER.loadEnum();
        Faction.ENUM_MANAGER.loadEnum();
        EntityPairingPacketType.ENUM_MANAGER.loadEnum();

        // Initialize extensible enums
        EpicFightExtensibleEnums.initExtensibleEnums();

        // Register all deferred registries
        EpicFightRegistries.DEFERRED_REGISTRIES.forEach(dr -> dr.accept());

        // Register item capabilities
        BuiltInRegistries.ITEM.forEach(item -> {
            EpicFightCapabilities.ITEM_CAPABILITY_PROVIDER.registerWeaponTypesByClass();
        });

        // Register entity patches
        CommonEntityPatchProvider.INSTANCE.registerVanillaEntityPatches();

        // Register armatures
        Armatures.registerEntityTypes();

        // Register default weapon types
        WeaponTypeReloadListener.registerDefaultWeaponTypes();

        // Register gamerules
        EpicFightGameRules.registerGameRules();

        // Register potions
        EpicFightPotions.addRecipes();
        EpicFightMobEffects.addOffhandModifier();

        // Register animation registry event
        AnimationManager.addNoWarningModId(EpicFight.EPICSKINS_MODID);
        AnimationManager.AnimationRegistryEvent animationRegistryEvent = new AnimationManager.AnimationRegistryEvent();
        animationRegistryEvent.getBuilders().stream()
            .sorted(java.util.Comparator.comparing(AnimationManager.AnimationBuilder::namespace))
            .forEach(builder -> builder.task().accept(builder));

        // Register skill book loot table
        EpicFightEventHooks.Registry.SKILLBOOK_LOOT_TABLE.registerEvent(event -> {});;

        // Register commands
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            PlayerModeCommand.register(dispatcher);
            PlayerSkillCommand.register(dispatcher);
            PlayerStaminaCommand.register(dispatcher);
            AnimatorCommand.register(dispatcher);
        });

        // Register reload listeners via server lifecycle
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            // These will be registered properly with the server's resource manager
        });

        // Load compat modules
        boolean isClientSide = EpicFightSharedConstants.isPhysicalClient();
        for (MinecraftMod mod : MinecraftMod.values()) {
            if (FabricLoader.getInstance().isModLoaded(mod.getModId())) {
                if (isClientSide || !mod.isClientOnly()) {
                    ICompatModule.loadCompatModule(mod.getCompatibilityModule());
                }
            }
        }

        // MCreator compat
        if (isClientSide) {
            // Check for bedrock_animations data
            // ICompatModule.loadCompatModule(MCreatorPlayerAnimationsCompat.class);
        }

        EpicFight.LOGGER.info("Epic Fight Fabric initialized successfully!");
    }
}
