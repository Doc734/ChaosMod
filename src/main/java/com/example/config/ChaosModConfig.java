package com.example.config;

/** Config defaults: ALL switches OFF by default. */
public class ChaosModConfig {
    // === Feature switches (all default false) ===
    public boolean allHostileEnabled = false;
    public boolean mobIgniteEnabled = false;
    public boolean mobSlownessEnabled = false;
    public boolean mobBlindnessEnabled = false;
    public boolean mobThornsEnabled = false;
    public boolean foodPoisonEnabled = false;
    public boolean enderDragonBucketEnabled = false;
    public boolean enderDragonKillEnabled = false;
    public boolean playerDamageShareEnabled = false;   // 贴身平摊
    public boolean sharedHealthEnabled = false;        // 镜像伤害
    public boolean sharedDamageSplitEnabled = false;   // 全服平摊
    public boolean randomDamageEnabled = false;        // 随机转移
    public boolean shieldNerfEnabled = false;
    public boolean lowHealthNoHealEnabled = false;
    public boolean waterToLavaEnabled = false;         // 玩家放水50%变岩浆
    public boolean endKeepOverrideEnabled = false;     // 末地强制掉落，其他维度保留物品
    public boolean reverseDamageEnabled = false;       // 反向伤害：不受伤扣血，受伤停止扣血
    public boolean sunburnEnabled = false;             // 阳光灼伤：晴天白天阳光下自燃
    public boolean healReverseEnabled = false;         // 治疗反转：回血时50%概率变扣血
    public boolean fallTrapEnabled = false;            // 跌落陷阱：平地跳跃落地20%概率扣0.5♥
    public boolean acrophobiaEnabled = false;          // 恐高症：Y>80越高伤害越大，最高2♥
    public boolean blockRevengeEnabled = false;        // 方块反噬：破坏方块10%概率被反伤
    public boolean containerCurseEnabled = false;      // 容器诅咒：开箱子/熔炉25%概率扣1♥
    public boolean inventoryCurseEnabled = false;      // 物品栏诅咒：切换物品槽12%概率扣0.5♥
    public boolean craftingTrapEnabled = false;        // 合成陷阱：合成物品10%概率扣1♥
    
    // === 新增的5个邪恶效果 ===
    public boolean playerHealOnAttackEnabled = false;   // 攻击玩家回血：攻击其他玩家时自己回复1♥血
    public boolean positionSwapEnabled = false;         // 位置互换：受伤时与随机队友交换位置  
    public boolean craftingBombEnabled = false;         // 合成炸弹：打开工作台超过5秒直接爆炸
    public boolean waterDamageEnabled = false;          // 水中溺死：触碰水时持续造成0.5♥伤害
    public boolean randomDamageAmountEnabled = false;   // 随机伤害值：任何伤害都变成0.5♥-10♥随机值
    
    // === v1.5.0 新增的混沌效果 ===
    public boolean delayedDamageEnabled = false;        // 延迟受伤：被打中不会马上掉血，系统随机拖延0-5秒
    public boolean keyDisableEnabled = false;           // 按键失灵：受伤累积10次随机禁用一个常用键，死亡恢复
    public boolean randomEffectsEnabled = false;        // 受伤随机增益：每次挨打随机关上或开一个状态效果
    public boolean damageScapegoatEnabled = false;      // 伤害背锅人：每隔5分钟选出背锅侠承受所有伤害
    public boolean painSpreadEnabled = false;           // 痛觉扩散：被打后5秒内"带电"，靠近会被雷劈
    
    // === v1.6.0 新增的混沌效果 (总计38种) ===
    public boolean panicMagnetEnabled = false;          // 惊惧磁铁：受伤后10秒磁化期，每2秒拽队友到身边并扣血
    public boolean pickupDrainEnabled = false;          // 贪婪吸血：拾取物品时立刻扣0.5♥血量
    public boolean vertigoScapegoatEnabled = false;     // 眩晕背锅侠：随机选择背锅侠承受他人受伤后果

    // v1.3.0: Language setting
    public String language = "zh_cn";                  // 默认中文，可选: "en_us", "zh_cn"

    // === Legacy-visible flags ===
    public boolean noHealActive = false;
    public long noHealEndTime = 0L;

    public void markDirty() { /* no-op */ }

    public boolean get(String key) {
        switch (key) {
            case "allHostileEnabled": return allHostileEnabled;
            case "mobIgniteEnabled": return mobIgniteEnabled;
            case "mobSlownessEnabled": return mobSlownessEnabled;
            case "mobBlindnessEnabled": return mobBlindnessEnabled;
            case "mobThornsEnabled": return mobThornsEnabled;
            case "foodPoisonEnabled": return foodPoisonEnabled;
            case "enderDragonBucketEnabled": return enderDragonBucketEnabled;
            case "enderDragonKillEnabled": return enderDragonKillEnabled;
            case "playerDamageShareEnabled": return playerDamageShareEnabled;
            case "sharedHealthEnabled": return sharedHealthEnabled;
            case "sharedDamageSplitEnabled": return sharedDamageSplitEnabled;
            case "randomDamageEnabled": return randomDamageEnabled;
            case "shieldNerfEnabled": return shieldNerfEnabled;
            case "lowHealthNoHealEnabled": return lowHealthNoHealEnabled;
            case "waterToLavaEnabled": return waterToLavaEnabled;
            case "endKeepOverrideEnabled": return endKeepOverrideEnabled;
            case "reverseDamageEnabled": return reverseDamageEnabled;
            case "sunburnEnabled": return sunburnEnabled;
            case "healReverseEnabled": return healReverseEnabled;
            case "fallTrapEnabled": return fallTrapEnabled;
            case "acrophobiaEnabled": return acrophobiaEnabled;
            case "blockRevengeEnabled": return blockRevengeEnabled;
            case "containerCurseEnabled": return containerCurseEnabled;
            case "inventoryCurseEnabled": return inventoryCurseEnabled;
            case "craftingTrapEnabled": return craftingTrapEnabled;
            case "playerHealOnAttackEnabled": return playerHealOnAttackEnabled;
            case "positionSwapEnabled": return positionSwapEnabled;
            case "craftingBombEnabled": return craftingBombEnabled;
            case "waterDamageEnabled": return waterDamageEnabled;
            case "randomDamageAmountEnabled": return randomDamageAmountEnabled;
            case "delayedDamageEnabled": return delayedDamageEnabled;
            case "keyDisableEnabled": return keyDisableEnabled;
            case "randomEffectsEnabled": return randomEffectsEnabled;
            case "damageScapegoatEnabled": return damageScapegoatEnabled;
            case "painSpreadEnabled": return painSpreadEnabled;
            case "panicMagnetEnabled": return panicMagnetEnabled;
            case "pickupDrainEnabled": return pickupDrainEnabled;
            case "vertigoScapegoatEnabled": return vertigoScapegoatEnabled;
            default: return false;
        }
    }
    public void set(String key, boolean value) {
        switch (key) {
            case "allHostileEnabled": allHostileEnabled = value; break;
            case "mobIgniteEnabled": mobIgniteEnabled = value; break;
            case "mobSlownessEnabled": mobSlownessEnabled = value; break;
            case "mobBlindnessEnabled": mobBlindnessEnabled = value; break;
            case "mobThornsEnabled": mobThornsEnabled = value; break;
            case "foodPoisonEnabled": foodPoisonEnabled = value; break;
            case "enderDragonBucketEnabled": enderDragonBucketEnabled = value; break;
            case "enderDragonKillEnabled": enderDragonKillEnabled = value; break;
            case "playerDamageShareEnabled": playerDamageShareEnabled = value; break;
            case "sharedHealthEnabled": sharedHealthEnabled = value; break;
            case "sharedDamageSplitEnabled": sharedDamageSplitEnabled = value; break;
            case "randomDamageEnabled": randomDamageEnabled = value; break;
            case "shieldNerfEnabled": shieldNerfEnabled = value; break;
            case "lowHealthNoHealEnabled": lowHealthNoHealEnabled = value; break;
            case "waterToLavaEnabled": waterToLavaEnabled = value; break;
            case "endKeepOverrideEnabled": endKeepOverrideEnabled = value; break;
            case "reverseDamageEnabled": reverseDamageEnabled = value; break;
            case "sunburnEnabled": sunburnEnabled = value; break;
            case "healReverseEnabled": healReverseEnabled = value; break;
            case "fallTrapEnabled": fallTrapEnabled = value; break;
            case "acrophobiaEnabled": acrophobiaEnabled = value; break;
            case "blockRevengeEnabled": blockRevengeEnabled = value; break;
            case "containerCurseEnabled": containerCurseEnabled = value; break;
            case "inventoryCurseEnabled": inventoryCurseEnabled = value; break;
            case "craftingTrapEnabled": craftingTrapEnabled = value; break;
            case "playerHealOnAttackEnabled": playerHealOnAttackEnabled = value; break;
            case "positionSwapEnabled": positionSwapEnabled = value; break;
            case "craftingBombEnabled": craftingBombEnabled = value; break;
            case "waterDamageEnabled": waterDamageEnabled = value; break;
            case "randomDamageAmountEnabled": randomDamageAmountEnabled = value; break;
            case "delayedDamageEnabled": delayedDamageEnabled = value; break;
            case "keyDisableEnabled": keyDisableEnabled = value; break;
            case "randomEffectsEnabled": randomEffectsEnabled = value; break;
            case "damageScapegoatEnabled": damageScapegoatEnabled = value; break;
            case "painSpreadEnabled": painSpreadEnabled = value; break;
            case "panicMagnetEnabled": panicMagnetEnabled = value; break;
            case "pickupDrainEnabled": pickupDrainEnabled = value; break;
            case "vertigoScapegoatEnabled": vertigoScapegoatEnabled = value; break;
            default: break;
        }
        markDirty();
    }
    
    // Language configuration methods
    public String getLanguage() {
        return language;
    }
    
    public void setLanguage(String language) {
        this.language = language;
        markDirty();
    }
}