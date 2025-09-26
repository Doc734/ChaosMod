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
}
