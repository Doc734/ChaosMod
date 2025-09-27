package com.example.network;

import com.example.ChaosMod;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

/**
 * C2S packet for applying fourth wall damage from client effects
 * 客户端向服务端发送第四面墙效果造成的额外伤害
 */
public record FourthWallDamageC2SPacket(float damageAmount, String damageType) implements CustomPayload {
    public static final CustomPayload.Id<FourthWallDamageC2SPacket> ID = 
        new CustomPayload.Id<>(Identifier.of("chaosmod", "fourth_wall_damage"));
    
    public static final PacketCodec<RegistryByteBuf, FourthWallDamageC2SPacket> CODEC = 
        PacketCodec.tuple(
            PacketCodec.of(
                (value, buf) -> buf.writeFloat(value),
                (buf) -> buf.readFloat()
            ), FourthWallDamageC2SPacket::damageAmount,
            PacketCodec.of(
                (value, buf) -> buf.writeString(value),
                (buf) -> buf.readString()
            ), FourthWallDamageC2SPacket::damageType,
            FourthWallDamageC2SPacket::new
        );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }

    /**
     * Register packet handler on server side
     */
    public static void registerServerReceiver() {
        ServerPlayNetworking.registerGlobalReceiver(ID, (packet, context) -> {
            ServerPlayerEntity player = context.player();
            
            // 在服务端主线程中处理
            context.server().execute(() -> {
                // 验证伤害类型和数量的合理性
                if (packet.damageAmount() <= 0 || packet.damageAmount() > 10.0f) {
                    return; // 防止恶意数据
                }
                
                // 验证效果是否启用
                boolean validDamage = false;
                switch (packet.damageType()) {
                    case "window_shake":
                        validDamage = ChaosMod.config.windowViolentShakeEnabled;
                        break;
                    case "desktop_file":
                        validDamage = ChaosMod.config.desktopPrankInvasionEnabled;
                        break;
                    default:
                        return; // 无效类型
                }
                
                if (!validDamage) return;
                
                // 应用第四面墙伤害
                player.damage(player.getServerWorld().getDamageSources().magic(), packet.damageAmount());
            });
        });
    }
}
