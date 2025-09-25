package com.example.mixin;

import com.example.util.ChaosEffects;
import net.minecraft.entity.Entity;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerManager.class)
public abstract class PlayerManagerRespawnMixin {
    
    @Inject(method = "respawnPlayer", at = @At("RETURN"))
    private void chaos$onRespawnPlayer(ServerPlayerEntity player, boolean alive, Entity.RemovalReason removalReason, CallbackInfoReturnable<ServerPlayerEntity> cir) {
        ServerPlayerEntity respawnedPlayer = cir.getReturnValue();
        
        // Mixin备用方案：在玩家管理器层面检测复活
        ChaosEffects.resetOnRespawn(respawnedPlayer);
    }
}
