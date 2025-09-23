package com.example.mixin;

import com.example.ChaosMod;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Cancel vanilla dropInventory() when feature is enabled:
 *  - NOT in the End: cancel (keep items; copy handled in Server mixin)
 *  - In the End: cancel as well because we've already manually dropped in Server mixin
 */
@Mixin(PlayerEntity.class)
public abstract class PlayerEntityDropInventoryMixin {

    @Inject(method = "dropInventory", at = @At("HEAD"), cancellable = true)
    private void chaos$cancelDropWhenKeeping(CallbackInfo ci) {
        if (!ChaosMod.config.endKeepOverrideEnabled) return;
        PlayerEntity self = (PlayerEntity)(Object)this;
        if (self.getWorld().getRegistryKey() != World.END) {
            // Overworld/Nether: keep inventory
            ci.cancel();
        } else {
            // End: manual drop already done in Server mixin; avoid duplicate
            ci.cancel();
        }
    }
}