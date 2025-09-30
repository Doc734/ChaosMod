package com.example.mixin;

import com.example.ChaosMod;
import com.example.util.ChaosEffects;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.ServerPlayerInteractionManager;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
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
        // 方块反噬效果
        if (ChaosMod.config.blockRevengeEnabled) {
            if (!player.isCreative() && !player.isSpectator()) {
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
        
        // 旧效果已移除
    }
    
    /**
     * 触控地狱：拦截方块交互
     * 右键点击方块50%概率传送到地表岩浆池
     */
    @Inject(method = "interactBlock", at = @At("HEAD"), cancellable = true)
    private void chaos$touchHell(ServerPlayerEntity player, net.minecraft.world.World world, net.minecraft.item.ItemStack stack, Hand hand, BlockHitResult hitResult, CallbackInfoReturnable<ActionResult> cir) {
        // 触控地狱效果
        ActionResult result = ChaosEffects.handleTouchHell(player, world);
        if (result != ActionResult.PASS) {
            cir.setReturnValue(result); // 阻断原交互
        }
    }
}
