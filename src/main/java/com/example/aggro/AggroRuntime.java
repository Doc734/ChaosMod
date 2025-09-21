package com.example.aggro;

import net.minecraft.entity.LivingEntity;

/** Compatibility stub for older references. Runtime aggro handled in mixins. */
public final class AggroRuntime {
    private AggroRuntime(){}
    public static void tick(LivingEntity entity) { /* no-op */ }
}