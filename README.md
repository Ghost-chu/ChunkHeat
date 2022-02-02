# ChunkHeat
广谱怪物农场限制器，达到上限后取消怪物生成和怪物掉落。

## 插件特性
* 生物生成控制
* 生物掉落控制
* 限制自动重置
* 特定生物加倍

## 工作原理

ChunkHeat 监听生物生成/死亡事件，并为其行为所在的区块按照权重更新计数器。  
当计数器达到设置上限后，所有生成行为将会被抑制。  
同时，死亡后的经验和物品也不再掉落，作为抑制惩罚（这样玩家就不能欺骗插件在A生成，在B处死）。

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

## 关于重传

该插件是我在 Bilicraft-Community 任职时编写的插件，起初是为了解决服务器怪物农场导致的 TPS 尖峰问题。  
在离开 Bilicraft 后我决定继续对其维护更新。

## 重传是否存在版权问题

不存在，本插件 100% 由我个人编写，没有任何版权问题。  
同时，在任职期间 Bilicraft 并未向我支付任何劳动报酬，因此也不存在任何的风险。
