package com.example.mixin;

import com.example.ChaosMod;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntity.class)
public class PlayerEntityActionMixin {
    
    @Inject(method = "attack", at = @At("TAIL"))
    private void chaos$onPlayerAttack(Entity target, CallbackInfo ci) {
        PlayerEntity player = (PlayerEntity)(Object)this;
        
        // 只在服务端处理
        if (player.getWorld().isClient()) return;
        if (!(player instanceof ServerPlayerEntity serverPlayer)) return;
        
        // 攻击行为处理（暂时为空）
    }
    
    @Inject(method = "interact", at = @At("TAIL"))
    private void chaos$onPlayerInteract(Entity entity, Hand hand, CallbackInfoReturnable<ActionResult> cir) {
        PlayerEntity player = (PlayerEntity)(Object)this;
        
        // 只在服务端处理
        if (player.getWorld().isClient()) return;
        if (!(player instanceof ServerPlayerEntity serverPlayer)) return;
        
        // 交互行为处理（暂时为空）
    }
}

