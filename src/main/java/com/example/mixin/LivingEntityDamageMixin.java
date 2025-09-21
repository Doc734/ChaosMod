
package com.example.mixin;

import com.example.ChaosMod;
import com.example.util.DamageRouting;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(LivingEntity.class)
public abstract class LivingEntityDamageMixin {
    @Inject(method = "damage", at = @At("TAIL"))
    private void chaos$afterDamage(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        LivingEntity victim = (LivingEntity)(Object)this;
        Entity attacker = source.getAttacker();

        // Buffs when mob hits player + EnderDragon bucket conversion
        if (victim instanceof PlayerEntity player && attacker instanceof MobEntity mob) {
            if (!player.isCreative() && !player.isSpectator()) {
                DamageRouting.applyOnHitEffects(player, mob);
                if (ChaosMod.config.enderDragonBucketEnabled && attacker instanceof EnderDragonEntity) {
                    for (int i = 0; i < player.getInventory().size(); i++) {
                        ItemStack st = player.getInventory().getStack(i);
                        if (!st.isEmpty() && st.getItem() == Items.WATER_BUCKET) {
                            player.getInventory().setStack(i, new ItemStack(Items.MILK_BUCKET));
                        }
                    }
                }
            }
        }

        // Reflect 50% when player damages a mob
        if (ChaosMod.config.mobThornsEnabled && victim instanceof MobEntity mob && attacker instanceof PlayerEntity player) {
            if (!player.isCreative() && !player.isSpectator() && amount > 0.0f) {
                player.damage(player.getDamageSources().thorns(victim), amount * 0.5f);
            }
        }

        // === 共享生命：若任一玩家死亡 -> 全员死亡（含受击者） ===
        if (ChaosMod.config.sharedHealthEnabled && victim instanceof PlayerEntity v) {
            if (v.getWorld().isClient()) return;
            if (v.isDead() || v.getHealth() <= 0f) {
                ServerWorld sw = (ServerWorld) v.getWorld();
                List<ServerPlayerEntity> players = sw.getPlayers();
                for (ServerPlayerEntity p : players) {
                    if (!p.isDead()) p.kill();
                }
            }
        }
    }
}
