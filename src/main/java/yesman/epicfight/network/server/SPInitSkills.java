package yesman.epicfight.network.server;

import java.util.function.Supplier;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import yesman.epicfight.client.ClientEngine;
import yesman.epicfight.client.world.capabilites.entitypatch.player.LocalPlayerPatch;
import yesman.epicfight.network.EpicFightByteBufs;
import yesman.epicfight.world.capabilities.skill.CapabilitySkill;

public record SPInitSkills(CompoundTag serializedSkill) {
	public SPInitSkills(CapabilitySkill skillCapability) {
		this(skillCapability.serialize());
	}
	
	public static SPInitSkills fromBytes(FriendlyByteBuf buf) {
		return new SPInitSkills(EpicFightByteBufs.readCompressedNbt(buf));
	}

	public static void toBytes(SPInitSkills msg, FriendlyByteBuf buf) {
		EpicFightByteBufs.writeCompressedNbt(buf, msg.serializedSkill());
	}
	
	public static void handle(SPInitSkills msg, Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> {
			LocalPlayerPatch playerpatch = ClientEngine.getInstance().getPlayerPatch();
			
			if (playerpatch != null) {
				playerpatch.getSkillCapability().deserialize(msg.serializedSkill());
			}
		});
		ctx.get().setPacketHandled(true);
	}
}
