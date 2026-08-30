# Fabric Port PR Cleanup Design

## Goal

Prepare the complete Minecraft 1.21.1 Fabric port for review by the official Epic Fight project. Remove proven-unused porting artifacts and dead implementation without changing supported behavior, document the port, and leave a minimal reviewable tree.

## Scope

Use conservative PR hygiene rather than a repository-wide refactor:

- Remove temporary audit, planning, and one-off migration scripts that are not used by the build.
- Remove dependencies, Java types, mixins, access-widener entries, and resources only when graph analysis, direct search, runtime registration checks, and the build all show that they are unnecessary.
- Keep runtime-loaded and API-compatibility code even when static analysis reports no direct caller.
- Clean temporary/debug wording while retaining comments that explain non-obvious Fabric constraints.
- Do not reformat the project or refactor unrelated gameplay code.

## README Compatibility Policy

Replace the undifferentiated “Fully Supported” list with loader-aware status based on evidence:

- Fabric load-tested: AzureLib, Female Gender, GeckoLib, JEI, and playerAnimator.
- Fabric integration present but requiring dedicated runtime verification: Better Third Person, First-person Model, Iris, Sodium, 3D Skin Layers, Shoulder Surfing, Simply Tooltips, Trinkets, and PlayerRevive.
- Not currently supported on Fabric: Controlify, KubeJS, Vampirism, and Werewolves.
- Do not claim Fabric support for Skill Tree or ParCool without test evidence.
- Preserve NeoForge claims separately; this cleanup does not retest NeoForge.

## Changelog

Add an English `[Unreleased]` section at the top of `CHANGELOG.md`. Describe shipped outcomes only:

- Fabric 1.21.1 loader support and Fabric-native registration/network/config wiring.
- Implemented or validated compatibility integrations.
- User-visible input, rendering, configuration, registry, and runtime fixes.
- Known compatibility limitations where useful.

Do not include AI attribution, prompts, internal audits, or failed porting experiments.

## Verification

Run the smallest sufficient checks after each cleanup group and a complete check at the end:

1. Graph and direct-text reference checks before deletion.
2. `git diff --check`.
3. `./gradlew build` with JDK 21.
4. Inspect the built JAR for excluded foreign namespaces and required Fabric metadata.
5. Run the dedicated server and client where automation supports reliable startup validation.
6. Review the final diff against `1.21.1` for accidental formatting or unrelated churn.

Any deletion that breaks a check is reverted unless a smaller root-cause correction is clear.

## History and PR Safety

The final Fabric-port history will be squashed into one BocchiSann-authored commit by the user so the PR does not expose intermediate AI trailers. No push, force-push, or PR creation occurs without explicit user confirmation at that stage.
