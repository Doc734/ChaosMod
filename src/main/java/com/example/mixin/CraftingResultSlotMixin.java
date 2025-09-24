package com.example.mixin;

import com.example.ChaosMod;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.CraftingResultSlot;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CraftingResultSlot.class)
public class CraftingResultSlotMixin {
    
    @Inject(method = "onTakeItem", at = @At("TAIL"))
    private void chaos$craftingTrap(PlayerEntity player, ItemStack stack, CallbackInfo ci) {
        // 只在服务端处理
        if (player.getWorld().isClient()) return;
        
        // 合成陷阱效果
        if (ChaosMod.config.craftingTrapEnabled) {
            if (!player.isCreative() && !player.isSpectator()) {
                // 10% chance for crafting trap
                if (player.getRandom().nextFloat() < 0.1f) {
                    // Deal 1 heart damage
                    player.damage(player.getDamageSources().generic(), 2.0f);
                }
            }
        }
        
        // 旧效果已移除
    }
}
