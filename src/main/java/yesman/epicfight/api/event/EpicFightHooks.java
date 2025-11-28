package yesman.epicfight.api.event;

import yesman.epicfight.api.client.event.EpicFightClientHooks;

public final class EpicFightHooks {

    /// we will eventually put all epic fight neoforge events here to decouple the event handling system originally
    /// conducted by mod-loaders (Forge, Neoforge, Fabric)
    /// There are a bunch of event definitions under {@link yesman.epicfight.api.neoevent} package. We
    /// plan to define each class as static fields of {@link EventHook} in future API model.
    ///
    /// Example snippet
    /// yesman.epicfight.api.event.EventHook<BattleModeSustainableEvent> BATTLE_MODE_TICK = EventHook.createHook();
    /// yesman.epicfight.api.event.EventHook<BuilderModificationEvent> MODIFY_SKILL_BUILDER = EventHook.createHook();
    /// ...
    ///
    /// See also with {@link EpicFightClientHooks}

	private EpicFightHooks() {}
}
