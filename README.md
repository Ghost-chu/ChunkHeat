# ChunkHeat
Broad Spectrum Monster Farm Limiter.

## 插件特性
* Mob spawning control
* Mob drops control
* Automaticlly reset for restriction
* Custom Weight

## 工作原理

ChunkHeat monitoring the mob spawning/deathing events and update the counter.  
Once the counter reached the limit, the spawning and dropping will be engage.  
And same time, the exp points and items drops will be gone until counter reset.

## Commands
* /chunkheat - Check the chunks heat.
* /chunkheat get - Check the current chunks heat.

## Configuration
```yaml
whitelist-worlds:
  - world_ignored
# Reset Time: TimeUnit: minute
reset-time: 60
limit: 1000
whitelist-spawnreason:
  - BEEHIVE
  - BREEDING
  - BUILD_IRONGOLEM
  - BUILD_SNOWMAN
  - BUILD_WITHER
  - COMMAND
  - SPAWNER
  - SPAWNER_EGG
  - EXPLOSION
  entity-weight:
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
