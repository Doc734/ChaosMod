package com.example.mixin;

import com.example.ChaosMod;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.GoalSelector;
import net.minecraft.entity.ai.goal.PrioritizedGoal;
import net.minecraft.entity.mob.MobEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Iterator;
import java.util.Locale;
import java.util.Set;

@Mixin(MobEntity.class)
public abstract class MobEntityInitGoalsMixin {

    @Shadow protected GoalSelector goalSelector;
    @Shadow protected GoalSelector targetSelector;

    @Inject(method = "initGoals", at = @At("TAIL"))
    private void chaos$stripEscapeGoals(CallbackInfo ci) {
        if (!ChaosMod.config.allHostileEnabled) return;
        try {
            Set<PrioritizedGoal> set = ((GoalSelectorAccessor)(Object)this.goalSelector).getGoals();
            Iterator<PrioritizedGoal> it = set.iterator();
            while (it.hasNext()) {
                Goal g = it.next().getGoal();
                String n = g.getClass().getSimpleName().toLowerCase(Locale.ROOT);
                if (n.contains("panic") || n.contains("avoid") || n.contains("flee") || n.contains("escape") ||
                    n.contains("restrictsun") || n.contains("sun") || n.contains("tempt")) {
                    it.remove();
                }
            }
        } catch (Throwable ignored) { }
    }
}