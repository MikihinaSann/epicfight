## Summary
- Gate `CPUpdatePlayerInput` send on `xxa`/`zza` change instead of firing every tick (20/s) unconditionally
- NaN-init forces first-tick sync so initial state is always sent
- Bump `mod_version` to `20.14.17.1` and add changelog entry (required by the `publishMods` changelog gate)

## Why
The client streamed `CPUpdatePlayerInput` every tick unconditionally. The server re-broadcast it to every tracking player and flushed each packet, which dominated the server-thread profile (`sendToClient` ~29% incl, native `eventfd_write` ~15% as the hottest leaf). The payload is only `xxa`/`zza`, which change on input edges, so steady/stationary input was re-sent identical 20x/s. Gating the send on change cuts the send/flush fan-out at its source.

## Note
This branch was authored before `35c5a4a8` (Harden compute shader pipeline, AR compat fixes) landed on `1.20.1`, which refactored the same `MixinLocalPlayer#epicfight$tick` site to use `localPlayerPatch.sendPlayerInput(zza, xxa)` and fixed the `zza=forward, xxa=strafe` swap. There will be a merge conflict on `MixinLocalPlayer.java` — the throttle gate needs to be reworked against `sendPlayerInput` if this is merged, or rebased on top of `35c5a4a8`.

#### Test plan
- [ ] Build passes
- [ ] Movement still works (forward/back/strafe) on dedicated server
- [ ] Other clients see correct pose direction when a player moves
- [ ] Bandwidth/profile: confirm `CPUpdatePlayerInput` no longer fires every tick when stationary

Generated with [Devin](https://devin.ai)
