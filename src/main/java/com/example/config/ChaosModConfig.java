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
            default: break;
        }
        markDirty();
    }
}