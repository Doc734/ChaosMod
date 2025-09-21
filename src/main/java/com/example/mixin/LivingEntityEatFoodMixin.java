package com.example.mixin;

import com.example.ChaosMod;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraft.component.type.FoodComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityEatFoodMixin {
    // 1.21: LivingEntity#eatFood(World, ItemStack, FoodComponent) -> ItemStack
    @Inject(method = "eatFood", at = @At("TAIL"))
    private void chaos$poisonOnEat(World world, ItemStack stack, FoodComponent foodComponent, CallbackInfoReturnable<ItemStack> cir) {
        if (!ChaosMod.config.foodPoisonEnabled) return;
        LivingEntity self = (LivingEntity)(Object)this;
        if (world.isClient()) return;
        if (!(self instanceof PlayerEntity)) return;
        if (self.getRandom().nextFloat() < 0.30f) {
            self.addStatusEffect(new StatusEffectInstance(StatusEffects.POISON, 20 * 10, 0));
        }
    }
}