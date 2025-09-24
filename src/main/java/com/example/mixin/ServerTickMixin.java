package com.example.mixin;

import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftServer.class)
public class ServerTickMixin {
    
    @Inject(method = "tick", at = @At("TAIL"))
    private void chaos$serverTick(CallbackInfo ci) {
        // 新的效果将在其他mixin中实现
        // 这里保留为空，以防将来需要服务器级别的tick处理
    }
}