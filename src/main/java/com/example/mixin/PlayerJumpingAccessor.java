package com.example.mixin;

import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(LivingEntity.class)
public interface PlayerJumpingAccessor {
    @Accessor("jumping")
    boolean chaos$isJumping();
}
