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
            player.sendMessage(Text.literal(com.example.config.LanguageManager.getMessage("config_permission_denied"))
                .formatted(Formatting.RED, Formatting.BOLD), false);
            return;
        }
        
        // 验证配置键的有效性
        if (!isValidConfigKey(key)) {
            player.sendMessage(Text.literal(com.example.config.LanguageManager.getMessage("config_invalid_key") + ": " + key)
                .formatted(Formatting.RED), false);
            return;
        }
        
        // 更新配置
        ChaosMod.config.set(key, value);
        
        // 发送确认消息（支持多语言）
        String state = value ? "✓ 启用" : "✗ 禁用";
        String stateEn = value ? "✓ Enabled" : "✗ Disabled";
        String currentState = "zh_cn".equals(com.example.ChaosMod.config.getLanguage()) ? state : stateEn;
        
        player.sendMessage(Text.literal("[" + com.example.config.LanguageManager.getMessage("config_updated") + "] " + key + " -> " + currentState)
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
                 "craftingTrapEnabled", "playerHealOnAttackEnabled", "positionSwapEnabled",
                 "craftingBombEnabled", "waterDamageEnabled", "randomDamageAmountEnabled",
                 "delayedDamageEnabled", "keyDisableEnabled", "randomEffectsEnabled",
                 "damageScapegoatEnabled", "painSpreadEnabled", "panicMagnetEnabled",
                 "pickupDrainEnabled", "vertigoScapegoatEnabled", "windowViolentShakeEnabled",
                 "desktopPrankInvasionEnabled" -> true;
            default -> false;
        };
    }
    
    private static void broadcastConfigChange(net.minecraft.server.MinecraftServer server,
                                            ServerPlayerEntity sender, String key, boolean value) {
        if (server == null) return;
        
        // 多语言状态显示
        String language = com.example.ChaosMod.config.getLanguage();
        String state, changedText;
        if ("en_us".equals(language)) {
            state = value ? "✓ Enabled" : "✗ Disabled";
            changedText = com.example.config.LanguageManager.getMessage("config_changed");
        } else {
            state = value ? "✓ 启用" : "✗ 禁用";
            changedText = com.example.config.LanguageManager.getMessage("config_changed");
        }
        
        // 完全多语言的管理员广播消息
        String broadcastTemplate = "en_us".equals(language) ? 
            "[ChaosMod] %s %s %s to %s" : 
            "[ChaosMod] %s %s %s 设置为 %s";
        
        Text message = Text.literal(String.format(broadcastTemplate, 
            sender.getName().getString(), changedText, key, state))
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