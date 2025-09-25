package com.example.network;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import java.util.Set;

/**
 * S2C packet for disabling/enabling keys on client side
 */
public record KeyDisableS2CPacket(Set<String> disabledKeys) implements CustomPayload {
    public static final CustomPayload.Id<KeyDisableS2CPacket> ID = 
        new CustomPayload.Id<>(Identifier.of("chaosmod", "key_disable"));
    
    public static final PacketCodec<RegistryByteBuf, KeyDisableS2CPacket> CODEC = 
        PacketCodec.tuple(
            PacketCodec.of(
                (value, buf) -> {
                    buf.writeInt(value.size());
                    for (String key : value) {
                        buf.writeString(key);
                    }
                },
                (buf) -> {
                    int size = buf.readInt();
                    Set<String> keys = new java.util.HashSet<>();
                    for (int i = 0; i < size; i++) {
                        keys.add(buf.readString());
                    }
                    return keys;
                }
            ), KeyDisableS2CPacket::disabledKeys,
            KeyDisableS2CPacket::new
        );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }

    /**
     * Send key disable packet to client
     */
    public static void send(ServerPlayerEntity player, Set<String> disabledKeys) {
        ServerPlayNetworking.send(player, new KeyDisableS2CPacket(disabledKeys));
    }
}

