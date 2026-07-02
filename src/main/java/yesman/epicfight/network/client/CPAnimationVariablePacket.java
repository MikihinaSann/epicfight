package yesman.epicfight.network.client;

import java.util.function.Supplier;

import javax.annotation.Nullable;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import yesman.epicfight.api.animation.AnimationManager;
import yesman.epicfight.api.animation.SynchedAnimationVariableKey;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.api.asset.AssetAccessor;
import yesman.epicfight.network.EpicFightByteBufs;
import yesman.epicfight.network.EpicFightNetworkManager;
import yesman.epicfight.network.common.AnimationVariablePacket;
import yesman.epicfight.network.server.SPAnimationVariablePacket;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.player.ServerPlayerPatch;

public class CPAnimationVariablePacket<T> extends AnimationVariablePacket<T> {
	public CPAnimationVariablePacket(SynchedAnimationVariableKey<T> animationVariableKey, @Nullable AssetAccessor<? extends StaticAnimation> animation, T value, AnimationVariablePacket.Action action) {
		super(animationVariableKey, animation, value, action);
	}
	
	public static <T> CPAnimationVariablePacket<T> fromBytes(FriendlyByteBuf buf) {
		SynchedAnimationVariableKey<T> variableKey = SynchedAnimationVariableKey.byId(buf.readVarInt());
		AssetAccessor<? extends StaticAnimation> animation = AnimationManager.byId(EpicFightByteBufs.readSignedVarInt(buf));
		AnimationVariablePacket.Action action = buf.readEnum(AnimationVariablePacket.Action.class);

		return new CPAnimationVariablePacket<> (variableKey, animation, action == AnimationVariablePacket.Action.PUT ? variableKey.getPacketBufferCodec().decode(buf) : null, action);
	}

	public static <T> void toBytes(CPAnimationVariablePacket<T> msg, FriendlyByteBuf buf) {
		buf.writeVarInt(msg.animationVariableKey.getId());
		EpicFightByteBufs.writeSignedVarInt(buf, msg.animation.get().getId());
		buf.writeEnum(msg.action);
		
		if (msg.action == AnimationVariablePacket.Action.PUT) {
			msg.animationVariableKey.getPacketBufferCodec().encode(msg.value, buf);
		}
	}
	
	public static <T> void handle(CPAnimationVariablePacket<T> msg, Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(()-> {
			EpicFightCapabilities.getUnparameterizedEntityPatch(ctx.get().getSender(), ServerPlayerPatch.class).ifPresent(playerpatch -> {
				msg.process(playerpatch);
				EpicFightNetworkManager.sendToAllPlayerTrackingThisEntity(new SPAnimationVariablePacket<>(playerpatch, msg.animationVariableKey, msg.animation, msg.value, msg.action), playerpatch.getOriginal());
			});
		});
		ctx.get().setPacketHandled(true);
	}
}
