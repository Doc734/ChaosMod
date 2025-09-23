package com.example.mixin;

import com.example.ChaosMod;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.ServerPlayerInteractionManager;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayerInteractionManager.class)
public class ServerPlayerInteractionManagerMixin {
    
    @Shadow
    public ServerPlayerEntity player;
    
    @Inject(method = "tryBreakBlock", at = @At("TAIL"))
    private void chaos$blockRevenge(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        if (!ChaosMod.config.blockRevengeEnabled) return;
        if (player.isCreative() || player.isSpectator()) return;
        
        // Only trigger if block was successfully broken
        if (cir.getReturnValue()) {
            // 10% chance for block revenge
            if (player.getRandom().nextFloat() < 0.1f) {
                // Deal 1 heart damage
                player.damage(player.getDamageSources().generic(), 2.0f);
            }
        }
    }
}
