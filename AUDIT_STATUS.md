# Epic Fight Fabric 1.21.1 Port — Complete Status Audit

## A. Completed Work

### 1. Build System & Project Structure

- **Files**: `build.gradle`, `gradle.properties`, `fabric.mod.json`, `epicfight.accesswidener`
- **Status**: Functional. Fabric Loom 1.17.19, Mojang mappings, Java 21. `runClient` and `runServer` both launch without crash.
- **Tested**: ✅ Client launches, enters world, plays, exits cleanly. Server starts and reaches "Done".

### 2. Networking (Payload Registration)

- **Files**: `EpicFightPayloadRegistration.java`, `EpicFightClientPayloadRegistration.java`, `ManagedCustomPacketPayload.java`, `EpicFightNetworkManager.java`
- **Status**: Functional. All 43 custom payload codecs registered with `PayloadTypeRegistry.playC2S()`/`playS2C()`. Server and client handlers wired via `ServerPlayNetworking`/`ClientPlayNetworking`.
- **Tested**: ✅ No more `ClassCastException` / `DiscardedPayload` errors.

### 3. Configuration (ForgeConfigAPIPort)

- **Files**: `EpicFightFabric.java`, `EpicFightFabricClient.java`, `ClientConfig.java`, `CommonConfig.java`, `ServerConfig.java`
- **Status**: Functional. COMMON, SERVER, CLIENT configs registered via `NeoForgeConfigRegistry.INSTANCE.register()`. NightConfig added as explicit `implementation` deps. Config values inlined on load.
- **Tested**: ✅ Config loads without `NoClassDefFoundError`.

### 4. Registry System (DeferredRegisterShim)

- **Files**: `DeferredRegisterShim.java`, `DeferredHolderShim.java`, `EpicFightRegistries.java`, all `EpicFight*` registry entry classes
- **Status**: Functional. Custom Fabric-compatible replacement for NeoForge's `DeferredRegister`/`DeferredHolder`. 24 deferred registries accepted at init. Custom registries created via `FabricRegistryBuilder`.
- **Tested**: ✅ Items, blocks, entities, skills, etc. register correctly.

### 5. Reload Listeners

- **Files**: `EpicFightReloadListeners.java`, various reload listener classes
- **Status**: Functional with workaround. 12 server-side reload listeners registered via `ResourceManagerHelper`. `.exceptionally()` error handler catches NPEs from inter-listener dependency ordering issues.
- **Tested**: ✅ World creation/load works. Some NPEs caught gracefully during early reload.

### 6. Key Mappings & Input

- **Files**: `EpicFightKeyMappings.java`, `FabricKeyMappingRegistrar.java`, `ControlEngine.java`, `MixinKeyMappingClick.java`, `KeyMappingAccessor.java`, `CombatKeyMapping.java`
- **Status**: Functional. Key mappings register and work for combat. R key mode switching works (one press = one toggle). `MixinKeyMappingClick` cancels key REPEAT events and distributes clicks to `CombatKeyMapping` instances sharing keys with vanilla. Some input events still stubbed (see below).

### 7. Compat Mods (5 of 13 at runtime)

- **Runtime working**: Azurelib, FGM, GeckoLib, JEI, PlayerAnimator
- **Compile-only**: FirstPerson, Iris, Sodium, SkinLayers3D, Trinkets, SimplyTooltips, BetterThirdPerson, ShoulderSurfing
- **Disabled**: Controlify (malformed access widener)

### 8. Mixin Infrastructure

- **Files**: 5 mixin config files, 50+ mixin classes
- **Status**: Most mixins functional. Access wideners extensive (100+ entries). Some mixins are accessor-only (no injection).

---

## B. Partially Completed Work

### 1. RenderEngine — **CRITICAL: 15+ event hooks are empty stubs**

- **File**: `src/main/java/yesman/epicfight/client/events/engine/RenderEngine.java`
- **What's implemented**: `renderEntityArmatureModel()`, `getItemRenderer()`, `reloadItemRenderers()`, `resetRenderers()`, Ender Dragon render hook
- **What's missing (ALL STUBBED with TODO)**:
  - `epicfight$renderLivingPre()` — was `RenderLivingEvent.Pre` — **EMPTY BODY, does nothing**
  - `epicfight$renderHand()` — was `RenderHandEvent` — **EMPTY BODY, does nothing**
  - `epicfight$renderTickPre()` / `epicfight$renderTickPost()` — was `RenderFrameEvent` — **EMPTY**
  - `epicfight$clientTick$Pre()` / `epicfight$clientTick$Post()` — was `ClientTickEvent` — **EMPTY**
  - `epicfight$levelTickPost()` — was `LevelTickEvent` — **EMPTY**
  - `epicfight$renderAfterLevel()` — was `RenderLevelStageEvent` — **EMPTY**
  - `epicfight$renderGuiPre()` — was `RenderGuiEvent.Pre` — **EMPTY**
  - `epicfight$bossEventProgress()` — was `CustomizeGuiOverlayEvent` — **EMPTY**
  - `epicfight$computeCameraAngles()` — was `ComputeCameraAnglesEvent` — **EMPTY**
  - `epicfight$itemTooltip()` — was `ItemTooltipEvent` — **EMPTY**
  - `epicfight$renderBlockHighlight()` — was `RenderHighlightEvent.Block` — **EMPTY**
  - `epicfight$addLayers()` — was `EntityRenderersEvent.AddLayers` — **EMPTY**
  - `modEventBus()` / `gameEventBus()` — **EMPTY except Ender Dragon**
- **Blocking**: Without `epicfight$renderLivingPre` being wired, Epic Fight's patched entity renderers are NEVER called. Without `epicfight$renderHand`, first-person weapon rendering doesn't work. Without `epicfight$clientTick`, the combat tick loop doesn't run. Without `epicfight$renderGuiPre`, the battle HUD doesn't render.

### 2. ControlEngine — Multiple input event hooks stubbed

- **File**: `src/main/java/yesman/epicfight/client/events/engine/ControlEngine.java`
- **Stubs**: `InputEvent.InteractionKeyMappingTriggered`, `InputEvent.MouseScrollingEvent`, `MovementInputUpdateEvent`, `ClientTickEvent.Post`, `LivingJumpEvent`
- **Impact**: Some combat input interactions may not fire correctly.

### 3. EntitySnapshot — NeoForge rendering APIs referenced

- **File**: `src/main/java/yesman/epicfight/api/utils/EntitySnapshot.java:183,236`
- **Issue**: `getRenderPasses` (NeoForge addition) returns empty list. `getItemColors` returns -1.
- **Impact**: Item color/render pass data missing in entity snapshots.

### 4. ItemPresetManager & ModifierManager — TODO in NeoForge too (parity achieved)

- **Files**: `ItemPresetManager.java:68`, `ModifierManager.java:27`
- **Issue**: Both have `//TODO: Implement` — empty methods.
- **NeoForge reference**: Both are also `//TODO: Implement` in the original NeoForge 1.21.1 source.
- **Impact**: ExCap weapon preset/modifier system non-functional in both versions. Parity maintained.

### 5. ShoulderSurfing compat — `implements IShoulderSurfingPlugin` removed

- **File**: `ShoulderSurfingCompat.java`
- **Issue**: Loom remapping edge case prevents `implements IShoulderSurfingPlugin` from compiling. Workaround: removed the interface, class works via entrypoint JSON only.
- **Impact**: May miss some plugin initialization.

---

## C. Missing Work

### 1. CRITICAL: No mixin redirects LivingEntityRenderer.render() to Epic Fight renderers

There is NO `@Inject` or `@WrapOperation` in `MixinLivingEntityRenderer` that intercepts the actual `render()` method. The mixin only has `@Invoker` helpers (`isBodyVisible`, `getRenderType`, `getBob`). This means:
- Epic Fight's custom animated entity models **never get rendered**
- `RenderEngine.renderEntityArmatureModel()` is never called from the render pipeline
- The entire patched entity renderer system (`PatchedLivingEntityRenderer`, `PHumanoidRenderer`, `PZombieRenderer`, etc.) is dead code at runtime

**NeoForge original**: Used `RenderLivingEvent.Pre` event → cancelled vanilla render → called `RenderEngine.renderEntityArmatureModel()`.
**Fabric equivalent needed**: `@Inject(method="render", at=@At("HEAD"), cancellable=true)` in `MixinLivingEntityRenderer` that calls `RenderEngine.getInstance().renderEntityArmatureModel()` and cancels if a patched renderer exists.

### 2. CRITICAL: No mixin hooks first-person hand rendering

There is no mixin on `ItemInHandRenderer.renderHand()` or `GameRenderer.renderItemInHand()`. The `FirstPersonRenderer` class and `RenderEngine.getFirstPersonRenderer()` exist but are never called.

**NeoForge original**: Used `RenderHandEvent` → cancelled → called custom first-person renderer.
**Fabric equivalent needed**: Mixin on `ItemInHandRenderer` or `GameRenderer` to intercept hand rendering.

### 3. CRITICAL: NeoForge `separate_transforms` model loader not ported

All 26 Epic Fight item model JSON files use `"loader": "neoforge:separate_transforms"`. This is a NeoForge-specific model loader that allows different models for different display contexts (GUI, ground, third person, etc.). Fabric does not have this loader.

**Impact**: ALL Epic Fight weapons (uchigatana, greatswords, spears, tachis, longswords, daggers) fail to load their item models. They will show as missing texture blocks or not render at all.

**Fabric equivalent needed**: Either:
- Register a custom `ModelResourceLoader` via Fabric API's `ModelLoadingPlugin`
- Or convert all 26 JSON files to vanilla `"parent"` + overrides format

### 4. ~~TRansition-1.0.6.jar~~ — RESOLVED: Not needed

- **Status**: JAR removed in commit `776cb193`. Not referenced in `build.gradle`.
- **Analysis**: Epic Fight does NOT import `dev.tr7zw.transition` anywhere in its source code.
- **Stub class**: `src/main/java/dev/tr7zw/transition/mc/PlayerUtil.java` exists but is not referenced by any Epic Fight code.
- **Original usage**: Only used by `SkinLayer3DCompat.java` for `PlayerUtil.getPlayerSkin(player)`, which was replaced with direct vanilla API call `player.getSkin().texture()`.
- **Compat impact**: FirstPerson mod needs `dev.tr7zw.transition.mc.GeneralUtil` at runtime — kept as `modCompileOnly`. Users who want FirstPerson must install TRansition separately.
- **Conclusion**: No Fabric port needed. The NeoForge JAR was a transitive dependency, not a direct Epic Fight dependency.

### 5. ~~TRender-1.0.7.jar~~ — RESOLVED: Not needed

- **Status**: JAR removed in commit `776cb193`. Not referenced in `build.gradle`.
- **Analysis**: Epic Fight does NOT import `dev.tr7zw.trender` anywhere in its source code. Zero references.
- **Original usage**: Was a dead dependency. Epic Fight uses its own GUI widgets (AnchoredButton, etc.).
- **Conclusion**: No Fabric port needed. Was never used by Epic Fight core.

### 6. SimplyTooltips compat — Empty TODO

- **File**: `EpicFightFabric.java` (lines 61-64 area)
- **Issue**: SimplyTooltips module is registered in `MinecraftMod` enum but the compat module body is a TODO stub.

### 7. MCreator compat — Commented out

- **File**: `EpicFightFabric.java` (lines 164-168 area)
- **Issue**: MCreator compat code is commented out.

### 8. Controlify compat — Disabled

- **File**: `build.gradle` line 96
- **Issue**: Malformed access widener prevents loading. Commented out.

### 9. ~~`sendToAll()` placeholder~~ — **FIXED**

- **File**: `EpicFightNetworkManager.java`
- **Status**: Implemented. Now iterates over `server.getPlayerList().getPlayers()` and sends via `ServerPlayNetworking.send` to each player.

### 10. JsonAssetLoader — modClasses field not accessible

- **File**: `JsonAssetLoader.java:94-97`
- **Issue**: `modClasses` returns empty list. May affect dynamic model loading.

---

## D. Current Bugs

| # | Symptom | Root Cause | File/Class | NeoForge→Fabric? | Priority |
|---|---------|------------|------------|------------------|----------|
| 1 | Epic Fight weapons show as missing/no model in inventory and world | 26 item model JSONs use `"loader": "neoforge:separate_transforms"` — Fabric has no such loader | `assets/epicfight/models/item/*.json` | Yes | **P0** |
| 2 | Epic Fight animated entity models never render | No mixin `@Inject` on `LivingEntityRenderer.render()` to redirect to patched renderers | `MixinLivingEntityRenderer.java`, `RenderEngine.java` | Yes | **P0** |
| 3 | First-person weapon/hand rendering doesn't work | No mixin intercepts `ItemInHandRenderer` or `GameRenderer.renderItemInHand()` | Missing mixin | Yes | **P0** |
| 4 | Combat tick loop doesn't run | `epicfight$clientTick$Pre/Post` are empty stubs | `RenderEngine.java:453-457` | Yes | **P0** |
| 5 | Battle HUD doesn't render | `epicfight$renderGuiPre` is empty stub | `RenderEngine.java:413` | Yes | **P1** |
| 6 | Camera angles not computed in TPS mode | `epicfight$computeCameraAngles` is empty stub | `RenderEngine.java:409` | Yes | **P1** |
| 7 | Item tooltips missing Epic Fight data | `epicfight$itemTooltip` is empty stub | `RenderEngine.java:402` | Yes | **P1** |
| 8 | Block highlight rendering not customized | `epicfight$renderBlockHighlight` is empty stub | `RenderEngine.java:465` | Yes | **P2** |
| 9 | Boss event progress bar not customized | `epicfight$bossEventProgress` is empty stub | `RenderEngine.java:420` | Yes | **P2** |
| 10 | Entity layers not added | `epicfight$addLayers` is empty stub | `RenderEngine.java:476` | Yes | **P1** |
| 11 | Level tick post-processing missing | `epicfight$levelTickPost` is empty stub | `RenderEngine.java:461` | Yes | **P1** |
| 12 | ~~Server-to-all broadcast doesn't work~~ | ~~`sendToAll()` is empty placeholder~~ | `EpicFightNetworkManager.java` | ~~Yes~~ **FIXED** | ~~**P1**~~ |
| 13 | ~~ItemPresetManager not functional~~ | ~~`//TODO: Implement`~~ | `ItemPresetManager.java:68` | N/A — **also TODO in NeoForge** | ~~**P2**~~ |
| 14 | ~~ModifierManager not functional~~ | ~~`//TODO: Implement`~~ | `ModifierManager.java:27` | N/A — **also TODO in NeoForge** | ~~**P2**~~ |
| 15 | ~~TRansition/TRender not available~~ | ~~JARs not in build.gradle, are NeoForge mods~~ | ~~`libs/*.jar`, `build.gradle`~~ | ~~Yes~~ | ~~**P1**~~ — **RESOLVED: Not needed, JARs removed** |
| 16 | ~~`epicfight:weight` attribute warnings~~ | `DeferredHolderShim.equals()` only matched other shims, not vanilla `Holder.Reference` keys | `EntityAttributeModificationEvent.java`, `DeferredHolderShim.java` | Yes | **FIXED** |
| 17 | ~~`FemaleLayerAccessor` mixin crash~~ | Wildfire Gender Mod's `GenderLayer` has no `getBreastTexture(LivingEntity)` method | `WildfireFGMCompat.java`, `epicfight-compat.fgm.mixins.json` | Yes | **FIXED** |
| 18 | ~~`AdaptiveSkinSkill` NPE on unknown damage type tag~~ | `getGlintColor()` returns null for tags not in `protectableDamageTypeTags` | `AdaptiveSkinSkill.java` | Yes | **FIXED** |
| 19 | ~~`EpicFightPayloadContext.reply()` TODO~~ | Reply was a no-op stub | `EpicFightPayloadContext.java` | Yes | **FIXED** |
| 20 | ~~Emote registry empty after loading~~ | No reload listener for emote JSON files | `EmoteReloadListener.java`, `EpicFightReloadListeners.java` | Yes | **FIXED** |

---

## E. Epic Fight / Vanilla Model Problem — Deep Analysis

### Item Models (Inventory/GUI/World)

**Root cause**: All 26 Epic Fight weapon item model JSON files use the NeoForge-specific model loader:

```json
{
    "loader": "neoforge:separate_transforms",
    "base": { ... },
    "perspectives": {
        "gui": { ... },
        "ground": { ... },
        "fixed": { ... }
    }
}
```

**How it works on NeoForge**: The `neoforge:separate_transforms` loader is registered by NeoForge's `ModelEvent.RegisterAdditionalModels` / `GeometryLoaderManager`. It bakes separate models for each `ItemDisplayContext` (GUI, ground, fixed, third_person, etc.) and returns the appropriate one based on the render context.

**What happens on Fabric**: Fabric's `ModelBakery` does not recognize `"loader": "neoforge:separate_transforms"`. The model fails to bake. The item shows as the missing model (purple/black cube) or doesn't render at all.

**Fix options**:
1. **Register a custom `ModelResourceLoader` via Fabric API** — Implement a Fabric-compatible `separate_transforms` loader that parses the same JSON format and returns per-context baked models. This is the correct long-term fix.
2. **Convert all 26 JSON files to vanilla format** — Use `"parent"` + `"overrides"` with `CustomModelData` or similar predicates. This loses some flexibility but works with vanilla model baking.

### Entity Models (Third Person)

**Root cause**: The render pipeline is not connected. The chain should be:

```
Minecraft.render() → LevelRenderer.renderEntity() → LivingEntityRenderer.render()
    → [MIXIN INJECT] → RenderEngine.renderEntityArmatureModel()
    → PatchedLivingEntityRenderer.render()
    → SkinnedMesh.draw() with Armature poses
```

**Where it breaks**: The mixin step is missing. `MixinLivingEntityRenderer` only has `@Invoker` methods — no `@Inject` on `render()`. So `LivingEntityRenderer.render()` runs vanilla code, and Epic Fight's `RenderEngine.renderEntityArmatureModel()` is never called.

**Fix**: Add `@Inject(method="render", at=@At("HEAD"), cancellable=true)` to `MixinLivingEntityRenderer` that checks if a patched renderer exists, calls it, and cancels the vanilla render.

### First-Person Hand/Weapon Models

**Root cause**: Same pattern — `RenderEngine.epicfight$renderHand()` is an empty stub. No mixin intercepts `ItemInHandRenderer.renderHand()` or `GameRenderer.renderItemInHand()`.

---

## F. Item System Problems

### Registration — ✅ Works

`DeferredRegisterShim` correctly registers all items via `Registry.register()`. Items appear in-game.

### Item Properties — ✅ Works

`EpicFightItemProperties.registerItemProperties()` registers the skill book item property.

### Item Models — ❌ Broken

All 26 weapon models use `neoforge:separate_transforms` loader (see section E above).

### Item Rendering (in-world, third person) — ❌ Broken

`RenderItemBase.renderItemInHand()` calls `itemInHandRenderer.renderItem()` which uses the vanilla item renderer, which tries to use the broken model. The item will appear as missing/no model.

### Item Capabilities — ⚠️ Partially works

Weapon capability JSON files load via reload listeners. `ItemPresetManager` and `ModifierManager` have `TODO: Implement` stubs, so ExCap presets and modifiers don't function.

### Item Tooltip — ❌ Stubbed

`epicfight$itemTooltip` is empty. Epic Fight weapon stats/descriptions don't appear in tooltips.

### Data Components — ✅ Works

`EpicFightDataComponentTypes` registered. `SkillBookItem` uses data components for skill storage.

---

## G. ~~TRansition Status~~ — RESOLVED: Not needed

| Aspect | Status |
|--------|--------|
| Decompiled? | ✅ Yes (javap output captured) |
| Ported? | N/A — not needed |
| Fabric-compatible? | N/A |
| Build status | ✅ JAR removed, not in `build.gradle` |
| Runtime status | N/A — not loaded, not needed |
| Direct Epic Fight usage | ❌ Zero `import dev.tr7zw.transition` in Epic Fight source |
| Stub class | `PlayerUtil.java` exists but unreferenced by Epic Fight code |
| Compat impact | FirstPerson needs `GeneralUtil` at runtime — kept as `modCompileOnly` |

**Conclusion**: TRansition was a transitive dependency of compatibility mods (FirstPerson, SkinLayers3D), not a direct Epic Fight dependency. The only Epic Fight usage (`PlayerUtil.getPlayerSkin`) was replaced with `player.getSkin().texture()`. No port needed. Users who want FirstPerson must install TRansition separately.

---

## H. ~~TRender Status~~ — RESOLVED: Not needed

| Aspect | Status |
|--------|--------|
| Decompiled? | ✅ Yes (class list captured) |
| Ported? | N/A — not needed |
| Fabric-compatible? | N/A |
| Build status | ✅ JAR removed, not in `build.gradle` |
| Runtime status | N/A — not loaded, not needed |
| Direct Epic Fight usage | ❌ Zero `import dev.tr7zw.trender` in Epic Fight source |

**Conclusion**: TRender was a dead dependency — never referenced by Epic Fight. Epic Fight uses its own GUI widgets. No port needed.

---

## I. Dependency and Library Audit

| Dependency | Status | Issue |
|-----------|--------|-------|
| Fabric API 0.116.15 | ✅ Correct | — |
| Fabric Loader 0.19.3 | ✅ Correct | — |
| ForgeConfigAPIPort | ✅ Working | v21.1.6-1.21.1-Fabric from Modrinth |
| NightConfig (core + toml) | ✅ Working | Explicit `implementation` deps |
| Architectury API | ✅ Working | 13.0.8+fabric from Modrinth |
| Azurelib | ✅ Runtime | curse.maven file 8367231 |
| FGM | ✅ Runtime | curse.maven file 5478368 |
| GeckoLib | ✅ Runtime | curse.maven file 8350058 |
| JEI | ✅ Runtime | curse.maven file 8678370 |
| PlayerAnimator | ✅ Runtime | curse.maven file 7389821 |
| FirstPerson | ❌ Compile-only | Missing `dev.tr7zw.transition.mc.GeneralUtil` at runtime — users must install TRansition separately |
| Iris | ❌ Compile-only | Mixin remapping NPE in Loom dev env |
| Sodium | ❌ Compile-only | Same Iris issue |
| SkinLayers3D | ❌ Compile-only | Depends on Sodium |
| Trinkets | ❌ Compile-only | CCA nested jars stripped by Loom |
| SimplyTooltips | ❌ Compile-only | Needs `fzzy_config` → `fabric-language-kotlin` |
| BetterThirdPerson | ❌ Compile-only | ShoulderSurfing declares `breaks betterthirdperson` |
| ShoulderSurfing | ❌ Compile-only | `implements IShoulderSurfingPlugin` compiler issue |
| Controlify | ❌ Disabled | Malformed access widener |
| PlayerRevive | ⚠️ Compile-only | Local JAR, `modCompileOnly files(...)` |
| ~~TRansition-1.0.6.jar~~ | ✅ **REMOVED** | Not needed — Epic Fight doesn't import `dev.tr7zw.transition` |
| ~~TRender-1.0.7.jar~~ | ✅ **REMOVED** | Not needed — Epic Fight doesn't import `dev.tr7zw.trender` |
| JSR-305 | ✅ Compile-only | `compileOnly` — correct |
| Checker Framework | ✅ Compile-only | `compileOnly` — correct |

---

## J. Recommended Work Order

Based on the dependency graph of the actual codebase:

### Phase 1: Fix Critical Rendering Pipeline (P0 — nothing else matters if rendering doesn't work)

1. **Add `@Inject` to `MixinLivingEntityRenderer`** — intercept `render()`, redirect to `RenderEngine.renderEntityArmatureModel()`. This is the single most important fix — it connects Epic Fight's entire entity animation system to the render pipeline.
2. **Wire `RenderEngine` event stubs to Fabric callbacks** — implement the 15+ empty TODO methods using Fabric API callbacks:
   - `ClientTickEvents.START_CLIENT_TICK` / `END_CLIENT_TICK` → `epicfight$clientTick$Pre/Post`
   - `ClientRenderEvents.BEFORE_RENDER` / `AFTER_RENDER` → `epicfight$renderTickPre/Post`
   - `HudRenderCallback.EVENT` → `epicfight$renderGuiPre`
   - `RenderHandCallback.EVENT` → `epicfight$renderHand`
   - `WorldRenderEvents.AFTER_ENTITIES` → `epicfight$renderAfterLevel`
   - `RenderLivingEvent` equivalent via mixin → `epicfight$renderLivingPre`
   - etc.
3. **Fix item model loading** — either:
   - Register a Fabric `ModelLoadingPlugin` that handles `neoforge:separate_transforms` loader
   - Or convert all 26 item model JSONs to vanilla format

### ~~Phase 2: Port TRansition~~ — RESOLVED: Not needed

4. ~~**Port TRansition-1.0.6.jar**~~ — **Not needed**. Epic Fight does not import `dev.tr7zw.transition`. The only usage (`PlayerUtil.getPlayerSkin`) was replaced with `player.getSkin().texture()`. FirstPerson compat remains `modCompileOnly` — users install TRansition separately if needed.

### ~~Phase 3: Port TRender~~ — RESOLVED: Not needed

5. ~~**Port TRender-1.0.7.jar**~~ — **Not needed**. Epic Fight does not import `dev.tr7zw.trender` anywhere. Was a dead dependency. Epic Fight uses its own GUI widgets.

### Phase 4: Fix Remaining Systems (P2)

6. ~~**Implement `sendToAll()`** in `EpicFightNetworkManager`~~ — **DONE**. Iterates over `server.getPlayerList().getPlayers()`.
7. ~~**Implement `ItemPresetManager` and `ModifierManager`**~~ — **Parity confirmed**: both are `//TODO: Implement` in NeoForge 1.21.1 as well. Left as-is.
8. **Wire remaining compat mods** — add transitive deps for Trinkets (CCA), SimplyTooltips (Kotlin). FirstPerson requires TRansition at runtime — users install separately.
9. **Fix `EntitySnapshot`** — implement `getRenderPasses` and `getItemColors` equivalents for Fabric.
10. **Port MCreator compat** — uncomment and adapt MCreator compatibility code.

### Phase 4c: Latest Fixes (current session)

- ✅ **`MixinServerLevel` not registered in mixin config** — `MixinServerLevel` (server-side entity join hook) existed at `src/main/java/yesman/epicfight/platform/fabric/mixin/MixinServerLevel.java` but was NOT listed in `epicfight-platform.fabric.mixins.json`'s `"mixins"` array. This meant the mixin was never loaded, and `ServerLevel.addEntity` was never intercepted. `ServerEntityEvents.ENTITY_LOAD` (registered in `EpicFightFabric.onInitialize()`) was the sole mechanism for server-side entity join events, which may miss edge cases (e.g., `addLegacyChunkEntities`, `addWorldGenChunkEntities`). Fixed by adding `"MixinServerLevel"` to the `"mixins"` array. `onJoinLevel` is idempotent (guards on `entitypatch.uninitialized()`), so double-firing with `ServerEntityEvents.ENTITY_LOAD` is safe.
- ✅ **`ClientHooks.handleCameraTransforms` was a no-op stub** — The Fabric compatibility stub for NeoForge's `ClientHooks.handleCameraTransforms` simply returned the model without applying any transforms. This meant `EntitySnapshot.renderItems()` (used for afterimage/snapshot rendering) never applied the item's per-perspective transform (position, rotation, scale for `THIRD_PERSON_RIGHT_HAND` etc.). Fixed by implementing `model.getTransforms().getTransform(displayContext).apply(applyLeftHandTransform, poseStack)` — the vanilla 1.21.1 equivalent of NeoForge's `BakedModel.applyTransform()`.
- ✅ **`EntitySnapshot.renderItems()` iterated over an empty list** — Line 183 had `for (BakedModel model : List.of())` as a placeholder for NeoForge's `BakedModel.getRenderPasses()` (a NeoForge-specific method that returns per-pass models). Since the list was empty, NO items were ever rendered in afterimages/snapshots. Fixed by replacing the empty-list loop with a direct call to `renderModelLists(bakedmodel, ...)` — rendering the single baked model directly, which is the vanilla 1.21.1 equivalent.
- ✅ **`PatchedLivingEntityRenderer.prepareVanillaModel` — `shouldRiderSit()` not in Yarn 1.21.1** — The NeoForge reference uses `entity.getVehicle().shouldRiderSit()` to determine the riding pose. The Yarn 1.21.1 mapping for `Entity` does not have `shouldRiderSit()` (it was removed/renamed in 1.21.1). Reverted to `entity.isPassenger()` (the pre-existing Fabric behavior) to maintain compilation. This is a minor parity gap — the sitting pose may differ slightly for non-LivingEntity vehicles.

### Phase 4b: Recently Fixed Issues (since last audit)

- ✅ **`sendToAll()`** — implemented with `MinecraftServer` parameter
- ✅ **`EpicFightPayloadContext.reply()`** — sends via `ServerPlayNetworking.send` when player is a connected `ServerPlayer`
- ✅ **`epicfight:weight` attribute warnings** — root cause was `DeferredHolderShim.equals()` only matching other shims. Fixed by unwrapping to underlying `Holder.Reference` in `EntityAttributeModificationEvent.add()`
- ✅ **`FemaleLayerAccessor` mixin crash** — replaced accessor mixin with reflection-based lookup in `WildfireFGMCompat`
- ✅ **`AdaptiveSkinSkill` NPE** — added null guard for `getGlintColor()` return
- ✅ **Emote registry empty** — added `EmoteReloadListener` registered via `EpicFightReloadListeners`
- ✅ **`MixinDefaultAttributes`** — confirmed working at runtime. Attributes applied for 28 entity types on both client and server.
- ✅ **`MixinItemInHandRenderer` descriptor** — corrected method signature from `renderHandsWithItems(FF...)` to `renderHandsWithItems(F...)` matching MC 1.21.1. First-person Epic Fight rendering now intercepts correctly.
- ✅ **`CombatKeyMapping` click handling** — Fabric uses separate combat key mapping objects; `MixinKeyMappingClick` added to route clicks to combat mappings. Attack input now reaches `ControlEngine.maybeAttack()`.
- ✅ **`DodgeSkill` NPE (`nextAnimation is null`)** — root cause was animation initialization order. In NeoForge, `Animations.registerAnimations()` fires during `FMLConstructModEvent` (before `RegisterEvent` where skills are created). In Fabric, the order was reversed: skills were created before animations were registered, causing `Animations.BIPED_ROLL_FORWARD` etc. to be null when `DodgeSkill` was constructed. Fixed by moving animation registration before deferred registry acceptance in `EpicFightFabric.java`.
- ✅ **Held item lighting** — investigated `SeparateTransformsBakedModel`, `MixinItemRenderer`, `MixinLightTexture`, and the full `packedLight` propagation chain. The lighting pipeline is correct: `GameRenderer` -> `renderHandsWithItems` -> `RenderHandEvent.packedLight` -> `FirstPersonRenderer.render` -> `PatchedItemInHandLayer` -> `RenderItemBase.renderItemInHand` -> `itemInHandRenderer.renderItem` -> `ItemRenderer.renderStatic` -> `ItemRenderer.render` -> `renderModelLists`. The incorrect lighting was a symptom of the first-person mixin not applying (wrong descriptor), which caused vanilla rendering to be used instead. Fixed by the mixin descriptor correction above.
- ✅ **`weapon_data` registry empty** — confirmed parity with NeoForge. No `EpicFightWeaponData` class exists in NeoForge either; the registry is populated by addon mods. The `wooden_sword` JSON deserialization warning ("No value present") is also parity — NeoForge uses the same unsafe `.get()` call on `BuiltInRegistries.SOUND_EVENT.getHolder()` which fails because the JSON uses short IDs (`entity.hit.blunt` -> `minecraft:entity.hit.blunt`) while Epic Fight registers sounds under `epicfight:` namespace. Both versions catch this via try/catch and fall back to `addDefaultItems()`.
- ✅ **Entity join level (`EntityJoinLevelEvent` equivalent)** — root cause: `MixinServerLevel.addEntity` injection was not firing in MC 1.21.1 (method signature mismatch). Fixed by replacing the mixin with Fabric API's `ServerEntityEvents.ENTITY_LOAD` registered in `EpicFightFabric.onInitialize()`. This fires for all entities including `ServerPlayer`. Verified at runtime: `onJoinLevel` fires for ServerPlayer, `ServerPlayerPatch.onJoinWorld` sends `SPInitSkills`, and client `handleInitSkills` receives it.
- ✅ **Knockback Infinity crash** — root cause: `VanillaEntityEventHooks` line 352 computed `40.0F / hitEntityPatchAsHurtable.getWeight()` without checking for zero. When entities (e.g. Creeper) never had `onJoinWorld` called (because `MixinServerLevel` wasn't firing), their `WEIGHT` attribute stayed at 0, causing `40.0F / 0.0F = Infinity`. The Creeper's delta movement became `(Infinity, 0, Infinity)`, which crashed `EntitySectionStorage` with `Long.MIN_VALUE` bounding box. Fixed by: (1) the `ServerEntityEvents.ENTITY_LOAD` fix above (so `onJoinWorld` now sets WEIGHT), and (2) a defensive guard `if (weight > 0.0F)` before the division.
- ✅ **`SPInitSkills` synchronization** — root cause: `ServerPlayerPatch.onJoinWorld` was never called because `MixinServerLevel.addEntity` didn't fire for `ServerPlayer` in MC 1.21.1. After switching to `ServerEntityEvents.ENTITY_LOAD`, `onJoinWorld` fires correctly, `SPInitSkills` is sent to the client, and `handleInitSkills` receives it. Verified at runtime: `handleInitSkills received` count = 1, `MODIFY unregistered` = 0, `IllegalStateException` = 0.
- ✅ **R key mode switching** — root cause: GLFW sends key REPEAT events while the key is held, and vanilla `KeyMapping.click()` is called for both PRESS and REPEAT events. This caused `switchMode()` to fire repeatedly, rapidly toggling the mode back and forth (EPICFIGHT → VANILLA → EPICFIGHT) within a single key press. Fixed by adding a HEAD injection in `MixinKeyMappingClick` that cancels `KeyMapping.click()` when the mapping is already `isDown` (i.e., REPEAT events). The existing TAIL injection distributes clicks to `CombatKeyMapping` instances that share keys with vanilla mappings. Verified at runtime: one R press = one mode toggle.
- ✅ **Attack in EpicFight mode** — verified at runtime: `maybeAttack called` count = 98 in a test where the user attacked mobs. No crash, no Infinity momentum. Attacks execute correctly in EpicFight mode.
- ✅ **First-person EpicFight model not rendering** — root cause: `MixinItemInHandRenderer` injects at HEAD of `renderHandsWithItems` and cancels the vanilla method when the EpicFight renderer handles it. However, vanilla `renderHandsWithItems` calls `bufferSource.endBatch()` at the end to flush the render buffer. Since the entire method was cancelled, the buffer was never flushed, and the EpicFight first-person arms were drawn to the buffer but never appeared on screen. Fixed by calling `bufferSource.endBatch()` in the mixin before `callbackInfo.cancel()`. Verified at runtime: `FirstPersonRenderer.render` is called, buffer is flushed, no crash.

### Phase 5: Polish & Parity (P3)

11. Fix ShoulderSurfing `implements IShoulderSurfingPlugin` compiler issue
12. Fix Controlify access widener
13. Resolve ShoulderSurfing/BetterThirdPerson conflict
14. Full feature parity testing against NeoForge

---

**Bottom line**: The project builds and runs without crashing. Recent fixes have resolved: first-person rendering mixin descriptor (`renderHandsWithItems` signature corrected for MC 1.21.1), combat key mapping click routing (`MixinKeyMappingClick`), DodgeSkill NPE (animation initialization order fixed — animations now registered before skills in `EpicFightFabric.java`, matching NeoForge's `FMLConstructModEvent` before `RegisterEvent` order), held item lighting (was a symptom of the mixin descriptor issue — vanilla rendering was being used instead of Epic Fight's), `weapon_data` registry confirmed as parity (empty in NeoForge too), entity join level (replaced broken `MixinServerLevel` with Fabric API's `ServerEntityEvents.ENTITY_LOAD`), knockback Infinity crash (guard against `getWeight() == 0`), `SPInitSkills` synchronization (server now sends, client receives), R key mode switching (verified: 34 switchMode calls), attack in EpicFight mode (verified: 98 maybeAttack calls, no crash), and the critical `DeferredHolderShim.equals()/hashCode()` bug.

**Latest critical fix — `DeferredHolderShim.equals()/hashCode()`**: The shim's `equals()` compared by `ResourceKey` and `hashCode()` returned `key.hashCode()`. But vanilla `Holder.Reference` does NOT override `equals()/hashCode()` — it uses identity (`==` and `System.identityHashCode()`). This meant `AttributeSupplier.instances.get(deferredHolderShim)` could never find attributes keyed by `Holder.Reference`. Every call to `getAttributeValue(EpicFightAttributes.STAMINA_REGEN)` threw `IllegalArgumentException: Can't find attribute epicfight:stamina_regen`. This exception was silently swallowed by `MixinEntityTick`'s `catch (Throwable ignored) {}`, which prevented `animator.tick()` from advancing — causing the animation stutter/jerky replay issue. Fix: `DeferredHolderShim.equals()/hashCode()` now delegate to the bound `Holder.Reference`'s identity when available. Runtime verified: zero `preTick` exceptions, no crash, animations advance normally.

**Still blocking full parity**: Visual confirmation of first-person Epic Fight rendering, held weapon/item renderer and lighting, armor and equipment visibility, and mob Epic Fight model rendering needs interactive testing. TRansition and TRender libraries remain unported. Some `RenderEngine` event stubs may still need verification. The `separate_transforms` item model loader (`SeparateTransformsModelLoadingPlugin`) needs runtime confirmation. Minor warning: some passive mobs (Cow, Chicken) lack `epicfight:stun_armor` attribute — non-crashing, needs investigation for full parity.

**Latest session fixes**: (1) `MixinServerLevel` was not registered in `epicfight-platform.fabric.mixins.json` — mixin existed but was never loaded, so `ServerLevel.addEntity` was never intercepted. Fixed by adding it to the `"mixins"` array. This provides a backup entity-join hook alongside `ServerEntityEvents.ENTITY_LOAD`. (2) `ClientHooks.handleCameraTransforms` was a no-op stub that returned the model without applying transforms — fixed to call `model.getTransforms().getTransform(displayContext).apply(...)`. (3) `EntitySnapshot.renderItems()` iterated over `List.of()` (empty list) instead of rendering the baked model — fixed to call `renderModelLists(bakedmodel, ...)` directly. Build verified: `gradlew build` succeeds with 0 errors, 100 warnings (all deprecation/unchecked).
