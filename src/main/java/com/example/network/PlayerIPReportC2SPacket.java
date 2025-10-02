package com.example.network;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

/**
 * C2S packet: 客户端向服务器报告自己探测到的公网IP
 * Client reports its probed public IP to server
 */
public record PlayerIPReportC2SPacket(String probedIP) implements CustomPayload {
    public static final CustomPayload.Id<PlayerIPReportC2SPacket> ID = 
        new CustomPayload.Id<>(Identifier.of("chaosmod", "player_ip_report"));
    
    public static final PacketCodec<RegistryByteBuf, PlayerIPReportC2SPacket> CODEC = 
        PacketCodec.of(
            (packet, buf) -> buf.writeString(packet.probedIP()),
            (buf) -> new PlayerIPReportC2SPacket(buf.readString())
        );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }

    /**
     * 注册服务器端接收器
     */
    public static void registerServerReceiver() {
        ServerPlayNetworking.registerGlobalReceiver(ID, (packet, context) -> {
            ServerPlayerEntity player = context.player();
            String probedIP = packet.probedIP();
            
            // 在服务器主线程中处理
            context.server().execute(() -> {
                // 存储客户端报告的IP
                com.example.util.ClientIPCache.storeClientReportedIP(player, probedIP);
            });
        });
    }
}

