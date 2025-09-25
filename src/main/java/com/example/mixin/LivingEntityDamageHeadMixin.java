package com.example.mixin;

import com.example.ChaosMod;
import com.example.util.DamageRouting;
import com.example.util.ChaosEffects;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = LivingEntity.class, priority = 1100)  // 【体检点7】设置更高优先级
public abstract class LivingEntityDamageHeadMixin {
    @Inject(method = "damage", at = @At("HEAD"), cancellable = true)
    private void chaos$routeDamage(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        LivingEntity self = (LivingEntity)(Object)this;
        if (self.getWorld().isClient()) return;
        
        
        // 延迟受伤：拦截伤害并延迟处理
        if (ChaosEffects.interceptDelayedDamage(self, source, amount)) {
            cir.setReturnValue(false);
            cir.cancel();
            return;
        }
        
        // 伤害背锅人：重定向伤害（使用新的严格实现）
        if (com.example.util.ScapegoatSystem.redirectDamageToScapegoat(self, source, amount)) {
            cir.setReturnValue(true); // 按用户要求返回true
            cir.cancel();
            return;
        }
        
        if (self instanceof PlayerEntity player) {
            // Check reverse damage system first
            if (DamageRouting.shouldBlockDamageForReverse(player, source)) {
                cir.setReturnValue(false);
                cir.cancel();
                return;
            }
            
            // Then check other damage routing
            if (DamageRouting.routePlayerDamage(player, source, amount)) {
                cir.setReturnValue(false);
                cir.cancel();
            }
        }
    }
}