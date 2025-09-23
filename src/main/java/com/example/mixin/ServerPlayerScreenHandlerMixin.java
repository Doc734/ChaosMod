package com.example.mixin;

import com.example.ChaosMod;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.AbstractFurnaceScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayerEntity.class)
public class ServerPlayerScreenHandlerMixin {
    
    @Inject(method = "onScreenHandlerOpened", at = @At("TAIL"))
    private void chaos$containerCurse(ScreenHandler screenHandler, CallbackInfo ci) {
        ServerPlayerEntity player = (ServerPlayerEntity)(Object)this;
        if (!ChaosMod.config.containerCurseEnabled) return;
        if (player.isCreative() || player.isSpectator()) return;
        
        // Check if it's a container or furnace
        if (screenHandler instanceof GenericContainerScreenHandler || 
            screenHandler instanceof AbstractFurnaceScreenHandler) {
            // 25% chance for container curse
            if (player.getRandom().nextFloat() < 0.25f) {
                // Deal 1 heart damage
                player.damage(player.getDamageSources().generic(), 2.0f);
            }
        }
    }
}
