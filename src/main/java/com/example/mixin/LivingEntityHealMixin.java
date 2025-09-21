package com.example.mixin;

import com.example.util.DamageRouting;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class LivingEntityHealMixin {
    @Inject(method = "heal", at = @At("HEAD"), cancellable = true)
    private void chaos$blockHeal(float amount, CallbackInfo ci) {
        LivingEntity self = (LivingEntity)(Object)this;
        if (self instanceof PlayerEntity p && DamageRouting.isNoHeal(p)) {
            ci.cancel();
        }
    }
}