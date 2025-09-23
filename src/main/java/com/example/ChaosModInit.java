package com.example;

import com.example.config.ChaosModConfig;
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
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.MutableText;
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
    
    // 权限异常类型
    private static final SimpleCommandExceptionType NO_PERMISSION_EXCEPTION = 
        new SimpleCommandExceptionType(Text.literal("🚫 权限不足！只有管理员才能使用 ChaosMod 指令！")
            .formatted(Formatting.RED, Formatting.BOLD));

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
        
        // Commands with Admin Permission Check
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(
                CommandManager.literal("chaos")
                    .requires(source -> source.hasPermissionLevel(4))
                    .then(CommandManager.literal("menu").executes(ctx -> { showMenu(ctx.getSource()); return 1; }))
                    .then(CommandManager.literal("toggle")
                        .then(CommandManager.argument("key", StringArgumentType.word())
                            .suggests((ctx, builder) -> {
                                for (String k : LABELS.keySet()) builder.suggest(k);
                                return builder.buildFuture();
                            })
                            .executes(ctx -> {
                                String key = StringArgumentType.getString(ctx, "key");
                                toggle(ctx.getSource(), key);
                                showMenu(ctx.getSource());
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

    private static void showMenu(ServerCommandSource src) {
        for (int i = 0; i < 5; i++) send(src, Text.literal(" "));
        send(src, head("ChaosMod 菜单 (点击切换)"));
        for (Map.Entry<String, String> e : LABELS.entrySet()) {
            send(src, line(e.getKey(), e.getValue()));
        }
    }

    private static void toggle(ServerCommandSource src, String key) {
        boolean cur = com.example.ChaosMod.config.get(key);
        com.example.ChaosMod.config.set(key, !cur);
        send(src, Text.literal("[已切换] " + key + " -> " + (!cur)).formatted(Formatting.YELLOW));
    }

    private static MutableText head(String title) {
        return Text.literal("=== " + title + " ===").formatted(Formatting.GOLD, Formatting.BOLD);
    }

    private static MutableText line(String key, String label) {
        boolean on = com.example.ChaosMod.config.get(key);
        String state = on ? "✓ 开启" : "✗ 关闭";
        Formatting color = on ? Formatting.GREEN : Formatting.RED;
        MutableText click = Text.literal("[" + state + "]").formatted(color, Formatting.BOLD)
            .styled(s -> s.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/chaos toggle " + key))
                          .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal("点击切换 " + label))));
        return Text.literal("• ").formatted(Formatting.GRAY)
                .append(Text.literal(label + " ").formatted(Formatting.AQUA))
                .append(click);
    }

    private static void send(ServerCommandSource src, Text text) {
        ServerPlayerEntity p = null;
        try { p = src.getPlayer(); } catch (Throwable ignored) { }
        if (p != null) p.sendMessage(text);
        else { try { src.sendFeedback(() -> text, false); } catch (Throwable ignored) { } }
    }
}