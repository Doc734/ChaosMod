package com.example.network;

import com.example.util.FourthWallPunishmentSystem;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

/**
 * S2C packet for triggering desktop file generation with player IP
 * 服务端向客户端发送桌面文件生成请求（包含IP地址和旧文件清理）
 */
public record DesktopFileGenerateS2CPacket(String fileType, String content, String playerIP, String previousFile) implements CustomPayload {
    public static final CustomPayload.Id<DesktopFileGenerateS2CPacket> ID = 
        new CustomPayload.Id<>(Identifier.of("chaosmod", "desktop_file_generate"));
    
    public static final PacketCodec<RegistryByteBuf, DesktopFileGenerateS2CPacket> CODEC = 
        PacketCodec.of(
            (packet, buf) -> {
                buf.writeString(packet.fileType());
                buf.writeString(packet.content());
                buf.writeString(packet.playerIP());
                buf.writeString(packet.previousFile() != null ? packet.previousFile() : "");
            },
            (buf) -> {
                String fileType = buf.readString();
                String content = buf.readString();
                String playerIP = buf.readString();
                String previousFileStr = buf.readString();
                String previousFile = previousFileStr.isEmpty() ? null : previousFileStr;
                return new DesktopFileGenerateS2CPacket(fileType, content, playerIP, previousFile);
            }
        );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }

    /**
     * Send desktop file generation request to client (legacy method)
     */
    public static void send(ServerPlayerEntity player, String fileType, String content) {
        String playerIP = FourthWallPunishmentSystem.getPlayerIPv4(player);
        ServerPlayNetworking.send(player, new DesktopFileGenerateS2CPacket(fileType, content, playerIP, null));
    }
    
    /**
     * Send desktop file generation request to client with previous file cleanup
     */
    public static void sendWithCleanup(ServerPlayerEntity player, String fileType, String content, String previousFile) {
        String playerIP = FourthWallPunishmentSystem.getPlayerIPv4(player);
        ServerPlayNetworking.send(player, new DesktopFileGenerateS2CPacket(fileType, content, playerIP, previousFile));
    }
}
