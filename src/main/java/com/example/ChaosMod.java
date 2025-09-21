package com.example;

import com.example.config.ChaosModConfig;

/** Minimal bootstrap so references like ChaosMod.config / ChaosMod.MOD_ID resolve. */
public final class ChaosMod {
    public static final String MOD_ID = "chaosmod";
    public static final ChaosModConfig config = new ChaosModConfig();
    private ChaosMod() {}
}