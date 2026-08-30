# Fabric Port PR Cleanup Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Produce a conservative, reviewable Minecraft 1.21.1 Fabric-port PR with no temporary port artifacts, no proven-dead compatibility shims, accurate compatibility documentation, and verified build/runtime behavior.

**Architecture:** Preserve the current single-loader Fabric architecture and remove only code whose absence is proven by graph, direct registration searches, and successful builds. Replace local NeoForge side-marker stand-ins with Fabric Loader APIs, fail fast for essential initialization, and retain recovery only for genuinely optional runtime integrations.

**Tech Stack:** Java 21, Minecraft 1.21.1, Fabric Loader 0.19.3, Fabric API, Fabric Loom, Gradle 9.5.1, NightConfig.

**Spec:** `docs/superpowers/specs/2026-08-31-fabric-port-pr-cleanup-design.md`

## Global Constraints

- Do not change unrelated gameplay behavior or reformat untouched files.
- Preserve original CRLF/LF endings; edit existing files without whole-file line-ending conversion.
- Treat Mixin JSON, Fabric entrypoints, service providers, codecs, and reflection as runtime references even when static graph fan-in is zero.
- Every deleted Java type must have zero external source references and zero runtime registration references before deletion.
- Run Gradle with `JAVA_HOME="C:/Program Files/Java/jdk-21.0.11"`.
- Do not push, force-push, squash, or create a PR without explicit user confirmation.
- Temporary design and plan documents must not appear in the final upstream diff.

---

### Task 1: Remove One-Off Port Artifacts

**Files:**
- Delete: `AUDIT_STATUS.md`
- Delete: `PLAN.md`
- Delete: `generate_azurelib_stubs.ps1`
- Delete: `generate_controlify_stubs.ps1`
- Delete: `port_fix_patterns.ps1`
- Delete: `port_replacements.ps1`
- Delete: `port_replacements_2.ps1`
- Delete: `port_replacements_3.ps1`

**Interfaces:**
- Consumes: repository build files and checked-in generated/stub sources.
- Produces: a source tree without unreferenced migration-session artifacts.

- [ ] **Step 1: Prove none is part of the build**

```bash
grep -RInE 'AUDIT_STATUS|PLAN\.md|generate_(azurelib|controlify)_stubs|port_(fix_patterns|replacements)' \
  --exclude-dir=.git --exclude-dir=.remember --exclude-dir=build .
```

Expected: no Gradle, resource, or Java code invokes the eight files.

- [ ] **Step 2: Delete the artifacts**

```bash
git rm AUDIT_STATUS.md PLAN.md generate_azurelib_stubs.ps1 generate_controlify_stubs.ps1 \
  port_fix_patterns.ps1 port_replacements.ps1 port_replacements_2.ps1 port_replacements_3.ps1
```

- [ ] **Step 3: Compile**

```bash
JAVA_HOME="C:/Program Files/Java/jdk-21.0.11" ./gradlew compileJava --console=plain
```

Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 4: Commit**

Commit message: `Remove one-off Fabric port artifacts`. This is intermediate history; the user will replace it with one final BocchiSann-authored squash.

---

### Task 2: Delete Proven-Dead Java Compatibility Code

**Files:**
- Delete: `src/main/java/yesman/epicfight/api/utils/RegistryHelper.java`
- Delete: `src/main/java/yesman/epicfight/generated/SoundKeys.java`
- Delete: `src/main/java/yesman/epicfight/mixin/common/AttributeSupplierBuilderAccessor.java`
- Delete: `src/main/java/yesman/epicfight/data/DataEvents.java`
- Delete: `src/main/java/yesman/epicfight/registry/entries/EpicFightAttachmentTypes.java`
- Delete: `src/main/java/yesman/epicfight/platform/neoforged/attachment/AttachmentType.java`
- Delete: `src/main/java/yesman/epicfight/platform/neoforged/common/NeoForgeConfig.java`
- Delete: `src/main/java/yesman/epicfight/platform/neoforged/common/loot/LootModifier.java`
- Delete: `src/main/java/yesman/epicfight/platform/neoforged/data/event/GatherDataEvent.java`
- Delete: `src/main/java/yesman/epicfight/platform/neoforged/event/LootTableLoadEvent.java`
- Delete: `src/main/java/yesman/epicfight/platform/neoforged/registries/DeferredHolder.java`
- Delete: `src/main/java/yesman/epicfight/platform/neoforged/registries/NeoForgeRegistries.java`
- Delete: `src/main/java/yesman/epicfight/platform/neoforged/event/entity/RegisterSpawnPlacementsEvent.java`
- Delete: `src/main/java/yesman/epicfight/platform/neoforged/capabilities/ICapabilityProvider.java`
- Delete: `src/main/java/yesman/epicfight/platform/neoforged/fml/config/IConfigSpec.java`
- Modify: `src/main/java/yesman/epicfight/registry/entries/EpicFightEntityTypes.java`
- Modify: `src/main/java/yesman/epicfight/world/capabilities/provider/CommonItemCapabilityProvider.java`
- Modify: `src/main/java/yesman/epicfight/platform/neoforged/common/ModConfigSpec.java`
- Modify: `src/main/java/yesman/epicfight/registry/deferred/holders/DeferredPreset.java`
- Modify: `src/main/java/yesman/epicfight/api/ex_cap/modules/core/data/BuilderEntry.java`

**Interfaces:**
- Consumes: direct Fabric spawn registration in `EpicFightFabric`, item capability lookup through `CommonItemCapabilityProvider.INSTANCE`, and the NightConfig-backed `ModConfigSpec` API.
- Produces: the same runtime registrations without unused NeoForge-shaped declarations.

- [ ] **Step 1: Verify references**

Run exact-name searches for every candidate. Expected external references are limited to:

- `RegisterSpawnPlacementsEvent`: unused `EpicFightEntityTypes.registerSpawnPlacementsEvent`.
- `ICapabilityProvider`: `CommonItemCapabilityProvider` implements clause.
- `IConfigSpec`: `ModConfigSpec` implements clause.
- `DeferredHolder`: documentation in `DeferredPreset` and `BuilderEntry`.
- `GatherDataEvent`: unused `DataEvents`.
- All others: definitions or prose only.

Also verify `AttributeSupplierBuilderAccessor` is absent from every Mixin JSON.

- [ ] **Step 2: Remove declarations and references**

Delete the listed files. Remove the unused spawn-placement method/import from `EpicFightEntityTypes`; remove the provider interface/import from `CommonItemCapabilityProvider`; remove the config interface/import from `ModConfigSpec`. Update the two deferred-holder comments to refer to `DeferredHolderShim` or deferred holders generically.

- [ ] **Step 3: Prove removed symbols are gone**

```bash
grep -RInE 'RegistryHelper|SoundKeys|AttributeSupplierBuilderAccessor|DataEvents|EpicFightAttachmentTypes|NeoForgeConfig|LootModifier|LootTableLoadEvent|GatherDataEvent|RegisterSpawnPlacementsEvent|ICapabilityProvider|IConfigSpec|NeoForgeRegistries' src/main/java src/main/resources
```

Expected: no code references. Comments may mention a NeoForge concept only when they explain an active Fabric replacement.

- [ ] **Step 4: Build**

```bash
JAVA_HOME="C:/Program Files/Java/jdk-21.0.11" ./gradlew build --console=plain
```

Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 5: Commit**

Commit message: `Remove unused compatibility scaffolding`.

---

### Task 3: Use Native Fabric Side Markers

**Files:**
- Delete: `src/main/java/yesman/epicfight/platform/neoforged/api/distmarker/Dist.java`
- Delete: `src/main/java/yesman/epicfight/platform/neoforged/api/distmarker/OnlyIn.java`
- Delete: `src/main/java/yesman/epicfight/platform/neoforged/fml/loading/FMLEnvironment.java`
- Modify: `src/main/java/yesman/epicfight/api/asset/JsonAssetLoader.java`
- Modify: `src/main/java/yesman/epicfight/api/client/camera/EpicFightCameraAPI.java`
- Modify: `src/main/java/yesman/epicfight/api/utils/side/ClientOnly.java`
- Modify: `src/main/java/yesman/epicfight/data/conditions/Condition.java`
- Modify: the eleven files under `src/main/java/yesman/epicfight/data/conditions/entity/` currently importing `OnlyIn`

**Interfaces:**
- Consumes: `FabricLoader.getInstance().getEnvironmentType()` and `@Environment(EnvType.CLIENT)`.
- Produces: unchanged client/server gating without NeoForge side-marker stand-ins.

- [ ] **Step 1: Replace server checks**

In `JsonAssetLoader`, import `EnvType` and `FabricLoader`, compute:

```java
boolean dedicatedServer = FabricLoader.getInstance().getEnvironmentType() == EnvType.SERVER;
```

Use it in both conditions currently comparing with `Dist.DEDICATED_SERVER`.

- [ ] **Step 2: Replace client annotations**

Replace the local `OnlyIn` import with `net.fabricmc.api.Environment`, then replace `@OnlyIn(EnvType.CLIENT)` with `@Environment(EnvType.CLIENT)` in every listed file.

- [ ] **Step 3: Delete stand-ins and search**

```bash
git rm src/main/java/yesman/epicfight/platform/neoforged/api/distmarker/{Dist,OnlyIn}.java \
  src/main/java/yesman/epicfight/platform/neoforged/fml/loading/FMLEnvironment.java
grep -RInE 'platform\.neoforged\.(api\.distmarker|fml\.loading)|@OnlyIn' src/main/java
```

Expected: no matches.

- [ ] **Step 4: Build and commit**

Run the full build; expected `BUILD SUCCESSFUL`. Commit message: `Use Fabric environment APIs directly`.

---

### Task 4: Remove Port-Time Failure Masking and Diagnostic Noise

**Files:**
- Modify: `src/main/java/yesman/epicfight/EpicFightFabric.java`
- Modify: `src/main/java/yesman/epicfight/EpicFightFabricClient.java`
- Modify: `src/main/java/yesman/epicfight/mixin/client/MixinClientPacketListener.java`
- Modify: `src/main/java/yesman/epicfight/platform/fabric/mixin/MixinServerLevel.java`
- Modify: `src/main/java/yesman/epicfight/platform/fabric/event/EntityAttributeModificationEvent.java`
- Modify: `src/main/java/yesman/epicfight/network/EpicFightClientPayloadRegistration.java`
- Modify: `src/main/java/yesman/epicfight/network/EpicFightPayloadRegistration.java`
- Modify: `src/main/java/yesman/epicfight/network/EpicFightServerBoundPayloadHandler.java`
- Modify: `src/main/java/yesman/epicfight/network/EpicFightReloadListeners.java`
- Modify: `src/main/java/yesman/epicfight/registry/callbacks/SkillDataKeyCallbacks.java`
- Modify: `src/main/java/yesman/epicfight/mixin/client/MixinInventory.java`
- Modify: `build.gradle`
- Modify: `gradle.properties`

**Interfaces:**
- Consumes: Fabric lifecycle registration and existing optional compatibility gates.
- Produces: deterministic required initialization; recovery remains only around optional integrations, resource reloads, compute capability checks, and external services.

- [ ] **Step 1: Simplify required initialization**

Call required networking, config, registry, renderer, particle, key mapping, and event registrations directly instead of catching `Throwable` around each group. Remove startup-success and per-group success logs. Required registration failures must abort startup rather than leave a partially initialized mod.

Keep recovery only for optional compatibility modules, compute capability checks, resource/data reload futures, and external authentication/network requests. For retained catches, pass the exception object to the logger instead of logging only `getMessage()`.

- [ ] **Step 2: Remove trace spam**

Remove unconditional “fired”, fixed-count registration, and per-entry summary logs from the listed Mixin, networking, attribute, and callback files. Keep diagnostics for malformed external data or genuinely recoverable failures. Remove the two temporary Controlify investigation comments in `MixinInventory` without changing behavior.

- [ ] **Step 3: Remove dormant build configuration**

Delete the commented Controlify dependency and `controlify_file_id`. Remove the direct Architectury runtime dependency and run `compileJava`; expected success because Epic Fight imports no Architectury API. If Simply Tooltips signatures require Architectury during compilation, restore it as `modCompileOnly`, not `modImplementation`.

- [ ] **Step 4: Verify and build**

```bash
grep -RInE 'static initializer fired|TAIL.*fired|HEAD fired|initialized successfully|registered \([0-9]+\)|Some adjustments may be required|Remove this comment' src/main/java build.gradle
grep -RIn 'catch (Throwable' src/main/java/yesman/epicfight/EpicFightFabric*.java
JAVA_HOME="C:/Program Files/Java/jdk-21.0.11" ./gradlew build --console=plain
```

Expected: no marker matches; remaining broad catches occur only at optional/recoverable boundaries; build succeeds.

- [ ] **Step 5: Commit**

Commit message: `Fail fast during required Fabric initialization`.

---

### Task 5: Document Actual Compatibility and Port Changes

**Files:**
- Modify: `README.md:183-212`
- Modify: `CHANGELOG.md:1-8`

**Interfaces:**
- Consumes: dependency/runtime status in `build.gradle`, `fabric.mod.json`, and validated startup evidence.
- Produces: loader-aware compatibility claims and an English unreleased changelog.

- [ ] **Step 1: Replace README compatibility list**

Replace the unconditional support list with a table containing `Mod`, `NeoForge`, `Fabric`, and `Notes`:

- Fabric load-tested: AzureLib, Female Gender, GeckoLib, JEI, playerAnimator.
- Integration present; dedicated verification pending: Better Third Person, First-person Model, Iris, Sodium, 3D Skin Layers, Shoulder Surfing Reloaded, Simply Tooltips, Trinkets, PlayerRevive.
- Not currently supported: Controlify, KubeJS, Vampirism, Werewolves.
- No Fabric claim without evidence: Epic Fight: Skill Tree and ParCool.

Preserve existing links. Do not lower existing NeoForge claims solely because this work did not retest NeoForge.

- [ ] **Step 2: Add `[Unreleased]` to CHANGELOG**

Add `Added`, `Changed`, `Fixed`, and `Known limitations` sections above `21.17.3`. Cover Fabric support; native registries/networking/lifecycle/reload/config/capability/input/render wiring; Trinkets replacing Curios; shared mouse binding, block outline, normal transform, baked-light UV, registry/payload, config class collision fixes; and current compatibility limits. Omit prompts, AI attribution, audits, and failed experiments.

- [ ] **Step 3: Validate and commit**

```bash
grep -nE '^## \[Unreleased\]|Fabric|Controlify|KubeJS|Trinkets' CHANGELOG.md README.md
grep -n 'Fully Supported and Compatible Mods' README.md
```

Expected: new sections/statuses present; old unconditional heading absent. Commit message: `Document Fabric support and compatibility status`.

---

### Task 6: Full Verification and Upstream-Diff Cleanup

**Files:**
- Delete before handoff: `docs/superpowers/specs/2026-08-31-fabric-port-pr-cleanup-design.md`
- Delete before handoff: `docs/superpowers/plans/2026-08-31-fabric-port-pr-cleanup.md`
- Inspect: every path changed relative to `1.21.1...HEAD`

**Interfaces:**
- Consumes: cleaned source/build/docs from Tasks 1–5.
- Produces: verified working tree ready for the user-owned squash and explicit PR confirmation.

- [ ] **Step 1: Static and build checks**

```bash
git diff --check 1.21.1...HEAD
JAVA_HOME="C:/Program Files/Java/jdk-21.0.11" ./gradlew clean build --console=plain
```

Expected: no whitespace errors and `BUILD SUCCESSFUL`.

- [ ] **Step 2: Inspect the JAR**

```bash
JAR=$(find build/libs -maxdepth 1 -name 'Epic Fight-fabric-*.jar' ! -name '*sources*' ! -name '*api*' | head -1)
unzip -l "$JAR" | grep -E 'net/neoforged|dev/isxander|mod/azure|AUDIT_STATUS|PLAN\.md|port_.*\.ps1'
unzip -p "$JAR" fabric.mod.json | grep '"id"'
```

Expected: forbidden namespace/artifact grep has no matches; Fabric metadata contains the Epic Fight mod id.

- [ ] **Step 3: Smoke-test server and client**

Run `./gradlew runServer` with a bounded timeout and confirm normal server startup without Epic Fight exceptions. Run `./gradlew runClient` with a bounded timeout and confirm title-screen or world startup without Epic Fight exceptions. Stop each cleanly after evidence is captured.

- [ ] **Step 4: Review complete upstream diff**

```bash
git diff --stat 1.21.1...HEAD
git diff --name-status 1.21.1...HEAD
git log --format='%h %s%n%(trailers:key=Co-Authored-By)' 1.21.1..HEAD
```

Reject unrelated formatting churn, binaries, local logs, planning artifacts, and unsupported compatibility claims.

- [ ] **Step 5: Remove temporary planning documents**

Delete the design and plan files. After the user-owned squash, confirm neither path appears in the upstream diff.

- [ ] **Step 6: Stop for user review**

Report changed/deleted files, build/JAR/server/client evidence, and unresolved limitations. Do not squash, push, force-push, or create the PR. Ask the user to inspect the diff and explicitly approve the history rewrite and later PR creation.
