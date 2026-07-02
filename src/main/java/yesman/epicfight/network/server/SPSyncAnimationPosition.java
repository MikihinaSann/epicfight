package yesman.epicfight.network.server;

import java.util.function.Supplier;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkEvent;
import yesman.epicfight.network.EpicFightByteBufs;
import yesman.epicfight.network.common.SyncAnimationPositionPacket;

public class SPSyncAnimationPosition extends SyncAnimationPositionPacket {
	public SPSyncAnimationPosition(int entityId, float elapsedTime, Vec3 position, int lerpSteps) {
		super(entityId, elapsedTime, position, lerpSteps);
	}
	
	public static SPSyncAnimationPosition fromBytes(FriendlyByteBuf buf) {
		return new SPSyncAnimationPosition(EpicFightByteBufs.readEntityId(buf), buf.readFloat(), new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble()), buf.readByte());
	}

	public static void toBytes(SPSyncAnimationPosition msg, FriendlyByteBuf buf) {
		EpicFightByteBufs.writeEntityId(buf, msg.entityId);
		buf.writeFloat(msg.elapsedTime);
		buf.writeDouble(msg.position.x);
		buf.writeDouble(msg.position.y);
		buf.writeDouble(msg.position.z);
		buf.writeByte(msg.lerpSteps);
	}
	
	public static void handle(SPSyncAnimationPosition msg, Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> {
			Entity entity = Minecraft.getInstance().level.getEntity(msg.entityId);
			
			if (entity != null && entity instanceof LivingEntity livingentity) {
				livingentity.lerpX = msg.position.x;
				livingentity.lerpY = msg.position.y;
				livingentity.lerpZ = msg.position.z;
				livingentity.lerpSteps = msg.lerpSteps;
			}
		});
		ctx.get().setPacketHandled(true);
	}
}
