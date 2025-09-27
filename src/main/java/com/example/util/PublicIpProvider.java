package com.example.util;

import net.minecraft.server.network.ServerPlayerEntity;

import java.net.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * 公网IP提供器 - 按照用户完整技术方案实现
 * 方案一（推荐）：服务端获取连接来源
 * 方案二（备用）：客户端主动探测公网出口
 */
public class PublicIpProvider {
    
    // === IP缓存系统（TTL 5-10分钟） ===
    private static final ConcurrentHashMap<String, CachedIP> ipCache = new ConcurrentHashMap<>();
    private static final long CACHE_TTL = 5 * 60 * 1000; // 5分钟TTL
    
    // HTTP回显服务端点（备用方案）
    private static final String[] IP_ECHO_ENDPOINTS = {
        "https://api.ipify.org",
        "https://ifconfig.me/ip",
        "https://icanhazip.com",
        "https://checkip.amazonaws.com"
    };
    
    /**
     * 缓存的IP信息
     */
    private static class CachedIP {
        final String ip;
        final long timestamp;
        
        CachedIP(String ip) {
            this.ip = ip;
            this.timestamp = System.currentTimeMillis();
        }
        
        boolean isExpired() {
            return System.currentTimeMillis() - timestamp > CACHE_TTL;
        }
    }
    
    /**
     * 统一入口：获取玩家的准确公网IP
     * 按照用户方案：优先服务端连接来源，备用客户端探测
     */
    public static String getPublicIP(ServerPlayerEntity player) {
        if (player == null) {
            return "127.0.0.1";
        }
        
        String playerId = player.getUuid().toString();
        
        // 检查缓存
        CachedIP cached = ipCache.get(playerId);
        if (cached != null && !cached.isExpired()) {
            return cached.ip;
        }
        
        // 方案一（推荐）：服务端获取连接来源
        String serverSourceIP = getSimpleServerIP(player);
        
        // 如果是本地地址，尝试探测公网IP
        if (isLocalAddress(serverSourceIP)) {
            // 异步探测公网IP，立即返回本地地址
            probePublicIPAsync(playerId);
            ipCache.put(playerId, new CachedIP("127.0.0.1"));
            return "127.0.0.1";
        }
        
        // 缓存并返回
        ipCache.put(playerId, new CachedIP(serverSourceIP));
        return serverSourceIP;
    }
    
    /**
     * 简化版：直接获取服务端看到的IP地址
     * 按照用户技术方案最基础的实现
     */
    private static String getSimpleServerIP(ServerPlayerEntity player) {
        if (player == null || player.networkHandler == null) {
            return "127.0.0.1";
        }
        
        try {
            // 从会话取地址：ServerPlayNetworkHandler#getConnectionAddress()
            SocketAddress socketAddress = player.networkHandler.getConnectionAddress();
            
            // 检查是否为网络套接字地址
            if (!(socketAddress instanceof InetSocketAddress inetSocketAddress)) {
                return "127.0.0.1"; // 本地集成服务器
            }
            
            // 获取IP地址
            InetAddress inetAddress = inetSocketAddress.getAddress();
            
            // 直接返回IP地址字符串，不做复杂分类
            return inetAddress.getHostAddress();
            
        } catch (Exception e) {
            return "127.0.0.1";
        }
    }
    
    /**
     * 从分类结果中提取纯净的IP地址
     */
    private static String extractCleanIP(String classifiedIP) {
        if (classifiedIP == null) return "127.0.0.1";
        
        // 检查是否为本地连接描述
        if (classifiedIP.contains("本地通道连接") || 
            classifiedIP.contains("本地集成服务器") ||
            classifiedIP.contains("LOCAL-PIPE") ||
            classifiedIP.contains("INTEGRATED")) {
            return "127.0.0.1"; // 本地连接统一返回127.0.0.1
        }
        
        // 提取冒号后的IP地址部分
        if (classifiedIP.contains(": ")) {
            String[] parts = classifiedIP.split(": ");
            if (parts.length >= 2) {
                String ipPart = parts[1];
                // 去掉括号内的说明文字
                if (ipPart.contains(" (")) {
                    ipPart = ipPart.substring(0, ipPart.indexOf(" ("));
                }
                // 验证是否为有效IP格式
                if (isValidIPAddress(ipPart.trim())) {
                    return ipPart.trim();
                }
            }
        }
        
        // 如果没有分类前缀，检查是否为有效IP
        String candidate = classifiedIP.trim();
        if (isValidIPAddress(candidate)) {
            return candidate;
        }
        
        // 无效格式，返回默认值
        return "127.0.0.1";
    }
    
    /**
     * 检查是否为有效的IP地址格式
     */
    private static boolean isValidIPAddress(String ip) {
        if (ip == null || ip.isEmpty()) return false;
        
        // 简单的IP格式检查（IPv4: x.x.x.x）
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
     * 检查是否为本地地址
     */
    private static boolean isLocalAddress(String ip) {
        if (ip == null) return true;
        
        return ip.startsWith("127.") || 
               ip.startsWith("192.168.") || 
               ip.startsWith("10.") ||
               (ip.startsWith("172.") && isPrivate172(ip)) ||
               ip.equals("127.0.0.1") ||
               ip.equals("0.0.0.0") ||
               ip.startsWith("169.254."); // 链路本地地址
    }
    
    /**
     * 检查172段是否为私网 (172.16.0.0 到 172.31.255.255)
     */
    private static boolean isPrivate172(String ip) {
        try {
            String[] parts = ip.split("\\.");
            if (parts.length >= 2) {
                int second = Integer.parseInt(parts[1]);
                return second >= 16 && second <= 31;
            }
        } catch (Exception e) {
            // 解析失败，当作非私网处理
        }
        return false;
    }
    
    // 删除复杂的旧方法，使用简化版本
    
    /**
     * IPv4地址分类矩阵（按照用户技术方案）
     */
    private static String classifyIPv4Address(Inet4Address inet4Address) {
        String ip = inet4Address.getHostAddress();
        
        // 1. 回环地址检测 (127.0.0.0/8)
        if (inet4Address.isLoopbackAddress()) {
            return "LOOPBACK: " + ip + " (本机自连)";
        }
        
        // 2. 未指定地址检测 (0.0.0.0)
        if (inet4Address.isAnyLocalAddress()) {
            return "UNSPECIFIED: " + ip + " (无意义占位地址)";
        }
        
        // 3. 链路本地地址检测 (169.254.0.0/16)
        if (inet4Address.isLinkLocalAddress()) {
            return "LINK-LOCAL: " + ip + " (同链路可达，非公网)";
        }
        
        // 4. 站点本地/私网地址检测 (RFC1918: 10/8, 172.16/12, 192.168/16)
        if (inet4Address.isSiteLocalAddress()) {
            return "PRIVATE: " + ip + " (内网地址，可能在NAT后)";
        }
        
        // 5. 多播地址检测
        if (inet4Address.isMulticastAddress()) {
            return "MULTICAST: " + ip + " (组播地址)";
        }
        
        // 6. 公网地址
        return "PUBLIC: " + ip + " (真正的公网IPv4地址)";
    }
    
    /**
     * IPv6地址分类矩阵
     */
    private static String classifyIPv6Address(Inet6Address inet6Address) {
        String ip = inet6Address.getHostAddress();
        
        // 1. 回环地址检测 (::1)
        if (inet6Address.isLoopbackAddress()) {
            return "LOOPBACK: " + ip + " (IPv6本机自连)";
        }
        
        // 2. 未指定地址检测 (::)
        if (inet6Address.isAnyLocalAddress()) {
            return "UNSPECIFIED: " + ip + " (IPv6未指定地址)";
        }
        
        // 3. 链路本地地址检测 (fe80::/10)
        if (inet6Address.isLinkLocalAddress()) {
            return "LINK-LOCAL: " + ip + " (IPv6链路本地)";
        }
        
        // 4. 站点本地地址检测 (已废弃的fec0::/10)
        if (inet6Address.isSiteLocalAddress()) {
            return "SITE-LOCAL: " + ip + " (已废弃的IPv6站点本地)";
        }
        
        // 5. 多播地址检测
        if (inet6Address.isMulticastAddress()) {
            return "MULTICAST: " + ip + " (IPv6组播地址)";
        }
        
        // 6. ULA检测 (fc00::/7) - 手动检查前缀
        byte[] addr = inet6Address.getAddress();
        if (addr.length == 16 && (addr[0] & 0xfe) == 0xfc) {
            return "ULA: " + ip + " (IPv6唯一本地地址，内网)";
        }
        
        // 7. 公网IPv6
        return "PUBLIC-IPv6: " + ip + " (公网IPv6地址)";
    }
    
    /**
     * 检查是否为本地或内部IP
     */
    private static boolean isLocalOrInternalIP(String ipResult) {
        return ipResult.startsWith("LOOPBACK") || 
               ipResult.startsWith("PRIVATE") || 
               ipResult.startsWith("LINK-LOCAL") || 
               ipResult.startsWith("INTEGRATED") ||
               ipResult.startsWith("ULA");
    }
    
    /**
     * 检查是否应该允许客户端探测
     */
    private static boolean shouldAllowClientProbe() {
        // 只在单机世界或明确配置时允许
        return true; // 简化版本，可以根据需要添加更多限制
    }
    
    /**
     * 方案二：异步探测公网IP（不阻塞游戏）
     */
    private static void probePublicIPAsync(String playerId) {
        CompletableFuture.supplyAsync(() -> {
            return probePublicIPSync();
        }).orTimeout(5, TimeUnit.SECONDS)
        .whenComplete((result, throwable) -> {
            if (result != null && !result.startsWith("ERROR")) {
                // 探测成功，更新缓存（直接使用纯净IP）
                ipCache.put(playerId, new CachedIP(result));
            }
        });
    }
    
    /**
     * 方案二：同步探测公网IP（HTTP回显）
     */
    private static String probePublicIPSync() {
        for (String endpoint : IP_ECHO_ENDPOINTS) {
            try {
                URL url = new URL(endpoint);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(3000); // 3秒超时
                connection.setReadTimeout(3000);
                connection.setRequestProperty("User-Agent", "ChaosMod/1.6.0");
                
                int responseCode = connection.getResponseCode();
                if (responseCode == 200) {
                    try (BufferedReader reader = new BufferedReader(
                            new InputStreamReader(connection.getInputStream()))) {
                        String ip = reader.readLine();
                        if (ip != null) {
                            ip = ip.trim();
                            // 验证是否为有效IPv4
                            InetAddress addr = InetAddress.getByName(ip);
                            if (addr instanceof Inet4Address) {
                                return ip; // 直接返回纯净IP
                            }
                        }
                    }
                }
                
            } catch (Exception e) {
                // 尝试下一个端点
                continue;
            }
        }
        
        return "ERROR: 无法探测公网IP";
    }
    
    /**
     * 清理过期缓存
     */
    public static void cleanupExpiredCache() {
        ipCache.entrySet().removeIf(entry -> entry.getValue().isExpired());
    }
    
    /**
     * 重置IP缓存（调试用）
     */
    public static void resetCache() {
        ipCache.clear();
    }
}
