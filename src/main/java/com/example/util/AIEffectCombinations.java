package com.example.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * AI随机效果组合 - 为玩家量身定做的20种邪恶套餐
 */
public class AIEffectCombinations {
    
    public static class EffectCombination {
        public final String name;
        public final String description;
        public final String[] effects;
        public final String chatMessage;
        
        public EffectCombination(String name, String description, String[] effects, String chatMessage) {
            this.name = name;
            this.description = description;
            this.effects = effects;
            this.chatMessage = chatMessage;
        }
    }
    
    // 20种AI预设组合
    public static final List<EffectCombination> ALL_COMBINATIONS = new ArrayList<>();
    
    static {
        // 用户指定的4种组合
        ALL_COMBINATIONS.add(new EffectCombination(
            "[火焰缓慢] 缓慢烧烤套餐",
            "慢慢烧死你，逃都逃不掉！",
            new String[]{"mobIgniteEnabled", "mobSlownessEnabled", "enderDragonBucketEnabled", "shieldNerfEnabled"},
            "AI为你推荐：缓慢烧烤套餐 - 让你体验慢火炖煮的绝望！"
        ));
        
        ALL_COMBINATIONS.add(new EffectCombination(
            "[生死共享] 生死与共组合",
            "团队绑定，一人死亡全员死亡！",
            new String[]{"sharedHealthEnabled", "mobIgniteEnabled", "shieldNerfEnabled", "mobSlownessEnabled"},
            "AI为你推荐：生死与共组合 - 和朋友一起享受团队灭绝！"
        ));
        
        ALL_COMBINATIONS.add(new EffectCombination(
            "[环境杀手] 环境杀手模式",
            "环境都是你的敌人！",
            new String[]{"waterToLavaEnabled", "randomDamageEnabled", "shieldNerfEnabled"},
            "AI为你推荐：环境杀手模式 - 连水都要背叛你！"
        ));
        
        ALL_COMBINATIONS.add(new EffectCombination(
            "[传送陷阱] 传送陷阱大师",
            "受伤就传送，传送到死亡陷阱！",
            new String[]{"positionSwapEnabled", "waterDamageEnabled", "randomDamageEnabled", "shieldNerfEnabled"},
            "AI为你推荐：传送陷阱大师 - 每次传送都是一场赌博！"
        ));
        
        // 额外的16种组合
        ALL_COMBINATIONS.add(new EffectCombination(
            "[新手] 新手入门套餐",
            "温和的邪恶入门体验",
            new String[]{"foodPoisonEnabled", "shieldNerfEnabled", "lowHealthNoHealEnabled"},
            "AI为你推荐：新手入门套餐 - 温柔地开始你的痛苦之旅！"
        ));
        
        ALL_COMBINATIONS.add(new EffectCombination(
            "[恐高] 恐高症专家",
            "高度越高越绝望！",
            new String[]{"acrophobiaEnabled", "reverseDamageEnabled", "fallTrapEnabled", "shieldNerfEnabled"},
            "AI为你推荐：恐高症专家 - 让天空成为你的噩梦！"
        ));
        
        ALL_COMBINATIONS.add(new EffectCombination(
            "[阳光] 阳光死神",
            "白天户外就是死亡！",
            new String[]{"sunburnEnabled", "mobIgniteEnabled", "healReverseEnabled", "reverseDamageEnabled"},
            "AI为你推荐：阳光死神 - 让每个晴天都成为末日！"
        ));
        
        ALL_COMBINATIONS.add(new EffectCombination(
            "[操作恐惧] 操作恐惧症",
            "基本操作都变成陷阱！",
            new String[]{"inventoryCurseEnabled", "craftingTrapEnabled", "containerCurseEnabled", "blockRevengeEnabled"},
            "AI为你推荐：操作恐惧症 - 让每个动作都充满恐惧！"
        ));
        
        ALL_COMBINATIONS.add(new EffectCombination(
            "[治疗背叛] 治疗背叛者",
            "回血变扣血的绝望！",
            new String[]{"healReverseEnabled", "lowHealthNoHealEnabled", "reverseDamageEnabled", "mobThornsEnabled"},
            "AI为你推荐：治疗背叛者 - 连治疗都是陷阱！"
        ));
        
        ALL_COMBINATIONS.add(new EffectCombination(
            "[末地龙王] 末地龙王复仇",
            "击杀龙王的代价！",
            new String[]{"enderDragonKillEnabled", "enderDragonBucketEnabled", "endKeepOverrideEnabled", "sharedHealthEnabled"},
            "AI为你推荐：末地龙王复仇 - 胜利即是死亡！"
        ));
        
        ALL_COMBINATIONS.add(new EffectCombination(
            "[全敌对] 全敌对模式",
            "世界都是你的敌人！",
            new String[]{"allHostileEnabled", "mobThornsEnabled", "mobBlindnessEnabled", "shieldNerfEnabled"},
            "AI为你推荐：全敌对模式 - 整个世界都想杀死你！"
        ));
        
        ALL_COMBINATIONS.add(new EffectCombination(
            "[PVP鼓励] PVP鼓励套餐",
            "鼓励玩家互相攻击！",
            new String[]{"playerHealOnAttackEnabled", "randomDamageEnabled", "mobThornsEnabled", "reverseDamageEnabled"},
            "AI为你推荐：PVP鼓励套餐 - 朋友就是用来攻击的！"
        ));
        
        ALL_COMBINATIONS.add(new EffectCombination(
            "[随机命运] 完全随机命运",
            "一切都靠运气！",
            new String[]{"randomDamageAmountEnabled", "randomDamageEnabled", "fallTrapEnabled", "foodPoisonEnabled"},
            "AI为你推荐：完全随机命运 - 生死全凭天意！"
        ));
        
        ALL_COMBINATIONS.add(new EffectCombination(
            "[水系绝杀] 水系绝杀",
            "水变成最危险的东西！",
            new String[]{"waterDamageEnabled", "waterToLavaEnabled", "positionSwapEnabled", "healReverseEnabled"},
            "AI为你推荐：水系绝杀 - 从此再也不敢碰水！"
        ));
        
        ALL_COMBINATIONS.add(new EffectCombination(
            "[合成恐怖] 合成恐怖",
            "合成变成生死游戏！",
            new String[]{"craftingBombEnabled", "craftingTrapEnabled", "inventoryCurseEnabled", "containerCurseEnabled"},
            "AI为你推荐：合成恐怖 - 每次合成都是拆弹游戏！"
        ));
        
        ALL_COMBINATIONS.add(new EffectCombination(
            "[末地龙王] 末地龙王复仇",
            "击杀龙王的代价！",
            new String[]{"enderDragonKillEnabled", "enderDragonBucketEnabled", "endKeepOverrideEnabled", "sharedHealthEnabled"},
            "AI为你推荐：末地龙王复仇 - 胜利即是死亡！"
        ));
        
        ALL_COMBINATIONS.add(new EffectCombination(
            "[全敌对] 全敌对模式",
            "世界都是你的敌人！",
            new String[]{"allHostileEnabled", "mobThornsEnabled", "mobBlindnessEnabled", "shieldNerfEnabled"},
            "AI为你推荐：全敌对模式 - 整个世界都想杀死你！"
        ));
        
        ALL_COMBINATIONS.add(new EffectCombination(
            "[PVP鼓励] PVP鼓励套餐",
            "鼓励玩家互相攻击！",
            new String[]{"playerHealOnAttackEnabled", "randomDamageEnabled", "mobThornsEnabled", "reverseDamageEnabled"},
            "AI为你推荐：PVP鼓励套餐 - 朋友就是用来攻击的！"
        ));
        
        ALL_COMBINATIONS.add(new EffectCombination(
            "[随机命运] 完全随机命运",
            "一切都靠运气！",
            new String[]{"randomDamageAmountEnabled", "randomDamageEnabled", "fallTrapEnabled", "foodPoisonEnabled"},
            "AI为你推荐：完全随机命运 - 生死全凭天意！"
        ));
        
        ALL_COMBINATIONS.add(new EffectCombination(
            "[水系绝杀] 水系绝杀",
            "水变成最危险的东西！",
            new String[]{"waterDamageEnabled", "waterToLavaEnabled", "positionSwapEnabled", "healReverseEnabled"},
            "AI为你推荐：水系绝杀 - 从此再也不敢碰水！"
        ));
        
        ALL_COMBINATIONS.add(new EffectCombination(
            "[合成恐怖] 合成恐怖",
            "合成变成生死游戏！",
            new String[]{"craftingBombEnabled", "craftingTrapEnabled", "inventoryCurseEnabled", "containerCurseEnabled"},
            "AI为你推荐：合成恐怖 - 每次合成都是拆弹游戏！"
        ));
        
        ALL_COMBINATIONS.add(new EffectCombination(
            "[传送混沌] 传送混沌",
            "位置永远不安全！",
            new String[]{"positionSwapEnabled", "acrophobiaEnabled", "waterDamageEnabled", "sunburnEnabled"},
            "AI为你推荐：传送混沌 - 每次传送都可能是死亡陷阱！"
        ));
        
        ALL_COMBINATIONS.add(new EffectCombination(
            "[生存绝望] 生存绝望",
            "基本生存需求都是陷阱！",
            new String[]{"foodPoisonEnabled", "healReverseEnabled", "lowHealthNoHealEnabled", "inventoryCurseEnabled"},
            "AI为你推荐：生存绝望 - 连活着都是奢望！"
        ));
        
        ALL_COMBINATIONS.add(new EffectCombination(
            "[团队背叛] 团队背叛",
            "朋友都是潜在敌人！",
            new String[]{"playerDamageShareEnabled", "sharedDamageSplitEnabled", "playerHealOnAttackEnabled", "randomDamageEnabled"},
            "AI为你推荐：团队背叛 - 朋友比敌人更危险！"
        ));
        
        ALL_COMBINATIONS.add(new EffectCombination(
            "[终极混沌] 终极混沌",
            "所有类型的痛苦集合！",
            new String[]{"randomDamageAmountEnabled", "allHostileEnabled", "sharedHealthEnabled", "acrophobiaEnabled", "craftingBombEnabled"},
            "AI为你推荐：终极混沌 - 真正的地狱难度挑战！"
        ));
        
        ALL_COMBINATIONS.add(new EffectCombination(
            "[爆炸专家] 爆炸专家",
            "到处都是爆炸！",
            new String[]{"craftingBombEnabled", "mobThornsEnabled", "blockRevengeEnabled", "enderDragonKillEnabled"},
            "AI为你推荐：爆炸专家 - 让世界充满爆炸！"
        ));
        
        ALL_COMBINATIONS.add(new EffectCombination(
            "[黑暗恐怖] 黑暗恐怖",
            "看不见的恐惧！",
            new String[]{"mobBlindnessEnabled", "allHostileEnabled", "reverseDamageEnabled", "randomDamageAmountEnabled"},
            "AI为你推荐：黑暗恐怖 - 在黑暗中感受真正的恐惧！"
        ));
        
        ALL_COMBINATIONS.add(new EffectCombination(
            "[混沌嘉年华] 混沌嘉年华",
            "疯狂的效果组合！",
            new String[]{"foodPoisonEnabled", "waterToLavaEnabled", "fallTrapEnabled", "inventoryCurseEnabled", "containerCurseEnabled"},
            "AI为你推荐：混沌嘉年华 - 疯狂的痛苦盛宴！"
        ));
        
        ALL_COMBINATIONS.add(new EffectCombination(
            "[电击专用] 电击专用版",
            "为电击设备优化！",
            new String[]{"acrophobiaEnabled", "reverseDamageEnabled", "healReverseEnabled", "randomDamageAmountEnabled", "sunburnEnabled"},
            "AI为你推荐：电击专用版 - 持续稳定的电击体验！"
        ));
        
        ALL_COMBINATIONS.add(new EffectCombination(
            "[运气测试] 运气测试",
            "全凭运气的游戏！",
            new String[]{"randomDamageAmountEnabled", "fallTrapEnabled", "foodPoisonEnabled", "craftingTrapEnabled", "blockRevengeEnabled"},
            "AI为你推荐：运气测试 - 看看你的人品如何！"
        ));
    }
}
