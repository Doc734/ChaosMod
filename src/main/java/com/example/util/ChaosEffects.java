package com.example.util;

import com.example.ChaosMod;
import com.example.network.KeyDisableS2CPacket;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LightningEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 混沌效果系统 - 实现5个新的混沌效果
 */
public final class ChaosEffects {
    private ChaosEffects() {}

    // === 延迟受伤系统 ===
    public static class DelayedDamage {
        public final DamageSource source;
        public final float amount;
        public final long dueTick;
        public final Entity attacker;
        
        public DelayedDamage(DamageSource source, float amount, long dueTick, Entity attacker) {
            this.source = source;
            this.amount = amount;
            this.dueTick = dueTick;
            this.attacker = attacker;
        }
    }

    // 实体级延迟伤害队列
    private static final Map<LivingEntity, Queue<DelayedDamage>> DELAYED_DAMAGE_QUEUES = new WeakHashMap<>();
    // 递归抑制标志
    private static final ThreadLocal<Boolean> DELAYED_DAMAGE_REENTRY = ThreadLocal.withInitial(() -> Boolean.FALSE);

    // === 按键失灵系统 ===
    private static final Map<PlayerEntity, Integer> DAMAGE_COUNTS = new WeakHashMap<>();
    private static final Map<PlayerEntity, Set<String>> DISABLED_KEYS = new WeakHashMap<>();
    private static final Map<PlayerEntity, Boolean> DEATH_FLAGS = new WeakHashMap<>();

    // === 受伤随机增益系统 ===
    private static final Map<PlayerEntity, Long> LAST_EFFECT_TIME = new WeakHashMap<>();
    private static final int EFFECT_COOLDOWN = 60; // 3秒防刷屏

    // 好效果池
    private static final List<RegistryEntry<StatusEffect>> GOOD_EFFECTS = List.of(
        StatusEffects.SPEED,           // 速度
        StatusEffects.HASTE,           // 急迫  
        StatusEffects.STRENGTH,        // 力量
        StatusEffects.INSTANT_HEALTH,  // 瞬间治疗
        StatusEffects.JUMP_BOOST,      // 跳跃提升
        StatusEffects.REGENERATION,    // 生命恢复
        StatusEffects.RESISTANCE,      // 抗性提升
        StatusEffects.FIRE_RESISTANCE, // 抗火
        StatusEffects.WATER_BREATHING, // 水下呼吸
        StatusEffects.INVISIBILITY,    // 隐身
        StatusEffects.NIGHT_VISION,    // 夜视
        StatusEffects.HEALTH_BOOST,    // 生命提升
        StatusEffects.ABSORPTION,      // 伤害吸收
        StatusEffects.SATURATION,      // 饱和
        StatusEffects.GLOWING,         // 发光(相对好)
        StatusEffects.LUCK,            // 幸运
        StatusEffects.SLOW_FALLING,    // 缓降
        StatusEffects.CONDUIT_POWER,   // 潮涌能量
        StatusEffects.DOLPHINS_GRACE,  // 海豚的恩惠
        StatusEffects.HERO_OF_THE_VILLAGE // 村庄英雄
    );

    // 坏效果池
    private static final List<RegistryEntry<StatusEffect>> BAD_EFFECTS = List.of(
        StatusEffects.SLOWNESS,        // 缓慢
        StatusEffects.MINING_FATIGUE,  // 挖掘疲劳
        StatusEffects.INSTANT_DAMAGE,  // 瞬间伤害
        StatusEffects.NAUSEA,          // 反胃
        StatusEffects.BLINDNESS,       // 失明
        StatusEffects.HUNGER,          // 饥饿
        StatusEffects.WEAKNESS,        // 虚弱
        StatusEffects.POISON,          // 中毒
        StatusEffects.WITHER,          // 凋零
        StatusEffects.LEVITATION,      // 飘浮(相对坏)
        StatusEffects.UNLUCK,          // 霉运
        StatusEffects.BAD_OMEN,        // 不祥之兆
        StatusEffects.DARKNESS,        // 黑暗
        StatusEffects.INFESTED,        // 感染
        StatusEffects.OOZING,          // 渗浆
        StatusEffects.WEAVING,         // 盘丝
        StatusEffects.WIND_CHARGED,    // 蓄风
        StatusEffects.RAID_OMEN,       // 袭击之兆
        StatusEffects.TRIAL_OMEN       // 试炼之兆
    );

    // === 伤害背锅人系统 ===
    private static ServerPlayerEntity currentScapegoat = null;
    private static long lastScapegoatTime = 0;
    private static final long SCAPEGOAT_INTERVAL = 5 * 60 * 20; // 5分钟 = 6000 ticks

    // === 痛觉扩散系统 ===
    private static final Map<PlayerEntity, Long> ELECTRIFIED_PLAYERS = new WeakHashMap<>(); 
    private static final Map<PlayerEntity, Set<PlayerEntity>> LIGHTNING_COOLDOWNS = new WeakHashMap<>();
    private static final int ELECTRIFIED_DURATION = 100; // 5秒 = 100 ticks
    private static final double SPREAD_RADIUS = 3.5; // 扩散半径
    private static final int LIGHTNING_COOLDOWN = 20; // 1秒防重复

    /**
     * 延迟受伤：拦截LivingEntity#damage并将伤害入队
     */
    public static boolean interceptDelayedDamage(LivingEntity entity, DamageSource source, float amount) {
        if (!ChaosMod.config.delayedDamageEnabled) return false;
        if (DELAYED_DAMAGE_REENTRY.get()) return false; // 防止递归
        if (entity.getWorld().isClient()) return false;

        // 计算延迟时间：0-5秒 = 0-100 ticks
        long currentTick = entity.getWorld().getTime();
        long delay = ThreadLocalRandom.current().nextLong(0, 101); // 0-100 ticks
        long dueTick = currentTick + delay;

        // 获取攻击者
        Entity attacker = source.getAttacker();

        // 创建延迟伤害
        DelayedDamage delayedDamage = new DelayedDamage(source, amount, dueTick, attacker);

        // 添加到队列
        DELAYED_DAMAGE_QUEUES.computeIfAbsent(entity, k -> new LinkedList<>()).offer(delayedDamage);

        return true; // 取消当前伤害
    }

    /**
     * 延迟受伤：在实体tick时处理延迟伤害队列
     */
    public static void tickDelayedDamage(LivingEntity entity) {
        if (!ChaosMod.config.delayedDamageEnabled) return;
        if (entity.getWorld().isClient()) return;

        Queue<DelayedDamage> queue = DELAYED_DAMAGE_QUEUES.get(entity);
        if (queue == null || queue.isEmpty()) return;

        long currentTick = entity.getWorld().getTime();

        // 处理到期的延迟伤害
        while (!queue.isEmpty()) {
            DelayedDamage delayed = queue.peek();
            if (delayed.dueTick > currentTick) break; // 还没到时间

            queue.poll(); // 移除已处理的

            // 检查实体是否仍然存活和在线
            if (entity.isDead() || entity.isRemoved()) {
                queue.clear(); // 清空队列
                break;
            }

            // 应用延迟伤害
            try {
                DELAYED_DAMAGE_REENTRY.set(Boolean.TRUE);
                entity.damage(delayed.source, delayed.amount);
            } finally {
                DELAYED_DAMAGE_REENTRY.set(Boolean.FALSE);
            }
        }

        // 如果实体死亡或离线，清理队列
        if (entity.isDead() || entity.isRemoved()) {
            DELAYED_DAMAGE_QUEUES.remove(entity);
        }
    }

    /**
     * 按键失灵：累计受伤次数并触发按键禁用
     */
    public static void handleKeyDisable(PlayerEntity player) {
        if (!ChaosMod.config.keyDisableEnabled) return;
        if (player.getWorld().isClient()) return;
        if (!(player instanceof ServerPlayerEntity serverPlayer)) return;
        
        // 检查死亡标记，防止死亡动画期误触发
        if (isPlayerMarkedDead(player)) {
            return;
        }

        // 累积受伤次数
        int count = DAMAGE_COUNTS.getOrDefault(player, 0) + 1;
        DAMAGE_COUNTS.put(player, count);


        // 检查是否达到10的倍数
        if (count % 10 == 0) {
            // 随机选择一个常用键禁用
            String[] keys = {"forward", "back", "left", "right", "jump", "sprint", "attack", "use", "drop", "sneak"};
            String keyToDisable = keys[ThreadLocalRandom.current().nextInt(keys.length)];

            // 添加到禁用列表
            Set<String> disabledKeysSet = DISABLED_KEYS.computeIfAbsent(player, k -> new HashSet<>());
            disabledKeysSet.add(keyToDisable);

            // 发送网络包到客户端
            KeyDisableS2CPacket.send(serverPlayer, new HashSet<>(disabledKeysSet));

            // 通知玩家
            String keyName = getKeyDisplayName(keyToDisable);
            serverPlayer.sendMessage(Text.literal("⚡ 按键失灵！" + keyName + " 键已被禁用！死亡后恢复。")
                .formatted(Formatting.RED, Formatting.BOLD), true);
        }
    }

    /**
     * 按键失灵：玩家死亡时设置死亡标记（防止死亡动画期误触发）
     */
    public static void markPlayerDead(PlayerEntity player) {
        if (player instanceof ServerPlayerEntity) {
            DEATH_FLAGS.put(player, true);
        }
    }

    /**
     * 按键失灵：玩家复活时完全重置（真正的重置时机）
     */
    public static void resetOnRespawn(ServerPlayerEntity player) {
        try {
            // 清理死亡标记
            DEATH_FLAGS.remove(player);
            
            // 清理状态
            DAMAGE_COUNTS.remove(player);
            DISABLED_KEYS.remove(player);
            
            // 发送重置包给客户端
            KeyDisableS2CPacket.send(player, new HashSet<>());
                
        } catch (Exception e) {
            // 静默处理错误
        }
    }

    /**
     * 检查玩家是否处于死亡状态（防止死亡动画期误触发）
     */
    public static boolean isPlayerMarkedDead(PlayerEntity player) {
        return DEATH_FLAGS.getOrDefault(player, false);
    }

    /**
     * 清除玩家的死亡标记
     */
    public static void clearDeathFlag(PlayerEntity player) {
        DEATH_FLAGS.remove(player);
    }

    
    /**
     * 同步按键禁用状态给客户端
     */
    public static void syncKeyDisableState(ServerPlayerEntity player) {
        if (!ChaosMod.config.keyDisableEnabled) {
            // 如果功能被禁用，确保客户端没有按键被禁用
            KeyDisableS2CPacket.send(player, new HashSet<>());
            return;
        }
        
        // 同步当前的禁用状态
        Set<String> disabled = DISABLED_KEYS.get(player);
        if (disabled == null) {
            disabled = new HashSet<>();
        }
        
        try {
            KeyDisableS2CPacket.send(player, new HashSet<>(disabled));
        } catch (Exception e) {
            // 静默处理错误
        }
    }

    /**
     * 获取按键显示名称
     */
    private static String getKeyDisplayName(String key) {
        return switch (key) {
            case "forward" -> "前进(W)";
            case "back" -> "后退(S)";
            case "left" -> "左移(A)";
            case "right" -> "右移(D)";
            case "jump" -> "跳跃(空格)";
            case "sprint" -> "冲刺(Ctrl)";
            case "attack" -> "攻击(左键)";
            case "use" -> "使用(右键)";
            case "drop" -> "丢弃(Q)";
            case "sneak" -> "潜行(Shift)";
            default -> key;
        };
    }

    /**
     * 受伤随机增益：随机添加或移除状态效果
     */
    public static void handleRandomEffects(LivingEntity entity) {
        if (!ChaosMod.config.randomEffectsEnabled) return;
        if (entity.getWorld().isClient()) return;
        if (!(entity instanceof PlayerEntity player)) return;

        // 冷却检查防刷屏
        long currentTime = entity.getWorld().getTime();
        Long lastTime = LAST_EFFECT_TIME.get(player);
        if (lastTime != null && currentTime - lastTime < EFFECT_COOLDOWN) return;
        
        LAST_EFFECT_TIME.put(player, currentTime);

        // 50%概率选择好效果，50%选择坏效果
        List<RegistryEntry<StatusEffect>> effectPool = ThreadLocalRandom.current().nextBoolean() ? GOOD_EFFECTS : BAD_EFFECTS;
        RegistryEntry<StatusEffect> effect = effectPool.get(ThreadLocalRandom.current().nextInt(effectPool.size()));

        // 检查是否已有该效果
        if (player.hasStatusEffect(effect)) {
            // 如果有，移除它
            player.removeStatusEffect(effect);
            if (player instanceof ServerPlayerEntity serverPlayer) {
                serverPlayer.sendMessage(Text.literal("🔴 失去了 " + getEffectName(effect) + " 效果")
                    .formatted(Formatting.YELLOW), true);
            }
        } else {
            // 如果没有，添加它
            int duration = ThreadLocalRandom.current().nextInt(300, 1201); // 15-60秒
            int amplifier = ThreadLocalRandom.current().nextInt(0, 3); // 0-2级
            
            // 特殊处理瞬间效果
            if (effect.equals(StatusEffects.INSTANT_HEALTH) || effect.equals(StatusEffects.INSTANT_DAMAGE)) {
                duration = 1; // 瞬间效果
                amplifier = 0; // 固定0级
            }
            
            player.addStatusEffect(new StatusEffectInstance(effect, duration, amplifier));
            if (player instanceof ServerPlayerEntity serverPlayer) {
                serverPlayer.sendMessage(Text.literal("🟢 获得了 " + getEffectName(effect) + " 效果")
                    .formatted(Formatting.GREEN), true);
            }
        }
    }

    /**
     * 获取效果名称
     */
    private static String getEffectName(RegistryEntry<StatusEffect> effect) {
        // 这里可以返回中文名称，简化版本直接返回英文
        return effect.value().getName().getString();
    }

    /**
     * 伤害背锅人：服务器tick时更新背锅人
     */
    public static void tickScapegoat(MinecraftServer server) {
        if (!ChaosMod.config.damageScapegoatEnabled) return;

        long currentTime = server.getOverworld().getTime();
        
        // 检查是否需要选择新的背锅人
        if (currentTime - lastScapegoatTime >= SCAPEGOAT_INTERVAL) {
            selectNewScapegoat(server);
            lastScapegoatTime = currentTime;
        }

        // 检查当前背锅人是否仍然在线
        if (currentScapegoat != null && (currentScapegoat.isDisconnected() || currentScapegoat.isRemoved())) {
            currentScapegoat = null; // 清除离线的背锅人
        }
    }

    /**
     * 选择新的背锅人
     */
    private static void selectNewScapegoat(MinecraftServer server) {
        List<ServerPlayerEntity> players = server.getPlayerManager().getPlayerList();
        if (players.isEmpty()) return;

        // 过滤掉上一个背锅人
        List<ServerPlayerEntity> candidates = new ArrayList<>();
        for (ServerPlayerEntity player : players) {
            if (player != currentScapegoat && !player.isDisconnected()) {
                candidates.add(player);
            }
        }

        if (candidates.isEmpty()) {
            candidates = new ArrayList<>(players); // 如果没有其他人，包括上次的背锅人
        }

        if (!candidates.isEmpty()) {
            currentScapegoat = candidates.get(ThreadLocalRandom.current().nextInt(candidates.size()));
            
            // 广播模糊警告
            Text warning = Text.literal("⚠️ 有人成为了伤害背锅人...").formatted(Formatting.DARK_RED, Formatting.BOLD);
            for (ServerPlayerEntity player : players) {
                player.sendMessage(warning, true);
            }
        }
    }

    /**
     * 伤害背锅人：重定向伤害
     */
    public static boolean redirectDamageToScapegoat(LivingEntity victim, DamageSource source, float amount) {
        if (!ChaosMod.config.damageScapegoatEnabled) return false;
        if (victim.getWorld().isClient()) return false;
        if (currentScapegoat == null) return false;
        if (victim == currentScapegoat) return false; // 避免循环

        try {
            // 重定向伤害到背锅人
            currentScapegoat.damage(source, amount);
            
            // 给背锅人发送提示
            currentScapegoat.sendMessage(Text.literal("💥 你替别人承受了伤害！")
                .formatted(Formatting.RED), true);
                
            return true; // 取消原始伤害
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 痛觉扩散：标记被打的玩家为"带电"
     */
    public static void markElectrified(LivingEntity entity) {
        if (!ChaosMod.config.painSpreadEnabled) return;
        if (entity.getWorld().isClient()) return;
        if (!(entity instanceof PlayerEntity player)) return;

        long currentTime = entity.getWorld().getTime();
        ELECTRIFIED_PLAYERS.put(player, currentTime + ELECTRIFIED_DURATION);
        
        if (player instanceof ServerPlayerEntity serverPlayer) {
            serverPlayer.sendMessage(Text.literal("⚡ 你带电了！5秒内靠近你的人会被雷劈！")
                .formatted(Formatting.YELLOW, Formatting.BOLD), true);
        }
    }

    /**
     * 痛觉扩散：tick处理带电玩家
     */
    public static void tickElectrified(PlayerEntity player) {
        if (!ChaosMod.config.painSpreadEnabled) return;
        if (player.getWorld().isClient()) return;
        if (!(player instanceof ServerPlayerEntity serverPlayer)) return;

        Long electrifiedUntil = ELECTRIFIED_PLAYERS.get(player);
        if (electrifiedUntil == null) return;

        long currentTime = player.getWorld().getTime();
        
        // 检查是否已经过期
        if (currentTime >= electrifiedUntil) {
            ELECTRIFIED_PLAYERS.remove(player);
            LIGHTNING_COOLDOWNS.remove(player);
            serverPlayer.sendMessage(Text.literal("✅ 带电状态已结束")
                .formatted(Formatting.GREEN), true);
            return;
        }

        // 每10 tick检查一次周围玩家
        if (currentTime % 10 != 0) return;

        // 搜索半径内的其他玩家
        ServerWorld world = serverPlayer.getServerWorld();
        Vec3d pos = player.getPos();
        Box searchBox = new Box(pos.x - SPREAD_RADIUS, pos.y - SPREAD_RADIUS, pos.z - SPREAD_RADIUS,
                               pos.x + SPREAD_RADIUS, pos.y + SPREAD_RADIUS, pos.z + SPREAD_RADIUS);

        List<PlayerEntity> nearbyPlayers = world.getEntitiesByClass(PlayerEntity.class, searchBox, 
            p -> p != player && p.squaredDistanceTo(player) <= SPREAD_RADIUS * SPREAD_RADIUS);

        Set<PlayerEntity> cooldownSet = LIGHTNING_COOLDOWNS.computeIfAbsent(player, k -> new HashSet<>());
        
        for (PlayerEntity nearbyPlayer : nearbyPlayers) {
            // 检查冷却
            if (cooldownSet.contains(nearbyPlayer)) continue;
            
            // 添加到冷却列表
            cooldownSet.add(nearbyPlayer);
            
            // 生成雷击
            LightningEntity lightning = EntityType.LIGHTNING_BOLT.create(world);
            if (lightning != null) {
                lightning.refreshPositionAfterTeleport(nearbyPlayer.getX(), nearbyPlayer.getY(), nearbyPlayer.getZ());
                lightning.setCosmetic(false); // 造成真实伤害
                world.spawnEntity(lightning);
            }
            
            // 发送消息
            if (nearbyPlayer instanceof ServerPlayerEntity nearbyServerPlayer) {
                nearbyServerPlayer.sendMessage(Text.literal("⚡ 你被带电的玩家雷劈了！")
                    .formatted(Formatting.RED), true);
            }
        }

        // 清理过期的冷却
        if (currentTime % 20 == 0) { // 每秒清理一次
            cooldownSet.clear();
        }
    }

    /**
     * 获取当前背锅人（用于调试）
     */
    public static ServerPlayerEntity getCurrentScapegoat() {
        return currentScapegoat;
    }

    /**
     * 检查玩家是否被禁用某个按键
     */
    public static boolean isKeyDisabled(PlayerEntity player, String key) {
        Set<String> disabled = DISABLED_KEYS.get(player);
        return disabled != null && disabled.contains(key);
    }

}
