package yesman.epicfight;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.registry.RegistryEntryAddedCallback;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.loader.api.FabricLoader;
import yesman.epicfight.api.animation.AnimationManager;
import yesman.epicfight.api.animation.LivingMotion;
import yesman.epicfight.api.animation.LivingMotions;
import yesman.epicfight.api.data.reloader.ItemCapabilityReloadListener;
import yesman.epicfight.api.data.reloader.MobPatchReloadListener;
import yesman.epicfight.api.data.reloader.SkillReloadListener;
import yesman.epicfight.api.event.EpicFightEventHooks;
import yesman.epicfight.api.event.impl.VanillaEntityEventHooks;
import yesman.epicfight.api.event.impl.VanillaWorldEventHooks;
import yesman.epicfight.api.ex_cap.listeners.ConditionalReloadListener;
import yesman.epicfight.api.ex_cap.listeners.ItemPresetReloadListener;
import yesman.epicfight.api.ex_cap.listeners.MovesetReloadListener;
import yesman.epicfight.api.ex_cap.listeners.WeaponModifierReloadListener;
import yesman.epicfight.client.online.cosmetics.Emote;
import yesman.epicfight.compat.ICompatModule;
import yesman.epicfight.compat.MinecraftMod;
import yesman.epicfight.compat.mcreator.MCreatorPlayerAnimationsCompat;
import yesman.epicfight.gameasset.Animations;
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
import yesman.epicfight.registry.entries.EpicFightCreativeTabs;
import yesman.epicfight.registry.entries.EpicFightItems;
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
import yesman.epicfight.world.capabilities.entitypatch.EntityPatch;
import yesman.epicfight.world.capabilities.entitypatch.Faction;
import yesman.epicfight.world.capabilities.entitypatch.Factions;
import yesman.epicfight.world.capabilities.item.CapabilityItem;
import yesman.epicfight.world.capabilities.item.WeaponTypeReloadListener;
import yesman.epicfight.world.capabilities.provider.CommonEntityPatchProvider;
import yesman.epicfight.world.gamerule.EpicFightGameRules;
import yesman.epicfight.world.item.SkillBookItem;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;

import java.nio.file.Files;
import java.nio.file.Path;

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

        // SimplyTooltips compat is handled via the MinecraftMod compat module loop below

        // Register configs via ForgeConfigAPIPort (NeoForge API — ModConfigSpec implements IConfigSpec)
        try {
            // Register COMMON and SERVER config specs
            fuzs.forgeconfigapiport.fabric.api.neoforge.v4.NeoForgeConfigRegistry.INSTANCE.register(
                EpicFight.MODID, net.neoforged.fml.config.ModConfig.Type.COMMON,
                yesman.epicfight.config.CommonConfig.SPEC);
            fuzs.forgeconfigapiport.fabric.api.neoforge.v4.NeoForgeConfigRegistry.INSTANCE.register(
                EpicFight.MODID, net.neoforged.fml.config.ModConfig.Type.SERVER,
                yesman.epicfight.config.ServerConfig.SPEC);

            // Wire config loading events
            fuzs.forgeconfigapiport.fabric.api.neoforge.v4.NeoForgeModConfigEvents.loading(EpicFight.MODID).register(config -> {
                if (config.getType() == net.neoforged.fml.config.ModConfig.Type.COMMON) {
                    yesman.epicfight.config.CommonConfig.onLoad(config);
                } else if (config.getType() == net.neoforged.fml.config.ModConfig.Type.SERVER) {
                    yesman.epicfight.config.ServerConfig.onLoad(config);
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

        // Register animation registry event BEFORE deferred registries
        // On NeoForge, animations are registered during FMLConstructModEvent (which fires before RegisterEvent).
        // Skills reference Animations.BIPED_ROLL_FORWARD etc. in their constructors, so animations must be set first.
        AnimationManager.addNoWarningModId(EpicFight.EPICSKINS_MODID);
        AnimationManager.AnimationRegistryEvent animationRegistryEvent = new AnimationManager.AnimationRegistryEvent();
        Animations.registerAnimations(animationRegistryEvent);
        try {
            yesman.epicfight.epicskins.animation.EpicSkinsAnimations.registerAnimations(animationRegistryEvent);
        } catch (Throwable ignored) {}
        animationRegistryEvent.getBuilders().stream()
            .sorted(java.util.Comparator.comparing(AnimationManager.AnimationBuilder::namespace))
            .forEach(builder -> builder.task().accept(builder));

        // Register all deferred registries (skills reference animation accessors in their constructors)
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

        // Register entity patches (vanilla mobs + player)
        try {
            EpicFightCapabilities.ENTITY_PATCH_PROVIDER.registerVanillaEntityPatches();
            EpicFight.LOGGER.info("EpicFight entity patches registered");
        } catch (Throwable e) {
            EpicFight.LOGGER.warn("Failed to register entity patches: " + e.getMessage());
        }

        // Wire EntityJoinLevelEvent equivalent via Fabric API
        // On NeoForge, EntityJoinLevelEvent fired when any entity joined a world.
        // On Fabric, ServerEntityEvents.ENTITY_LOAD is the standard equivalent.
        // This is critical: LivingEntityPatch.onJoinWorld sets the WEIGHT attribute,
        // and without it, knockback calculations divide by zero (40.0F / weight = Infinity).
        ServerEntityEvents.ENTITY_LOAD.register((entity, serverLevel) -> {
            try {
                VanillaEntityEventHooks.onJoinLevel(entity, serverLevel, false);

                // On NeoForge, Entity.onAddedToLevel() is a patched method that fires when
                // an entity is added to the level's entity storage. Fabric doesn't have this
                // method, so we call the entity patch's onAddedToLevel() here instead.
                EpicFightCapabilities.getUnparameterizedEntityPatch(entity, EntityPatch.class).ifPresent(entitypatch -> {
                    try { entitypatch.onAddedToLevel(); } catch (Throwable ignored) {}
                });

                // Cancel spawning enderman on the main island where Ender Dragon exists
                if (entity.getType() == net.minecraft.world.entity.EntityType.ENDERMAN) {
                    if (VanillaEntityEventHooks.onEnderManSapwns((net.minecraft.world.entity.monster.EnderMan) entity)) {
                        entity.discard();
                    }
                }
            } catch (Throwable e) {
                EpicFight.LOGGER.error("[EpicFight] ServerEntityEvents.ENTITY_LOAD exception for entity {}", entity, e);
            }
        });

        // Register entity attributes (replaces NeoForge's EntityAttributeCreationEvent / EntityAttributeModificationEvent)
        // On Fabric, we collect the modifications into static maps and apply them via MixinDefaultAttributes
        try {
            yesman.epicfight.platform.fabric.event.EntityAttributeCreationEvent creationEvent = new yesman.epicfight.platform.fabric.event.EntityAttributeCreationEvent();
            EpicFightAttributes.EventBus.entityAttributeCreationEvent(creationEvent);

            yesman.epicfight.platform.fabric.event.EntityAttributeModificationEvent modificationEvent = new yesman.epicfight.platform.fabric.event.EntityAttributeModificationEvent();
            EpicFightAttributes.EventBus.entityAttributeModificationEvent(modificationEvent);
            yesman.epicfight.platform.fabric.event.EntityAttributeModificationEvent.logSummary();

            EpicFight.LOGGER.info("EpicFight entity attributes registered");
        } catch (Throwable e) {
            EpicFight.LOGGER.warn("Failed to register entity attributes: " + e.getMessage());
        }

        // Register spawn placements for WitherSkeletonMinion (replaces NeoForge's RegisterSpawnPlacementsEvent)
        // On Fabric, SpawnPlacements.register is package-private, so we use a mixin accessor.
        try {
            yesman.epicfight.platform.fabric.mixin.SpawnPlacementsAccessor.epicfight$register(
                EpicFightEntityTypes.WITHER_SKELETON_MINION.get(),
                net.minecraft.world.entity.SpawnPlacementTypes.ON_GROUND,
                net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                net.minecraft.world.entity.monster.Monster::checkAnyLightMonsterSpawnRules
            );
        } catch (Throwable e) {
            EpicFight.LOGGER.warn("Failed to register spawn placements: " + e.getMessage());
        }

        // Register item capability type mappings (called once, not per-item)
        try {
            EpicFightCapabilities.ITEM_CAPABILITY_PROVIDER.registerWeaponTypesByClass();
        } catch (Throwable e) {
            EpicFight.LOGGER.warn("Item capability registration failed: " + e.getMessage());
        }

        // Register armatures
        try { Armatures.registerEntityTypes(); } catch (Throwable e) { EpicFight.LOGGER.warn("Armatures registration failed: " + e.getMessage()); }

        // Register default weapon types
        WeaponTypeReloadListener.registerDefaultWeaponTypes();

        // Add Skill Books to the EpicFight creative tab — port of NeoForge's buildCreativeTabWithSkillBooks
        // In NeoForge, BuildCreativeModeTabContentsEvent fires for each tab and adds skill book items
        // for each learnable skill. On Fabric, we use ItemGroupEvents.modifyEntriesEvent.
        try {
            ItemGroupEvents.modifyEntriesEvent(EpicFightCreativeTabs.ITEMS.getKey()).register(entries -> {
                EpicFightRegistries.SKILL.holders()
                    .filter(skill -> skill.value().getCategory().learnable() && skill.value().getCreativeTab() == null)
                    .forEach(holder -> {
                        ItemStack stack = new ItemStack(EpicFightItems.SKILLBOOK.get());
                        SkillBookItem.setContainingSkill(holder, stack);
                        entries.accept(stack);
                    });
            });
        } catch (Throwable e) {
            EpicFight.LOGGER.warn("Failed to add skill books to creative tab: " + e.getMessage());
        }

        // Register gamerules
        try { EpicFightGameRules.registerGameRules(); } catch (Throwable e) { EpicFight.LOGGER.warn("Gamerule registration failed: " + e.getMessage()); }

        // Register potions
        EpicFightPotions.addRecipes();
        EpicFightMobEffects.addOffhandModifier();

        // Register skill book loot tables — port of NeoForge's LootTableLoadEvent + global loot modifier
        // 1. Register the SKILLBOOK_LOOT_TABLE event callback to populate entity skill book drops
        // 2. Register LootTableEvents.MODIFY to inject skill book pools into chest and entity loot tables
        EpicFightEventHooks.Registry.SKILLBOOK_LOOT_TABLE.registerEvent(event -> {
            EpicFightLootTables.createSkillLootTable(event);
        });
        try {
            EpicFightLootTables.registerLootTableEvents();
        } catch (Throwable e) {
            EpicFight.LOGGER.warn("Failed to register loot table events: " + e.getMessage());
        }

        // Register commands
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            PlayerModeCommand.register(dispatcher);
            PlayerSkillCommand.register(dispatcher);
            PlayerStaminaCommand.register(dispatcher);
            AnimatorCommand.register(dispatcher);
        });

        // Register reload listeners with Fabric's resource manager
        try {
            yesman.epicfight.network.EpicFightReloadListeners.register();
        } catch (Throwable e) {
            EpicFight.LOGGER.warn("Failed to register reload listeners: " + e.getMessage());
        }

        // Wire datapack sync — fires when a player joins (replaces NeoForge's OnDatapackSyncEvent)
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            try {
                VanillaWorldEventHooks.onDatapackSync(handler.getPlayer());
            } catch (Throwable e) {
                EpicFight.LOGGER.warn("Failed to sync EpicFight datapack data to player: " + e.getMessage());
            }
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

        // MCreator compat — loaded if any mod provides bedrock_animations data
        // On NeoForge, this checks each mod file's data directory for a bedrock_animations subdirectory.
        // On Fabric, we check each mod's source paths for the same.
        if (isClientSide) {
            try {
                boolean hasBedrockAnimations = FabricLoader.getInstance().getAllMods().stream().anyMatch(modContainer -> {
                    try {
                        return modContainer.getOrigin().getPaths().stream().anyMatch(path -> {
                            try {
                                Path dataPath = path.resolve("data");
                                if (!Files.exists(dataPath)) return false;
                                try (var stream = Files.list(dataPath)) {
                                    return stream.anyMatch(namespace -> Files.exists(namespace.resolve("bedrock_animations")));
                                }
                            } catch (Exception e) {
                                return false;
                            }
                        });
                    } catch (Exception e) {
                        return false;
                    }
                });
                if (hasBedrockAnimations) {
                    ICompatModule.loadCompatModule(MCreatorPlayerAnimationsCompat.class);
                }
            } catch (Throwable e) {
                EpicFight.LOGGER.warn("Failed to check MCreator compat: " + e.getMessage());
            }
        }

        EpicFight.LOGGER.info("Epic Fight Fabric initialized successfully!");
    }
}
