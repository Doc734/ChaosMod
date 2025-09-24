package com.example;

import com.example.gui.ChaosModConfigScreen;
import com.example.screen.ChaosModScreenHandler;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public class ChaosModClient implements ClientModInitializer {
	private static KeyBinding configKeyBinding;
	
	@Override
	public void onInitializeClient() {
		// v1.3.0: Initialize language system on client
		com.example.config.LanguageManager.loadLanguageFromConfig();
		
		// 注册客户端屏幕处理器
		HandledScreens.register(ChaosMod.CHAOS_MOD_SCREEN_HANDLER_TYPE, ChaosModConfigScreen::new);
		
		// Register key binding for opening config menu
		configKeyBinding = KeyBindingHelper.registerKeyBinding(new KeyBinding(
			"key.chaosmod.config_menu",
			InputUtil.Type.KEYSYM,
			GLFW.GLFW_KEY_P, // Default key: P
			"category.chaosmod.general"
		));
		
		// Register client tick event to handle key presses
		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			while (configKeyBinding.wasPressed()) {
				if (client.player != null) {
					// 简化版本：直接打开配置界面
					// 在单人游戏中，玩家默认有管理员权限
					// 在多人游戏中，需要按照服务器权限设置
					boolean hasPermission = client.isInSingleplayer() || 
						(client.player != null && client.player.hasPermissionLevel(4));
					
					client.setScreen(new ChaosModConfigScreen(
						new ChaosModScreenHandler(0, client.player.getInventory(), hasPermission),
						client.player.getInventory(),
						Text.literal("ChaosMod 配置菜单")
					));
				}
			}
		});
	}
}
