package com.example.mixin;

import com.example.util.ChaosEffects;
import net.minecraft.entity.ItemEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemEntity.class)
public class ItemEntityPickupMixin {

    @Inject(method = "onPlayerCollision", at = @At("TAIL"))
    private void chaos$onPlayerPickup(net.minecraft.entity.player.PlayerEntity player, CallbackInfo ci) {
        // 仅在服务端执行
        if (player.getWorld().isClient()) return;
        if (!(player instanceof ServerPlayerEntity serverPlayer)) return;
        
        ItemEntity itemEntity = (ItemEntity)(Object)this;
        
        // 检查物品是否已经被拾取（物品实体被移除或堆叠为0）
        if (itemEntity.isRemoved() || itemEntity.getStack().isEmpty()) {
            // 物品成功拾取，触发贪婪吸blood效果
            ChaosEffects.handlePickupDrain(serverPlayer);
        }
    }
}

