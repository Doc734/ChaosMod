package com.example.util;

import com.example.ChaosMod;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 桌面恶作剧入侵系统 - 第40种终极第四面墙效果
 * 技术实现：跨平台桌面路径 + UTF-8文件写入 + 冷却机制
 */
public class DesktopPrankSystem {
    
    // === 冷却管理 ===
    private static final Map<String, Long> fileCooldowns = new HashMap<>();
    private static final long FILE_COOLDOWN = 5000; // 5秒冷却防刷屏
    
    // 移除客户端死亡检测，现在由服务端控制
    
    // === 文件内容模板（多语言支持） ===
    private static String getLocalizedText(String key) {
        return net.minecraft.client.resource.language.I18n.translate("chaosmod.desktop." + key);
    }
    
    /**
     * 客户端Tick - 简化版：只处理客户端相关逻辑
     * 文件生成已移至服务端控制，这里只保留必要的客户端逻辑
     */
    public static void clientTick() {
        // 文件生成现在完全由服务端控制，客户端只负责接收和处理
        // 清理过期的冷却记录
        cleanupExpiredCooldowns();
    }
    
    /**
     * 清理过期的冷却记录
     */
    private static void cleanupExpiredCooldowns() {
        long currentTime = System.currentTimeMillis();
        fileCooldowns.entrySet().removeIf(entry -> 
            currentTime - entry.getValue() > FILE_COOLDOWN * 2 // 清理超过2倍冷却时间的记录
        );
    }
    
    // 移除旧的文件生成方法，现在由服务端控制
    
    /**
     * 获取桌面目录 - 跨平台实现
     */
    private static File getDesktopDirectory() {
        String os = System.getProperty("os.name").toLowerCase();
        String home = System.getProperty("user.home");
        
        if (os.contains("win")) {
            // Windows: %USERPROFILE%/Desktop
            return new File(home, "Desktop");
        } else if (os.contains("mac")) {
            // macOS: $HOME/Desktop
            return new File(home, "Desktop");
        } else {
            // Linux: 先尝试 $HOME/Desktop，再尝试 XDG
            File desktop = new File(home, "Desktop");
            if (desktop.exists()) {
                return desktop;
            }
            // 可以扩展XDG user-dirs.dirs解析
            return new File(home, "桌面"); // 中文系统fallback
        }
    }
    
    /**
     * 获取当前生命值
     */
    private static float getCurrentHealth() {
        PlayerEntity player = MinecraftClient.getInstance().player;
        return player != null ? player.getHealth() : 0.0f;
    }
    
    /**
     * 应用文件生成伤害
     */
    private static void applyFileDamage(PlayerEntity player, float amount) {
        if (player == null) return;
        
        if (player.getWorld().isClient()) {
            // 客户端：通过网络包发送到服务端
            net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.send(
                new com.example.network.FourthWallDamageC2SPacket(amount, "desktop_file")
            );
        } else {
            // 服务端：直接应用伤害
            player.damage(player.getDamageSources().magic(), amount);
        }
    }
    
    /**
     * 清理所有生成的文件（管理功能）
     */
    public static void cleanupGeneratedFiles() {
        try {
            File desktopDir = getDesktopDirectory();
            if (desktopDir == null || !desktopDir.exists()) return;
            
            File[] files = desktopDir.listFiles();
            if (files == null) return;
            
            int cleaned = 0;
            for (File file : files) {
                String name = file.getName();
                // 检查多语言文件名
                if (name.contains(getLocalizedText("help_5hp")) || 
                    name.contains(getLocalizedText("help_3hp")) || 
                    name.contains(getLocalizedText("death")) ||
                    name.contains("救命啊！") || name.contains("啊啊啊！") || name.contains("游戏结束！") ||
                    name.contains("Help me!") || name.contains("AAAHHH") || name.contains("Game Over")) {
                    if (file.delete()) {
                        cleaned++;
                    }
                }
            }
            
            if (cleaned > 0) {
                // 可以添加清理成功的反馈
            }
            
        } catch (Exception e) {
            // 静默处理
        }
    }
    
    // 移除客户端版本的IP获取和提示发送，现在由服务端控制
    
    /**
     * 处理服务端请求的桌面文件生成
     * 按照用户方案：服务端监听生命值变化，发送包含IP地址的文件生成请求
     * 新增：支持旧文件删除，防止桌面文件堆积
     */
    public static void handleServerRequest(String fileType, String content, String playerIP, String previousFile) {
        try {
            File desktopDir = getDesktopDirectory();
            if (desktopDir == null || !desktopDir.exists()) {
                return;
            }
            
            // 先删除旧文件（防止桌面堆积）
            if (previousFile != null && !previousFile.isEmpty()) {
                File oldFile = new File(desktopDir, previousFile);
                if (oldFile.exists()) {
                    oldFile.delete();
                }
            }
            
            // 清理所有ChaosMod相关的旧文件
            cleanupOldChaoModFiles(desktopDir);
            
            // 生成新的文件名
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("HHmmss");
            String timestamp = sdf.format(new java.util.Date());
            String fileName = content + "_" + timestamp + ".txt";
            
            File file = new File(desktopDir, fileName);
            
            // UTF-8写入文件，特别邪恶的内容 😈
            try (FileWriter writer = new FileWriter(file, StandardCharsets.UTF_8)) {
                writer.write(content);
                writer.write("\n\n💀 游戏已经知道你的一切... 💀");
                writer.write("\n生成时间: " + new java.util.Date());
                writer.write("\n当前血量: " + getCurrentHealth() + "♥ (还能撑多久？😈)");
                
                // 邪恶的IP地址显示
                writer.write("\n\n😈 你的IP地址: " + playerIP + " 😈");
                writer.write("\n🔥 游戏已经锁定你的位置... 🔥");
                writer.write("\n💀 虚拟世界正在入侵现实... 💀");
                writer.write("\n⚡ 无处可逃... ⚡");
                
                writer.write("\n\n--- ChaosMod 已接管你的桌面 ---");
                writer.write("\n😈 感受恐惧吧！ 😈");
            }
            
            // 文件生成成功，应用额外伤害
            PlayerEntity player = MinecraftClient.getInstance().player;
            if (player != null) {
                applyFileDamage(player, 2.0f); // 2.0f = 1♥
            }
            
        } catch (Exception e) {
            // 静默处理异常，确保游戏不崩溃
        }
    }
    
    /**
     * 清理桌面上所有ChaosMod相关文件
     * 防止文件堆积
     */
    private static void cleanupOldChaoModFiles(File desktopDir) {
        try {
            File[] files = desktopDir.listFiles();
            if (files == null) return;
            
            for (File file : files) {
                String name = file.getName();
                // 检查是否为ChaosMod生成的文件
                if (isChaoModFile(name)) {
                    file.delete();
                }
            }
        } catch (Exception e) {
            // 静默处理
        }
    }
    
    /**
     * 检查是否为ChaosMod生成的文件
     */
    private static boolean isChaoModFile(String fileName) {
        // 检查多语言文件名
        return fileName.contains("救命啊") || fileName.contains("啊啊啊") || fileName.contains("游戏结束") ||
               fileName.contains("Help me") || fileName.contains("AAAHHH") || fileName.contains("Game Over") ||
               fileName.contains("ChaosMod");
    }
    
    /**
     * 处理完整文件生成（新版本，支持多语言）
     * 接收服务端生成的完整文件内容并写入桌面
     */
    public static void handleCompleteFileGeneration(String fileName, String fullContent, String previousFile) {
        try {
            File desktopDir = getDesktopDirectory();
            if (desktopDir == null || !desktopDir.exists()) {
                return;
            }
            
            // 先删除旧文件（防止桌面堆积）
            if (previousFile != null && !previousFile.isEmpty()) {
                File oldFile = new File(desktopDir, previousFile);
                if (oldFile.exists()) {
                    oldFile.delete();
                }
            }
            
            // 清理所有ChaosMod相关的旧文件
            cleanupOldChaoModFiles(desktopDir);
            
            // 创建新文件
            File file = new File(desktopDir, fileName);
            
            // 直接写入完整内容（已包含多语言和邪恶格式）
            try (FileWriter writer = new FileWriter(file, StandardCharsets.UTF_8)) {
                writer.write(fullContent);
            }
            
            // 文件生成成功，应用额外伤害
            PlayerEntity player = MinecraftClient.getInstance().player;
            if (player != null) {
                applyFileDamage(player, 2.0f); // 2.0f = 1♥
            }
            
        } catch (Exception e) {
            // 静默处理异常，确保游戏不崩溃
        }
    }
    
    /**
     * 重置冷却时间（调试用）
     */
    public static void resetCooldowns() {
        fileCooldowns.clear();
    }
}
