package com.example.mixin;

import com.example.util.ChaosEffects;
import com.example.network.KeyDisableS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityJoinMixin {
    
    @Inject(method = "onSpawn", at = @At("TAIL"))
    private void chaos$onPlayerJoin(CallbackInfo ci) {
        ServerPlayerEntity player = (ServerPlayerEntity)(Object)this;
        
        // 玩家加入时清除死亡标记并同步按键禁用状态
        // 清除死亡标记（防止重连时残留死亡状态）
        ChaosEffects.clearDeathFlag(player);
        // 同步当前状态
        ChaosEffects.syncKeyDisableState(player);
    }
}
