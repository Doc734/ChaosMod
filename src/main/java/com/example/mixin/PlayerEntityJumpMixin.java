package com.example.mixin;

import com.example.util.ChaosEffects;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 跳跃税：拦截玩家跳跃
 * 每次跳跃必定扣0.5♥血
 */
@Mixin(PlayerEntity.class)
public class PlayerEntityJumpMixin {

    @Inject(method = "jump", at = @At("HEAD"))
    private void onJump(CallbackInfo ci) {
        PlayerEntity player = (PlayerEntity) (Object) this;
        
        // 跳跃税：每次跳跃扣血
        ChaosEffects.handleJumpTax(player);
    }
}

