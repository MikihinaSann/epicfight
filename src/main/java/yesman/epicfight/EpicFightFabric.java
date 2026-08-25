package yesman.epicfight;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.registry.RegistryEntryAddedCallback;
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
import yesman.epicfight.config.CommonConfig;
import yesman.epicfight.config.ServerConfig;
import yesman.epicfight.data.loot.EpicFightLootTables;
import yesman.epicfight.gameasset.Armatures;
import yesman.epicfight.gameasset.ColliderPreset;
import yesman.epicfight.main.EpicFightExtensibleEnums;
import yesman.epicfight.main.EpicFightSharedConstants;
import yesman.epicfight.network.EpicFightPayloadRegistration;
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

public class EpicFightFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        EpicFight.initialize(new FabricModPlatform());

        // Register payload codecs and server-side handlers with Fabric networking
        try {
            EpicFightPayloadRegistration.registerCodecs();
            EpicFightPayloadRegistration.registerServerHandlers();
        } catch (Throwable e) {
            EpicFight.LOGGER.warn("Failed to register payload networking: " + e.getMessage());
        }

        // Register SimplyTooltips provider
        try {
            // TODO: SimplyTooltips compat
        } catch (Throwable ignored) {}

        // Register configs via ForgeConfigAPIPort (NeoForge API — ModConfigSpec remaps to neoforged)
        try {
            // Register COMMON and SERVER config specs
            fuzs.forgeconfigapiport.fabric.api.neoforge.v4.NeoForgeConfigRegistry.INSTANCE.register(
                EpicFight.MODID, net.neoforged.fml.config.ModConfig.Type.COMMON,
                (net.neoforged.fml.config.IConfigSpec) yesman.epicfight.config.CommonConfig.SPEC);
            fuzs.forgeconfigapiport.fabric.api.neoforge.v4.NeoForgeConfigRegistry.INSTANCE.register(
                EpicFight.MODID, net.neoforged.fml.config.ModConfig.Type.SERVER,
                (net.neoforged.fml.config.IConfigSpec) yesman.epicfight.config.ServerConfig.SPEC);

            // Wire config loading events — inline the config value reads since v3 ModConfigEvent
            // types are remapped by Loom and don't match v4 Fabric event types
            fuzs.forgeconfigapiport.fabric.api.neoforge.v4.NeoForgeModConfigEvents.loading(EpicFight.MODID).register(config -> {
                if (config.getType() == net.neoforged.fml.config.ModConfig.Type.COMMON) {
                    yesman.epicfight.config.CommonConfig.skillBookMobDropChanceModifier =
                        yesman.epicfight.config.CommonConfig.SKILL_BOOK_MOB_DROP_CHANCE_MODIFIER.get();
                    yesman.epicfight.config.CommonConfig.skillBookChestLootModifier =
                        yesman.epicfight.config.CommonConfig.SKILL_BOOK_CHEST_LOOT_MODIFIER.get();
                } else if (config.getType() == net.neoforged.fml.config.ModConfig.Type.SERVER) {
                    yesman.epicfight.config.ServerConfig.allowCustomAnimations =
                        yesman.epicfight.config.ServerConfig.ALLOW_CUSTOM_ANIMATIONS.get();
                }
            });
        } catch (Throwable e) {
            EpicFight.LOGGER.warn("Failed to register configs: " + e.getMessage());
        }

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
        EpicFightBlocks.REGISTRY.accept();
        EpicFightItems.REGISTRY.accept();
        for (var dr : EpicFightRegistries.DEFERRED_REGISTRIES) {
            try { dr.accept(); } catch (Throwable e) { EpicFight.LOGGER.warn("Failed to register some entries: " + e.getMessage()); }
        }

        // Register command argument types (must be done after deferred registries are accepted)
        try {
            EpicFightCommandArgumentTypes.registerArgumentTypes();
        } catch (Throwable e) {
            EpicFight.LOGGER.warn("Failed to register command argument types: " + e.getMessage());
        }

        // Wire dynamic registry callbacks (for future datapack reloads) and bake registries
        try {
            EpicFightRegistries.registerDynamicCallbacks();
        } catch (Throwable e) {
            EpicFight.LOGGER.warn("Failed to register dynamic registry callbacks: " + e.getMessage());
        }
        try {
            EpicFightRegistries.bakeRegistries();
        } catch (Throwable e) {
            EpicFight.LOGGER.warn("Failed to bake registries: " + e.getMessage());
        }

        // Register item capabilities
        try {
            BuiltInRegistries.ITEM.forEach(item -> {
                EpicFightCapabilities.ITEM_CAPABILITY_PROVIDER.registerWeaponTypesByClass();
            });
        } catch (Throwable e) {
            EpicFight.LOGGER.warn("Item capability registration failed: " + e.getMessage());
        }

        // Register armatures
        try { Armatures.registerEntityTypes(); } catch (Throwable e) { EpicFight.LOGGER.warn("Armatures registration failed: " + e.getMessage()); }

        // Register default weapon types
        WeaponTypeReloadListener.registerDefaultWeaponTypes();

        // Register gamerules
        try { EpicFightGameRules.registerGameRules(); } catch (Throwable e) { EpicFight.LOGGER.warn("Gamerule registration failed: " + e.getMessage()); }

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

        // Register reload listeners with Fabric's resource manager
        // TODO: Reload listeners have inter-dependencies that need proper ordering via
        // IdentifiableResourceReloadListener.getFabricDependencies(). Currently causes NPEs
        // during world creation because listeners fire before their dependencies are loaded.
        // try {
        //     yesman.epicfight.network.EpicFightReloadListeners.register();
        // } catch (Throwable e) {
        //     EpicFight.LOGGER.warn("Failed to register reload listeners: " + e.getMessage());
        // }

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
