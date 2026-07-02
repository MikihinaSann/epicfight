package yesman.epicfight.network;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import io.netty.handler.codec.EncoderException;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.network.FriendlyByteBuf;

/**
 * Wire-format helpers for Epic Fight packets. Entity ids are small non-negative
 * ints in practice, so VarInt encoding shrinks them to 1-3 bytes. Ids that can
 * legitimately be negative (animation ids use -1 for "none", grapple target id
 * uses -1 for "clear") must go through the zigzag-encoded signed variant so the
 * sign bit doesn't force a 5-byte VarInt.
 */
public class EpicFightByteBufs {
	public static void writeEntityId(FriendlyByteBuf buf, int entityId) {
		buf.writeVarInt(entityId);
	}

	public static int readEntityId(FriendlyByteBuf buf) {
		return buf.readVarInt();
	}

	public static void writeSignedVarInt(FriendlyByteBuf buf, int value) {
		buf.writeVarInt((value << 1) ^ (value >> 31));
	}

	public static int readSignedVarInt(FriendlyByteBuf buf) {
		int encoded = buf.readVarInt();
		return (encoded >>> 1) ^ -(encoded & 1);
	}

	public static void writeUniversalOrdinal(FriendlyByteBuf buf, int universalOrdinal) {
		buf.writeVarInt(universalOrdinal);
	}

	public static int readUniversalOrdinal(FriendlyByteBuf buf) {
		return buf.readVarInt();
	}

	/** GZIP-compressed NBT for large login-time payloads. Nullable-safe. */
	public static void writeCompressedNbt(FriendlyByteBuf buf, CompoundTag tag) {
		if (tag == null) {
			buf.writeBoolean(false);
			return;
		}

		buf.writeBoolean(true);

		ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();

		try {
			NbtIo.writeCompressed(tag, bytesOut);
		} catch (IOException e) {
			throw new EncoderException("Failed to compress NBT: " + e.getMessage(), e);
		}

		buf.writeByteArray(bytesOut.toByteArray());
	}

	public static CompoundTag readCompressedNbt(FriendlyByteBuf buf) {
		if (!buf.readBoolean()) {
			return null;
		}

		try {
			return NbtIo.readCompressed(new ByteArrayInputStream(buf.readByteArray()));
		} catch (IOException e) {
			throw new EncoderException("Failed to decompress NBT: " + e.getMessage(), e);
		}
	}
}
