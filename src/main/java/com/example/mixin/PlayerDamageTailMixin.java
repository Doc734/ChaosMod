package com.example.mixin;

import com.example.ChaosMod;
import com.example.util.ChaosEffects;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntity.class)
public abstract class PlayerDamageTailMixin {
    @Inject(method = "damage", at = @At("TAIL"))
    private void chaos$onDamageEnd(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        PlayerEntity p = (PlayerEntity)(Object)this;
        if (p.getWorld().isClient()) return;
        
        // 只有在实际受到伤害时才触发（返回值为true）
        if (cir.getReturnValue()) {
            // 按键失灵：累计受伤次数
            ChaosEffects.handleKeyDisable(p);
            
            // 受伤随机增益：随机添加或移除状态效果
            ChaosEffects.handleRandomEffects(p);
            
            // 痛觉扩散：标记为带电状态（使用新的严格实现）
            com.example.util.PainSpreadSystem.markElectrified(p);
        }
        
        // 盾牌削弱（原有功能）
        if (!ChaosMod.config.shieldNerfEnabled) return;
        if (amount <= 0) return;
        // If the player is currently blocking, add 20% penetration
        if (p.isBlocking()) {
            float extra = amount * 0.20f;
            p.damage(p.getDamageSources().generic(), extra);
        }
    }
}