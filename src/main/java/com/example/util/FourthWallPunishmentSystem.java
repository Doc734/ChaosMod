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
            // 英文邪恶提示
            switch (fileType) {
                case "help" -> {
                    hintMessage = Text.literal("😈 ChaosMod has invaded your desktop! Go check the evil file!")
                        .formatted(Formatting.YELLOW, Formatting.BOLD);
                }
                case "emergency" -> {
                    hintMessage = Text.literal("💀 Death's message has descended to desktop! Check your doomsday file immediately!")
                        .formatted(Formatting.RED, Formatting.BOLD);
                }
                case "death" -> {
                    hintMessage = Text.literal("😈 Your death has been recorded! Desktop has proof of your defeat!")
                        .formatted(Formatting.DARK_RED, Formatting.BOLD);
                }
                default -> {
                    hintMessage = Text.literal("😈 Evil files have appeared on desktop! Go feel the fear!")
                        .formatted(Formatting.GRAY, Formatting.BOLD);
                }
            }
        } else {
            // 中文邪恶提示
            switch (fileType) {
                case "help" -> {
                    hintMessage = Text.literal("😈 ChaosMod已入侵你的桌面！快去查看邪恶文件！")
                        .formatted(Formatting.YELLOW, Formatting.BOLD);
                }
                case "emergency" -> {
                    hintMessage = Text.literal("💀 死神的信息已降临桌面！立即查看你的末日文件！")
                        .formatted(Formatting.RED, Formatting.BOLD);
                }
                case "death" -> {
                    hintMessage = Text.literal("😈 你的死亡已被记录！桌面上有你的败北证明！")
                        .formatted(Formatting.DARK_RED, Formatting.BOLD);
                }
                default -> {
                    hintMessage = Text.literal("😈 邪恶的文件已出现在桌面！去感受恐惧吧！")
                        .formatted(Formatting.GRAY, Formatting.BOLD);
                }
            }
        }
        
        // 发送到聊天栏（false = 聊天栏，不是动作栏）
        player.sendMessage(hintMessage, false);
    }
}
