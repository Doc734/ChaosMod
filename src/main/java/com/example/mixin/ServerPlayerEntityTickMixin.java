package com.example.mixin;

import com.example.ChaosMod;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayerEntity.class)
public class ServerPlayerEntityTickMixin {
    
    @Inject(method = "tick", at = @At("TAIL"))
    private void chaos$acrophobiaTick(CallbackInfo ci) {
        ServerPlayerEntity player = (ServerPlayerEntity)(Object)this;
        
        // 检查恐高症配置
        if (!ChaosMod.config.acrophobiaEnabled) return;
        
        // 检查玩家模式
        if (player.isCreative() || player.isSpectator()) return;
        
        // 检查高度 (Y > 80为危险高度)
        int height = player.getBlockY();
        if (height <= 80) return;
        
        // 每2秒触发一次 (40 ticks = 2 seconds)
        if (player.age % 40 != 0) return;
        
        // 计算基于高度的伤害：越高越痛苦
        float damage = calculateHeightDamage(height);
        
        // 造成恐高症伤害
        player.damage(player.getServerWorld().getDamageSources().magic(), damage);
    }
    
    /**
     * 计算基于高度的恐高症伤害
     * Y=80: 安全线，无伤害
     * Y=81-120: 渐进式恐惧，每10层增加约0.5♥
     * Y=120+: 极度恐高，固定2♥伤害
     */
    private float calculateHeightDamage(int height) {
        if (height <= 80) {
            return 0.0F; // 安全高度
        } else if (height <= 120) {
            // 渐进式恐惧：40层高度内从0增长到2♥
            // 公式：(height - 80) * 0.1F = 每层0.05♥，每10层0.5♥
            return (height - 80) * 0.1F;
        } else {
            // 极度恐高：超过120层固定最大伤害2♥
            return 4.0F; // 4.0F = 2♥
        }
    }
}
