package com.example.network;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

/**
 * S2C packet: 服务器请求客户端探测自己的公网IP
 * Server requests client to probe its public IP
 */
public record RequestIPProbeS2CPacket() implements CustomPayload {
    public static final CustomPayload.Id<RequestIPProbeS2CPacket> ID = 
        new CustomPayload.Id<>(Identifier.of("chaosmod", "request_ip_probe"));
    
    public static final PacketCodec<RegistryByteBuf, RequestIPProbeS2CPacket> CODEC = 
        PacketCodec.of(
            (packet, buf) -> {}, // 无数据
            (buf) -> new RequestIPProbeS2CPacket()
        );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }

    /**
     * 发送IP探测请求到客户端
     */
    public static void send(ServerPlayerEntity player) {
        ServerPlayNetworking.send(player, new RequestIPProbeS2CPacket());
    }
}

