package yesman.epicfight.network.client;

import java.util.function.Supplier;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkEvent;
import yesman.epicfight.network.EpicFightByteBufs;
import yesman.epicfight.network.EpicFightNetworkManager;
import yesman.epicfight.network.common.SyncAnimationPositionPacket;
import yesman.epicfight.network.server.SPSyncAnimationPosition;

public class CPSyncPlayerAnimationPosition extends SyncAnimationPositionPacket {
	public CPSyncPlayerAnimationPosition(int entityId, float elapsedTime, Vec3 position, int lerpSteps) {
		super(entityId, elapsedTime, position, lerpSteps);
	}
	
	public static CPSyncPlayerAnimationPosition fromBytes(FriendlyByteBuf buf) {
		return new CPSyncPlayerAnimationPosition(EpicFightByteBufs.readEntityId(buf), buf.readFloat(), new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble()), buf.readByte());
	}

	public static void toBytes(CPSyncPlayerAnimationPosition msg, FriendlyByteBuf buf) {
		EpicFightByteBufs.writeEntityId(buf, msg.entityId);
		buf.writeFloat(msg.elapsedTime);
		buf.writeDouble(msg.position.x);
		buf.writeDouble(msg.position.y);
		buf.writeDouble(msg.position.z);
		buf.writeByte(msg.lerpSteps);
	}
	
	public static void handle(CPSyncPlayerAnimationPosition msg, Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> {
			ServerPlayer sender = ctx.get().getSender();

			// Only allow a client to sync the position of ITS OWN player entity. Without this
			// check a client could spoof any entity id and broadcast arbitrary position/animation
			// state to everyone tracking that victim (teleport/desync griefing).
			if (sender == null || msg.entityId != sender.getId()) {
				return;
			}

			EpicFightNetworkManager.sendToAllPlayerTrackingThisEntity(new SPSyncAnimationPosition(sender.getId(), msg.elapsedTime, msg.position, msg.lerpSteps), sender);
		});
		ctx.get().setPacketHandled(true);
	}
}
