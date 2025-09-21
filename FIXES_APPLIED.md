# 混乱模组 - 修复说明

## 🔧 已修复的问题

### 1. 怪物反伤无效问题
**问题**：玩家攻击怪物时没有反伤效果
**修复**：
- 创建了新的 `PlayerAttackMixin.java`
- 当玩家攻击怪物时，怪物会对玩家造成反伤
- 使用 `player.damage(player.getDamageSources().thorns(mob), damage)` 实现反伤
- 修复了Mixin注入方法错误（MobEntity没有damage方法）
- 修复了PlayerEntity.attack方法的参数类型错误（应该是Entity而不是LivingEntity）

### 2. 所有生物敌对无效问题
**问题**：开启所有生物敌对后，生物仍然不攻击玩家
**修复**：
- 创建了新的 `EntityMixin.java`
- 在实体tick事件中强制让所有生物对最近的玩家敌对
- 支持所有类型的生物：MobEntity、AnimalEntity、PassiveEntity
- 检测范围：50格内的最近玩家

### 3. 被怪物攻击给玩家buff的检测问题
**问题**：靠近怪物就给予buff，而不是被攻击时才给buff
**修复**：
- 移除了 `LivingEntityMixin` 中重复的buff逻辑
- 保留了 `PlayerDamageMixin` 中的正确实现
- 现在只有在玩家实际受到伤害时才会触发buff效果

## 🎯 技术实现细节

### 怪物反伤机制
```java
@Inject(method = "attack", at = @At("HEAD"))
private void onPlayerAttack(net.minecraft.entity.Entity target, CallbackInfo ci) {
    // 当玩家攻击怪物时，怪物对玩家造成反伤
    if (config.mobThornsEnabled && target instanceof MobEntity mob) {
        player.damage(player.getDamageSources().thorns(mob), damage);
    }
}
```

### 所有生物敌对机制
```java
@Inject(method = "tick", at = @At("HEAD"))
private void onTick(CallbackInfo ci) {
    // 让所有生物对最近的玩家敌对
    PlayerEntity nearestPlayer = entity.getWorld().getClosestPlayer(entity, 50.0);
    if (nearestPlayer != null) {
        // 强制设置目标为最近的玩家
        mob.setTarget(nearestPlayer);
    }
}
```

### 被攻击时给予buff机制
```java
@Inject(method = "damage", at = @At("HEAD"))
private void onPlayerDamage(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
    // 只有在玩家实际受到伤害时才触发buff
    if (source.getAttacker() instanceof MobEntity mob) {
        // 给予点燃、缓慢、失明效果
    }
}
```

## 📋 支持的生物类型

根据提供的生物列表，现在支持以下所有生物类型：

### 被动生物
- 鸡、猪、绵羊、牛、兔子、猫、豹猫、狐狸、蝙蝠、鹦鹉
- 马、驴、骡、僵尸马、骷髅马、骆驼
- 鲑鱼、热带鱼、鳕鱼、鱿鱼、发光鱿鱼
- 美西螈、青蛙、蝌蚪、海龟、悦灵、嗅探兽、炽足兽
- 村民、流浪商人

### 中立生物
- 熊猫、羊驼、行商羊驼、蜜蜂、猪灵、僵尸猪灵
- 北极熊、洞穴蜘蛛、蜘蛛、海豚、末影人、狼

### 敌对生物
- 僵尸、溺尸、尸壳、骷髅、凋灵骷髅、流浪者
- 苦力怕、闪电苦力怕、史莱姆
- 守卫者、远古守卫者、监守者、蠹虫、幻翼、恼鬼
- 女巫、僵尸村民、掠夺者、唤魔者、卫道士、劫掠兽
- 疣猪兽、僵尸疣猪兽、猪灵蛮兵
- 恶魂、烈焰人、岩浆怪、末影螨、潜影贝
- 鸡骑士、蜘蛛骑士

### Boss
- 凋灵、末影龙

### 无差别攻击
- 山羊、河豚

### 效用生物
- 铁傀儡、雪傀儡

## ✅ 验证方法

1. **测试怪物反伤**：
   - 开启 `mobthorns` 效果
   - 攻击任何怪物
   - 应该看到玩家受到反伤

2. **测试所有生物敌对**：
   - 开启 `allhostile` 效果
   - 靠近任何生物
   - 所有生物都应该攻击玩家

3. **测试被攻击buff**：
   - 开启 `mobignite`、`mobslowness`、`mobblindness` 效果
   - 让怪物攻击玩家
   - 应该获得相应的buff效果

## 🎮 使用方法

1. 进入游戏
2. 输入 `/chaos` 打开主菜单
3. 点击相应的效果按钮进行测试
4. 享受修复后的混乱体验！

所有问题已修复，模组现在应该按照预期工作。
