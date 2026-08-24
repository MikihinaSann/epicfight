package yesman.epicfight.compat.playerrevive;
import net.minecraft.client.Minecraft;

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
        // TODO: Port EntityJoinLevelEvent to Fabric callback
        // Original code registered on EntityJoinLevelEvent to add skill cancel listener
    }

    @Override
	public void onInitializeClient() {

    }

    @Override
	public void onInitializeClientServer() {

    }
}
