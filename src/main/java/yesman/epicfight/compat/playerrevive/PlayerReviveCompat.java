package yesman.epicfight.compat.playerrevive;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.minecraft.world.entity.player.Player;

import team.creative.playerrevive.server.PlayerReviveServer;
import yesman.epicfight.api.event.EpicFightEventHooks;
import yesman.epicfight.api.event.IdentifierProvider;
import yesman.epicfight.compat.ICompatModule;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;

public class PlayerReviveCompat implements ICompatModule {
    @Override
	public void onInitialize() {

    }

    @Override
	public void onInitializeServer() {
        // Fabric equivalent of NeoForge's EntityJoinLevelEvent.
        // When a player joins the level, register a permanent skill-cast listener
        // that cancels skill casting while the player is downed (bleeding).
        ServerEntityEvents.ENTITY_LOAD.register((entity, level) -> {
            if (entity instanceof Player player) {
                EpicFightCapabilities.getPlayerPatchAsOptional(player).ifPresent(playerPatch -> {
                    playerPatch.getEventListener().registerEvent(
                        EpicFightEventHooks.Player.CAST_SKILL,
                        skillCastEvent -> {
                            if (PlayerReviveServer.isBleeding(player)) {
                                skillCastEvent.cancel();
                            }
                        },
                        IdentifierProvider.PERMANENT_LISTENER
                    );
                });
            }
        });
    }

    @Override
	public void onInitializeClient() {

    }

    @Override
	public void onInitializeClientServer() {

    }
}
