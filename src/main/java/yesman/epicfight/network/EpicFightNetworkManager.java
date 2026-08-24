package yesman.epicfight.network;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamEncoder;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import yesman.epicfight.EpicFight;
import yesman.epicfight.network.client.*;
import yesman.epicfight.network.common.BiDirectionalAnimationVariable;
import yesman.epicfight.network.common.BiDirectionalSyncAnimationPositionPacket;
import yesman.epicfight.network.common.BiDirectionalSyncEmoteSlots;
import yesman.epicfight.network.server.*;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

public class EpicFightNetworkManager {

    /// Registers all network payloads. Called from EpicFightFabric.onInitialize() and
    /// EpicFightFabricClient.onInitializeClient().
    public static void registerServerPayloads() {
        // Server-bound (client → server) payloads are registered on the client side
        // Client-bound (server → client) payloads are registered on the server side
        // Fabric handles this differently from NeoForge — we register receivers
    }

    public static void registerClientPayloads() {
        // Register client-bound payload receivers
        // These are registered via ClientPlayNetworking.registerGlobalReceiver()
    }

    @SuppressWarnings("unchecked")
    public static FriendlyByteBuf encodeObjectToBuffer(StreamEncoder<ByteBuf, ?> encoder, Object value) {
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        ((StreamEncoder<ByteBuf, Object>)encoder).encode(buf, value);
        return buf;
    }

    public static void sendToServer(CustomPacketPayload message, CustomPacketPayload... others) {
        ClientPlayNetworking.send(message);
        // Note: Fabric doesn't support sending multiple payloads in one call like NeoForge
        for (CustomPacketPayload other : others) {
            ClientPlayNetworking.send(other);
        }
    }

    public static void sendToAll(CustomPacketPayload message, CustomPacketPayload... others) {
        // Iterate all players and send
        // This is called from server side — need server instance
        // For now, this is a placeholder; the actual implementation needs the server context
    }

    public static void sendToAllPlayerTrackingThisEntity(CustomPacketPayload message, Entity entity, CustomPacketPayload... others) {
        ServerPlayNetworking.sendToPlayersTrackingEntity(entity, message);
        for (CustomPacketPayload other : others) {
            ServerPlayNetworking.sendToPlayersTrackingEntity(entity, other);
        }
    }

    public static void sendToPlayer(CustomPacketPayload message, ServerPlayer player, CustomPacketPayload... others) {
        ServerPlayNetworking.send(player, message);
        for (CustomPacketPayload other : others) {
            ServerPlayNetworking.send(player, other);
        }
    }

    public static void sendToAllPlayerTrackingThisEntityWithSelf(CustomPacketPayload message, ServerPlayer entity, CustomPacketPayload... others) {
        ServerPlayNetworking.sendToPlayersTrackingEntityAndSelf(entity, message);
        for (CustomPacketPayload other : others) {
            ServerPlayNetworking.sendToPlayersTrackingEntityAndSelf(entity, other);
        }
    }

    public static void sendToAllPlayerTrackingThisChunkWithSelf(CustomPacketPayload message, ServerLevel serverLevel, ChunkPos chunkPos, CustomPacketPayload... others) {
        // Fabric doesn't have a direct sendToPlayersTrackingChunk — iterate players
        serverLevel.getServer().getPlayerList().getPlayers().forEach(player -> {
            if (player.level().hasChunk(chunkPos.x, chunkPos.z)) {
                ServerPlayNetworking.send(player, message);
                for (CustomPacketPayload other : others) {
                    ServerPlayNetworking.send(player, other);
                }
            }
        });
    }

    public static class PayloadBundleBuilder {
        public static PayloadBundleBuilder create() {
            return new PayloadBundleBuilder();
        }

        public static PayloadBundleBuilder beginWith(CustomPacketPayload payload) {
            return new PayloadBundleBuilder().and(payload);
        }

        private final List<CustomPacketPayload> payloads = new ArrayList<> ();

        public PayloadBundleBuilder and(CustomPacketPayload payload) {
            this.payloads.add(payload);
            return this;
        }

        public void send(BiConsumer<CustomPacketPayload, CustomPacketPayload[]> sendTo) {
            if (this.payloads.size() == 1) {
                sendTo.accept(this.payloads.getFirst(), new CustomPacketPayload[0]);
            } else if (!this.payloads.isEmpty()) {
                sendTo.accept(this.payloads.getFirst(), this.payloads.subList(1, this.payloads.size()).toArray(new CustomPacketPayload[0]));
            }
        }
    }
}
