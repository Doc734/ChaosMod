# 混乱模组使用说明

## 安装
1. 确保已安装 Minecraft 1.21
2. 安装 Fabric Loader 0.17.2+
3. 安装 Fabric API 0.102.0+1.21
4. 将 `chaosmod-1.0.0.jar` 放入 mods 文件夹

## 命令使用

所有命令都需要管理员权限（OP）。

### 基本语法
```
/chaosmod <效果名> <true/false>
```

### 可用命令
- `/chaosmod foodpoison <true/false>` - 食物中毒效果
- `/chaosmod mobignite <true/false>` - 怪物点燃效果
- `/chaosmod mobslowness <true/false>` - 怪物缓慢效果
- `/chaosmod mobblindness <true/false>` - 怪物失明效果
- `/chaosmod mobthorns <true/false>` - 怪物反伤效果
- `/chaosmod enderdragonbucket <true/false>` - 末影龙水桶变牛奶效果
- `/chaosmod enderdragonkill <true/false>` - 末影龙击杀死亡效果
- `/chaosmod playerdamageshare <true/false>` - 玩家平摊伤害效果
- `/chaosmod shieldnerf <true/false>` - 盾牌削弱效果
- `/chaosmod allhostile <true/false>` - 所有生物敌对效果
- `/chaosmod randomdamage <true/false>` - 随机伤害效果
- `/chaosmod lowhealthnoheal <true/false>` - 低血量无法回血效果
- `/chaosmod sharedhealth <true/false>` - 共享生命值效果
- `/chaosmod shareddamagesplit <true/false>` - 共享平摊伤害效果

### 状态命令
- `/chaosmod status` - 显示所有效果的当前状态
- `/chaosmod reset` - 重置所有效果为关闭状态

## 效果说明

### 食物效果
- **食物中毒**: 吃食物有30%概率中毒10秒

### 怪物战斗效果
- **怪物点燃**: 被怪物攻击时玩家会被点燃5秒
- **怪物缓慢**: 被怪物攻击时获得缓慢2效果5秒
- **怪物失明**: 被怪物攻击时获得失明效果1秒
- **怪物反伤**: 攻击怪物时受到50%反伤
- **所有生物敌对**: 所有生物都会主动攻击玩家

### 特殊效果
- **末影龙水桶**: 被末影龙攻击后背包中所有水桶变成牛奶桶
- **末影龙击杀死亡**: 击杀末影龙的玩家会立即死亡
- **盾牌削弱**: 盾牌只能吸收80%伤害，剩余20%会传递给玩家

### 玩家伤害系统
- **玩家平摊伤害**: 两个或多个玩家贴在一起时会平摊受到的伤害
- **随机伤害**: 受伤时伤害会随机转移到其他玩家身上
- **共享生命值**: 所有玩家共享生命值，一个受伤其他也受伤
- **共享平摊伤害**: 受伤时伤害平分给所有玩家

### 回血限制
- **低血量无法回血**: 血量只剩1颗心时有50%概率无法回血20秒

## 注意事项

1. **冲突效果**: 以下效果不能同时开启：
   - 玩家平摊伤害
   - 共享生命值
   - 共享平摊伤害

2. **默认状态**: 所有效果默认都是关闭的，需要手动开启

3. **权限要求**: 所有命令都需要管理员权限

## 示例

开启所有效果：
```
/chaosmod foodpoison true
/chaosmod mobignite true
/chaosmod mobslowness true
/chaosmod mobblindness true
/chaosmod mobthorns true
/chaosmod enderdragonbucket true
/chaosmod enderdragonkill true
/chaosmod playerdamageshare true
/chaosmod shieldnerf true
/chaosmod allhostile true
/chaosmod randomdamage true
/chaosmod lowhealthnoheal true
```

查看状态：
```
/chaosmod status
```

重置所有效果：
```
/chaosmod reset
```

