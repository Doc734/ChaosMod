package com.example.mixin;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.network.ClientConnection;
import net.minecraft.server.PlayerManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 服务端玩家离线清理Mixin - 清理桌面文件生成状态
 */
@Mixin(PlayerManager.class)
public class ServerPlayerDisconnectMixin {
    
    @Inject(method = "remove", at = @At("HEAD"))
    private void chaos$onPlayerDisconnect(ServerPlayerEntity player, CallbackInfo ci) {
        if (player == null) return;
        
        String playerId = player.getUuid().toString();
        
        // 清理玩家的桌面文件生成状态
        com.example.util.DesktopFileRespawnResetSystem.cleanupPlayerData(player.getUuid());
        com.example.util.DesktopFileManager.cleanupPlayerData(player.getUuid());
        
        // 清理玩家的IP缓存
        com.example.util.SimpleIPProvider.cleanupPlayerCache(playerId);
    }
}
