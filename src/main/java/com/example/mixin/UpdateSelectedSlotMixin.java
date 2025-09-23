package com.example.mixin;

import com.example.ChaosMod;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayNetworkHandler.class)
public class UpdateSelectedSlotMixin {
    
    @Shadow
    public net.minecraft.server.network.ServerPlayerEntity player;
    
    @Inject(method = "onUpdateSelectedSlot", at = @At("TAIL"))
    private void chaos$inventoryCurse(UpdateSelectedSlotC2SPacket packet, CallbackInfo ci) {
        if (!ChaosMod.config.inventoryCurseEnabled) return;
        if (player.isCreative() || player.isSpectator()) return;
        
        // 12% chance for inventory curse
        if (player.getRandom().nextFloat() < 0.12f) {
            // Deal 0.5 heart damage
            player.damage(player.getDamageSources().generic(), 1.0f);
        }
    }
}
