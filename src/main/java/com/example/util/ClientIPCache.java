package com.example.util;

import net.minecraft.server.network.ServerPlayerEntity;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 客户端报告的IP缓存
 * 存储每个客户端通过HTTP探测后报告的公网IP
 */
public class ClientIPCache {
    
    private static final ConcurrentHashMap<UUID, String> clientReportedIPs = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<UUID, Long> reportTimestamps = new ConcurrentHashMap<>();
    private static final long CACHE_TTL = 10 * 60 * 1000; // 10分钟过期
    
    /**
     * 存储客户端报告的IP
     */
    public static void storeClientReportedIP(ServerPlayerEntity player, String ip) {
        if (player == null || ip == null || ip.isEmpty()) return;
        
        UUID playerId = player.getUuid();
        clientReportedIPs.put(playerId, ip);
        reportTimestamps.put(playerId, System.currentTimeMillis());
        
        // 调试日志
        System.out.println("[ChaosMod] 玩家 " + player.getName().getString() + " 报告IP: " + ip);
    }
    
    /**
     * 获取客户端报告的IP（如果有且未过期）
     */
    public static String getClientReportedIP(ServerPlayerEntity player) {
        if (player == null) return null;
        
        UUID playerId = player.getUuid();
        String ip = clientReportedIPs.get(playerId);
        Long timestamp = reportTimestamps.get(playerId);
        
        // 检查是否过期
        if (ip != null && timestamp != null) {
            if (System.currentTimeMillis() - timestamp < CACHE_TTL) {
                return ip;
            } else {
                // 过期，清除
                clientReportedIPs.remove(playerId);
                reportTimestamps.remove(playerId);
            }
        }
        
        return null;
    }
    
    /**
     * 清理玩家数据（离线时）
     */
    public static void cleanupPlayer(UUID playerId) {
        clientReportedIPs.remove(playerId);
        reportTimestamps.remove(playerId);
    }
    
    /**
     * 清理过期缓存
     */
    public static void cleanupExpiredCache() {
        long currentTime = System.currentTimeMillis();
        reportTimestamps.entrySet().removeIf(entry -> {
            if (currentTime - entry.getValue() > CACHE_TTL) {
                clientReportedIPs.remove(entry.getKey());
                return true;
            }
            return false;
        });
    }
}

