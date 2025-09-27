package com.example.util;

import net.minecraft.server.network.ServerPlayerEntity;

import java.net.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 简化IP提供器 - 统一使用HTTP探测方案
 * 按照用户要求：去掉方案一，只用方案二HTTP探测（最可靠）
 */
public class SimpleIPProvider {
    
    // === IP缓存系统 ===
    private static final ConcurrentHashMap<String, String> ipCache = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, Long> cacheTime = new ConcurrentHashMap<>();
    private static final long CACHE_TTL = 5 * 60 * 1000; // 5分钟
    
    // HTTP回显端点
    private static final String[] IP_ENDPOINTS = {
        "https://api.ipify.org",
        "https://ifconfig.me/ip", 
        "https://icanhazip.com"
    };
    
    // 去掉调试信息变量
    
    /**
     * 获取玩家的真实公网出口IP地址
     * 简化版：统一使用HTTP探测方案，对每个玩家独立缓存
     */
    public static String getPlayerIP(ServerPlayerEntity player) {
        if (player == null) {
            return getPublicIPFromHTTPSync();
        }
        
        String playerId = player.getUuid().toString();
        
        // 检查每个玩家的独立缓存
        String cachedIP = ipCache.get(playerId);
        Long cacheTimestamp = cacheTime.get(playerId);
        if (cachedIP != null && cacheTimestamp != null) {
            if (System.currentTimeMillis() - cacheTimestamp < CACHE_TTL) {
                return cachedIP; // 返回该玩家的缓存IP
            }
        }
        
        // 为每个玩家独立进行HTTP探测
        String httpIP = getPublicIPFromHTTPSync();
        if (httpIP != null && !httpIP.startsWith("ERROR") && isValidIPFormat(httpIP)) {
            // 成功获取到公网IP，为该玩家缓存
            ipCache.put(playerId, httpIP);
            cacheTime.put(playerId, System.currentTimeMillis());
            return httpIP;
        }
        
        // HTTP探测失败，返回默认值
        return "127.0.0.1";
    }
    
    // 删除服务端连接获取方法，统一使用HTTP探测
    // 删除本地IP检测方法，不再需要
    
    /**
     * 同步HTTP探测公网IP - 简洁版
     * 直接获取真实公网出口地址
     */
    private static String getPublicIPFromHTTPSync() {
        for (String endpoint : IP_ENDPOINTS) {
            try {
                URL url = new URL(endpoint);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(3000);
                connection.setReadTimeout(3000);
                connection.setRequestProperty("User-Agent", "ChaosMod/1.6.0");
                
                if (connection.getResponseCode() == 200) {
                    try (BufferedReader reader = new BufferedReader(
                            new InputStreamReader(connection.getInputStream()))) {
                        String ip = reader.readLine();
                        if (ip != null) {
                            ip = ip.trim();
                            // 验证IP格式
                            if (isValidIPFormat(ip)) {
                                return ip; // 返回真实的公网IP
                            }
                        }
                    }
                }
                
            } catch (Exception e) {
                // 静默处理，尝试下一个端点
            }
        }
        return "127.0.0.1"; // 所有端点都失败，返回默认值
    }
    
    /**
     * 验证IP格式
     */
    private static boolean isValidIPFormat(String ip) {
        if (ip == null || ip.isEmpty()) return false;
        
        String[] parts = ip.split("\\.");
        if (parts.length != 4) return false;
        
        try {
            for (String part : parts) {
                int num = Integer.parseInt(part);
                if (num < 0 || num > 255) return false;
            }
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
    
    /**
     * 清理过期缓存（多玩家支持）
     */
    public static void cleanupExpiredCache() {
        long currentTime = System.currentTimeMillis();
        
        // 同时清理两个Map中的过期条目
        cacheTime.entrySet().removeIf(entry -> {
            if (currentTime - entry.getValue() > CACHE_TTL) {
                // 同时从IP缓存中移除
                ipCache.remove(entry.getKey());
                return true;
            }
            return false;
        });
    }
    
    /**
     * 清理指定玩家的缓存
     */
    public static void cleanupPlayerCache(String playerId) {
        ipCache.remove(playerId);
        cacheTime.remove(playerId);
    }
    
    /**
     * 获取缓存状态（调试用）
     */
    public static int getCacheSize() {
        return ipCache.size();
    }
}
