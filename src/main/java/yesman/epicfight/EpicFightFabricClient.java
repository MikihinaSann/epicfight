package yesman.epicfight;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientWorldEvents;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.CoreShaderRegistrationCallback;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import net.minecraft.client.Minecraft;
import yesman.epicfight.api.client.input.action.EpicFightInputAction;
import yesman.epicfight.api.client.input.action.InputAction;
import yesman.epicfight.api.client.input.action.MinecraftInputAction;
import yesman.epicfight.client.events.engine.IEventBasedEngine;
import yesman.epicfight.client.gui.screen.SkillBookScreen;
import yesman.epicfight.client.gui.widgets.AnchoredButton;
import yesman.epicfight.client.gui.widgets.ColorDeterminator;
import yesman.epicfight.client.gui.widgets.common.WidgetTheme;
import yesman.epicfight.client.input.EpicFightKeyMappings;
import yesman.epicfight.client.model.SeparateTransformsModelLoadingPlugin;
import yesman.epicfight.client.particle.AirBurstParticle;
import yesman.epicfight.client.particle.AnimationTrailParticle;
import yesman.epicfight.client.particle.AshDirectionalParticle;
import yesman.epicfight.client.particle.BloodParticle;
import yesman.epicfight.client.particle.BladeRushParticle;
import yesman.epicfight.client.particle.CatharsisParticle;
import yesman.epicfight.client.particle.CutParticle;
import yesman.epicfight.client.particle.DustParticle;
import yesman.epicfight.client.particle.EnderParticle;
import yesman.epicfight.client.particle.EntityAfterimageParticle;
import yesman.epicfight.client.particle.EviscerateParticle;
import yesman.epicfight.client.particle.FeatherParticle;
import yesman.epicfight.client.particle.ForceFieldEndParticle;
import yesman.epicfight.client.particle.ForceFieldParticle;
import yesman.epicfight.client.particle.GroundSlamParticle;
import yesman.epicfight.client.particle.HitBluntParticle;
import yesman.epicfight.client.particle.HitCutParticle;
import yesman.epicfight.client.particle.LaserParticle;
import yesman.epicfight.client.particle.ProjectileTrailParticle;
import yesman.epicfight.client.particle.TsunamiSplashParticle;
import yesman.epicfight.client.renderer.EpicFightShaders;
import yesman.epicfight.client.renderer.blockentity.FractureBlockRenderer;
import yesman.epicfight.client.renderer.entity.DroppedNetherStarRenderer;
import yesman.epicfight.client.renderer.entity.WitherGhostRenderer;
import yesman.epicfight.client.renderer.entity.WitherSkeletonMinionRenderer;
import yesman.epicfight.client.renderer.patched.item.EpicFightItemProperties;
import yesman.epicfight.client.renderer.patched.item.RenderItemBase;
import yesman.epicfight.client.renderer.shader.compute.loader.ComputeShaderProvider;
import yesman.epicfight.network.EpicFightClientPayloadRegistration;
import yesman.epicfight.network.EpicFightReloadListeners;
import yesman.epicfight.platform.fabric.client.FabricClientModPlatform;
import yesman.epicfight.registry.entries.EpicFightBlockEntities;
import yesman.epicfight.registry.entries.EpicFightEntityTypes;
import yesman.epicfight.registry.entries.EpicFightParticles;
import yesman.epicfight.world.capabilities.provider.CommonEntityPatchProvider;

public class EpicFightFabricClient implements ClientModInitializer {

    private static boolean computeShaderChecked = false;

    @Override
    public void onInitializeClient() {
        EpicFightClient.initialize(new FabricClientModPlatform());

        // Register core shaders — NeoForge uses @SubscribeEvent on RegisterShadersEvent
        // On Fabric we use CoreShaderRegistrationCallback which fires during resource reload
        // (on the render thread, so the OpenGL context is current).
        CoreShaderRegistrationCallback.EVENT.register(context -> {
            try {
                context.register(
                        EpicFight.identifier("solid_model"),
                        DefaultVertexFormat.POSITION_COLOR_NORMAL,
                        shader -> EpicFightShaders.positionColorNormalShader = shader);
                EpicFight.LOGGER.info("EpicFight solid_model shader registered");
            } catch (Throwable e) {
                EpicFight.LOGGER.warn("Failed to register solid_model shader: {}", e.getMessage());
            }

            // Register compute shaders if the hardware supports them.
            // The Fabric RegistrationContext does not expose a ResourceProvider, so we
            // build the NeoForge stub RegisterShadersEvent with Minecraft's resource manager.
            try {
                ComputeShaderProvider.checkIfSupports();
                if (ComputeShaderProvider.supportComputeShader()) {
                    net.neoforged.neoforge.client.event.RegisterShadersEvent evt =
                            new net.neoforged.neoforge.client.event.RegisterShadersEvent(
                                    Minecraft.getInstance().getResourceManager());
                    ComputeShaderProvider.epicfight$registerComputeShaders(evt);
                }
            } catch (Throwable e) {
                EpicFight.LOGGER.warn("Compute shader registration failed: {}", e.getMessage());
            }
        });

        // Register entity renderers for Epic Fight's custom entities
        // On NeoForge this is done via EntityRenderersEvent.RegisterRenderers
        // On Fabric we use EntityRendererRegistry.register()
        try {
            EntityRendererRegistry.register(EpicFightEntityTypes.AREA_EFFECT_BREATH.get(), net.minecraft.client.renderer.entity.NoopRenderer::new);
            EntityRendererRegistry.register(EpicFightEntityTypes.DROPPED_NETHER_STAR.get(), DroppedNetherStarRenderer::new);
            EntityRendererRegistry.register(EpicFightEntityTypes.DEATH_HARVEST_ORB.get(), net.minecraft.client.renderer.entity.NoopRenderer::new);
            EntityRendererRegistry.register(EpicFightEntityTypes.DODGE_LOCATION_INDICATOR.get(), net.minecraft.client.renderer.entity.NoopRenderer::new);
            EntityRendererRegistry.register(EpicFightEntityTypes.WITHER_GHOST_CLONE.get(), WitherGhostRenderer::new);
            EntityRendererRegistry.register(EpicFightEntityTypes.WITHER_SKELETON_MINION.get(), WitherSkeletonMinionRenderer::new);
            BlockEntityRendererRegistry.register(EpicFightBlockEntities.FRACTURE.get(), FractureBlockRenderer::new);
            EpicFight.LOGGER.info("EpicFight entity renderers registered");
        } catch (Throwable e) {
            EpicFight.LOGGER.warn("Failed to register entity renderers: " + e.getMessage());
        }

        // Register particle providers — NeoForge uses RegisterParticleProvidersEvent
        // On Fabric we use ParticleFactoryRegistry
        try {
            ParticleFactoryRegistry registry = ParticleFactoryRegistry.getInstance();
            // registerSpriteSet equivalents
            registry.register(EpicFightParticles.ENDERMAN_DEATH_EMIT.get(), EnderParticle.EndermanDeathEmitProvider::new);
            registry.register(EpicFightParticles.HIT_BLUNT.get(), HitBluntParticle.Provider::new);
            registry.register(EpicFightParticles.CUT.get(), CutParticle.Provider::new);
            registry.register(EpicFightParticles.NORMAL_DUST.get(), DustParticle.NormalDustProvider::new);
            registry.register(EpicFightParticles.DUST_EXPANSIVE.get(), DustParticle.ExpansiveDustProvider::new);
            registry.register(EpicFightParticles.DUST_CONTRACTIVE.get(), DustParticle.ContractiveDustProvider::new);
            registry.register(EpicFightParticles.BLOOD.get(), BloodParticle.Provider::new);
            registry.register(EpicFightParticles.BLADE_RUSH_SKILL.get(), BladeRushParticle.Provider::new);
            registry.register(EpicFightParticles.BREATH_FLAME.get(), EnderParticle.BreathFlameProvider::new);
            registry.register(EpicFightParticles.TSUNAMI_SPLASH.get(), TsunamiSplashParticle.Provider::new);
            registry.register(EpicFightParticles.FEATHER.get(), FeatherParticle.Provider::new);
            registry.register(EpicFightParticles.ASH_DIRECTIONAL.get(), AshDirectionalParticle.Provider::new);
            registry.register(EpicFightParticles.CATHARSIS.get(), CatharsisParticle.Provider::new);
            // registerSpecial equivalents
            registry.register(EpicFightParticles.HIT_BLADE.get(), new HitCutParticle.Provider());
            registry.register(EpicFightParticles.EVISCERATE.get(), new EviscerateParticle.Provider());
            registry.register(EpicFightParticles.GROUND_SLAM.get(), new GroundSlamParticle.Provider());
            registry.register(EpicFightParticles.GROUND_FRACTURE.get(), new GroundSlamParticle.BlockParticleProvider());
            registry.register(EpicFightParticles.FORCE_FIELD.get(), new ForceFieldParticle.Provider());
            registry.register(EpicFightParticles.FORCE_FIELD_END.get(), new ForceFieldEndParticle.Provider());
            registry.register(EpicFightParticles.ADRENALINE_PLAYER_BEATING.get(), new EntityAfterimageParticle.AdrenalineParticleProvider());
            registry.register(EpicFightParticles.WHITE_AFTERIMAGE.get(), new EntityAfterimageParticle.WhiteAfterimageProvider());
            registry.register(EpicFightParticles.LASER.get(), new LaserParticle.Provider());
            registry.register(EpicFightParticles.NEUTRALIZE.get(), new DustParticle.ExpansiveMetaParticle.Provider());
            registry.register(EpicFightParticles.BOSS_CASTING.get(), new DustParticle.ContractiveMetaParticle.Provider());
            registry.register(EpicFightParticles.SWING_TRAIL.get(), new AnimationTrailParticle.Provider());
            registry.register(EpicFightParticles.PROJECTILE_TRAIL.get(), new ProjectileTrailParticle.Provider());
            registry.register(EpicFightParticles.AIR_BURST.get(), new AirBurstParticle.Provider());
            EpicFight.LOGGER.info("EpicFight particle providers registered");
        } catch (Throwable e) {
            EpicFight.LOGGER.warn("Failed to register particle providers: " + e.getMessage());
        }

        // Initialize render/control engines — registers Fabric callbacks for render pipeline
        // The null params are ignored; gameEventBus()/modEventBus() register Fabric callbacks directly
        IEventBasedEngine.init(null, null);

        // Register client-side reload listeners (item skins, meshes, joint masks, animations)
        // These correspond to NeoForge's RegisterClientReloadListenersEvent registrations
        EpicFightReloadListeners.registerClient();

        // Register the separate_transforms model loader plugin (handles neoforge:separate_transforms format)
        ModelLoadingPlugin.register(new SeparateTransformsModelLoadingPlugin());

        // Register the built-in "epicfight_legacy" resource pack — port of NeoForge's addPackFindersEvent
        // On NeoForge, this uses AddPackFindersEvent + Pack.readMetaAndCreate.
        // On Fabric, we use ResourceManagerHelper.registerBuiltinResourcePack which expects
        // the pack to be under "resourcepacks/<id path>/" in the mod JAR.
        try {
            net.fabricmc.fabric.api.resource.ResourceManagerHelper.registerBuiltinResourcePack(
                EpicFight.identifier("epicfight_legacy"),
                FabricLoader.getInstance().getModContainer(EpicFight.MODID).orElseThrow(),
                net.fabricmc.fabric.api.resource.ResourcePackActivationType.NORMAL
            );
        } catch (Throwable e) {
            EpicFight.LOGGER.warn("Failed to register epicfight_legacy resource pack: {}", e.getMessage());
        }

        // Register CLIENT config via ForgeConfigAPIPort
        try {
            fuzs.forgeconfigapiport.fabric.api.neoforge.v4.NeoForgeConfigRegistry.INSTANCE.register(
                EpicFight.MODID, net.neoforged.fml.config.ModConfig.Type.CLIENT,
                yesman.epicfight.config.ClientConfig.SPEC);

            fuzs.forgeconfigapiport.fabric.api.neoforge.v4.NeoForgeModConfigEvents.loading(EpicFight.MODID).register(config -> {
                if (config.getType() == net.neoforged.fml.config.ModConfig.Type.CLIENT) {
                    yesman.epicfight.config.ClientConfig.epicfight$modConfigLoading(config);
                    EpicFight.LOGGER.info("EpicFight client config loaded");
                }
            });
        } catch (Throwable e) {
            EpicFight.LOGGER.warn("Failed to register client config: " + e.getMessage());
        }

        // Register client-bound payload handlers with Fabric networking
        try {
            EpicFightClientPayloadRegistration.registerClientHandlers();
        } catch (Throwable e) {
            EpicFight.LOGGER.warn("Failed to register client payload handlers: " + e.getMessage());
        }

        // Register client-side extensible enums
        try {
            InputAction.ENUM_MANAGER.registerEnumCls(EpicFight.MODID, EpicFightInputAction.class);
            InputAction.ENUM_MANAGER.registerEnumCls("minecraft", MinecraftInputAction.class);
            InputAction.ENUM_MANAGER.loadEnum();
            WidgetTheme.ENUM_MANAGER.loadEnum();
        } catch (Throwable e) {
            EpicFight.LOGGER.warn("Client enum registration failed: {}", e.getMessage());
        }

        // Register key mappings
        try {
            EpicFightKeyMappings.registerKeys();
        } catch (Throwable e) {
            EpicFight.LOGGER.warn("Key mapping registration failed: {}", e.getMessage());
        }

        // Register client player patches
        try {
            CommonEntityPatchProvider.ClientModule.registerClientPlayerPatches();
        } catch (Throwable e) {
            EpicFight.LOGGER.warn("Client player patch registration failed: {}", e.getMessage());
        }

        // Register skill book icons
        try {
            SkillBookScreen.registerIconItems();
        } catch (Throwable e) {
            EpicFight.LOGGER.warn("Skill book icon registration failed: {}", e.getMessage());
        }

        // Register item properties
        try {
            EpicFightItemProperties.registerItemProperties();
        } catch (Throwable e) {
            EpicFight.LOGGER.warn("Item properties registration failed: {}", e.getMessage());
        }

        // Defer compute shader check to first client tick (OpenGL context not ready during init)
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (!computeShaderChecked) {
                computeShaderChecked = true;
                try {
                    // Initialize item renderers (requires Minecraft.gameRenderer.itemInHandRenderer)
                    // In NeoForge this is done in RenderLevelStageEvent.RegisterStageEvent
                    RenderItemBase.initItemRenderers(client);
                    ComputeShaderProvider.checkIfSupports();
                    // Reset item preferences if empty — port of NeoForge's FMLClientSetupEvent
                    try {
                        if (yesman.epicfight.config.ClientConfig.combatCategorizedItems.isEmpty() && yesman.epicfight.config.ClientConfig.miningCategorizedItems.isEmpty()) {
                            yesman.epicfight.client.gui.screen.config.ItemsPreferenceScreen.resetItems();
                        }
                    } catch (Throwable e) {
                        EpicFight.LOGGER.warn("Failed to reset item preferences: {}", e.getMessage());
                    }
                    // Retry compute shader registration in case the CoreShaderRegistrationCallback
                    // ran before the GL context was fully ready (or support wasn't detected then).
                    if (ComputeShaderProvider.supportComputeShader() && ComputeShaderProvider.meshComputeVanilla == null) {
                        net.neoforged.neoforge.client.event.RegisterShadersEvent evt =
                                new net.neoforged.neoforge.client.event.RegisterShadersEvent(
                                        Minecraft.getInstance().getResourceManager());
                        ComputeShaderProvider.epicfight$registerComputeShaders(evt);
                    }
                } catch (Throwable e) {
                    EpicFight.LOGGER.warn("Compute shader check failed: {}", e.getMessage());
                }
            }
        });

        // Wire client level load/unload — replaces NeoForge's LevelEvent.Load / LevelEvent.Unload
        // Used by FakeLevel to create/unload the fake client level for rendering previews
        ClientWorldEvents.AFTER_CLIENT_WORLD_CHANGE.register((client, newLevel) -> {
            try {
                if (newLevel instanceof yesman.epicfight.client.world.util.FakeLevel) return;
                if (newLevel instanceof net.minecraft.client.multiplayer.ClientLevel clientLevel) {
                    yesman.epicfight.client.world.util.FakeLevel.getFakeLevel(clientLevel);
                }
            } catch (Throwable e) {
                EpicFight.LOGGER.warn("FakeLevel load failed: {}", e.getMessage());
            }
        });

        // On client disconnect, unload the FakeLevel
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            try {
                yesman.epicfight.client.world.util.FakeLevel.unloadFakeLevel();
            } catch (Throwable e) {
                EpicFight.LOGGER.warn("FakeLevel unload failed: {}", e.getMessage());
            }
        });

        EpicFight.LOGGER.info("Epic Fight Fabric client initialized successfully!");
    }
}
