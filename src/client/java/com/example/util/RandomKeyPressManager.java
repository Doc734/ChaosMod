package com.example.util;

import com.example.network.RandomKeyPressC2SPacket;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.network.PacketByteBuf;

import java.util.*;

/**
 * 电击中毒管理器
 * 使用ClientTickEvents驱动2分钟固定计时器
 */
public class RandomKeyPressManager {
    
    private static final Random RANDOM = new Random();
    
    // 计时器状态
    private static int tickTimer = 0;
    private static int nextTriggerTicks = 0;
    private static boolean isEffectActive = false;
    
    /**
     * 初始化电击中毒系统
     */
    public static void initialize() {
        resetTimer();
        
        // 注册客户端Tick事件
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null || client.world == null) return;
            
            // 效果开关检查
            if (!com.example.ChaosMod.config.randomKeyPressEnabled) {
                if (isEffectActive) {
                    cleanup();
                }
                return;
            }
            
            isEffectActive = true;
            tickTimer++;
            
            // 检查是否到达触发时间
            if (tickTimer >= nextTriggerTicks) {
                triggerPoisonEffect();
                resetTimer();
            }
        });
    }
    
    /**
     * 重置计时器（2分钟固定间隔）
     */
    private static void resetTimer() {
        tickTimer = 0;
        // 2分钟 = 2400 ticks (20 ticks/秒)
        nextTriggerTicks = 2400;
        System.out.println("电击中毒计时器重置：" + nextTriggerTicks + " ticks (2分钟)");
    }
    
    /**
     * 触发电击中毒效果
     */
    private static void triggerPoisonEffect() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;
        
        // 排除创造/旁观模式
        if (client.player.isCreative() || client.player.isSpectator()) {
            return;
        }
        
        System.out.println("触发电击中毒效果");
        
        // 向服务端发送C2S数据包
        sendRandomKeyPressPacket();
    }
    
    /**
     * 发送C2S数据包到服务端
     */
    private static void sendRandomKeyPressPacket() {
        if (ClientPlayNetworking.canSend(RandomKeyPressC2SPacket.ID)) {
            ClientPlayNetworking.send(new RandomKeyPressC2SPacket());
        }
    }
    
    
    /**
     * 清理所有状态（断线时调用）
     */
    public static void cleanup() {
        tickTimer = 0;
        isEffectActive = false;
        System.out.println("电击中毒系统已清理");
    }
}
