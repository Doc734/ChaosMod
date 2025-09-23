package com.example;

import com.example.config.ChaosModConfig;
import com.example.screen.ChaosModScreenHandler;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.Identifier;

/** Minimal bootstrap so references like ChaosMod.config / ChaosMod.MOD_ID resolve. */
public final class ChaosMod {
    public static final String MOD_ID = "chaosmod";
    public static final ChaosModConfig config = new ChaosModConfig();
    
    // 注册 ExtendedScreenHandler - 使用原版注册方式
    public static final ScreenHandlerType<ChaosModScreenHandler> CHAOS_MOD_SCREEN_HANDLER_TYPE =
        Registry.register(Registries.SCREEN_HANDLER, Identifier.of(MOD_ID, "chaos_mod_config"),
            new ScreenHandlerType<>((syncId, inventory) -> 
                new ChaosModScreenHandler(syncId, inventory, false), null));
    
    static {
        // 设置 ScreenHandlerType 引用
        ChaosModScreenHandler.setScreenHandlerType(CHAOS_MOD_SCREEN_HANDLER_TYPE);
    }
    
    private ChaosMod() {}
}