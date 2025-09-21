package com.example.mixin;

import com.example.ChaosMod;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntity.class)
public abstract class PlayerDamageTailMixin {
    @Inject(method = "damage", at = @At("TAIL"))
    private void chaos$shieldNerf(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        PlayerEntity p = (PlayerEntity)(Object)this;
        if (!ChaosMod.config.shieldNerfEnabled) return;
        if (p.getWorld().isClient()) return;
        if (amount <= 0) return;
        // If the player is currently blocking, add 20% penetration
        if (p.isBlocking()) {
            float extra = amount * 0.20f;
            p.damage(p.getDamageSources().generic(), extra);
        }
    }
}