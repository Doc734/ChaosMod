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
        // 物品栏诅咒效果
        if (ChaosMod.config.inventoryCurseEnabled) {
            if (!player.isCreative() && !player.isSpectator()) {
                // 12% chance for inventory curse
                if (player.getRandom().nextFloat() < 0.12f) {
                    // Deal 0.5 heart damage
                    player.damage(player.getDamageSources().generic(), 1.0f);
                }
            }
        }
        
        // 旧效果已移除
    }
}
