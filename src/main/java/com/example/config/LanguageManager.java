package com.example.config;

import java.util.HashMap;
import java.util.Map;

public class LanguageManager {
    public enum Language {
        ENGLISH("en_us", "English"),
        CHINESE("zh_cn", "中文");
        
        public final String code;
        public final String displayName;
        
        Language(String code, String displayName) {
            this.code = code;
            this.displayName = displayName;
        }
    }
    
    private static Language currentLanguage = Language.CHINESE; // 默认中文
    
    // 初始化时从配置加载语言
    public static void loadLanguageFromConfig() {
        String configLang = com.example.ChaosMod.config.getLanguage();
        for (Language lang : Language.values()) {
            if (lang.code.equals(configLang)) {
                currentLanguage = lang;
                break;
            }
        }
    }
    
    // 中文标签
    private static final Map<String, String> CHINESE_LABELS = new HashMap<>();
    static {
        CHINESE_LABELS.put("allHostileEnabled", "所有生物敌对");
        CHINESE_LABELS.put("mobIgniteEnabled", "被怪命中点燃");
        CHINESE_LABELS.put("mobSlownessEnabled", "被怪命中缓慢II");
        CHINESE_LABELS.put("mobBlindnessEnabled", "被怪命中失明");
        CHINESE_LABELS.put("mobThornsEnabled", "反伤=50%");
        CHINESE_LABELS.put("foodPoisonEnabled", "吃食物概率中毒");
        CHINESE_LABELS.put("enderDragonBucketEnabled", "被龙打→水桶变牛奶");
        CHINESE_LABELS.put("enderDragonKillEnabled", "击杀末影龙者自杀");
        CHINESE_LABELS.put("playerDamageShareEnabled", "贴身平摊伤害");
        CHINESE_LABELS.put("sharedHealthEnabled", "共享生命(镜像)");
        CHINESE_LABELS.put("sharedDamageSplitEnabled", "全服平摊伤害");
        CHINESE_LABELS.put("randomDamageEnabled", "随机转移伤害");
        CHINESE_LABELS.put("shieldNerfEnabled", "盾牌仅吸收80%");
        CHINESE_LABELS.put("lowHealthNoHealEnabled", "≤1♥禁回血(10s)");
        CHINESE_LABELS.put("waterToLavaEnabled", "放水50%变岩浆(仅玩家)");
        CHINESE_LABELS.put("endKeepOverrideEnabled", "末地死亡掉落/其他维度保留物品");
        CHINESE_LABELS.put("reverseDamageEnabled", "反向伤害：不受伤扣血");
        CHINESE_LABELS.put("sunburnEnabled", "晴天白天阳光下自燃");
        CHINESE_LABELS.put("healReverseEnabled", "回血时50%概率变扣血");
        CHINESE_LABELS.put("fallTrapEnabled", "平地跳跃落地20%概率扣0.5♥");
        CHINESE_LABELS.put("acrophobiaEnabled", "恐高症：Y>80越高伤害越大(最高2♥)");
        CHINESE_LABELS.put("blockRevengeEnabled", "破坏方块10%概率被反伤");
        CHINESE_LABELS.put("containerCurseEnabled", "开箱子/熔炉25%概率扣1♥");
        CHINESE_LABELS.put("inventoryCurseEnabled", "切换物品槽12%概率扣0.5♥");
        CHINESE_LABELS.put("craftingTrapEnabled", "合成物品10%概率扣1♥");
        CHINESE_LABELS.put("playerHealOnAttackEnabled", "攻击玩家回血");
        CHINESE_LABELS.put("positionSwapEnabled", "位置互换");
        CHINESE_LABELS.put("craftingBombEnabled", "合成炸弹");
        CHINESE_LABELS.put("waterDamageEnabled", "水中溺死");
        CHINESE_LABELS.put("randomDamageAmountEnabled", "随机伤害值");
        CHINESE_LABELS.put("delayedDamageEnabled", "延迟受伤");
        CHINESE_LABELS.put("keyDisableEnabled", "按键失灵");
        CHINESE_LABELS.put("randomEffectsEnabled", "受伤随机增益");
        CHINESE_LABELS.put("damageScapegoatEnabled", "伤害背锅人");
        CHINESE_LABELS.put("painSpreadEnabled", "痛觉扩散");
        
        // v1.6.0 新增效果
        CHINESE_LABELS.put("panicMagnetEnabled", "惊惧磁铁");
        CHINESE_LABELS.put("pickupDrainEnabled", "贪婪吸血");
        CHINESE_LABELS.put("vertigoScapegoatEnabled", "眩晕背锅侠");
        
        // v1.6.0 第四面墙突破效果
        CHINESE_LABELS.put("windowViolentShakeEnabled", "窗口暴力抖动");
        CHINESE_LABELS.put("desktopPrankInvasionEnabled", "桌面恶作剧入侵(会记录IP地址)");
    }
    
    // 英文标签
    private static final Map<String, String> ENGLISH_LABELS = new HashMap<>();
    static {
        ENGLISH_LABELS.put("allHostileEnabled", "All Mobs Hostile");
        ENGLISH_LABELS.put("mobIgniteEnabled", "Mob Hits Ignite");
        ENGLISH_LABELS.put("mobSlownessEnabled", "Mob Hits Slowness II");
        ENGLISH_LABELS.put("mobBlindnessEnabled", "Mob Hits Blindness");
        ENGLISH_LABELS.put("mobThornsEnabled", "Reflection Damage = 50%");
        ENGLISH_LABELS.put("foodPoisonEnabled", "Food Poisoning Chance");
        ENGLISH_LABELS.put("enderDragonBucketEnabled", "Dragon Hit → Water to Milk");
        ENGLISH_LABELS.put("enderDragonKillEnabled", "Dragon Killer Suicide");
        ENGLISH_LABELS.put("playerDamageShareEnabled", "Close Range Damage Share");
        ENGLISH_LABELS.put("sharedHealthEnabled", "Shared Health (Mirror)");
        ENGLISH_LABELS.put("sharedDamageSplitEnabled", "Server-wide Damage Split");
        ENGLISH_LABELS.put("randomDamageEnabled", "Random Damage Transfer");
        ENGLISH_LABELS.put("shieldNerfEnabled", "Shield Only Absorbs 80%");
        ENGLISH_LABELS.put("lowHealthNoHealEnabled", "≤1♥ No Healing (10s)");
        ENGLISH_LABELS.put("waterToLavaEnabled", "Water 50% Becomes Lava");
        ENGLISH_LABELS.put("endKeepOverrideEnabled", "End Death Drops/Other Keep Items");
        ENGLISH_LABELS.put("reverseDamageEnabled", "Reverse Damage: Safe Hurts");
        ENGLISH_LABELS.put("sunburnEnabled", "Sunny Daylight Combustion");
        ENGLISH_LABELS.put("healReverseEnabled", "Healing 50% Becomes Damage");
        ENGLISH_LABELS.put("fallTrapEnabled", "Jump Landing 20% → 0.5♥");
        ENGLISH_LABELS.put("acrophobiaEnabled", "Acrophobia: Y>80 Height Damage");
        ENGLISH_LABELS.put("blockRevengeEnabled", "Block Break 10% Revenge");
        ENGLISH_LABELS.put("containerCurseEnabled", "Container Open 25% → 1♥");
        ENGLISH_LABELS.put("inventoryCurseEnabled", "Item Switch 12% → 0.5♥");
        ENGLISH_LABELS.put("craftingTrapEnabled", "Crafting 10% → 1♥");
        ENGLISH_LABELS.put("playerHealOnAttackEnabled", "Attack Player Heal");
        ENGLISH_LABELS.put("positionSwapEnabled", "Position Swap");
        ENGLISH_LABELS.put("craftingBombEnabled", "Crafting Bomb");
        ENGLISH_LABELS.put("waterDamageEnabled", "Water Drowning");
        ENGLISH_LABELS.put("randomDamageAmountEnabled", "Random Damage Value");
        ENGLISH_LABELS.put("delayedDamageEnabled", "Delayed Damage");
        ENGLISH_LABELS.put("keyDisableEnabled", "Key Malfunction");
        ENGLISH_LABELS.put("randomEffectsEnabled", "Random Status Effects");
        ENGLISH_LABELS.put("damageScapegoatEnabled", "Damage Scapegoat");
        ENGLISH_LABELS.put("painSpreadEnabled", "Pain Spreading");
        
        // v1.6.0 New Effects
        ENGLISH_LABELS.put("panicMagnetEnabled", "Panic Magnet");
        ENGLISH_LABELS.put("pickupDrainEnabled", "Pickup Drain");
        ENGLISH_LABELS.put("vertigoScapegoatEnabled", "Vertigo Scapegoat");
        
        // v1.6.0 Fourth Wall Breaking Effects
        ENGLISH_LABELS.put("windowViolentShakeEnabled", "Window Violent Shake");
        ENGLISH_LABELS.put("desktopPrankInvasionEnabled", "Desktop Prank Invasion (Records IP Address)");
    }
    
    // UI 文本
    private static final Map<String, String> CHINESE_UI = new HashMap<>();
    static {
        CHINESE_UI.put("gui.title", "ChaosMod 配置");
        CHINESE_UI.put("gui.enabled", "✓ 已启用");
        CHINESE_UI.put("gui.disabled", "✗ 已禁用");
        CHINESE_UI.put("gui.enable_all", "🟢 全部启用");
        CHINESE_UI.put("gui.disable_all", "⚪ 全部禁用");
        CHINESE_UI.put("gui.close", "❌ 关闭");
        CHINESE_UI.put("gui.previous_page", "< 上一页");
        CHINESE_UI.put("gui.next_page", "下一页 >");
        CHINESE_UI.put("gui.page_info", "第 %d 页，共 %d 页");
        CHINESE_UI.put("gui.permission_denied", "权限不足");
        CHINESE_UI.put("gui.admin_required", "需要管理员权限");
        CHINESE_UI.put("gui.back", "返回");
        CHINESE_UI.put("gui.language", "🌐 Language");
        CHINESE_UI.put("permission.denied", "🚫 权限不足！只有管理员才能修改 ChaosMod 配置！");
        CHINESE_UI.put("config.updated", "[配置已更新] %s -> %s");
        CHINESE_UI.put("ai.random.button", "[AI] 随机效果(为你量身定做)");
        CHINESE_UI.put("ai.permission.denied", "[错误] 权限不足，只有管理员才能使用AI随机效果");
        CHINESE_UI.put("ai.effects.enabled", "[ChaosMod AI] 已启用效果：");
    }
    
    private static final Map<String, String> ENGLISH_UI = new HashMap<>();
    static {
        ENGLISH_UI.put("gui.title", "ChaosMod Config");
        ENGLISH_UI.put("gui.enabled", "✓ Enabled");
        ENGLISH_UI.put("gui.disabled", "✗ Disabled");
        ENGLISH_UI.put("gui.enable_all", "🟢 Enable All");
        ENGLISH_UI.put("gui.disable_all", "⚪ Disable All");
        ENGLISH_UI.put("gui.close", "❌ Close");
        ENGLISH_UI.put("gui.previous_page", "< Previous");
        ENGLISH_UI.put("gui.next_page", "Next >");
        ENGLISH_UI.put("gui.page_info", "Page %d of %d");
        ENGLISH_UI.put("gui.permission_denied", "Permission Denied");
        ENGLISH_UI.put("gui.admin_required", "Admin Rights Required");
        ENGLISH_UI.put("gui.back", "Back");
        ENGLISH_UI.put("gui.language", "🌐 语言");
        ENGLISH_UI.put("permission.denied", "🚫 Permission denied! Only admins can modify ChaosMod config!");
        ENGLISH_UI.put("config.updated", "[Config Updated] %s -> %s");
        ENGLISH_UI.put("ai.random.button", "[AI] Random Effects (Tailored for You)");
        ENGLISH_UI.put("ai.permission.denied", "[Error] Permission denied, only admins can use AI random effects");
        ENGLISH_UI.put("ai.effects.enabled", "[ChaosMod AI] Enabled Effects:");
    }
    
    public static Language getCurrentLanguage() {
        return currentLanguage;
    }
    
    public static void setLanguage(Language language) {
        currentLanguage = language;
        // 保存到配置文件
        com.example.ChaosMod.config.setLanguage(language.code);
    }
    
    public static String getLabel(String key) {
        Map<String, String> labels = currentLanguage == Language.ENGLISH ? ENGLISH_LABELS : CHINESE_LABELS;
        return labels.getOrDefault(key, key);
    }
    
    public static String getUI(String key) {
        Map<String, String> ui = currentLanguage == Language.ENGLISH ? ENGLISH_UI : CHINESE_UI;
        return ui.getOrDefault(key, key);
    }
    
    public static String getFormattedUI(String key, Object... args) {
        String format = getUI(key);
        try {
            return String.format(format, args);
        } catch (Exception e) {
            return format;
        }
    }
    
    public static Language[] getAllLanguages() {
        return Language.values();
    }
    
    /**
     * 根据语言代码获取桌面文件内容（服务端使用）
     */
    public static String getDesktopContentByLanguage(String languageCode, String contentKey) {
        if ("en_us".equals(languageCode)) {
            // 英文邪恶内容
            return switch (contentKey) {
                case "help_5hp" -> "😱 Help me! Low health! The game wants to kill me! 😱";
                case "help_3hp" -> "💀 AAAHHH!!! About to die! The game is torturing me! 💀";
                case "death" -> "😈 Game Over! You have been completely defeated by ChaosMod! 😈";
                default -> "😈 ChaosMod Desktop Invasion 😈";
            };
        } else {
            // 中文邪恶内容（默认）
            return switch (contentKey) {
                case "help_5hp" -> "😱 救命啊！血量不够了！游戏要杀死我！ 😱";
                case "help_3hp" -> "💀 啊啊啊！！！马上就要死掉了！游戏在折磨我！ 💀";
                case "death" -> "😈 游戏结束！你被ChaosMod彻底击败了！ 😈";
                default -> "😈 ChaosMod 桌面入侵 😈";
            };
        }
    }
    
    /**
     * 根据语言代码获取邪恶文件内容模板（服务端使用）
     */
    public static String getEvilFileContent(String languageCode, String contentKey, String playerIP, float currentHealth) {
        String baseContent = getDesktopContentByLanguage(languageCode, contentKey);
        
        if ("en_us".equals(languageCode)) {
            // 英文邪恶模板
            return baseContent + 
                   "\n\n💀 The game knows everything about you... 💀" +
                   "\nGeneration Time: " + new java.util.Date() +
                   "\nCurrent Health: " + currentHealth + "♥ (How long can you last? 😈)" +
                   "\n\n😈 Your IP Address: " + playerIP + " 😈" +
                   "\n🔥 The game has locked onto your location... 🔥" +
                   "\n💀 Virtual world is invading reality... 💀" +
                   "\n⚡ There's nowhere to escape... ⚡" +
                   "\n\n--- ChaosMod has taken over your desktop ---" +
                   "\n😈 Feel the fear! 😈";
        } else {
            // 中文邪恶模板
            return baseContent + 
                   "\n\n💀 游戏已经知道你的一切... 💀" +
                   "\n生成时间: " + new java.util.Date() +
                   "\n当前血量: " + currentHealth + "♥ (还能撑多久？😈)" +
                   "\n\n😈 你的IP地址: " + playerIP + " 😈" +
                   "\n🔥 游戏已经锁定你的位置... 🔥" +
                   "\n💀 虚拟世界正在入侵现实... 💀" +
                   "\n⚡ 无处可逃... ⚡" +
                   "\n\n--- ChaosMod 已接管你的桌面 ---" +
                   "\n😈 感受恐惧吧！ 😈";
        }
    }
    
    /**
     * 获取本地化消息（服务端使用）
     * 根据全局语言设置返回对应语言的消息
     */
    public static String getMessage(String messageKey) {
        String language = com.example.ChaosMod.config.getLanguage();
        
        if ("en_us".equals(language)) {
            // 英文消息
            return switch (messageKey) {
                case "window_shake_punishment" -> "The price of window shaking... respawn punishment descends...";
                case "damage_scapegoat_selected" -> "Someone has become the damage scapegoat...";
                case "damage_transferred" -> "Someone's damage has been transferred...";
                case "system_changed" -> "System has changed...";
                case "vertigo_target_selected" -> "Someone in the darkness has become... the target of some entity...";
                case "vertigo_responsibility" -> "You feel an ominous premonition... as if bearing some... responsibility...";
                case "vertigo_scapegoat_pain" -> "The pain of being a scapegoat... fate will turn to others...";
                case "vertigo_target_changed" -> "The target in the darkness has changed...";
                case "someone_took_pain" -> "Someone has endured pain for you...";
                case "feeling_others_pain" -> "You feel pain that doesn't belong to you...";
                case "pain_flows_in_darkness" -> "Pain flows in the darkness...";
                case "config_permission_denied" -> "Permission denied! Only administrators can modify ChaosMod configuration!";
                case "config_invalid_key" -> "Invalid configuration key";
                case "config_updated" -> "Configuration updated";
                case "config_changed" -> "has set";
                case "cannot_get_player" -> "Cannot get player information";
                // 效果相关消息
                case "key_disabled" -> "Key malfunction! %s key has been disabled! Recovers after death.";
                case "lost_effect" -> "Lost %s effect";
                case "gained_effect" -> "Gained %s effect";
                case "electrified" -> "You are electrified! Anyone close to you for 5 seconds will be struck by lightning!";
                case "electrified_ended" -> "Electrified status ended";
                case "struck_by_lightning" -> "You were struck by lightning from %s's electrified status!";
                case "magnetized" -> "You are magnetized! Will continuously pull teammates to your side for 10 seconds!";
                case "magnetized_ended" -> "Magnetized status ended";
                case "stay_away" -> "Stay away from me!";
                case "pulled_by_magnet" -> "You were pulled by a magnetized teammate! Gained brief magnetization immunity.";
                case "greed_penalty" -> "The price of greed! Picking up items costs you life!";
                case "damage_absorbed" -> "You absorbed damage for someone else!";
                default -> messageKey;
            };
        } else {
            // 中文消息
            return switch (messageKey) {
                case "window_shake_punishment" -> "窗口抖动的代价...复活的惩戒降临...";
                case "damage_scapegoat_selected" -> "有人成为了伤害背锅人...";
                case "damage_transferred" -> "有人的伤害被转移了...";
                case "system_changed" -> "系统发生了变化...";
                case "vertigo_target_selected" -> "黑暗中有人成为了...某种存在的目标...";
                case "vertigo_responsibility" -> "你感到一种不祥的预感...仿佛承担了某种...责任...";
                case "vertigo_scapegoat_pain" -> "作为背锅侠的痛苦...命运将转向他人...";
                case "vertigo_target_changed" -> "黑暗中的目标发生了改变...";
                case "someone_took_pain" -> "有人替你承受了痛苦...";
                case "feeling_others_pain" -> "你感受到了不属于自己的痛苦...";
                case "pain_flows_in_darkness" -> "痛苦在黑暗中流转...";
                case "config_permission_denied" -> "权限不足！只有管理员才能修改 ChaosMod 配置！";
                case "config_invalid_key" -> "无效的配置键";
                case "config_updated" -> "配置已更新";
                case "config_changed" -> "已将";
                case "cannot_get_player" -> "无法获取玩家信息";
                // 效果相关消息
                case "key_disabled" -> "按键失灵！%s 键已被禁用！死亡后恢复。";
                case "lost_effect" -> "失去了 %s 效果";
                case "gained_effect" -> "获得了 %s 效果";
                case "electrified" -> "你带电了！5秒内靠近你的人会被雷劈！";
                case "electrified_ended" -> "带电状态已结束";
                case "struck_by_lightning" -> "你被 %s 的带电状态雷劈了！";
                case "magnetized" -> "你被磁化了！10秒内会不断拉拽队友到身边！";
                case "magnetized_ended" -> "磁化状态已结束";
                case "stay_away" -> "别靠近我！";
                case "pulled_by_magnet" -> "你被磁化的队友拉了过去！获得短暂磁化免疫。";
                case "greed_penalty" -> "贪心的代价！拾取物品让你失去了生命！";
                case "damage_absorbed" -> "你替别人承受了伤害！";
                default -> messageKey;
            };
        }
    }
}
