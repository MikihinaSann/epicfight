package yesman.epicfight.network.server;

import java.util.function.Supplier;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import yesman.epicfight.api.animation.AnimationManager;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.api.asset.AssetAccessor;
import yesman.epicfight.network.EpicFightByteBufs;
import yesman.epicfight.network.common.AnimatorControlPacket;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

public class SPAnimatorControl extends AnimatorControlPacket {
	protected int entityId;
	protected Layer layer = Layer.ANIMATION;
	protected Priority priority = Priority.ANIMATION;
	
	public SPAnimatorControl(AnimatorControlPacket.Action action, AssetAccessor<? extends StaticAnimation> animation, float transitionTimeModifier, LivingEntityPatch<?> entitypatch) {
		this(action, animation.get().getId(), entitypatch.getOriginal().getId(), transitionTimeModifier, false);
	}
	
	public SPAnimatorControl(AnimatorControlPacket.Action action, AssetAccessor<? extends StaticAnimation> animation, int entityId, float transitionTimeModifier, boolean pause) {
		this(action, animation.get().getId(), entityId, transitionTimeModifier, pause);
	}
	
	public SPAnimatorControl(AnimatorControlPacket.Action action, int animationId, int entityId, float transitionTimeModifier, boolean pause) {
		super(action, animationId, transitionTimeModifier, pause);
		
		this.entityId = entityId;
	}
	
	public SPAnimatorControl(AnimatorControlPacket.Action action, AssetAccessor<? extends StaticAnimation> animation, float transitionTimeModifier, LivingEntityPatch<?> entitypatch, Layer layer, Priority priority) {
		this(action, animation.get().getId(), entitypatch.getOriginal().getId(), transitionTimeModifier, false);
		
		this.layer = layer;
		this.priority = priority;
	}
	
	public SPAnimatorControl(AnimatorControlPacket.Action action, int animationId, int entityId, float transitionTimeModifier, boolean pause, Layer layer, Priority priority) {
		super(action, animationId, transitionTimeModifier, pause);
		
		this.entityId = entityId;
		this.layer = layer;
		this.priority = priority;
	}
	
	public <T extends SPAnimatorControl> void onArrive() {
		EpicFightCapabilities.getUnparameterizedEntityPatch(Minecraft.getInstance().level.getEntity(this.entityId), LivingEntityPatch.class).ifPresent(entitypatch -> {
			if (this.action == Action.PLAY_CLIENT && this.layer != Layer.ANIMATION && this.priority != Priority.ANIMATION) {
				entitypatch.getClientAnimator().playAnimationAt(AnimationManager.byId(this.animationId), this.transitionTimeModifier, this.layer, this.priority);
			} else {
				this.process(entitypatch);
			}
		});
	}
	
	public static SPAnimatorControl fromBytes(FriendlyByteBuf buf) {
		Action action = buf.readEnum(Action.class);
		int animationId = EpicFightByteBufs.readSignedVarInt(buf);
		int entityId = EpicFightByteBufs.readEntityId(buf);
		float transitionTimeModifier = buf.readFloat();
		boolean pause = buf.readBoolean();

		// Layer/Priority are only consumed by the PLAY_CLIENT path in onArrive, so they're only on the wire for that action
		if (action == Action.PLAY_CLIENT) {
			return new SPAnimatorControl(action, animationId, entityId, transitionTimeModifier, pause, buf.readEnum(Layer.class), buf.readEnum(Priority.class));
		}

		return new SPAnimatorControl(action, animationId, entityId, transitionTimeModifier, pause);
	}

	public static void toBytes(SPAnimatorControl msg, FriendlyByteBuf buf) {
		buf.writeEnum(msg.action);
		EpicFightByteBufs.writeSignedVarInt(buf, msg.animationId);
		EpicFightByteBufs.writeEntityId(buf, msg.entityId);
		buf.writeFloat(msg.transitionTimeModifier);
		buf.writeBoolean(msg.pause);

		if (msg.action == Action.PLAY_CLIENT) {
			buf.writeEnum(msg.layer);
			buf.writeEnum(msg.priority);
		}
	}
	
	public static void handle(SPAnimatorControl msg, Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> {
			msg.onArrive();
		});
		
		ctx.get().setPacketHandled(true);
	}
}