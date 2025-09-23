
package com.example.mixin;

import com.example.util.DamageRouting;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityTickMixin {
    @Inject(method = "tick", at = @At("TAIL"))
    private void chaos$noHealDriver(CallbackInfo ci) {
        PlayerEntity p = (PlayerEntity)(Object)this;
        if (p.getWorld().isClient()) return;
        // First tick countdown
        DamageRouting.tickNoHeal(p);
        // Then detect downward crossing and maybe start the 10s window
        boolean crossed = DamageRouting.updateAndCheckCrossing(p);
        DamageRouting.maybeStartNoHeal(p, crossed);
        // Tick reverse damage system
        DamageRouting.tickReverseDamage(p);
        // Tick sunburn system
        DamageRouting.tickSunburn(p);
    }
}
