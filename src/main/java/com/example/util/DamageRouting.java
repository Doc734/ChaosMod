
package com.example.util;

import com.example.ChaosMod;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public final class DamageRouting {
    private DamageRouting(){}

    private static final Map<Entity, Integer> CONTACT_CD = new WeakHashMap<>();
    private static final ThreadLocal<Boolean> REENTRY = ThreadLocal.withInitial(() -> Boolean.FALSE);

    // no-heal 10s window storage (ticks)
    private static final Map<PlayerEntity, Integer> NO_HEAL = new WeakHashMap<>();
    // remember previous tick threshold state
    private static final Map<PlayerEntity, Boolean> BELOW_THRESHOLD = new WeakHashMap<>();

    public static boolean contactOnCooldown(Entity e) {
        Integer v = CONTACT_CD.get(e);
        if (v == null) return false;
        if (v <= 0) { CONTACT_CD.remove(e); return false; }
        CONTACT_CD.put(e, v - 1);
        return true;
    }
    public static void armContactCooldown(Entity e, int ticks) { CONTACT_CD.put(e, ticks); }

    public static void applyOnHitEffects(PlayerEntity player, LivingEntity attacker) {
        if (player.isCreative() || player.isSpectator()) return;
        if (ChaosMod.config.mobIgniteEnabled) player.setOnFireFor(3);
        if (ChaosMod.config.mobSlownessEnabled) {
            player.addStatusEffect(new net.minecraft.entity.effect.StatusEffectInstance(net.minecraft.entity.effect.StatusEffects.SLOWNESS, 20*5, 1));
        }
        if (ChaosMod.config.mobBlindnessEnabled) {
            player.addStatusEffect(new net.minecraft.entity.effect.StatusEffectInstance(net.minecraft.entity.effect.StatusEffects.BLINDNESS, 20, 0));
        }
    }

    public static boolean routePlayerDamage(PlayerEntity victim, DamageSource source, float amount) {
        if (REENTRY.get()) return false;
        ServerWorld sw = (ServerWorld) victim.getWorld();
        java.util.List<ServerPlayerEntity> online = new java.util.ArrayList<>(sw.getPlayers());

        if (ChaosMod.config.sharedDamageSplitEnabled && !online.isEmpty()) {
            int n = online.size();
            float each = Math.max(0.0f, amount / n);
            boolean anyDead = false;
            try {
                REENTRY.set(Boolean.TRUE);
                for (ServerPlayerEntity p : online) p.damage(source, each);
                for (ServerPlayerEntity p : online) if (p.isDead() || p.getHealth() <= 0f) { anyDead = true; break; }
                if (anyDead) for (ServerPlayerEntity p : online) if (!p.isDead()) p.kill();
            } finally { REENTRY.set(Boolean.FALSE); }
            return true;
        }

        if (ChaosMod.config.randomDamageEnabled && !online.isEmpty()) {
            ServerPlayerEntity pick = online.get(java.util.concurrent.ThreadLocalRandom.current().nextInt(online.size()));
            try { REENTRY.set(Boolean.TRUE); pick.damage(source, amount); }
            finally { REENTRY.set(Boolean.FALSE); }
            return true;
        }

        if (ChaosMod.config.playerDamageShareEnabled) {
            final double R = 2.0;
            java.util.List<ServerPlayerEntity> group = new java.util.ArrayList<>();
            for (ServerPlayerEntity p : online) {
                if (p == victim) continue;
                if (p.squaredDistanceTo(victim) <= R*R) group.add(p);
            }
            if (!group.isEmpty()) {
                group.add((ServerPlayerEntity) victim);
                int n = group.size();
                float each = Math.max(0.0f, amount / n);
                boolean anyDead = false;
                try {
                    REENTRY.set(Boolean.TRUE);
                    for (ServerPlayerEntity p : group) p.damage(source, each);
                    for (ServerPlayerEntity p : group) if (p.isDead() || p.getHealth() <= 0f) { anyDead = true; break; }
                    if (anyDead) for (ServerPlayerEntity p : group) if (!p.isDead()) p.kill();
                } finally { REENTRY.set(Boolean.FALSE); }
                return true;
            }
        }

        if (ChaosMod.config.sharedHealthEnabled && !online.isEmpty()) {
            try { REENTRY.set(Boolean.TRUE); for (ServerPlayerEntity p : online) if (p != victim) p.damage(source, amount); }
            finally { REENTRY.set(Boolean.FALSE); }
            return false;
        }

        return false;
    }

    /** Return true iff we CROSS from (>1♥) to (<=1♥) this tick; then update stored state. */
    public static boolean updateAndCheckCrossing(PlayerEntity p) {
        boolean prev = BELOW_THRESHOLD.getOrDefault(p, Boolean.FALSE);
        boolean now = p.getHealth() <= 2.0f;
        BELOW_THRESHOLD.put(p, now);
        return (!prev && now);
    }

    /** Count down the 10s no-heal window. */
    public static void tickNoHeal(PlayerEntity p) {
        Integer left = NO_HEAL.get(p);
        if (left == null) return;
        left -= 1;
        if (left <= 0) {
            NO_HEAL.remove(p);
            ChaosMod.config.noHealActive = false;
            ChaosMod.config.noHealEndTime = 0L;
        } else {
            NO_HEAL.put(p, left);
        }
    }

    /** Maybe start a 10s window; only on downward crossing. */
    public static void maybeStartNoHeal(PlayerEntity p, boolean crossedDown) {
        if (!ChaosMod.config.lowHealthNoHealEnabled) return;
        if (!crossedDown) return;
        if (NO_HEAL.containsKey(p)) return;
        if (p.getRandom().nextFloat() < 0.5f) {
            NO_HEAL.put(p, 20 * 10);
            ChaosMod.config.noHealActive = true;
            ChaosMod.config.noHealEndTime = p.getWorld().getTime() + 20 * 10;
        }
    }

    public static boolean isNoHeal(PlayerEntity p) {
        Integer left = NO_HEAL.get(p);
        return left != null && left > 0;
    }
}
