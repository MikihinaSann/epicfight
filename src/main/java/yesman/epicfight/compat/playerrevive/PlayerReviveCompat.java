package yesman.epicfight.compat.playerrevive;

// PlayerRevive compat — waiting for CurseForge approval (playerrevive-refabric).
// Once approved, uncomment imports and server logic below.
// https://www.curseforge.com/minecraft/mc-mods/playerrevive-refabric

// import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
// import net.minecraft.world.entity.player.Player;
// import team.creative.playerrevive.server.PlayerReviveServer;
// import yesman.epicfight.api.event.EpicFightEventHooks;
// import yesman.epicfight.api.event.IdentifierProvider;
// import yesman.epicfight.world.capabilities.EpicFightCapabilities;

import yesman.epicfight.compat.ICompatModule;

public class PlayerReviveCompat implements ICompatModule {
    @Override
	public void onInitialize() {
    }

    @Override
	public void onInitializeServer() {
        // Waiting for CurseForge approval (playerrevive-refabric)
        // ServerEntityEvents.ENTITY_LOAD.register((entity, level) -> {
        //     if (entity instanceof Player player) {
        //         EpicFightCapabilities.getPlayerPatchAsOptional(player).ifPresent(playerPatch -> {
        //             playerPatch.getEventListener().registerEvent(
        //                 EpicFightEventHooks.Player.CAST_SKILL,
        //                 skillCastEvent -> {
        //                     if (PlayerReviveServer.isBleeding(player)) {
        //                         skillCastEvent.cancel();
        //                     }
        //                 },
        //                 IdentifierProvider.PERMANENT_LISTENER
        //             );
        //         });
        //     }
        // });
    }

    @Override
	public void onInitializeClient() {
    }

    @Override
	public void onInitializeClientServer() {
    }
}
