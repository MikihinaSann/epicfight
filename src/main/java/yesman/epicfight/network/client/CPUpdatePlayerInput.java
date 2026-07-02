package yesman.epicfight.network.client;

import java.util.function.Supplier;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import yesman.epicfight.network.EpicFightNetworkManager;
import yesman.epicfight.network.server.SPUpdatePlayerInput;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.player.ServerPlayerPatch;

public class CPUpdatePlayerInput {
	private float forward;
	private float strafe;

	public CPUpdatePlayerInput() {
	}

	public CPUpdatePlayerInput(float forward, float strafe) {
		this.forward = forward;
		this.strafe = strafe;
	}

	public static CPUpdatePlayerInput fromBytes(FriendlyByteBuf buf) {
		return new CPUpdatePlayerInput(buf.readFloat(), buf.readFloat());
	}

	public static void toBytes(CPUpdatePlayerInput msg, FriendlyByteBuf buf) {
		buf.writeFloat(msg.forward);
		buf.writeFloat(msg.strafe);
	}

	public static void handle(CPUpdatePlayerInput msg, Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> {
			ServerPlayer player = ctx.get().getSender();
			ServerPlayerPatch playerpatch = EpicFightCapabilities.getEntityPatch(player, ServerPlayerPatch.class);

			if (playerpatch != null) {
				playerpatch.dx = msg.strafe;
				playerpatch.dz = msg.forward;

				// Clients resend unchanged inputs as a periodic keepalive; only relay actual changes to trackers
				if (playerpatch.updateBroadcastPlayerInput(msg.forward, msg.strafe)) {
					EpicFightNetworkManager.sendToAllPlayerTrackingThisEntity(new SPUpdatePlayerInput(player.getId(), msg.forward, msg.strafe), player);
				}
			}
		});
		ctx.get().setPacketHandled(true);
	}
}
