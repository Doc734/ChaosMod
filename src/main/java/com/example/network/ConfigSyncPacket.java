package com.example.network;

import com.example.ChaosMod;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

// Simplified version without networking for now - GUI will work in single player / locally
public class ConfigSyncPacket {
    private final String key;
    private final boolean value;
    
    public ConfigSyncPacket(String key, boolean value) {
        this.key = key;
        this.value = value;
    }
    
    public static ConfigSyncPacket create(String key, boolean value) {
        return new ConfigSyncPacket(key, value);
    }
    
    // Simplified version that directly updates config (works for integrated server)
    public static void updateConfig(String key, boolean value, ServerPlayerEntity player) {
        // Check if player has permission (admin level 4)
        if (player != null && !player.hasPermissionLevel(4)) {
            player.sendMessage(Text.literal(com.example.config.LanguageManager.getMessage("config_permission_denied"))
                .formatted(Formatting.RED, Formatting.BOLD));
            return;
        }
        
        // Update the configuration
        ChaosMod.config.set(key, value);
        
        // Send feedback to player (支持多语言)
        if (player != null) {
            String language = ChaosMod.config.getLanguage();
            String state, updateMsg;
            if ("en_us".equals(language)) {
                state = value ? "✓ Enabled" : "✗ Disabled";
                updateMsg = com.example.config.LanguageManager.getMessage("config_updated");
            } else {
                state = value ? "✓ 启用" : "✗ 禁用";
                updateMsg = com.example.config.LanguageManager.getMessage("config_updated");
            }
            
            player.sendMessage(Text.literal("[" + updateMsg + "] " + key + " -> " + state)
                .formatted(Formatting.YELLOW));
        }
    }
    
    // Placeholder for future full networking implementation
    public static void registerServerReceiver() {
        // TODO: Implement proper networking when needed for dedicated servers
        // For now, GUI works through direct config access in integrated server
    }
    
    public String getKey() {
        return key;
    }
    
    public boolean getValue() {
        return value;
    }
}