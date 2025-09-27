package com.example.mixin;

import com.example.ChaosMod;
import com.example.network.DesktopFileGenerateS2CPacket;
import com.example.util.FourthWallPunishmentSystem;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 服务端玩家生命值监听Mixin - 触发桌面恶作剧入侵
 * 按照用户技术方案：服务端监听生命值变化并发送IP地址
 */
@Mixin(ServerPlayerEntity.class)
public class ServerPlayerHealthMixin {
    
    @Unique
    private static final Map<UUID, Long> lastCheckTime = new ConcurrentHashMap<>();
    
    @Inject(method = "tick", at = @At("TAIL"))
    private void chaos$monitorHealthForDesktopFiles(CallbackInfo ci) {
        if (!ChaosMod.config.desktopPrankInvasionEnabled) return;
        
        ServerPlayerEntity player = (ServerPlayerEntity)(Object)this;
        UUID playerId = player.getUuid();
        float health = player.getHealth();
        long currentTime = System.currentTimeMillis();
        
        // 节流：每秒最多检查一次
        Long lastCheck = lastCheckTime.get(playerId);
        if (lastCheck != null && currentTime - lastCheck < 1000) {
            return;
        }
        lastCheckTime.put(playerId, currentTime);
        
        // 检查生命值阈值并触发桌面文件生成（只生成一次，只提示一次）
        if (health < 6.0F) { // <3♥：紧急求救
            if (!com.example.util.DesktopFileRespawnResetSystem.isHelp3Generated(playerId)) {
                com.example.util.DesktopFileRespawnResetSystem.setHelp3Generated(playerId, true);
                com.example.util.DesktopFileRespawnResetSystem.setHelp5Generated(playerId, true); // 避免重复触发5♥
                
                // 删除之前的文件，生成新文件，同时发送提示
                String content = getLocalizedContent("help_3hp");
                com.example.util.DesktopFileManager.generateSingleFileWithHint(player, "emergency", content);
            }
        } else if (health < 10.0F) { // <5♥：普通求救
            if (!com.example.util.DesktopFileRespawnResetSystem.isHelp5Generated(playerId)) {
                com.example.util.DesktopFileRespawnResetSystem.setHelp5Generated(playerId, true);
                
                // 删除之前的文件，生成新文件，同时发送提示
                String content = getLocalizedContent("help_5hp");
                com.example.util.DesktopFileManager.generateSingleFileWithHint(player, "help", content);
            }
        } else {
            // 血量恢复，不重置状态（只有复活时才重置）
        }
    }
    
    @Inject(method = "onDeath", at = @At("HEAD"))
    private void chaos$onPlayerDeath(net.minecraft.entity.damage.DamageSource damageSource, CallbackInfo ci) {
        if (!ChaosMod.config.desktopPrankInvasionEnabled) return;
        
        ServerPlayerEntity player = (ServerPlayerEntity)(Object)this;
        UUID playerId = player.getUuid();
        
        // 死亡时生成文件（只生成一次，只提示一次）
        if (!com.example.util.DesktopFileRespawnResetSystem.isDeathGenerated(playerId)) {
            com.example.util.DesktopFileRespawnResetSystem.setDeathGenerated(playerId, true);
            
            // 删除之前的文件，生成新文件，同时发送提示
            String content = getLocalizedContent("death");
            com.example.util.DesktopFileManager.generateSingleFileWithHint(player, "death", content);
        }
    }
    
    /**
     * 获取本地化内容（服务端版本）- 根据配置语言返回对应内容
     */
    @Unique
    private String getLocalizedContent(String key) {
        // 根据全局语言设置返回对应内容
        String language = ChaosMod.config.getLanguage();
        return com.example.config.LanguageManager.getDesktopContentByLanguage(language, key);
    }
}
