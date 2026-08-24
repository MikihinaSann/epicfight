# EpicFight NeoForge → Fabric 1.21.1 Port — Implementation Plan

## Overview
Port all 938 Java files + 1,232 resources from NeoForge 21.1.219 to Fabric (Loader 0.19.3, API 0.116.15+1.21.1, Loom 1.17-SNAPSHOT) on MC 1.21.1 with 100% feature parity. Create a new branch `1.21.1-fabric`. Port all 16 compat modules. Drop KubeJS, Vampirism, Werewolves (no Fabric port).

---

## Phase 1 — Build System & Project Setup

### 1.1 Branch
- `git checkout -b 1.21.1-fabric` from `1.21.1`

### 1.2 Gradle
- **`settings.gradle.kts`**: Remove NeoForge plugin portal + `gradle/build-logic` composite build. Add Fabric Maven.
- **`gradle.properties`**: Replace NeoForge versions with Fabric versions (loader 0.19.3, API 0.116.15+1.21.1, loom 1.17-SNAPSHOT, yarn 1.21.1+build.3). Keep mod metadata props.
- **`build.gradle.kts`**: Replace `net.neoforged.moddev` plugin with `net.fabricmc.fabric-loom-remap`. Use `officialMojangMappings()` (not Yarn — avoids renaming 938 files). Add all 16 compat CurseMaven deps. Add ForgeConfigAPIPort Fabric dep. Add PlayerRevive local dep (`flatDir` or includeJar). Add CurseMaven repo.
- **`gradle/libs.versions.toml`**: Replace all NeoForge compat versions with Fabric CurseMaven file IDs from the compat matrix.
- **`gradle/build-logic/`**: Remove NeoForge-specific convention plugin. Keep shared base config (encoding, Java 21, sources JAR).

### 1.3 Mod Metadata
- **Delete** `META-INF/neoforge.mods.toml`, `META-INF/accesstransformer.cfg`, `META-INF/enum_extensions.json`
- **Create** `fabric.mod.json`:
  - `id`: `epicfight`
  - `entrypoints`: `main` → `EpicFightFabric`, `client` → `EpicFightFabricClient`
  - `mixins`: all mixin configs (renamed)
  - `accessWidener`: `epicfight.accesswidener`
  - `depends`: fabricloader ≥0.19.3, minecraft ~1.21.1, java ≥21, fabric-api
  - Suggests: all 16 compat mods

### 1.4 Mixin Configs
- Rename `epicfight-platform.neoforge.mixins.json` → `epicfight-platform.fabric.mixins.json`
- Remove `"refmap"` lines from all 7 mixin JSONs (Loom auto-generates)
- Remove `epicfight-compat.vampirism.mixins.json` and `epicfight-compat.werewolves.mixins.json`
- Update `fabric.mod.json` mixin list to match

### 1.5 Access Widener
- Create `src/main/resources/epicfight.accesswidener` from `accesstransformer.cfg`:
  - `public <X>` → `accessible method <X>`
  - `public-f <X>` → `accessible method <X>` + `mutable method <X>` (for fields)
  - `protected-f <X>` → `accessible field <X>` + `mutable field <X>`
  - Fields use `accessible field`, methods use `accessible method`

---

## Phase 2 — Platform Layer

### 2.1 Fabric Platform Implementations
Create in `yesman.epicfight.platform.fabric`:

- **`FabricModPlatform`** implements `ModPlatform`:
  - `isDevelopmentEnvironment()` → `FabricLoader.getInstance().isDevelopmentEnvironment()`
  - `isModLoaded(id)` → `FabricLoader.getInstance().isModLoaded(id)`

- **`FabricClientModPlatform`** implements `ClientModPlatform`:
  - Holds `FabricKeyMappingRegistrar`
  - Constructor registers key mappings via `KeyBindingHelper.registerKeyBinding()`

- **`FabricKeyMappingRegistrar`** implements `KeyMappingRegistrar`:
  - `registerKeyMapping()` → `KeyBindingHelper.registerKeyMapping()`

### 2.2 Side Detection
- **`EpicFightSharedConstants`**: Replace `FMLEnvironment.dist` → `FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT`. Replace `FMLEnvironment.production` → `!FabricLoader.getInstance().isDevelopmentEnvironment()`.

### 2.3 Fabric Entrypoints
- **`EpicFightFabric`** implements `ModInitializer`:
  - Call `EpicFight.initialize(new FabricModPlatform())`
  - Register all DeferredRegister shims
  - Register custom registries
  - Register networking
  - Register capabilities (item capability map)
  - Register configs (ForgeConfigAPIPort)
  - Register commands (`CommandRegistrationCallback`)
  - Register reload listeners
  - Register gamerules
  - Load compat modules
  - Register extensible enums
  - Register creative tab contents (`ItemGroupEvents`)

- **`EpicFightFabricClient`** implements `ClientModInitializer`:
  - Call `EpicFightClient.initialize(new FabricClientModPlatform())`
  - Register key mappings
  - Register shaders (`CoreShaderRegistrationCallback`)
  - Register client reload listeners
  - Register compute shaders
  - Register item properties
  - Init `IEventBasedEngine` (adapted for Fabric)
  - Register client tick events, input events, render events

---

## Phase 3 — Registration System

### 3.1 DeferredRegister/DeferredHolder Shim
Create `yesman.epicfight.registry.deferred_shim`:

- **`DeferredRegisterShim<T>`**:
  - Constructor takes `ResourceKey<Registry<T>>` + mod ID
  - `register(String name, Supplier<T> supplier)` → returns `DeferredHolderShim<T>`
  - `register(String name, Function<RegistrationCallback<T>, T> factory)` → for complex registrations
  - At init time, calls `Registry.register(registry, key, supplier.get())`
  - Stores entries in a list, processed when `accept()` is called by the initializer

- **`DeferredHolderShim<T>`**:
  - Wraps `ResourceKey<T>` + `Supplier<T>`
  - `get()` → `BuiltInRegistries.REGISTRY.get(key).getValue()` or direct cached reference
  - `isPresent()` → checks if registered

### 3.2 Update 26 Registry Entry Classes
Each `EpicFight*Entries` class changes:
- `DeferredRegister.create(Registries.X, MODID)` → `new DeferredRegisterShim<>(Registries.X, MODID)`
- `DeferredHolder<Item, X>` → `DeferredHolderShim<Item>` (or typed)
- `.register(modEventBus)` → `.accept()` called from `EpicFightFabric.onInitialize()`

### 3.3 Custom Registries (10)
Replace `RegistryBuilder` + `NewRegistryEvent`:
- Use `Registry.registerSimple(key, callback)` or `Registry.register(key)` for non-synced
- For synced registries, use `Registry.registerSimple(key)` with sync flag
- Create registries directly in `EpicFightFabric.onInitialize()` before registering entries
- Remove `EpicFightRegistries.addNewRegistries()` NeoForge event handler

### 3.4 Data Pack Registry (EMOTE)
- Replace `DataPackRegistryEvent` → `Registry.registerSimple(EpicFightRegistries.Keys.EMOTE)` in initializer

---

## Phase 4 — Networking

### 4.1 Fabric Networking Registration
Rewrite `EpicFightNetworkManager`:
- Remove `@EventBusSubscriber`, `RegisterPayloadHandlersEvent`, `PayloadRegistrar`
- Register all 23 client-bound payloads via `ServerPlayNetworking.registerGlobalReceiver(type, handler)`
- Register all 10 server-bound payloads via `ClientPlayNetworking.registerGlobalReceiver(type, handler)`
- Register 3 bidirectional payloads on both sides
- Keep `ManagedCustomPacketPayload` interface and all `STREAM_CODEC` definitions unchanged
- Call registration from `EpicFightFabric.onInitialize()` and `EpicFightFabricClient.onInitializeClient()`

### 4.2 PacketDistributor Replacement
Replace 6 send methods:
- `sendToServer(payload)` → `ClientPlayNetworking.send(payload)`
- `sendToAll(payload)` → iterate `server.getPlayerList()`, `ServerPlayNetworking.send(player, payload)`
- `sendToPlayer(payload, player)` → `ServerPlayNetworking.send(player, payload)`
- `sendToAllPlayerTrackingThisEntity(payload, entity)` → `ServerPlayNetworking.sendToPlayersTrackingEntity(entity, payload)`
- `sendToAllPlayerTrackingThisEntityWithSelf(payload, entity)` → `ServerPlayNetworking.sendToPlayersTrackingEntityAndSelf(entity, payload)`
- `sendToAllPlayerTrackingThisChunkWithSelf(payload, level, chunkPos)` → iterate tracking players

### 4.3 Handler Signatures
- Replace `DirectionalPayloadHandler` with Fabric's `PlayPayloadHandler` or custom handler context
- Update `EpicFightClientBoundPayloadHandler` / `EpicFightServerBoundPayloadHandler` to accept Fabric's `ServerPlayNetworking.Context` / `ClientPlayNetworking.Context`

---

## Phase 5 — Capabilities & Entity Patches

### 5.1 Entity Attachment → Mixin Field
- Add mixin `MixinEntity` (extend existing one) to inject `AttachmentEntityPatchProvider epicfight$entityPatch` field
- Replace `entity.getData(EpicFightAttachmentTypes.ENTITY_PATCH)` → `entity.epicfight$getEntityPatch()`
- Remove `EpicFightAttachmentTypes` DeferredRegister (no longer a registry entry)
- `AttachmentEntityPatchProvider` constructed lazily on first access

### 5.2 Item Capability → Map Lookup
- Remove `ItemCapability` usage
- `EpicFightCapabilities.getItemStackCapability(stack)` → lookup `Map<Item, CapabilityItem>` populated during init
- `RegisterCapabilitiesEvent` → direct loop over `BuiltInRegistries.ITEM` in `EpicFightFabric.onInitialize()`
- `stack.getCapability(CAPABILITY_ITEM)` → `EpicFightCapabilities.getItemStackCapability(stack)`

### 5.3 AttachmentEntityPatchProvider
- Remove NeoForge `AttachmentType` builder
- Make it a plain class instantiated by the mixin field or on demand

---

## Phase 6 — Config System

### 6.1 ForgeConfigAPIPort Integration
- Add dependency: `modImplementation "fuzs.forgeconfigapiport:forgeconfigapiport-fabric:21.1.6"` (or CurseMaven)
- `ModConfigSpec` stays unchanged (provided by ForgeConfigAPIPort)
- Register configs in `EpicFightFabric.onInitialize()`:
  - `ForgeConfigAPIPort.registerConfig(ModConfig.Type.COMMON, CommonConfig.SPEC)`
  - `ForgeConfigAPIPort.registerConfig(ModConfig.Type.CLIENT, ClientConfig.SPEC)` (client only)
  - `ForgeConfigAPIPort.registerConfig(ModConfig.Type.SERVER, ServerConfig.SPEC)` (server only)

### 6.2 Config Event Subscribers
- Remove `@EventBusSubscriber` from config classes
- Register config reload listeners via ForgeConfigAPIPort's Fabric events

---

## Phase 7 — Events & Lifecycle

### 7.1 NeoForge Event → Fabric Callback Mapping
| NeoForge Event | Fabric Replacement |
|---|---|
| `FMLConstructModEvent` | `onInitialize()` body |
| `FMLCommonSetupEvent` | `onInitialize()` body (after registration) |
| `FMLClientSetupEvent` | `onInitializeClient()` body |
| `RegisterCommandsEvent` | `CommandRegistrationCallback.EVENT` |
| `AddReloadListenerEvent` | `ServerLifecycleEvents.SERVER_STARTED` + add listeners to `MinecraftServer.getServer().getResourceManager()` |
| `BuildCreativeModeTabContentsEvent` | `ItemGroupEvents.modifyEntriesEvent()` |
| `EntityJoinLevelEvent` | `ServerEntityEvents.ENTITY_LOAD` |
| `PlayerInteractEvent` | `UseBlockCallback` / `UseItemCallback` / `AttackBlockCallback` |
| `LivingEquipmentChangeEvent` | Custom mixin event (inject into `LivingEntity.setItemSlot`) |
| `LevelTickEvent` | `ServerTickEvents.END_SERVER_TICK` / `ClientTickEvents.END_CLIENT_TICK` |
| `ItemTooltipEvent` | `ItemTooltipCallback.EVENT` |
| `LootTableLoadEvent` | `LootTableEvents.MODIFY` |
| `InputEvent` | `ClientTickEvents` + custom key handling |
| `RegisterShadersEvent` | `CoreShaderRegistrationCallback.EVENT` |
| `AddPackFindersEvent` | Resource pack hook via mixin into `PackRepository` |
| `LivingEvent.LivingJumpEvent` | Mixin into `LivingEntity.jumpFromGround()` |
| `EventHooks` (direct calls) | Replace with vanilla hooks or custom events |

### 7.2 IEventBasedEngine Adaptation
- Remove `IEventBus` parameters from `IEventBasedEngine`
- `RenderEngine` / `ControlEngine` register Fabric callbacks directly instead of NeoForge event bus listeners
- `init()` called from `EpicFightFabricClient.onInitializeClient()`

### 7.3 Compat Module Interface
- `ICompatModule`: Replace `IEventBus` params with no-arg methods or Fabric callback registration
- `loadCompatModule()`: Remove `IEventBus` param, each module self-registers via Fabric callbacks
- `MinecraftMod`: Remove `ModList.get()` usage, use `FabricLoader.getInstance().isModLoaded()`
- Remove `MinecraftMod` entries for KubeJS, Vampirism, Werewolves

### 7.4 ModMixinPlugin
- Replace `LoadingModList.get().getModFileById()` → `FabricLoader.getInstance().isModLoaded()`

---

## Phase 8 — Mixins

### 8.1 Config Updates
- All 7→5 mixin configs: remove `"refmap"`, keep `"required": false`, update package paths
- Remove `epicfight-compat.vampirism.mixins.json`, `epicfight-compat.werewolves.mixins.json`
- Rename `epicfight-platform.neoforge.mixins.json` → `epicfight-platform.fabric.mixins.json`

### 8.2 Enum Extension Mixin
- Create `MixinRarity` with `@ExtendEnum` (Mixin Extras) to add `EPICFIGHT_UNIQUE`:
  ```java
  @Mixin(Rarity.class)
  public class MixinRarity {
      @ExtendEnum
      private static Rarity epicfight$unique() {
          return new Rarity("EPICFIGHT_UNIQUE", -1, style -> style.withColor(ChatFormatting.GREEN));
      }
  }
  ```
- Add to `epicfight.mixins.json` common mixins list
- Delete `EpicFightExtensibleEnums` NeoForge `EnumProxy` code, replace with direct reference

### 8.3 Compat Mixin Plugins
- Each `*MixinPlugin` (GeckoLib, FGM, SkinLayers3D): Replace `LoadingModList` → `FabricLoader.getInstance()`

### 8.4 Verify Mixin Targets
- All 48 mixins target vanilla classes — should work unchanged on Fabric
- Check for any NeoForge-specific `@At` targets or `@Coerce` usage

---

## Phase 9 — Access Widener (detailed conversion)

Convert all 117 entries. Categories:

### Fields (use `accessible field` + `mutable field` if was `-f`):
- `Camera.eyeHeight`, `Camera.eyeHeightOld`
- `DeltaTracker$Timer.deltaTickResidual`
- `Gui.CROSSHAIR_SPRITE`
- `GuiGraphics.scissorStack`, `GuiGraphics$ScissorStack.stack`
- `AbstractSelectionList.itemHeight`, `AbstractSelectionList.scrolling`
- `EditBox.responder`
- `Screen.INWORLD_MENU_BACKGROUND`, `Screen.initialized`
- `ModelPart.cubes`, `ModelPart.children`
- `AbstractSelectionList$Entry.focused`
- `AbstractSliderButton.setValue`
- `ClientLevel.connection`
- `Particle.alpha`
- `LocalPlayer.sprintTriggerTime`
- `LightTexture.lightTexture`
- `OutlineBufferSource.teamR/G/B/A`
- `ItemOverrides.overrides`, `ItemOverrides.properties`
- `EntityRenderDispatcher.renderers`, `EntityRenderDispatcher.playerRenderers`
- `LivingEntityRenderer.layers`
- `ElytraLayer.elytraModel`
- `HumanoidArmorLayer.ARMOR_LOCATION_CACHE`
- `VillagerProfessionLayer.typeHatCache`, `VillagerProfessionLayer.professionHatCache`
- `RenderStateShard$ShaderStateShard.shader`, `RenderStateShard$TextureStateShard.blur/mipmap/texture`
- `RenderType$CompositeState.*` (all state fields)
- `RenderType$CompositeRenderType.outline/state`
- `RenderType$OutlineProperty`
- `CompoundTag.tags`
- `FolderRepositorySource.DISCOVERED_PACK_SELECTION_CONFIG`
- `CombatTracker.mob`
- `AreaEffectCloud.victims`
- `Brain.availableBehaviorsByPriority`
- `AttributeMap.supplier`, `AttributeSupplier.instances`
- `NearestAttackableTargetGoal.targetType/targetConditions`
- `EnderDragon.growlTime`, `EnderDragon.phaseManager`
- `EnderDragonPhaseManager.dragon`
- `WitherBoss.TARGETING_CONDITIONS`
- `AbstractArrow.inGround`
- `Entity.dimensions`
- `LivingEntity.attackStrengthTicker/lastHurt/lerpSteps/lerpX/lerpY/lerpZ/lerpYRot/noJumpDelay`
- `EnderMan.DATA_CREEPY`
- `ThrownTrident.dealtDamage`
- `EndDragonFight.dragonEvent`

### Methods (use `accessible method`):
- `VertexFormat.<init>(...)`
- `Camera.getMaxZoom(F)F`, `Camera.move(FFF)V`, `Camera.setPosition(DDD)V`, `Camera.setRotation(FF)V`
- `KeyboardHandler.debugFeedbackTranslated(...)`
- `Minecraft.startAttack()Z`
- `GuiGraphics.innerBlit(...)`
- `AbstractSelectionList.getEntryAtPosition(DD)`
- `Screen` various
- `ModelPart$Cube.polygons` (mutable), `ModelPart$Polygon.normal/vertices` (mutable)
- `MultiPlayerGameMode.performUseItemOn(...)`
- `GameRenderer.getFov(...)`
- `HumanoidArmorLayer.getArmorModel(...)`
- `VillagerProfessionLayer.getResourceLocation(...)`
- `PlayerRenderer.setModelProperties(...)`
- `ItemInHandRenderer.renderMap(...)`
- `RenderType$CompositeState.<init>(...)`, `RenderType$CompositeRenderType.<init>(...)`
- `EnderDragonPhase.create(...)`
- `EnderMan.teleport()Z`
- `AbstractArrow.setPierceLevel(B)V`
- `ItemStack.<init>(Ljava/lang/Void;)V`
- `Entity.getInputVector(...)`
- `FolderRepositorySource.createDiscoveredFilePackInfo(...)`
- `FolderRepositorySource$FolderPackDetector.<init>(...)`

### Constructor access:
- `VertexFormat.<init>`
- `ItemOverrides$BakedOverride.<init>`
- `ItemOverrides$PropertyMatcher.<init>`
- `RenderType$CompositeState.<init>`
- `RenderType$CompositeRenderType.<init>`
- `FolderRepositorySource$FolderPackDetector.<init>`

---

## Phase 10 — Data Generation

### 10.1 Fabric Datagen
- Implement `DataGenInitializer` (registered as `data` entrypoint in `fabric.mod.json`)
- Replace `GatherDataEvent` with `FabricDataGenHelper` + `DatagenModInitializer`
- `EpicFightRecipeProvider` extends `FabricRecipeProvider`
- `EpicFightBlockTagsProvider` extends `FabricTagProvider.BlockTagProvider`
- `EpicFightItemTagsProvider` extends `FabricTagProvider.ItemTagProvider`
- Remove NeoForge `ExistingFileHelper` usage

---

## Phase 11 — Client Rendering

### 11.1 Shader Registration
- `ComputeShaderProvider.epicfight$registerComputeShaders` → `CoreShaderRegistrationCallback.EVENT.register()`
- `EpicFightShaders` — adapt registration to Fabric callback

### 11.2 Key Mappings
- `EpicFightKeyMappings.registerKeys()` → use `FabricKeyMappingRegistrar` which calls `KeyBindingHelper.registerKeyMapping()`
- `CombatKeyMapping` — adapt to Fabric key conflict context (or remove conflict context, Fabric doesn't have it)

### 11.3 Entity Renderer Overrides
- `PatchedEntityRenderer` system — uses access widener to access `EntityRenderDispatcher.renderers` map
- Should work with access widener on Fabric

### 11.4 Item Properties
- `EpicFightItemProperties.registerItemProperties()` → `ItemPropertyRegistryCallback` or direct `ItemProperties.register()`

### 11.5 Pack Finder
- `addPackFindersEvent` → mixin into `PackRepository` or use `LootTableEvents` equivalent for resource packs
- Or register via `ClientResourceReloader` / datapack registry

---

## Phase 12 — Compat Modules (16)

### 12.1 AzureLib (`curse.maven:azurelib-817423:8367231`)
- Port `AzureLibCompat` + `AzureLibArmorCompat`
- Replace NeoForge events with Fabric callbacks
- AzureLib Fabric may have different renderer API — adapt

### 12.2 BetterThirdPerson (`curse.maven:better-third-person-435044:5439141`)
- Port `BetterThirdPersonCompat`
- Simple event adaptation

### 12.3 Controlify (`curse.maven:controlify-835847:8406710`)
- Port Controlify compat
- Controlify has official Fabric API entrypoints — use those

### 12.4 Trinkets (replaces Curios) (`curse.maven:trinkets-341284:5534317`)
- **Full rewrite** of `CuriosCompat` → `TrinketsCompat`
- Trinkets API: `TrinketsApi.getTrinketComponent()` / `TrinketSlots` / `SlotType`
- Replace Curios slot lookup with Trinkets slot system
- This is the most complex compat rewrite

### 12.5 FGM (`curse.maven:female-gender-456319:5478368`)
- Port `WildfireFGMCompat` + compat mixins (`MixinGenderLayer`, `FemaleLayerAccessor`)
- Update `ModMixinPlugin` to use `FabricLoader`

### 12.6 FirstPersonModel (`curse.maven:first-person-model-333287:8294226`)
- Port `FirstPersonCompat`
- FirstPersonModel Fabric API may differ — adapt

### 12.7 GeckoLib (`curse.maven:geckolib-388172:8350058`)
- Port `GeckolibCompat` + `MixinGeoArmorRenderer` + `GeckolibMixinPlugin`
- GeckoLib Fabric uses different renderer events — replace `GeoRenderEvent.Entity.Pre/Post` with Fabric equivalents
- `GeoModelTransformer` registration stays same (it's EpicFight's own system)

### 12.8 Iris (`curse.maven:irisshaders-455508:8242801`)
- Port `IRISCompat`
- Iris Fabric API: `IrisApi.getInstance()` — should be similar

### 12.9 JEI (`curse.maven:jei-238222:8678370`)
- Port JEI compat (if any registration exists)
- JEI Fabric uses `JEIPlugin` annotation — should be similar

### 12.10 MCreator (no external dep)
- Port `MCreatorPlayerAnimationsCompat`
- No mod dependency — just checks for bedrock_animations data folder

### 12.11 PlayerAnimator (`curse.maven:playeranimator-658587:7389821`)
- Port `PlayerAnimatorCompat`
- player-animation-lib Fabric API should be similar

### 12.12 PlayerRevive (local `C:\Users\nonza\Documents\GitHub\PlayerRevive`)
- Port `PlayerReviveCompat`
- Add as local Gradle dependency: `include` or `modImplementation files(...)`
- Check PlayerRevive's Fabric API

### 12.13 ShoulderSurfing (`curse.maven:shoulder-surfing-reloaded-243190:8596429`) — v5 API
- **Major migration** per v5 API docs:
  - Update all package imports (see mapping table in Key Decisions §12)
  - `shouldersurfing_plugin.json`: `entrypoint` → `entrypoints` array
  - `register(IShoulderSurfingRegistrar)` → `register(IEventBus)`
  - Replace all callbacks with events:
    - `IAdaptiveItemCallback` → `ComputePlayerAimStateEvent`
    - `ICameraCouplingCallback` → `ComputeCameraCouplingEvent`
    - `ICameraEntityTransparencyCallback` → `ComputeCameraEntityTransparencyEvent`
    - `ICameraRotationSetupCallback` → `SetupCameraRotationEvent`
    - `IPlayerInputCallback` → `ForceVanillaPlayerInputEvent`
    - `IPlayerStateCallback.isAttacking` → `ComputePlayerAttackStateEvent`
    - `IPlayerStateCallback.isInteracting` → `ComputePlayerInteractionStateEvent`
    - `IPlayerStateCallback.isPicking` → `ComputePlayerPickStateEvent`
    - `IPlayerStateCallback.isUsingItem` → `ComputePlayerUseItemStateEvent`
    - `IPlayerStateCallback.isRidingBoat` → `ComputePlayerRideBoatStateEvent`
    - `ITargetCameraOffsetCallback` → `ComputeTargetCameraOffsetEvent`
    - `ITickableCallback` → `TickEvent`
  - Update config getter names per v5 rename table

### 12.14 SimplyTooltips (`curse.maven:simply-tooltips-1475755:8715135`)
- Port `SimplyTooltipsModule` + `EpicFightTooltipProvider`
- `TooltipProviderRegistry.register()` — check if Fabric version has same API

### 12.15 SkinLayers3D (`curse.maven:skin-layers-3d-521480:8274818`)
- Port `SkinLayer3DCompat` + compat mixins (`MixinCustomModelPart`, `MixinCustomizableCubeWrapper`)
- Update `ModMixinPlugin` to use `FabricLoader`

### 12.16 Sodium (`curse.maven:sodium-394468:8591793`) — 0.8.13-beta.2
- Port `SodiumFakeBlockRenderer` + Sodium compat
- Sodium Fabric API: `SodiumClientMod` / `SodiumOptions` — adapt for 0.8.13-beta.2 API

### 12.17 Remove Dropped Modules
- Delete `compat/kubejs/` directory entirely
- Delete `compat/vampirism/` directory entirely
- Delete `compat/werewolves/` directory entirely
- Remove from `MinecraftMod` enum
- Remove mixin configs for Vampirism and Werewolves
- Remove from `fabric.mod.json` mixin list

---

## Phase 13 — Resources

### 13.1 Metadata Files
- Delete `META-INF/neoforge.mods.toml`
- Delete `META-INF/accesstransformer.cfg` (replaced by access widener)
- Delete `META-INF/enum_extensions.json` (replaced by mixin)
- Create `fabric.mod.json` (as described in Phase 1.3)
- Create `epicfight.accesswidener` (as described in Phase 1.5)

### 13.2 Mixin Configs
- Update all mixin JSON files (remove refmap, rename platform config)
- Remove Vampirism + Werewolves mixin configs
- Update `fabric.mod.json` mixin list

### 13.3 Game Assets
- All 1,232 resource files (textures, models, animations, sounds, lang, data) are vanilla-format → **no changes needed**
- `pack.mcmeta` stays as-is
- Update `shouldersurfing_plugin.json` to v5 format

### 13.4 NeoForge-specific Resources
- `META-INF/services/` — check if any NeoForge service files need removal
- `accesstransformer.cfg` → deleted
- `enum_extensions.json` → deleted

---

## Phase 14 — Build & Test

### 14.1 Compilation
1. `gradle build` — fix all compilation errors iteratively
2. Expected error categories:
   - NeoForge imports not found → replace with Fabric equivalents
   - `DeferredRegister`/`DeferredHolder` → shim classes
   - `IEventBus` → remove or replace with Fabric callbacks
   - `@EventBusSubscriber` / `@SubscribeEvent` → remove, register manually
   - `ModConfigSpec` → keep (ForgeConfigAPIPort provides it)
   - `PacketDistributor` → Fabric networking
   - `AttachmentType` → mixin field
   - `ItemCapability` → map lookup
   - `FMLEnvironment` → `FabricLoader`
   - `ModList` → `FabricLoader`
   - `Dist.CLIENT` → `EnvType.CLIENT`

### 14.2 Runtime — Client
1. `gradle runClient` — fix runtime crashes
2. Expected crash categories:
   - Mixin application failures (wrong targets, refmap issues)
   - Access widener conflicts
   - Registry ordering issues
   - Network packet handling errors
   - Missing config initialization
   - Entity patch field injection failures

### 14.3 Runtime — Server
1. `gradle runServer` — fix server-side crashes
2. Expected crash categories:
   - Client-only code loaded on server (missing side checks)
   - Network handler registration issues
   - Config loading issues

### 14.4 Feature Parity Test
1. Combat system: weapons, attack animations, hit detection
2. Skill system: skill learning, activation, switching
3. Animation system: living motions, attack animations, dodge
4. Entity patches: mob AI, boss fights (wither, ender dragon)
5. Items: weapon capabilities, skill books
6. GUI: skill book screen, config screen, datapack editor
7. Networking: all 43 packets working
8. Each compat module tested with corresponding mod installed

---

## Execution Order & Checkpoints

| Session | Phases | Goal | Checkpoint |
|---------|--------|------|------------|
| 1 | 1-3 | Build system + Platform + Registration | `gradle build` compiles (may have errors in other phases) |
| 2 | 4-6 | Networking + Capabilities + Config | Core systems compile |
| 3 | 7-9 | Events + Mixins + Access Widener | `gradle build` succeeds |
| 4 | 10-11 | Data gen + Client rendering | `runClient` reaches main menu |
| 5 | 12 | All 16 compat modules | All compat compiles + loads |
| 6 | 13-14 | Resources + Build & Test | `runClient` + `runServer` no crash, features work |

---

## Files to Create (~35)

| File | Purpose |
|------|---------|
| `platform/fabric/FabricModPlatform.java` | ModPlatform impl |
| `platform/fabric/client/FabricClientModPlatform.java` | ClientModPlatform impl |
| `platform/fabric/client/FabricKeyMappingRegistrar.java` | KeyMappingRegistrar impl |
| `EpicFightFabric.java` | ModInitializer entrypoint |
| `EpicFightFabricClient.java` | ClientModInitializer entrypoint |
| `registry/deferred_shim/DeferredRegisterShim.java` | DeferredRegister compat |
| `registry/deferred_shim/DeferredHolderShim.java` | DeferredHolder compat |
| `mixin/common/MixinRarity.java` | Enum extension for Rarity |
| `fabric.mod.json` | Fabric mod metadata |
| `epicfight.accesswidener` | Access widener |
| `epicfight-platform.fabric.mixins.json` | Platform mixin config |
| `platform/fabric/event/FabricEntityEvent.java` | Entity event wrappers |
| `platform/fabric/event/FabricPlayerEvent.java` | Player event wrappers |
| `platform/fabric/event/FabricWorldEvent.java` | World event wrappers |
| `compat/trinkets/TrinketsCompat.java` | Trinkets compat (replaces Curios) |
| + ~20 more compat/event adapter files | |

## Files to Modify (~150)

All 152 files with NeoForge imports, plus build files, mixin configs, and resource metadata.
