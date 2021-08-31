# ChunkHeat
广谱怪物农场限制器，达到上限后取消怪物生成和怪物掉落。

## 插件特性
* 生物生成控制
* 生物掉落控制
* 限制自动重置
* 特定生物加倍

## 插件命令
* /chunkheat - 查看服务器所有区块状态、在限制时输出准确坐标
* /chunkheat get - 查看所在区块的状态

## 插件配置文件
```yaml
whitelist-worlds: # 不受控的世界
  - world_ignored
# Reset Time: TimeUnit: minute
reset-time: 60 # 重置时间
limit: 1000 # 限制上限
whitelist-spawnreason: # 白名单生成原因，不计入限制
  - BEEHIVE
  - BREEDING
  - BUILD_IRONGOLEM
  - BUILD_SNOWMAN
  - BUILD_WITHER
  - COMMAND
  - SPAWNER
  - SPAWNER_EGG
  - EXPLOSION
  entity-weight: # 实体倍率，默认为1，即每次加1；设置为2那就是limit每次加2
    ELDER_GUARDIAN: 1
    WITHER_SKELETON: 1
    STRAY: 2
    HUSK: 1
    ZOMBIE_VILLAGER: 1
    SKELETON_HORSE: 1
    ZOMBIE_HORSE: 1
    ARMOR_STAND: 1
    DONKEY: 1
  ```
