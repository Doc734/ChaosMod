package com.example.mixin;

import com.example.ChaosMod;
import com.example.util.DamageRouting;
import com.example.util.ThreatProfiles;
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

    @Inject(method = "tick", at = @At("TAIL"))
    private void chaos$aggroTick(CallbackInfo ci) {
        if (!ChaosMod.config.allHostileEnabled) return;
        MobEntity self = (MobEntity)(Object)this;
        if (self.getWorld().isClient()) return;
        ServerWorld sw = (ServerWorld) self.getWorld();

        chaos$ticker++;
        if ((chaos$ticker & 1) != 0) return; // every 2 ticks

        // === 使用原版的仇恨/跟随距离（FOLLOW_RANGE 属性），与原版一致 ===
        double range = 16.0;
        try {
            range = self.getAttributeValue(EntityAttributes.GENERIC_FOLLOW_RANGE);
        } catch (Throwable ignored) { }
        if (range <= 0) range = 16.0;

        PlayerEntity target = sw.getClosestPlayer(self, range);
        if (target == null || target.isCreative() || target.isSpectator()) return;

        double speed = ThreatProfiles.chaseSpeed(self.getType());
        try {
            EntityNavigation nav = self.getNavigation();
            if (nav != null) nav.startMovingTo(target, speed);
        } catch (Throwable t) {
            // fallback: small velocity push
            Vec3d d = target.getPos().subtract(self.getPos());
            Vec3d h = new Vec3d(d.x, 0, d.z);
            if (h.lengthSquared() > 1.0E-6) {
                Vec3d n = h.normalize().multiply(0.12);
                self.addVelocity(n.x, 0, n.z);
                self.velocityDirty = true;
            }
        }

        // Contact damage + knockback (本段仍保持原逻辑)
        double extra = ThreatProfiles.extraReach(self.getType());
        Box reach = self.getBoundingBox().expand(extra);
        if (reach.intersects(target.getBoundingBox())) {
            if (!DamageRouting.contactOnCooldown(self)) {
                DamageRouting.armContactCooldown(self, 10);
                float contact = ThreatProfiles.contactDamage(self.getType());
                target.damage(sw.getDamageSources().mobAttack(self), contact);
                DamageRouting.applyOnHitEffects((PlayerEntity) target, self);
                double dx = target.getX() - self.getX();
                double dz = target.getZ() - self.getZ();
                double len = Math.sqrt(dx*dx + dz*dz);
                if (len > 0.0001) {
                    double nx = dx/len;
                    double nz = dz/len;
                    float kbH = ThreatProfiles.kbH(self.getType());
                    double kbV = ThreatProfiles.kbV(self.getType());
                    target.takeKnockback(kbH, -nx, -nz);
                    if (kbV > 0) {
                        target.addVelocity(0, Math.min(0.9, kbV), 0);
                        target.velocityDirty = true;
                    }
                }
            }
        }
    }
}