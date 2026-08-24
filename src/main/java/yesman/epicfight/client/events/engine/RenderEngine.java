package yesman.epicfight.client.events.engine;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.StringUtil;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;




import net.sweenus.simplytooltips.client.TooltipNavigationConfig;
import net.sweenus.simplytooltips.client.render.ItemThemeRegistry;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import yesman.epicfight.EpicFight;
import yesman.epicfight.api.animation.JointTransform;
import yesman.epicfight.api.client.animation.AnimationSubFileReader;
import yesman.epicfight.api.client.camera.EpicFightCameraAPI;
import yesman.epicfight.api.client.event.EpicFightClientEventHooks;
import yesman.epicfight.api.client.event.types.registry.RegisterPatchedRenderersEvent;
import yesman.epicfight.api.client.event.types.render.RenderEnderDragonEvent;
import yesman.epicfight.api.client.input.InputManager;
import yesman.epicfight.api.client.input.action.EpicFightInputAction;
import yesman.epicfight.api.client.model.Meshes;
import yesman.epicfight.api.utils.math.MathUtils;
import yesman.epicfight.api.utils.math.OpenMatrix4f;
import yesman.epicfight.api.utils.math.Vec3f;
import yesman.epicfight.client.ClientEngine;
import yesman.epicfight.client.gui.BattleModeGui;
import yesman.epicfight.client.gui.EntityUI;
import yesman.epicfight.client.gui.VersionNotifier;
import yesman.epicfight.client.gui.screen.overlay.OverlayManager;
import yesman.epicfight.client.input.EpicFightKeyMappings;
import yesman.epicfight.client.mesh.HumanoidMesh;
import yesman.epicfight.client.renderer.EpicFightRenderTypes;
import yesman.epicfight.client.renderer.FakeBlockRenderer;
import yesman.epicfight.client.renderer.FirstPersonRenderer;
import yesman.epicfight.client.renderer.VanillaFakeBlockRenderer;
import yesman.epicfight.client.renderer.patched.entity.*;
import yesman.epicfight.client.renderer.patched.item.*;
import yesman.epicfight.client.world.capabilites.entitypatch.player.LocalPlayerPatch;
import yesman.epicfight.compat.MinecraftMod;
import yesman.epicfight.config.ClientConfig;
import yesman.epicfight.platform.ModPlatformProvider;
import yesman.epicfight.registry.entries.EpicFightEntityTypes;
import yesman.epicfight.skill.Skill;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;
import yesman.epicfight.world.capabilities.entitypatch.boss.BossPatch;
import yesman.epicfight.world.capabilities.entitypatch.boss.enderdragon.EnderDragonPatch;
import yesman.epicfight.world.capabilities.item.*;
import yesman.epicfight.world.gamerule.EpicFightGameRules;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

@SuppressWarnings("rawtypes")
public class RenderEngine implements IEventBasedEngine {
    private static final RenderEngine INSTANCE = new RenderEngine();

    public static RenderEngine getInstance() {
        return INSTANCE;
    }

    public final BattleModeGui battleModeHUD;
    public final VersionNotifier versionNotifier;
    public final Minecraft minecraft;

    private final BiMap<EntityType<?>, Function<EntityType<?>, PatchedEntityRenderer>> entityRendererProvider;
    private final Map<EntityType<?>, PatchedEntityRenderer> entityRendererCache;
    private final Map<Item, RenderItemBase> itemRendererMapByInstance;
    private final Map<Class<?>, RenderItemBase> itemRendererMapByClass;
    private final Map<UUID, BossPatch> bossEventOwners = new ConcurrentHashMap<> ();
    private final OverlayManager overlayManager;
    private FakeBlockRenderer fakeBlockRenderer;

    private FirstPersonRenderer firstPersonRenderer;
    private PHumanoidRenderer<?, ?, ?, ?, ?> basicHumanoidRenderer;
    private int modelInitTimer;

    private RenderEngine() {
        this.minecraft = Minecraft.getInstance();
        this.battleModeHUD = new BattleModeGui(this.minecraft);
        this.versionNotifier = new VersionNotifier(this.minecraft);
        this.entityRendererProvider = HashBiMap.create();
        this.entityRendererCache = new HashMap<> ();
        this.itemRendererMapByInstance = new HashMap<> ();
        this.itemRendererMapByClass = new HashMap<> ();
        this.overlayManager = new OverlayManager();
        this.fakeBlockRenderer = new VanillaFakeBlockRenderer();
    }

    public void reloadFakeBlockRenderer(FakeBlockRenderer fakeBlockRenderer) {
        this.fakeBlockRenderer = fakeBlockRenderer;
    }

    public void reloadItemRenderers(Map<ResourceLocation, JsonElement> objects) {
        //Clear item renderers
        this.itemRendererMapByInstance.clear();
        this.itemRendererMapByClass.clear();

        Map<ResourceLocation, Function<JsonElement, RenderItemBase>> itemRenderers = new HashMap<> ();
        itemRenderers.put(ResourceLocation.withDefaultNamespace("base"), RenderItemBase::new);
        itemRenderers.put(ResourceLocation.withDefaultNamespace("ranged"), RenderTwoHandedRangedWeapon::new);
        itemRenderers.put(ResourceLocation.withDefaultNamespace("map"), RenderFilledMap::new);
        itemRenderers.put(ResourceLocation.withDefaultNamespace("shield"), RenderShield::new);
        itemRenderers.put(ResourceLocation.withDefaultNamespace("trident"), RenderTrident::new);
        itemRenderers.put(EpicFight.identifier("uchigatana"), RenderKatana::new);

        EpicFightClientEventHooks.Registry.PATCHED_ITEM.post(new RegisterPatchedRenderersEvent.Item(itemRenderers));

        for (Map.Entry<ResourceLocation, JsonElement> entry : objects.entrySet()) {
            ResourceLocation rl = entry.getKey();
            String pathString = rl.getPath();
            ResourceLocation registryName = ResourceLocation.fromNamespaceAndPath(rl.getNamespace(), pathString);

            if (!BuiltInRegistries.ITEM.containsKey(registryName)) {
                EpicFight.LOGGER.warn("Failed to load item skin: no item named " + registryName);
                continue;
            }

            Item item = BuiltInRegistries.ITEM.get(registryName);
            Function<JsonElement, RenderItemBase> rendererProvider;

            if (entry.getValue().getAsJsonObject().has("renderer")) {
                ResourceLocation rendererName = ResourceLocation.parse(entry.getValue().getAsJsonObject().get("renderer").getAsString());

                if (itemRenderers.containsKey(rendererName)) {
                    rendererProvider = itemRenderers.get(rendererName);
                } else {
                    EpicFight.LOGGER.warn("No renderer named " + rendererName);
                    rendererProvider = RenderItemBase::new;
                }
            } else {
                rendererProvider = RenderItemBase::new;
            }

            RenderItemBase itemRenderer = rendererProvider.apply(entry.getValue());
            this.itemRendererMapByInstance.put(item, itemRenderer);
        }

        RenderItemBase baseRenderer = new RenderItemBase(new JsonObject());
        RenderTwoHandedRangedWeapon bowRenderer = new RenderTwoHandedRangedWeapon(objects.get(BuiltInRegistries.ITEM.getKey(Items.BOW)).getAsJsonObject());
        RenderTwoHandedRangedWeapon crossbowRenderer = new RenderTwoHandedRangedWeapon(objects.get(BuiltInRegistries.ITEM.getKey(Items.CROSSBOW)).getAsJsonObject());
        RenderTrident tridentRenderer = new RenderTrident(objects.get(BuiltInRegistries.ITEM.getKey(Items.TRIDENT)).getAsJsonObject());
        RenderFilledMap mapRenderer = new RenderFilledMap(objects.get(BuiltInRegistries.ITEM.getKey(Items.FILLED_MAP)).getAsJsonObject());
        RenderShield shieldRenderer = new RenderShield(objects.get(BuiltInRegistries.ITEM.getKey(Items.SHIELD)).getAsJsonObject());

        // Render by item classes
        this.itemRendererMapByClass.put(BowItem.class, bowRenderer);
        this.itemRendererMapByClass.put(CrossbowItem.class, crossbowRenderer);
        this.itemRendererMapByClass.put(ShieldItem.class, baseRenderer);
        this.itemRendererMapByClass.put(TridentItem.class, tridentRenderer);
        this.itemRendererMapByClass.put(ShieldItem.class, shieldRenderer);

        // Render by capability classes
        this.itemRendererMapByClass.put(BowCapability.class, bowRenderer);
        this.itemRendererMapByClass.put(CrossbowCapability.class, crossbowRenderer);
        this.itemRendererMapByClass.put(TridentCapability.class, tridentRenderer);
        this.itemRendererMapByClass.put(MapCapability.class, mapRenderer);
        this.itemRendererMapByClass.put(ShieldCapability.class, shieldRenderer);
    }

    public void resetRenderers() {
        this.entityRendererCache.clear();

        for (Map.Entry<EntityType<?>, Function<EntityType<?>, PatchedEntityRenderer>> entry : this.entityRendererProvider.entrySet()) {
            this.entityRendererCache.put(entry.getKey(), entry.getValue().apply(entry.getKey()));
        }

        EpicFightClientEventHooks.Registry.MODIFY_PATCHED_ENTITY.post(new RegisterPatchedRenderersEvent.ModifyEntity(this.entityRendererCache));
    }

    @SuppressWarnings("unchecked")
    public void registerCustomEntityRenderer(EntityType<?> entityType, String rendererName, CompoundTag compound) {
        if (StringUtil.isNullOrEmpty(rendererName)) {
            return;
        }

        EntityRenderDispatcher erd = this.minecraft.getEntityRenderDispatcher();
        EntityRendererProvider.Context context = new EntityRendererProvider.Context(erd, this.minecraft.getItemRenderer(), this.minecraft.getBlockRenderer(), erd.getItemInHandRenderer(), this.minecraft.getResourceManager(), this.minecraft.getEntityModels(), this.minecraft.font);

        if ("player".equals(rendererName)) {
            this.entityRendererCache.put(entityType, this.basicHumanoidRenderer);
        } else if ("epicfight:custom".equals(rendererName)) {
            if (compound.getBoolean("humanoid")) {
                this.entityRendererCache.put(entityType, new PCustomHumanoidEntityRenderer<> (Meshes.getOrCreate(ResourceLocation.parse(compound.getString("model")), (jsonAssetLoader) -> jsonAssetLoader.loadSkinnedMesh(HumanoidMesh::new)), context, entityType));
            } else {
                this.entityRendererCache.put(entityType, new PCustomEntityRenderer(Meshes.getOrCreate(ResourceLocation.parse(compound.getString("model")), (jsonAssetLoader) -> jsonAssetLoader.loadSkinnedMesh(HumanoidMesh::new)), context));
            }
        } else {
            EntityType<?> presetEntityType = BuiltInRegistries.ENTITY_TYPE.get(ResourceLocation.parse(rendererName));

            if (this.entityRendererProvider.containsKey(presetEntityType)) {
                PatchedEntityRenderer renderer = this.entityRendererProvider.get(presetEntityType).apply(entityType);

                if (!(this.minecraft.getEntityRenderDispatcher().renderers.get(entityType) instanceof LivingEntityRenderer) && (renderer instanceof PatchedLivingEntityRenderer patchedLivingEntityRenderer)) {
                    this.entityRendererCache.put(entityType, new PresetRenderer(context, entityType, (LivingEntityRenderer<LivingEntity, EntityModel<LivingEntity>>)context.getEntityRenderDispatcher().renderers.get(presetEntityType), patchedLivingEntityRenderer.getDefaultMesh()));
                } else {
                    this.entityRendererCache.put(entityType, this.entityRendererProvider.get(presetEntityType).apply(entityType));
                }
            } else {
                throw new IllegalArgumentException("Datapack Mob Patch Crash: Invalid Renderer type " + rendererName);
            }
        }
    }

    public RenderItemBase getItemRenderer(ItemStack itemstack) {
        RenderItemBase renderItem = this.itemRendererMapByInstance.get(itemstack.getItem());

        if (renderItem == null) {
            renderItem = this.findMatchingRendererByClass(itemstack.getItem().getClass());

            if (renderItem == null) {
                CapabilityItem itemCap = EpicFightCapabilities.getItemStackCapability(itemstack);
                renderItem = this.findMatchingRendererByClass(itemCap.getClass());
            }

            if (renderItem == null) {
                // Get generic renderer
                renderItem = this.itemRendererMapByInstance.get(Items.AIR);
            }

            this.itemRendererMapByInstance.put(itemstack.getItem(), renderItem);
        }

        return renderItem;
    }

    private RenderItemBase findMatchingRendererByClass(Class<?> clazz) {
        RenderItemBase renderer = null;

        for (; clazz != null && renderer == null; clazz = clazz.getSuperclass()) {
            renderer = this.itemRendererMapByClass.get(clazz);
        }

        return renderer;
    }

    @SuppressWarnings("unchecked")
    public void renderEntityArmatureModel(LivingEntity livingEntity, LivingEntityPatch<?> entitypatch, EntityRenderer<? extends Entity> renderer, MultiBufferSource buffer, PoseStack matStack, int packedLight, float partialTicks) {
        this.getEntityRenderer(livingEntity).render(livingEntity, entitypatch, renderer, buffer, matStack, packedLight, partialTicks);
    }

    public PatchedEntityRenderer getEntityRenderer(Entity entity) {
        return this.getEntityRenderer(entity.getType());
    }

    public PatchedEntityRenderer getEntityRenderer(EntityType entityType) {
        return this.entityRendererCache.get(entityType);
    }

    public boolean hasRendererFor(Entity entity) {
        return this.entityRendererCache.computeIfAbsent(entity.getType(), (key) -> this.entityRendererProvider.containsKey(key) ? this.entityRendererProvider.get(entity.getType()).apply(entity.getType()) : null) != null;
    }

    public Set<ResourceLocation> getRendererEntries() {
        Set<ResourceLocation> availableRendererEntities = this.entityRendererProvider.keySet().stream().map((entityType) -> EntityType.getKey(entityType)).collect(Collectors.toSet());
        availableRendererEntities.add(EpicFight.identifier("custom"));

        return availableRendererEntities;
    }

    public void setModelInitializerTimer(int tick) {
        this.modelInitTimer = tick;
    }

    public OverlayManager getOverlayManager() {
        return this.overlayManager;
    }

    public FirstPersonRenderer getFirstPersonRenderer() {
        return firstPersonRenderer;
    }

    public boolean shouldRenderVanillaModel() {
        return ClientEngine.getInstance().isVanillaModelDebuggingMode() || this.modelInitTimer > 0;
    }

    public void addBossEventOwner(UUID uuid, BossPatch bosspatch) {
        this.bossEventOwners.put(uuid, bosspatch);
    }

    public void removeBossEventOwner(UUID uuid, BossPatch bosspatch) {
        this.bossEventOwners.remove(uuid);
    }

    public void initHUD(LocalPlayerPatch playerpatch) {
        this.battleModeHUD.init(playerpatch);
        this.versionNotifier.init();
    }

    private void freeUnusedSources() {
        this.bossEventOwners.entrySet().removeIf((entry) -> {
            Entity entity = entry.getValue().cast().getOriginal();
            return !entity.isAlive() || entity.isRemoved();
        });

        if (!RenderSystem.isOnRenderThread()) {
            RenderSystem.recordRenderCall(() -> {
                EpicFightRenderTypes.freeUnusedWorldRenderTypes();
            });
        } else {
            EpicFightRenderTypes.freeUnusedWorldRenderTypes();
        }
    }

    public void clear() {
        EpicFightCameraAPI.getInstance().zoomOut(0);
        this.bossEventOwners.clear();

        if (!RenderSystem.isOnRenderThread()) {
            RenderSystem.recordRenderCall(() -> {
                this.resetRenderers();
                EpicFightRenderTypes.clearWorldRenderTypes();
            });
        } else {
            this.resetRenderers();
            EpicFightRenderTypes.clearWorldRenderTypes();
        }
    }

    public static boolean hitResultEquals(@Nullable HitResult hitResult, HitResult.Type hitType) {
        return hitResult == null ? false : hitType.equals(hitResult.getType());
    }

    public static boolean hitResultNotEquals(@Nullable HitResult hitResult, HitResult.Type hitType) {
        return hitResult == null ? true : !hitType.equals(hitResult.getType());
    }

    /// More strict type sensitive hit result getter by instanceof
    public static BlockHitResult asBlockHitResult(@Nullable HitResult hitResult) {
        if (hitResult == null) {
            return null;
        }

        if (hitResult.getType() == HitResult.Type.BLOCK && hitResult instanceof BlockHitResult blockHitResult) {
            return blockHitResult;
        }

        return null;
    }

    /// More strict type sensitive hit result getter by instanceof
    public static EntityHitResult asEntityHitResult(@Nullable HitResult hitResult) {
        if (hitResult == null) {
            return null;
        }

        if (hitResult.getType() == HitResult.Type.ENTITY && hitResult instanceof EntityHitResult entityHitResult) {
            return entityHitResult;
        }

        return null;
    }

    /******************
     * Forge EventHook listeners
     ******************/
    // TODO: Port to Fabric callback (was RenderLivingEvent.Pre)
    private void epicfight$renderLivingPre(Object event) {
    }

    // TODO: Port to Fabric callback (was ItemTooltipEvent)
    private boolean noSimplyTooltipsSupport(Object event) {
        return true;
    }

    // TODO: Port to Fabric callback (was ItemTooltipEvent)
    private void epicfight$itemTooltip(Object event) {
    }

    private static final Vector3f CAMERA_ROTATION_EULER = new Vector3f();
    private static final OpenMatrix4f PLAYER_ROTATION = new OpenMatrix4f();

    // TODO: Port to Fabric callback (was ComputeCameraAnglesEvent)
    private void epicfight$computeCameraAngles(Object event) {
    }

    // TODO: Port to Fabric callback (was RenderGuiEvent.Pre)
    private void epicfight$renderGuiPre(Object event) {
    }

    private static final ResourceLocation YELLOWBAR_BACKGROUND = ResourceLocation.withDefaultNamespace("boss_bar/yellow_background");
    private static final ResourceLocation YELLOWBAR_PROGRESS = ResourceLocation.withDefaultNamespace("boss_bar/yellow_progress");

    // TODO: Port to Fabric callback (was CustomizeGuiOverlayEvent.BossEventProgress)
    private void epicfight$bossEventProgress(Object event) {
    }

    // TODO: Port to Fabric callback (was RenderHandEvent)
    @SuppressWarnings("unchecked")
    private void epicfight$renderHand(Object event) {
    }

    // TODO: Port to Fabric callback (was RenderLevelStageEvent)
    private void epicfight$renderAfterLevel(Object event) {
    }

    @SuppressWarnings("unchecked")
    private void epicfight$renderEnderDragon(RenderEnderDragonEvent event) {
        EnderDragon livingentity = event.getEntity();

        if (this.hasRendererFor(livingentity)) {
            EpicFightCapabilities.getUnparameterizedEntityPatch(livingentity, EnderDragonPatch.class).ifPresent(enderdragonpatch -> {
                event.cancel();
                this.getEntityRenderer(livingentity).render(livingentity, enderdragonpatch, event.getRenderer(), event.getBuffers(), event.getPoseStack(), event.getLight(), event.getPartialRenderTick());
            });
        }
    }

    // TODO: Port to Fabric callback (was RenderFrameEvent.Pre)
    private void epicfight$renderTickPre(Object event) {
    }

    // TODO: Port to Fabric callback (was RenderFrameEvent.Post)
    private void epicfight$renderTickPost(Object event) {
    }

    // TODO: Port to Fabric callback (was ClientTickEvent.Pre)
    private void epicfight$clientTick$Pre(Object event) {
    }

    // TODO: Port to Fabric callback (was ClientTickEvent.Post)
    private void epicfight$clientTick$Post(Object event) {
    }

    // TODO: Port to Fabric callback (was LevelTickEvent.Post)
    private void epicfight$levelTickPost(Object event) {
    }

    // TODO: Port to Fabric callback (was RenderHighlightEvent.Block)
    private void epicfight$renderBlockHighlight(Object event) {
    }

    /**********************
     * Forge EventHook listeners end
     **********************/

    /**********************
     * Mod EventHook listeners
     **********************/
    // TODO: Port to Fabric callback (was EntityRenderersEvent.AddLayers)
    @SuppressWarnings("unchecked")
    private void epicfight$addLayers(Object event) {
    }
    /**********************
     * Mod EventHook listeners end
     **********************/

    @Override
    public void gameEventBus(Object gameEventBus) {
        // TODO: Port to Fabric callbacks (was NeoForge IEventBus.addListener)
        EpicFightClientEventHooks.Render.RENDER_ENDER_DRAGON.registerEvent(this::epicfight$renderEnderDragon);
    }

    @Override
    public void modEventBus(Object modEventBus) {
        // TODO: Port to Fabric callbacks (was NeoForge IEventBus.addListener)
    }

    /**
     * @deprecated Use {@link EpicFightCameraAPI#zoomIn()} instead
     */
    @Deprecated(forRemoval = true)
    public void zoomIn() {
        EpicFightCameraAPI.getInstance().zoomIn();
    }

    /**
     * @deprecated Use {@link EpicFightCameraAPI#zoomOut(int)} instead
     */
    @Deprecated(forRemoval = true)
    public void zoomOut(int zoomOutTicks) {
        EpicFightCameraAPI.getInstance().zoomOut(zoomOutTicks);
    }
}
