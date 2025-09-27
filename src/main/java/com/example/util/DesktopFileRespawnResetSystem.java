package com.example.util;

import java.util.UUID;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 桌面文件复活重置系统
 * 管理玩家复活后的文件生成状态重置
 */
public class DesktopFileRespawnResetSystem {
    
    // === 全局文件生成状态（跨Mixin实例共享） ===
    private static final Map<UUID, Boolean> help5Generated = new ConcurrentHashMap<>();
    private static final Map<UUID, Boolean> help3Generated = new ConcurrentHashMap<>();
    private static final Map<UUID, Boolean> deathGenerated = new ConcurrentHashMap<>();
    
    /**
     * 检查5♥文件是否已生成
     */
    public static boolean isHelp5Generated(UUID playerId) {
        return help5Generated.getOrDefault(playerId, false);
    }
    
    /**
     * 设置5♥文件已生成
     */
    public static void setHelp5Generated(UUID playerId, boolean generated) {
        help5Generated.put(playerId, generated);
    }
    
    /**
     * 检查3♥文件是否已生成
     */
    public static boolean isHelp3Generated(UUID playerId) {
        return help3Generated.getOrDefault(playerId, false);
    }
    
    /**
     * 设置3♥文件已生成
     */
    public static void setHelp3Generated(UUID playerId, boolean generated) {
        help3Generated.put(playerId, generated);
    }
    
    /**
     * 检查死亡文件是否已生成
     */
    public static boolean isDeathGenerated(UUID playerId) {
        return deathGenerated.getOrDefault(playerId, false);
    }
    
    /**
     * 设置死亡文件已生成
     */
    public static void setDeathGenerated(UUID playerId, boolean generated) {
        deathGenerated.put(playerId, generated);
    }
    
    /**
     * 复活时重置玩家的文件生成状态
     * 允许玩家复活后重新触发文件生成
     */
    public static void resetPlayerGenerationState(UUID playerId) {
        help5Generated.put(playerId, false);
        help3Generated.put(playerId, false);
        deathGenerated.put(playerId, false);
    }
    
    /**
     * 玩家离线时清理状态
     */
    public static void cleanupPlayerData(UUID playerId) {
        help5Generated.remove(playerId);
        help3Generated.remove(playerId);
        deathGenerated.remove(playerId);
    }
    
    /**
     * 清理所有状态（调试用）
     */
    public static void resetAllStates() {
        help5Generated.clear();
        help3Generated.clear();
        deathGenerated.clear();
    }
}


