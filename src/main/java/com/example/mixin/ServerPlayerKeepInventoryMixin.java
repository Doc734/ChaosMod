package com.example.mixin;

import com.example.ChaosMod;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Strict rule (when endKeepOverrideEnabled=true):
 *  - Death in the End  : FORCE manual drop once and clear inventory; respawn empty.
 *  - Death in OW/Nether: do NOT drop (handled by PlayerEntity mixin); copy inventory on respawn.
 */
@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerKeepInventoryMixin {

    @Unique private boolean chaos$endDroppedOnce = false;

    /** Force manual drop for End deaths, before vanilla tries to drop inventory. */
    @Inject(method = "onDeath", at = @At("HEAD"))
    private void chaos$forceEndDrop(DamageSource source, CallbackInfo ci) {
        if (!ChaosMod.config.endKeepOverrideEnabled) return;
        ServerPlayerEntity self = (ServerPlayerEntity)(Object)this;
        if (self.getWorld().getRegistryKey() == World.END) {
            chaos$manualDropAll(self);
            self.getInventory().clear();
            chaos$endDroppedOnce = true;
        } else {
            chaos$endDroppedOnce = false;
        }
    }

    /** Helper: drop all inventory items as ItemEntity (manual, to avoid calling protected dropInventory()). */
    @Unique
    private static void chaos$manualDropAll(ServerPlayerEntity self) {
        var inv = self.getInventory();
        for (int i = 0; i < inv.size(); i++) {
            ItemStack st = inv.getStack(i);
            if (!st.isEmpty()) {
                ItemEntity ent = self.dropItem(st.copy(), true, false);
                if (ent != null) { ent.setOwner(self.getUuid()); }
                inv.setStack(i, ItemStack.EMPTY);
            }
        }
        inv.updateItems();
    }

    /** After respawn: copy items ONLY when old player didn't die in the End. */
    @Inject(method = "copyFrom", at = @At("TAIL"))
    private void chaos$copyKeepUnlessEnd(ServerPlayerEntity oldPlayer, boolean alive, CallbackInfo ci) {
        if (!ChaosMod.config.endKeepOverrideEnabled) return;
        ServerPlayerEntity self = (ServerPlayerEntity)(Object)this;
        if (oldPlayer.getWorld().getRegistryKey() != World.END) {
            var src = oldPlayer.getInventory();
            var dst = self.getInventory();
            for (int i = 0; i < src.size(); i++) {
                ItemStack st = src.getStack(i);
                dst.setStack(i, st.copy());
            }
            dst.updateItems();
        } else {
            // died in End -> ensure empty (already cleared in onDeath)
            self.getInventory().clear();
        }
    }

    /** Exposed for PlayerEntityDropInventoryMixin to check if we already dropped. */
    @Unique
    public boolean chaos$didEndDrop() { return chaos$endDroppedOnce; }
}