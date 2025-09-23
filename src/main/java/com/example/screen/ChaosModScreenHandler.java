package com.example.screen;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;

public class ChaosModScreenHandler extends ScreenHandler {
    private final boolean hasPermission;
    
    // For client-side (receives permission from server)
    public ChaosModScreenHandler(int syncId, PlayerInventory inventory, boolean hasPermission) {
        super(getScreenHandlerType(), syncId);
        this.hasPermission = hasPermission;
    }
    
    // For server-side
    public ChaosModScreenHandler(int syncId, PlayerInventory inventory, PlayerEntity player) {
        super(getScreenHandlerType(), syncId);
        // Server-side permission check
        this.hasPermission = player instanceof net.minecraft.server.network.ServerPlayerEntity serverPlayer && 
                            serverPlayer.hasPermissionLevel(4);
    }
    
    @Override
    public boolean canUse(PlayerEntity player) {
        return true; // Always allow opening, functionality restricted by permission
    }
    
    @Override
    public net.minecraft.item.ItemStack quickMove(PlayerEntity player, int slot) {
        // No inventory slots in config screen
        return net.minecraft.item.ItemStack.EMPTY;
    }
    
    public boolean hasPermission() {
        return hasPermission;
    }
    
    // This will be set by the registry
    private static ScreenHandlerType<ChaosModScreenHandler> screenHandlerType;
    
    public static void setScreenHandlerType(ScreenHandlerType<ChaosModScreenHandler> type) {
        screenHandlerType = type;
    }
    
    private static ScreenHandlerType<ChaosModScreenHandler> getScreenHandlerType() {
        return screenHandlerType;
    }
}
