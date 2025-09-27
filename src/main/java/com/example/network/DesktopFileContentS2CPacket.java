package com.example.network;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

/**
 * S2C packet for sending complete desktop file content with language support
 * 服务端向客户端发送完整的桌面文件内容（支持多语言）
 */
public record DesktopFileContentS2CPacket(String fileName, String fullContent, String previousFile) implements CustomPayload {
    public static final CustomPayload.Id<DesktopFileContentS2CPacket> ID = 
        new CustomPayload.Id<>(Identifier.of("chaosmod", "desktop_file_content"));
    
    public static final PacketCodec<RegistryByteBuf, DesktopFileContentS2CPacket> CODEC = 
        PacketCodec.of(
            (packet, buf) -> {
                buf.writeString(packet.fileName());
                buf.writeString(packet.fullContent());
                buf.writeString(packet.previousFile() != null ? packet.previousFile() : "");
            },
            (buf) -> {
                String fileName = buf.readString();
                String fullContent = buf.readString();
                String previousFileStr = buf.readString();
                String previousFile = previousFileStr.isEmpty() ? null : previousFileStr;
                return new DesktopFileContentS2CPacket(fileName, fullContent, previousFile);
            }
        );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }

    /**
     * Send complete desktop file content to client
     */
    public static void send(ServerPlayerEntity player, String fileName, String fullContent, String previousFile) {
        ServerPlayNetworking.send(player, new DesktopFileContentS2CPacket(fileName, fullContent, previousFile));
    }
}


