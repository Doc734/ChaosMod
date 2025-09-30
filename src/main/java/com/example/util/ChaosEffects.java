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
import net.minecraft.world.Heightmap;
import net.minecraft.registry.tag.FluidTags;

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

    // === 惊惧磁铁系统 ===
    private static final Map<PlayerEntity, Long> PANIC_MAGNETIZED_PLAYERS = new WeakHashMap<>();
    private static final Map<PlayerEntity, Long> PANIC_MAGNET_IMMUNITY = new WeakHashMap<>(); // 磁化免疫
    private static final int PANIC_MAGNET_DURATION = 200; // 10秒 = 200 ticks
    private static final int PANIC_IMMUNITY_DURATION = 200; // 免疫持续时间，与磁化时长一致
    private static final double PANIC_MAGNET_RADIUS = 30.0; // 30格范围
    private static final ThreadLocal<Boolean> PANIC_MAGNET_REENTRY = ThreadLocal.withInitial(() -> false); // 递归抑制

    // === 眩晕背锅侠系统 ===
    private static ServerPlayerEntity vertigoScapegoat = null;
    private static long nextVertigoRollTick = 0;
    private static final Set<ServerPlayerEntity> visitedScapegoats = new HashSet<>();
    private static final long VERTIGO_SCAPEGOAT_INTERVAL = 6000; // 5分钟 = 6000 ticks

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
            serverPlayer.sendMessage(Text.literal("⚡ " + 
                String.format(com.example.config.LanguageManager.getMessage("key_disabled"), keyName))
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
            
            // 清理常规按键失灵状态
            DAMAGE_COUNTS.remove(player);
            DISABLED_KEYS.remove(player);
            
            // 检查是否有控制癫痫Plus活跃
            boolean hasControlSeizure = CONTROL_SEIZURE_END_TIME.containsKey(player);
            
            if (hasControlSeizure) {
                // 如果有控制癫痫Plus，只发送该效果的禁用键
                String disabledKey = CONTROL_SEIZURE_DISABLED_KEY.get(player);
                if (disabledKey != null) {
                    Set<String> onlySeizureKey = new HashSet<>();
                    onlySeizureKey.add(disabledKey);
                    KeyDisableS2CPacket.send(player, onlySeizureKey);
                } else {
                    KeyDisableS2CPacket.send(player, new HashSet<>());
                }
            } else {
                // 没有控制癫痫Plus，完全重置
                KeyDisableS2CPacket.send(player, new HashSet<>());
            }
                
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
                serverPlayer.sendMessage(Text.literal("🔴 " + 
                    String.format(com.example.config.LanguageManager.getMessage("lost_effect"), getEffectName(effect)))
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
                serverPlayer.sendMessage(Text.literal("🟢 " + 
                    String.format(com.example.config.LanguageManager.getMessage("gained_effect"), getEffectName(effect)))
                    .formatted(Formatting.GREEN), true);
            }
        }
    }

    /**
     * 获取效果名称
     */
    /**
     * 获取状态效果的本地化名称
     * 支持中英文效果名称显示
     */
    private static String getEffectName(RegistryEntry<StatusEffect> effect) {
        String language = com.example.ChaosMod.config.getLanguage();
        String effectKey = effect.getIdAsString();
        
        // 简化映射：主要状态效果的中英文名称
        if ("zh_cn".equals(language)) {
            return switch (effectKey) {
                case "minecraft:speed" -> "速度";
                case "minecraft:slowness" -> "缓慢";
                case "minecraft:haste" -> "急迫";
                case "minecraft:mining_fatigue" -> "挖掘疲劳";
                case "minecraft:strength" -> "力量";
                case "minecraft:instant_health" -> "瞬间治疗";
                case "minecraft:instant_damage" -> "瞬间伤害";
                case "minecraft:jump_boost" -> "跳跃提升";
                case "minecraft:nausea" -> "反胃";
                case "minecraft:regeneration" -> "生命恢复";
                case "minecraft:resistance" -> "抗性提升";
                case "minecraft:fire_resistance" -> "抗火";
                case "minecraft:water_breathing" -> "水下呼吸";
                case "minecraft:invisibility" -> "隐身";
                case "minecraft:blindness" -> "失明";
                case "minecraft:night_vision" -> "夜视";
                case "minecraft:hunger" -> "饥饿";
                case "minecraft:weakness" -> "虚弱";
                case "minecraft:poison" -> "中毒";
                case "minecraft:wither" -> "凋零";
                case "minecraft:health_boost" -> "生命提升";
                case "minecraft:absorption" -> "伤害吸收";
                case "minecraft:saturation" -> "饱和";
                case "minecraft:glowing" -> "发光";
                case "minecraft:levitation" -> "飘浮";
                case "minecraft:luck" -> "幸运";
                case "minecraft:unluck" -> "霉运";
                case "minecraft:slow_falling" -> "缓降";
                case "minecraft:conduit_power" -> "潮涌能量";
                case "minecraft:dolphins_grace" -> "海豚的恩惠";
                case "minecraft:bad_omen" -> "不祥之兆";
                case "minecraft:hero_of_the_village" -> "村庄英雄";
                case "minecraft:darkness" -> "黑暗";
                default -> effect.value().getName().getString(); // 未知效果返回原名
            };
        } else {
            // 英文直接返回原名
            return effect.value().getName().getString();
        }
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
            
            // 广播模糊警告（支持多语言）
            Text warning = Text.literal(com.example.config.LanguageManager.getMessage("damage_scapegoat_selected"))
                .formatted(Formatting.DARK_RED, Formatting.BOLD);
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
            
            // 给背锅人发送提示（支持多语言）
            currentScapegoat.sendMessage(Text.literal("💥 " + com.example.config.LanguageManager.getMessage("damage_absorbed"))
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
            serverPlayer.sendMessage(Text.literal("⚡ " + com.example.config.LanguageManager.getMessage("electrified"))
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
            serverPlayer.sendMessage(Text.literal("✅ " + com.example.config.LanguageManager.getMessage("electrified_ended"))
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
                nearbyServerPlayer.sendMessage(Text.literal("⚡ " + 
                    String.format(com.example.config.LanguageManager.getMessage("struck_by_lightning"), 
                    serverPlayer.getName().getString()))
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

    // ==================== 新增的三个混沌效果 ====================

    /**
     * 惊惧磁铁：标记受伤玩家进入磁化状态
     * 仅当玩家无免疫且伤害源非PANIC_PULL时才生效
     */
    public static void markPanicMagnetized(LivingEntity entity, DamageSource source) {
        if (!ChaosMod.config.panicMagnetEnabled) return;
        if (entity.getWorld().isClient()) return;
        if (!(entity instanceof ServerPlayerEntity player)) return;
        if (PANIC_MAGNET_REENTRY.get()) return; // 递归抑制

        // 检查是否有磁化免疫
        long currentTime = entity.getWorld().getTime();
        Long immunityUntil = PANIC_MAGNET_IMMUNITY.get(player);
        if (immunityUntil != null && currentTime < immunityUntil) {
            return; // 有免疫，不进入磁化状态
        }

        // 检查伤害源是否为PANIC_PULL（通过检查攻击者是否为磁化状态的玩家）
        if (source.getAttacker() instanceof ServerPlayerEntity attacker) {
            Long attackerMagnetized = PANIC_MAGNETIZED_PLAYERS.get(attacker);
            if (attackerMagnetized != null && currentTime < attackerMagnetized) {
                // 这是由磁化玩家造成的PANIC_PULL伤害，不触发新的磁化
                return;
            }
        }

        PANIC_MAGNETIZED_PLAYERS.put(player, currentTime + PANIC_MAGNET_DURATION);
        
        player.sendMessage(Text.literal("⚡ " + com.example.config.LanguageManager.getMessage("magnetized"))
            .formatted(Formatting.RED, Formatting.BOLD), true);
    }

    /**
     * 惊惧磁铁：tick处理磁化玩家
     */
    public static void tickPanicMagnet(ServerPlayerEntity player) {
        if (!ChaosMod.config.panicMagnetEnabled) return;
        if (player.getWorld().isClient()) return;

        Long magnetizedUntil = PANIC_MAGNETIZED_PLAYERS.get(player);
        if (magnetizedUntil == null) return;

        long currentTime = player.getWorld().getTime();
        
        // 检查是否已经过期
        if (currentTime >= magnetizedUntil) {
            PANIC_MAGNETIZED_PLAYERS.remove(player);
            player.sendMessage(Text.literal("✅ " + com.example.config.LanguageManager.getMessage("magnetized_ended"))
                .formatted(Formatting.GREEN), true);
            return;
        }

        // 清理过期的免疫状态
        cleanupExpiredImmunity(currentTime);

        // 每2秒触发一次 (40 ticks)
        if (player.age % 40 != 0) return;

        ServerWorld world = player.getServerWorld();

        // 寻找最近的有效队友并拉拽
        List<ServerPlayerEntity> validTargets = world.getServer().getPlayerManager().getPlayerList().stream()
            .filter(p -> p != player && p.getWorld() == player.getWorld())
            .filter(p -> p.distanceTo(player) <= PANIC_MAGNET_RADIUS)
            .filter(p -> isValidPanicTarget(p, currentTime)) // 过滤未免疫且未磁化的目标
            .sorted((p1, p2) -> Float.compare(p1.distanceTo(player), p2.distanceTo(player)))
            .toList();

        if (!validTargets.isEmpty()) {
            ServerPlayerEntity target = validTargets.get(0);
            
            // 传送到磁化玩家身边
            target.teleport(player.getX(), player.getY(), player.getZ(), true);
            
            // 施加磁化免疫（防止连锁反应）
            PANIC_MAGNET_IMMUNITY.put(target, currentTime + PANIC_IMMUNITY_DURATION);
            
            // 定向单播Title消息（与拉取伤害同tick发送）
            // 给磁化者发送"别靠近我！"（支持多语言）
            Text magnetTitle = Text.literal(com.example.config.LanguageManager.getMessage("stay_away"))
                .formatted(Formatting.RED, Formatting.BOLD);
            player.sendMessage(magnetTitle, true); // 发送到ActionBar
            
            // 给被拉者发送"玩家名：别靠近我！"（支持多语言）
            Text targetTitle = Text.literal(player.getName().getString() + "：" + 
                com.example.config.LanguageManager.getMessage("stay_away"))
                .formatted(Formatting.RED, Formatting.BOLD);
            target.sendMessage(targetTitle, true); // 发送到ActionBar
            
            // 对被拉拽的玩家造成0.5♥PANIC_PULL伤害（递归抑制）
            try {
                PANIC_MAGNET_REENTRY.set(true);
                // 使用玩家作为伤害源，在markPanicMagnetized中通过攻击者识别为PANIC_PULL
                target.damage(world.getDamageSources().playerAttack(player), 1.0F);
            } finally {
                PANIC_MAGNET_REENTRY.set(false);
            }
            
            target.sendMessage(Text.literal("💀 " + com.example.config.LanguageManager.getMessage("pulled_by_magnet"))
                .formatted(Formatting.YELLOW), false); // 改为聊天消息，避免与Title重叠
        } else {
            // 如果没有有效目标，只给磁化者发送Title（支持多语言）
            Text magnetTitle = Text.literal(com.example.config.LanguageManager.getMessage("stay_away"))
                .formatted(Formatting.RED, Formatting.BOLD);
            player.sendMessage(magnetTitle, true); // 发送到ActionBar
        }
    }

    /**
     * 检查玩家是否为有效的惊惧磁铁目标
     */
    private static boolean isValidPanicTarget(ServerPlayerEntity player, long currentTime) {
        // 检查是否有磁化免疫
        Long immunityUntil = PANIC_MAGNET_IMMUNITY.get(player);
        if (immunityUntil != null && currentTime < immunityUntil) {
            return false; // 有免疫，不是有效目标
        }

        // 检查是否已经磁化
        Long magnetizedUntil = PANIC_MAGNETIZED_PLAYERS.get(player);
        if (magnetizedUntil != null && currentTime < magnetizedUntil) {
            return false; // 已磁化，不是有效目标
        }

        return true; // 有效目标
    }

    /**
     * 清理过期的磁化免疫状态
     */
    private static void cleanupExpiredImmunity(long currentTime) {
        PANIC_MAGNET_IMMUNITY.entrySet().removeIf(entry -> currentTime >= entry.getValue());
    }

    /**
     * 贪婪吸血：物品拾取后扣血
     */
    public static void handlePickupDrain(ServerPlayerEntity player) {
        if (!ChaosMod.config.pickupDrainEnabled) return;
        if (player.getWorld().isClient()) return;
        if (player.isCreative() || player.isSpectator()) return;

        // 对拾取物品的玩家造成0.5♥伤害
        player.damage(player.getServerWorld().getDamageSources().magic(), 1.0F);
        
        player.sendMessage(Text.literal("⚡ " + com.example.config.LanguageManager.getMessage("greed_penalty"))
            .formatted(Formatting.RED), true);
    }

    /**
     * 眩晕背锅侠：服务器tick时管理背锅侠系统
     */
    public static void tickVertigoScapegoat(MinecraftServer server) {
        if (!ChaosMod.config.vertigoScapegoatEnabled) return;

        long currentTick = server.getOverworld().getTime();
        
        // 检查是否需要选择新的背锅侠
        if (currentTick >= nextVertigoRollTick) {
            selectNewVertigoScapegoat(server);
            nextVertigoRollTick = currentTick + VERTIGO_SCAPEGOAT_INTERVAL;
        }

        // 检查当前背锅侠是否仍然在线
        if (vertigoScapegoat != null && (vertigoScapegoat.isDisconnected() || vertigoScapegoat.isRemoved())) {
            vertigoScapegoat = null;
        }
    }

    /**
     * 选择新的眩晕背锅侠
     */
    private static void selectNewVertigoScapegoat(MinecraftServer server) {
        List<ServerPlayerEntity> allPlayers = server.getPlayerManager().getPlayerList();
        if (allPlayers.isEmpty()) return;

        // 过滤候选者：不包括上次的背锅侠且未被选过
        List<ServerPlayerEntity> candidates = allPlayers.stream()
            .filter(p -> !visitedScapegoats.contains(p) && p != vertigoScapegoat)
            .filter(p -> !p.isDisconnected())
            .toList();

        // 如果所有人都被选过，重置访问集合
        if (candidates.isEmpty()) {
            visitedScapegoats.clear();
            candidates = allPlayers.stream()
                .filter(p -> p != vertigoScapegoat && !p.isDisconnected())
                .toList();
        }

        if (!candidates.isEmpty()) {
            vertigoScapegoat = candidates.get(ThreadLocalRandom.current().nextInt(candidates.size()));
            visitedScapegoats.add(vertigoScapegoat);
            
            // 发送模糊警告（支持多语言）
            Text generalWarning = Text.literal(com.example.config.LanguageManager.getMessage("vertigo_target_selected"))
                .formatted(Formatting.DARK_PURPLE);
            Text scapegoatWarning = Text.literal(com.example.config.LanguageManager.getMessage("vertigo_responsibility"))
                .formatted(Formatting.DARK_RED);
            
            for (ServerPlayerEntity player : allPlayers) {
                if (player == vertigoScapegoat) {
                    player.sendMessage(scapegoatWarning, true);
                } else {
                    player.sendMessage(generalWarning, true);
                }
            }
        }
    }

    /**
     * 眩晕背锅侠：处理伤害重定向
     */
    public static boolean handleVertigoScapegoatDamage(LivingEntity victim, DamageSource source, float amount) {
        if (!ChaosMod.config.vertigoScapegoatEnabled) return false;
        if (victim.getWorld().isClient()) return false;
        if (vertigoScapegoat == null) return false;
        if (!(victim instanceof ServerPlayerEntity victimPlayer)) return false;

        MinecraftServer server = victimPlayer.getServer();
        if (server == null) return false;

        if (victimPlayer == vertigoScapegoat) {
            // 背锅侠自己受伤，给予10秒debuff并重新选择
            vertigoScapegoat.addStatusEffect(new StatusEffectInstance(StatusEffects.BLINDNESS, 200, 0)); // 10秒失明
            vertigoScapegoat.addStatusEffect(new StatusEffectInstance(StatusEffects.NAUSEA, 200, 0)); // 10秒反胃
            
            // 发送不同的消息（支持多语言）
            Text scapegoatMsg = Text.literal(com.example.config.LanguageManager.getMessage("vertigo_scapegoat_pain"))
                .formatted(Formatting.DARK_RED);
            Text othersMsg = Text.literal(com.example.config.LanguageManager.getMessage("vertigo_target_changed"))
                .formatted(Formatting.DARK_PURPLE);
            
            vertigoScapegoat.sendMessage(scapegoatMsg, true);
            
            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                if (player != vertigoScapegoat) {
                    player.sendMessage(othersMsg, true);
                }
            }
            
            // 立即重新选择背锅侠
            selectNewVertigoScapegoat(server);
            nextVertigoRollTick = server.getOverworld().getTime() + VERTIGO_SCAPEGOAT_INTERVAL;
            
            return false; // 不阻止原伤害
            
        } else {
            // 其他玩家受伤，背锅侠承受后果
            vertigoScapegoat.addStatusEffect(new StatusEffectInstance(StatusEffects.BLINDNESS, 200, 0)); // 10秒失明
            vertigoScapegoat.addStatusEffect(new StatusEffectInstance(StatusEffects.NAUSEA, 200, 0)); // 10秒反胃
            
            // 发送模糊提示（支持多语言）
            Text victimMsg = Text.literal(com.example.config.LanguageManager.getMessage("someone_took_pain"))
                .formatted(Formatting.YELLOW);
            Text scapegoatMsg = Text.literal(com.example.config.LanguageManager.getMessage("feeling_others_pain"))
                .formatted(Formatting.RED);
            Text othersMsg = Text.literal(com.example.config.LanguageManager.getMessage("pain_flows_in_darkness"))
                .formatted(Formatting.GRAY);
            
            victimPlayer.sendMessage(victimMsg, true);
            vertigoScapegoat.sendMessage(scapegoatMsg, true);
            
            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                if (player != victimPlayer && player != vertigoScapegoat) {
                    player.sendMessage(othersMsg, true);
                }
            }
            
            return false; // 不阻止原伤害
        }
    }

    /**
     * 获取当前眩晕背锅侠（用于调试）
     */
    public static ServerPlayerEntity getCurrentVertigoScapegoat() {
        return vertigoScapegoat;
    }

    // ==================== v1.7.0 电击地狱级效果 ====================

    // === 移动税系统 ===
    private static final Map<ServerPlayerEntity, Vec3d> LAST_POSITIONS = new WeakHashMap<>();
    private static final Map<ServerPlayerEntity, Double> MOVEMENT_ACCUMULATOR = new WeakHashMap<>();
    private static final double MOVEMENT_TAX_DISTANCE = 10.0; // 每10格扣血
    private static final ThreadLocal<Boolean> MOVEMENT_TAX_REENTRY = ThreadLocal.withInitial(() -> false);

    // === 控制癫痫Plus系统（独立的按键禁用机制） ===
    private static final Map<ServerPlayerEntity, Long> CONTROL_SEIZURE_END_TIME = new WeakHashMap<>();
    private static final Map<ServerPlayerEntity, String> CONTROL_SEIZURE_DISABLED_KEY = new WeakHashMap<>();
    private static final ThreadLocal<Boolean> CONTROL_SEIZURE_REENTRY = ThreadLocal.withInitial(() -> false);
    private static final long CONTROL_SEIZURE_DURATION = 1200; // 60秒 = 1200 ticks

    // === 跳跃税系统 ===
    private static final ThreadLocal<Boolean> JUMP_TAX_REENTRY = ThreadLocal.withInitial(() -> false);

    /**
     * 高度恐惧症Plus：处理跌落伤害拦截
     * 在LivingEntity#handleFallDamage的Mixin中调用
     * 配合ServerPlayerWalkDownMixin实现完整的"跳下"+"走下"检测
     */
    public static boolean handleExtremeFallDamage(LivingEntity entity, float fallDistance, float damageMultiplier) {
        return false; // 这个方法已废弃，高度恐惧症Plus已删除
    }

    /**
     * 触控地狱：处理方块交互拦截
     * 在ServerPlayerInteractionManager#interactBlock的Mixin中调用
     */
    public static net.minecraft.util.ActionResult handleTouchHell(ServerPlayerEntity player, net.minecraft.world.World world) {
        if (!ChaosMod.config.touchHellEnabled) return net.minecraft.util.ActionResult.PASS;
        if (world.isClient()) return net.minecraft.util.ActionResult.PASS;
        
        // 50%概率触发
        if (ThreadLocalRandom.current().nextBoolean()) {
            // 以玩家为中心做螺旋/环形搜索地表岩浆池
            Vec3d playerPos = player.getPos();
            ServerWorld serverWorld = player.getServerWorld();
            int playerX = (int) playerPos.x;
            int playerZ = (int) playerPos.z;
            
            // 螺旋/环形搜索候选位置
            for (int radius = 10; radius <= 100; radius += 10) {
                for (int angle = 0; angle < 360; angle += 30) {
                    double radians = Math.toRadians(angle);
                    int searchX = playerX + (int) (radius * Math.cos(radians));
                    int searchZ = playerZ + (int) (radius * Math.sin(radians));
                    
                    // 使用MOTION_BLOCKING_NO_LEAVES作为起点自上而下查找
                    int topY = serverWorld.getTopY(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, searchX, searchZ);
                        
                    for (int y = topY; y > serverWorld.getBottomY(); y--) {
                        net.minecraft.util.math.BlockPos pos = new net.minecraft.util.math.BlockPos(searchX, y, searchZ);
                        
                        // 检查FluidTags.LAVA且上方两格为空气的"地表池面"
                        if (serverWorld.getFluidState(pos).isIn(FluidTags.LAVA)) {
                            net.minecraft.util.math.BlockPos abovePos = pos.up();
                            net.minecraft.util.math.BlockPos above2Pos = pos.up(2);
                            
                            if (serverWorld.isAir(abovePos) && serverWorld.isAir(above2Pos)) {
                                // 找到地表岩浆池，传送玩家
                                player.teleport(serverWorld, 
                                    searchX + 0.5, y + 1.0, searchZ + 0.5, 
                                    player.getYaw(), player.getPitch());
                                player.sendMessage(Text.literal(com.example.config.LanguageManager.getMessage("touch_hell_activated")).formatted(Formatting.RED), true);
                                return net.minecraft.util.ActionResult.SUCCESS; // 终止原交互
                            }
                        }
                    }
                }
            }
        }
        return net.minecraft.util.ActionResult.PASS; // 放行原交互
    }

    /**
     * 移动税：处理玩家移动累计
     * 在ServerPlayerEntity#tick的Mixin中调用
     */
    public static void handleMovementTax(ServerPlayerEntity player) {
        if (!ChaosMod.config.movementTaxEnabled) return;
        if (player.getWorld().isClient()) return;
        if (player.isCreative() || player.isSpectator()) return;
        if (MOVEMENT_TAX_REENTRY.get()) return;

        Vec3d currentPos = player.getPos();
        Vec3d lastPos = LAST_POSITIONS.get(player);
        
        if (lastPos != null) {
            double distance = currentPos.distanceTo(lastPos);
            double accumulator = MOVEMENT_ACCUMULATOR.getOrDefault(player, 0.0) + distance;
            
            // 每满10格扣血
            if (accumulator >= MOVEMENT_TAX_DISTANCE) {
                try {
                    MOVEMENT_TAX_REENTRY.set(true);
                    player.damage(player.getServerWorld().getDamageSources().generic(), 1.0F); // 0.5♥
                    player.sendMessage(Text.literal(com.example.config.LanguageManager.getMessage("movement_tax_damage")).formatted(Formatting.YELLOW), true);
                    accumulator -= MOVEMENT_TAX_DISTANCE; // 减去已扣血的距离
                } finally {
                    MOVEMENT_TAX_REENTRY.set(false);
                }
            }
            
            MOVEMENT_ACCUMULATOR.put(player, accumulator);
        }
        
        LAST_POSITIONS.put(player, currentPos);
    }

    /**
     * 控制癫痫Plus：玩家死亡时触发
     * 在LivingEntityEvents.AFTER_DEATH中调用
     * 使用独立系统，避免与复活重置冲突
     */
    public static void handleControlSeizurePlus(ServerPlayerEntity player) {
        if (!ChaosMod.config.controlSeizurePlusEnabled) return;
        if (player.getWorld().isClient()) return;

        long currentTime = player.getServerWorld().getTime();
        CONTROL_SEIZURE_END_TIME.put(player, currentTime + CONTROL_SEIZURE_DURATION);
        
        // 随机选择WASD中的一个按键禁用
        String[] wasdKeys = {"forward", "left", "back", "right"}; // W A S D
        String keyToDisable = wasdKeys[ThreadLocalRandom.current().nextInt(wasdKeys.length)];
        
        // 使用独立的禁用系统（不依赖DISABLED_KEYS）
        CONTROL_SEIZURE_DISABLED_KEY.put(player, keyToDisable);
        
        // 发送独立的按键禁用包
        Set<String> onlyThisKey = new HashSet<>();
        onlyThisKey.add(keyToDisable);
        KeyDisableS2CPacket.send(player, onlyThisKey);
        
        // 通知玩家
        String keyName = getKeyDisplayName(keyToDisable);
        player.sendMessage(Text.literal(String.format(com.example.config.LanguageManager.getMessage("control_seizure_activated"), keyName)).formatted(Formatting.RED, Formatting.BOLD), true);
    }

    /**
     * 控制癫痫Plus：每5秒扣血处理
     * 在ServerPlayerEntity#tick的Mixin中调用
     */
    public static void tickControlSeizurePlus(ServerPlayerEntity player) {
        if (!ChaosMod.config.controlSeizurePlusEnabled) return;
        if (player.getWorld().isClient()) return;
        if (CONTROL_SEIZURE_REENTRY.get()) return;

        Long endTime = CONTROL_SEIZURE_END_TIME.get(player);
        if (endTime == null) return;

        long currentTime = player.getServerWorld().getTime();
        
        // 检查是否已过期
        if (currentTime >= endTime) {
            CONTROL_SEIZURE_END_TIME.remove(player);
            CONTROL_SEIZURE_DISABLED_KEY.remove(player);
            // 发送空的KeyDisableS2CPacket恢复键位
            KeyDisableS2CPacket.send(player, new HashSet<>());
            player.sendMessage(Text.literal(com.example.config.LanguageManager.getMessage("control_seizure_ended")).formatted(Formatting.GREEN), true);
            return;
        }

        // 每5秒扣血 (age % 100 == 0)
        if (player.age % 100 == 0) {
            try {
                CONTROL_SEIZURE_REENTRY.set(true);
                player.damage(player.getServerWorld().getDamageSources().generic(), 1.0F); // 0.5♥
                player.sendMessage(Text.literal(com.example.config.LanguageManager.getMessage("control_seizure_damage")).formatted(Formatting.RED), true);
            } finally {
                CONTROL_SEIZURE_REENTRY.set(false);
            }
        }
    }

    /**
     * 跳跃税：处理跳跃扣血
     * 在PlayerEntity#jump()的Mixin中调用
     */
    public static void handleJumpTax(PlayerEntity player) {
        if (!ChaosMod.config.jumpTaxEnabled) return;
        if (player.getWorld().isClient()) return;
        if (!(player instanceof ServerPlayerEntity serverPlayer)) return;
        if (player.isCreative() || player.isSpectator()) return;
        if (JUMP_TAX_REENTRY.get()) return;

        try {
            JUMP_TAX_REENTRY.set(true);
            serverPlayer.damage(serverPlayer.getServerWorld().getDamageSources().generic(), 1.0F); // 0.5♥
            serverPlayer.sendMessage(Text.literal(com.example.config.LanguageManager.getMessage("jump_tax_damage")).formatted(Formatting.YELLOW), true);
        } finally {
            JUMP_TAX_REENTRY.set(false);
        }
    }

    /**
     * 清理玩家数据（用于玩家离线时）
     */
    public static void cleanupPlayerData(ServerPlayerEntity player) {
        LAST_POSITIONS.remove(player);
        MOVEMENT_ACCUMULATOR.remove(player);
        CONTROL_SEIZURE_END_TIME.remove(player);
        CONTROL_SEIZURE_DISABLED_KEY.remove(player);
    }

}
