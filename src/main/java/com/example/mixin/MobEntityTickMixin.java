package com.example.mixin;

import com.example.ChaosMod;
import com.example.util.DamageRouting;
import com.example.util.ThreatProfiles;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.pathing.EntityNavigation;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MobEntity.class)
public abstract class MobEntityTickMixin {

    @Unique private int chaos$ticker;
    @Unique private int chaos$attackCd; // melee cooldown in ticks

    @Inject(method = "tick", at = @At("TAIL"))
    private void chaos$aggroTick(CallbackInfo ci) {
        if (!ChaosMod.config.allHostileEnabled) return;
        MobEntity self = (MobEntity)(Object)this;
        if (self.getWorld().isClient()) return;
        ServerWorld sw = (ServerWorld) self.getWorld();

        chaos$ticker++;
        if (chaos$attackCd > 0) chaos$attackCd--;

        if ((chaos$ticker & 1) != 0) return; // every 2 ticks

        // Follow range = vanilla FOLLOW_RANGE attribute
        double range = 16.0;
        try { range = self.getAttributeValue(EntityAttributes.GENERIC_FOLLOW_RANGE); } catch (Throwable ignored) {}
        if (range <= 0) range = 16.0;

        PlayerEntity target = sw.getClosestPlayer(self, range);
        if (target == null || target.isCreative() || target.isSpectator()) {
            self.setTarget(null);
            return;
        }

        // Let vanilla AI see the target for better aggression
        try { self.setTarget(target); } catch (Throwable ignored) {}

        // Navigate
        double speed = ThreatProfiles.chaseSpeed(self.getType());
        try {
            EntityNavigation nav = self.getNavigation();
            if (nav != null) nav.startMovingTo(target, speed);
        } catch (Throwable ignored) {}

        // Melee range check
        double dx = target.getX() - self.getX();
        double dz = target.getZ() - self.getZ();
        double distSq = dx*dx + dz*dz;

        double reach = (double)(self.getWidth() * 2.0f + 0.5f);
        double reachSq = reach * reach;

        int cdTicks = chaos$getAttackCooldownTicks(self);

        if (distSq <= reachSq) {
            if (chaos$attackCd == 0) {
                boolean hit = false;
                try {
                    hit = self.tryAttack(target); // vanilla-style attack: handles damage/anim/horizontal KB
                } catch (Throwable ignored) {}

                // Ensure special vertical knockback exists for IG/Ravager/Warden
                chaos$applySpecialVerticalKnockup(self, target);

                if (!hit) {
                    // Fallback contact hit + knockback if entity lacks real melee
                    chaos$contactHitAndKnock(self, target, sw);
                }
                chaos$attackCd = cdTicks; // per-entity cooldown
            }
        } else {
            // If nearly in reach but not intersecting, add a tiny push to avoid "stuck near target but not hitting"
            double gap = Math.sqrt(distSq) - reach;
            if (gap <= 0.5) {
                Vec3d h = new Vec3d(dx, 0, dz).normalize().multiply(0.045);
                self.addVelocity(h.x, 0, h.z);
                self.velocityDirty = true;
            }
        }

        // Contact damage + vanilla-like knockback, now respecting the same cooldown window
        double extra = ThreatProfiles.extraReach(self.getType());
        Box reachBox = self.getBoundingBox().expand(extra);
        if (reachBox.intersects(target.getBoundingBox())) {
            if (chaos$attackCd == 0 && !DamageRouting.contactOnCooldown(self)) {
                DamageRouting.armContactCooldown(self, cdTicks);
                float contact = ThreatProfiles.contactDamage(self.getType());
                target.damage(sw.getDamageSources().mobAttack(self), contact);

                // Horizontal knockback from attacker toward target
                double len = Math.sqrt(dx*dx + dz*dz);
                if (len > 0.0001) {
                    double nx = dx/len, nz = dz/len;
                    float strength = 0.4f;
                    try {
                        double kbAttr = self.getAttributeValue(EntityAttributes.GENERIC_ATTACK_KNOCKBACK);
                        strength = (float)(0.4f + Math.max(0.0, kbAttr));
                    } catch (Throwable ignored) {}
                    target.takeKnockback(strength, -nx, -nz);
                }

                // Vertical special add for certain mobs
                chaos$applySpecialVerticalKnockup(self, target);

                chaos$attackCd = cdTicks;
            }
        }
    }

    @Unique
    private static int chaos$getAttackCooldownTicks(MobEntity self) {
        // Conservative per-entity cooldowns (approximate vanilla feels)
        EntityType<?> t = self.getType();
        if (t == EntityType.IRON_GOLEM) return 20;   // 1.0s
        if (t == EntityType.RAVAGER)   return 40;   // 2.0s (slower, heavy)
        if (t == EntityType.WARDEN)    return 30;   // 1.5s
        return 20;                                  // default 1.0s
    }

    @Unique
    private static void chaos$applySpecialVerticalKnockup(MobEntity self, PlayerEntity target) {
        double kbV = 0.0;
        EntityType<?> t = self.getType();
        if (t == EntityType.IRON_GOLEM) kbV = 0.50;
        else if (t == EntityType.RAVAGER) kbV = 0.20;
        else if (t == EntityType.WARDEN) kbV = 0.10;
        if (kbV > 0) {
            target.addVelocity(0, Math.min(0.9, kbV), 0);
            target.velocityDirty = true;
        }
    }

    @Unique
    private static void chaos$contactHitAndKnock(MobEntity self, PlayerEntity target, ServerWorld sw) {
        // Deal a contact hit and apply vanilla-like knockback + special vertical
        float contact = ThreatProfiles.contactDamage(self.getType());
        target.damage(sw.getDamageSources().mobAttack(self), contact);

        double dx = target.getX() - self.getX();
        double dz = target.getZ() - self.getZ();
        double len = Math.sqrt(dx*dx + dz*dz);
        if (len > 0.0001) {
            double nx = dx/len, nz = dz/len;
            float strength = 0.4f;
            try {
                double kbAttr = self.getAttributeValue(EntityAttributes.GENERIC_ATTACK_KNOCKBACK);
                strength = (float)(0.4f + Math.max(0.0, kbAttr));
            } catch (Throwable ignored) {}
            target.takeKnockback(strength, -nx, -nz);
        }
        chaos$applySpecialVerticalKnockup(self, target);
    }
}