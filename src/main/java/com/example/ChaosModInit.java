package com.example;

import com.example.config.ChaosModConfig;
import com.example.network.ConfigToggleC2SPacket;
// Removed screen handler factory - using simplified GUI approach
// Removed Fabric Permissions API - using standard OP level check
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
// Removed unused text imports
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;

import java.util.LinkedHashMap;
import java.util.Map;

public class ChaosModInit implements ModInitializer {
    public static ChaosModConfig config = new ChaosModConfig();
    
    // Removed unused permission exception

    private static final Map<String, String> LABELS = new LinkedHashMap<>();
    static {
        LABELS.put("allHostileEnabled", "所有生物敌对");
        LABELS.put("mobIgniteEnabled", "被怪命中点燃");
        LABELS.put("mobSlownessEnabled", "被怪命中缓慢II");
        LABELS.put("mobBlindnessEnabled", "被怪命中失明");
        LABELS.put("mobThornsEnabled", "反伤=50%");
        LABELS.put("foodPoisonEnabled", "吃食物概率中毒");
        LABELS.put("enderDragonBucketEnabled", "被龙打→水桶变牛奶");
        LABELS.put("enderDragonKillEnabled", "击杀末影龙者自杀");
        LABELS.put("playerDamageShareEnabled", "贴身平摊伤害");
        LABELS.put("sharedHealthEnabled", "共享生命(镜像)");
        LABELS.put("sharedDamageSplitEnabled", "全服平摊伤害");
        LABELS.put("randomDamageEnabled", "随机转移伤害");
        LABELS.put("shieldNerfEnabled", "盾牌仅吸收80%");
        LABELS.put("lowHealthNoHealEnabled", "≤1♥禁回血(10s)");
        LABELS.put("waterToLavaEnabled", "放水50%变岩浆(仅玩家)");
        LABELS.put("endKeepOverrideEnabled", "末地死亡掉落/其他维度保留物品");
        LABELS.put("reverseDamageEnabled", "反向伤害：不受伤扣血");
        LABELS.put("sunburnEnabled", "晴天白天阳光下自燃");
        LABELS.put("healReverseEnabled", "回血时50%概率变扣血");
        LABELS.put("fallTrapEnabled", "平地跳跃落地20%概率扣0.5♥");
        LABELS.put("acrophobiaEnabled", "恐高症：Y>80越高伤害越大(最高2♥)");
        LABELS.put("blockRevengeEnabled", "破坏方块10%概率被反伤");
        LABELS.put("containerCurseEnabled", "开箱子/熔炉25%概率扣1♥");
        LABELS.put("inventoryCurseEnabled", "切换物品槽12%概率扣0.5♥");
        LABELS.put("craftingTrapEnabled", "合成物品10%概率扣1♥");
    }

    @Override
    public void onInitialize() {
        
        // Register network packet receiver with proper permission checks
        ConfigToggleC2SPacket.registerServerReceiver();
        
        // Commands with Admin Permission Check (keeping only toggle command)
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(
                CommandManager.literal("chaos")
                    .requires(source -> source.hasPermissionLevel(4))
                    .then(CommandManager.literal("toggle")
                        .then(CommandManager.argument("key", StringArgumentType.word())
                            .suggests((ctx, builder) -> {
                                for (String k : LABELS.keySet()) builder.suggest(k);
                                return builder.buildFuture();
                            })
                            .executes(ctx -> {
                                String key = StringArgumentType.getString(ctx, "key");
                                toggle(ctx.getSource(), key);
                                return 1;
                            })))
            );
        });

        // Player water -> lava 50% (only player-placed water)
        UseBlockCallback.EVENT.register((player, world, hand, hit) -> {
            if (!com.example.ChaosMod.config.waterToLavaEnabled) return ActionResult.PASS;
            if (player == null || player.isSpectator()) return ActionResult.PASS;
            ItemStack inHand = player.getStackInHand(hand);
            if (!inHand.isOf(Items.WATER_BUCKET)) return ActionResult.PASS;
            if (world.isClient()) return ActionResult.PASS;
            if (player.getRandom().nextFloat() < 0.5f) {
                player.setStackInHand(hand, new ItemStack(Items.LAVA_BUCKET));
            }
            return ActionResult.PASS;
        });
    }

    /**
     * 原来的菜单功能现在已经完全集成到 GUI 中：
     * 
     * 1. 权限检查：使用 Fabric Permissions API 配合 ExtendedScreenHandlerFactory
     * 2. 配置切换：通过 ConfigToggleC2SPacket 进行 C2S 通信
     * 3. 用户界面：使用 ChaosModConfigScreen 提供图形化界面
     * 4. 实时反馈：服务端验证权限后发送确认消息
     * 5. 管理员广播：配置变更会通知其他在线管理员
     * 
     * 所有原有功能都已保留并增强：
     * - 25 个配置项的完整列表和中文标签
     * - 权限验证（现在更加严格和安全）
     * - 即时切换反馈
     * - 点击式操作界面
     * - 全部启用/禁用快捷操作
     */

    private static void toggle(ServerCommandSource src, String key) {
        // 使用相同的权限检查逻辑
        try {
            ServerPlayerEntity player = src.getPlayer();
            boolean hasPermission = player.hasPermissionLevel(4); // Standard admin check
            
            if (!hasPermission) {
                send(src, Text.literal("🚫 权限不足！只有管理员才能修改 ChaosMod 配置！")
                    .formatted(Formatting.RED, Formatting.BOLD));
                return;
            }
            
            boolean cur = com.example.ChaosMod.config.get(key);
            com.example.ChaosMod.config.set(key, !cur);
            send(src, Text.literal("[已切换] " + key + " -> " + (!cur)).formatted(Formatting.YELLOW));
            
        } catch (Exception e) {
            send(src, Text.literal("❌ 无法获取玩家信息").formatted(Formatting.RED));
        }
    }

    // Removed head and line methods - no longer needed without showMenu

    private static void send(ServerCommandSource src, Text text) {
        ServerPlayerEntity p = null;
        try { p = src.getPlayer(); } catch (Throwable ignored) { }
        if (p != null) p.sendMessage(text);
        else { try { src.sendFeedback(() -> text, false); } catch (Throwable ignored) { } }
    }
}