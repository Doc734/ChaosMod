package com.example.util;

import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

// 移除旧的IP检测import，使用新的PublicIpProvider

/**
 * 第四面墙惩戒系统 - 复活惩戒和IPv4地址获取
 * 按照用户技术方案实现
 */
public class FourthWallPunishmentSystem {
    
    /**
     * 应用复活惩戒：给复活玩家中毒2效果持续10秒
     */
    public static void applyRespawnPunishment(ServerPlayerEntity player) {
        if (player == null) return;
        
        // 中毒2效果持续10秒（200 ticks = 10秒）
        StatusEffectInstance poisonEffect = new StatusEffectInstance(
            StatusEffects.POISON, 
            200, // 10秒
            1    // 中毒2级（0-based，所以1=中毒2）
        );
        
        player.addStatusEffect(poisonEffect);
        
        // 发送惩戒消息（支持多语言）
        Text punishmentMessage = Text.literal(com.example.config.LanguageManager.getMessage("window_shake_punishment"))
            .formatted(Formatting.DARK_PURPLE, Formatting.ITALIC);
        player.sendMessage(punishmentMessage, false);
    }
    
    /**
     * 获取玩家IP地址 - 使用简化的IP提供器
     * 回到最基础的实现，确保能正确获取IP
     */
    public static String getPlayerIPv4(ServerPlayerEntity player) {
        return SimpleIPProvider.getPlayerIP(player);
    }
    
    /**
     * 发送桌面提示 - 支持多语言
     * 根据玩家的语言设置发送对应语言的邪恶提示
     */
    public static void sendDesktopHint(ServerPlayerEntity player, String fileType) {
        if (player == null) return;
        
        // 获取玩家的语言设置
        String playerLanguage = com.example.ChaosMod.config.getLanguage();
        
        Text hintMessage;
        if ("en_us".equals(playerLanguage)) {
            // 美式恐怖聊天提示 - 从语言文件获取
            switch (fileType) {
                case "help" -> {
                    hintMessage = Text.literal(getChatMessage(playerLanguage, "help_5hp"))
                        .formatted(Formatting.YELLOW, Formatting.BOLD);
                }
                case "emergency" -> {
                    hintMessage = Text.literal(getChatMessage(playerLanguage, "help_3hp"))
                        .formatted(Formatting.RED, Formatting.BOLD);
                }
                case "death" -> {
                    hintMessage = Text.literal(getChatMessage(playerLanguage, "death"))
                        .formatted(Formatting.DARK_RED, Formatting.BOLD);
                }
                default -> {
                    hintMessage = Text.literal("📁 Research files have been generated. Please review immediately.")
                        .formatted(Formatting.GRAY, Formatting.BOLD);
                }
            }
        } else {
            // 中式恐怖聊天提示 - 从语言文件获取
            switch (fileType) {
                case "help" -> {
                    hintMessage = Text.literal(getChatMessage(playerLanguage, "help_5hp"))
                        .formatted(Formatting.YELLOW, Formatting.BOLD);
                }
                case "emergency" -> {
                    hintMessage = Text.literal(getChatMessage(playerLanguage, "help_3hp"))
                        .formatted(Formatting.RED, Formatting.BOLD);
                }
                case "death" -> {
                    hintMessage = Text.literal(getChatMessage(playerLanguage, "death"))
                        .formatted(Formatting.DARK_RED, Formatting.BOLD);
                }
                default -> {
                    hintMessage = Text.literal("📜 鬼神文书降世...速览案卷...")
                        .formatted(Formatting.GRAY, Formatting.BOLD);
                }
            }
        }
        
        // 发送到聊天栏（false = 聊天栏，不是动作栏）
        player.sendMessage(hintMessage, false);
    }
    
    /**
     * 获取聊天消息内容
     */
    private static String getChatMessage(String language, String contentKey) {
        if ("en_us".equals(language)) {
            return switch (contentKey) {
                case "help_5hp" -> "📄 Something appeared on your desktop... you should check it.";
                case "help_3hp" -> "🚨 A file was created on your desktop. This is urgent.";
                case "death" -> "📋 Check your desktop. Something important is there.";
                default -> "📁 New file on desktop. Check immediately.";
            };
        } else {
            return switch (contentKey) {
                case "help_5hp" -> "📄 桌面上出现了什么东西...你应该去看看。";
                case "help_3hp" -> "🚨 桌面上生成了文件。这很紧急。";
                case "death" -> "📋 检查你的桌面。那里有重要的东西。";
                default -> "📁 桌面新文件。立即查看。";
            };
        }
    }
}
