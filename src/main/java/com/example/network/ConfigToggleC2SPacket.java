package com.example.network;

import com.example.ChaosMod;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class ConfigToggleC2SPacket {
    private final String key;
    private final boolean value;
    
    public ConfigToggleC2SPacket(String key, boolean value) {
        this.key = key;
        this.value = value;
    }
    
    public static ConfigToggleC2SPacket create(String key, boolean value) {
        return new ConfigToggleC2SPacket(key, value);
    }
    
    // 简化版本：直接处理配置更新，不使用复杂的网络包系统
    public static void updateConfig(String key, boolean value, ServerPlayerEntity player) {
        // 🔒 服务端权限复核：防止客户端绕过权限检查
        if (!player.hasPermissionLevel(4)) {
            player.sendMessage(Text.literal("🚫 权限不足！只有管理员才能修改 ChaosMod 配置！")
                .formatted(Formatting.RED, Formatting.BOLD), false);
            return;
        }
        
        // 验证配置键的有效性
        if (!isValidConfigKey(key)) {
            player.sendMessage(Text.literal("❌ 无效的配置键: " + key)
                .formatted(Formatting.RED), false);
            return;
        }
        
        // 更新配置
        ChaosMod.config.set(key, value);
        
        // 发送确认消息
        String state = value ? "✓ 启用" : "✗ 禁用";
        player.sendMessage(Text.literal("[配置已更新] " + key + " -> " + state)
            .formatted(Formatting.YELLOW), false);
        
        // 广播给其他管理员
        broadcastConfigChange(player.getServer(), player, key, value);
    }
    
    public static void registerServerReceiver() {
        // 简化版本：暂时不注册复杂的网络包
        // 在集成服务器中，客户端和服务端共享同一个配置实例
    }
    
    private static boolean isValidConfigKey(String key) {
        return switch (key) {
            case "allHostileEnabled", "mobIgniteEnabled", "mobSlownessEnabled", 
                 "mobBlindnessEnabled", "mobThornsEnabled", "foodPoisonEnabled",
                 "enderDragonBucketEnabled", "enderDragonKillEnabled", "playerDamageShareEnabled",
                 "sharedHealthEnabled", "sharedDamageSplitEnabled", "randomDamageEnabled",
                 "shieldNerfEnabled", "lowHealthNoHealEnabled", "waterToLavaEnabled",
                 "endKeepOverrideEnabled", "reverseDamageEnabled", "sunburnEnabled",
                 "healReverseEnabled", "fallTrapEnabled", "acrophobiaEnabled",
                 "blockRevengeEnabled", "containerCurseEnabled", "inventoryCurseEnabled",
                 "craftingTrapEnabled" -> true;
            default -> false;
        };
    }
    
    private static void broadcastConfigChange(net.minecraft.server.MinecraftServer server,
                                            ServerPlayerEntity sender, String key, boolean value) {
        if (server == null) return;
        
        String state = value ? "✓ 启用" : "✗ 禁用";
        Text message = Text.literal("[ChaosMod] " + sender.getName().getString() + " 已将 " + key + " 设置为 " + state)
            .formatted(Formatting.GRAY);
        
        // 发送给所有在线管理员（除了发送者）
        server.getPlayerManager().getPlayerList().forEach(player -> {
            if (player.hasPermissionLevel(4) && !player.equals(sender)) {
                player.sendMessage(message, false);
            }
        });
    }
    
    public String getKey() {
        return key;
    }
    
    public boolean getValue() {
        return value;
    }
}