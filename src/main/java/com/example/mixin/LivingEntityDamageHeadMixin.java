package com.example.mixin;

import com.example.ChaosMod;
import com.example.util.DamageRouting;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityDamageHeadMixin {
    @Inject(method = "damage", at = @At("HEAD"), cancellable = true)
    private void chaos$routeDamage(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        LivingEntity self = (LivingEntity)(Object)this;
        if (self.getWorld().isClient()) return;
        if (self instanceof PlayerEntity player) {
            if (DamageRouting.routePlayerDamage(player, source, amount)) {
                cir.setReturnValue(false);
                cir.cancel();
            }
        }
    }
}