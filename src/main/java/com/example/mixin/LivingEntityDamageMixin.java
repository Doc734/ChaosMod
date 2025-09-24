
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
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Mixin(LivingEntity.class)
public abstract class LivingEntityDamageMixin {
    
    @Unique
    private static final ThreadLocal<Boolean> RANDOM_DAMAGE_REENTRY = ThreadLocal.withInitial(() -> false);
    
    @Unique
    private static final List<Float> damagePool = new ArrayList<>();
    
    @Unique
    private static final float[] allDamageValues = {
        1.0F, 2.0F, 3.0F, 4.0F, 5.0F, 6.0F, 7.0F, 8.0F, 9.0F, 10.0F,  // 0.5♥到5♥
        11.0F, 12.0F, 13.0F, 14.0F, 15.0F, 16.0F, 17.0F, 18.0F, 19.0F, 20.0F  // 5.5♥到10♥
    };
    
    @Inject(method = "damage", at = @At("HEAD"), cancellable = true)
    private void chaos$randomizeDamage(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        // 随机伤害值效果：完全替换原版伤害值
        if (ChaosMod.config.randomDamageAmountEnabled) {
            LivingEntity entity = (LivingEntity)(Object)this;
            
            // 防止递归
            if (RANDOM_DAMAGE_REENTRY.get()) return;
            
            // 初始化候选池（首次使用或池子为空时重置）
            if (damagePool.isEmpty()) {
                for (float value : allDamageValues) {
                    damagePool.add(value);
                }
            }
            
            // 从候选池中随机抽取一个值并移除
            int randomIndex = ThreadLocalRandom.current().nextInt(damagePool.size());
            float randomDamage = damagePool.remove(randomIndex);
            
            // 取消原版伤害处理
            cir.cancel();
            
            // 置入递归抑制后直接调用damage并setReturnValue(true)
            try {
                RANDOM_DAMAGE_REENTRY.set(true);
                entity.damage(entity.getWorld().getDamageSources().generic(), randomDamage);
                cir.setReturnValue(true);
            } finally {
                RANDOM_DAMAGE_REENTRY.set(false);
            }
        }
    }
    
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

        // === 攻击玩家回血：攻击其他玩家时自己回复血量 ===
        if (ChaosMod.config.playerHealOnAttackEnabled && cir.getReturnValue() && 
            victim instanceof ServerPlayerEntity victimPlayer && 
            attacker instanceof ServerPlayerEntity attackerPlayer) {
            
            // 防止递归：添加标记避免治疗触发新的伤害事件
            if (!attackerPlayer.isCreative() && !attackerPlayer.isSpectator() && 
                !victimPlayer.isCreative() && !victimPlayer.isSpectator()) {
                
                // 恢复攻击者2.0F血量（1♥）
                attackerPlayer.heal(2.0F);
            }
        }

        // === 位置互换：受伤时与随机队友交换位置 ===
        if (ChaosMod.config.positionSwapEnabled && cir.getReturnValue() && 
            victim instanceof ServerPlayerEntity victimPlayer) {
            
            if (!victimPlayer.isCreative() && !victimPlayer.isSpectator()) {
                ServerWorld serverWorld = victimPlayer.getServerWorld();
                List<ServerPlayerEntity> allPlayers = serverWorld.getPlayers();
                
                // 过滤出同世界的其他在线玩家
                List<ServerPlayerEntity> validTargets = new ArrayList<>();
                for (ServerPlayerEntity player : allPlayers) {
                    if (player != victimPlayer && !player.isCreative() && !player.isSpectator() && 
                        player.getWorld() == victimPlayer.getWorld()) {
                        validTargets.add(player);
                    }
                }
                
                if (!validTargets.isEmpty()) {
                    // 随机选择一个目标
                    ServerPlayerEntity target = validTargets.get(victimPlayer.getRandom().nextInt(validTargets.size()));
                    
                    // 保存双方位置信息
                    double victimX = victimPlayer.getX();
                    double victimY = victimPlayer.getY();
                    double victimZ = victimPlayer.getZ();
                    float victimYaw = victimPlayer.getYaw();
                    float victimPitch = victimPlayer.getPitch();
                    
                    double targetX = target.getX();
                    double targetY = target.getY();
                    double targetZ = target.getZ();
                    float targetYaw = target.getYaw();
                    float targetPitch = target.getPitch();
                    
                    // 执行位置交换 - 每次受伤都触发  
                    victimPlayer.teleport(targetX, targetY, targetZ, true);
                    victimPlayer.setYaw(targetYaw);
                    victimPlayer.setPitch(targetPitch);
                    
                    target.teleport(victimX, victimY, victimZ, true);
                    target.setYaw(victimYaw);
                    target.setPitch(victimPitch);
                }
            }
        }

        // 背刺回血效果已移除，保持30个效果
    }
}
