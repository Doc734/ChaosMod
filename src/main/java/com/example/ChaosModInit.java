
package com.example;

import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.LinkedHashMap;
import java.util.Map;

public class ChaosModInit implements ModInitializer {

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
    }

    @Override
    public void onInitialize() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(
                CommandManager.literal("chaos")
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
