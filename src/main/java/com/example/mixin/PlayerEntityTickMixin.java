
package com.example.mixin;

import com.example.util.DamageRouting;
import com.example.util.ChaosEffects;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityTickMixin {
    @Inject(method = "tick", at = @At("TAIL"))
    private void chaos$tickSystems(CallbackInfo ci) {
        PlayerEntity p = (PlayerEntity)(Object)this;
        if (p.getWorld().isClient()) return;
        
        // 原有系统
        // First tick countdown
        DamageRouting.tickNoHeal(p);
        // Then detect downward crossing and maybe start the 10s window
        boolean crossed = DamageRouting.updateAndCheckCrossing(p);
        DamageRouting.maybeStartNoHeal(p, crossed);
        // Tick reverse damage system
        DamageRouting.tickReverseDamage(p);
        // Tick sunburn system
        DamageRouting.tickSunburn(p);
        
        // 新的混沌效果系统
        // 延迟受伤：处理延迟伤害队列
        ChaosEffects.tickDelayedDamage(p);
        // 痛觉扩散：处理带电状态（使用新的严格实现）
        com.example.util.PainSpreadSystem.tickElectrified(p);
    }
}
