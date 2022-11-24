# ChunkHeat
Broad Spectrum Monster Farm Limiter.

[Modrinth](https://modrinth.com/plugin/chunkheat)

## Features

* Mob spawning control.
* Mob drops control.
* Automatically reset for restriction.
* Custom Weight.

## Working principle

ChunkHeat monitoring the mob spawning/death events and update the counter, once the counter reached the limit, the
spawning and dropping will be engaged.  
And same time, the experiences and items drops will be disabled until restriction removed on target chunk.

Different from other Farm restriction plugins, ChunkHeat can fundamentally solve the problem of players creating mob
farm, and can strengthen restrictions for specific creature types.

## Work mode

ChunkHeat have two work mode:

* Reset every `<reset-time>` minutes, and restriction will be removed.
* Reset when chunk keep `<reset-time>` minutes no mob spawns/deaths and then restriction will be removed.

## Commands

* /chunkheat - Check the chunks heat.
* /chunkheat get - Check the current chunks heat.

## Configuration

```yaml
config-version: 1
# The world name that won't count
whitelist-worlds:
  - world_ignored
# Reset Time: TimeUnit: minute
reset-time: 60
# Reset Mode: 0 for reset every <reset-time> minutes
#             1 for reset every <reset-time> minutes but only that chunk no any activity in <reset-time> minutes
reset-mode: 0
# The threshold that will restrict the mob spawning and items/exp dropping.
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
  - CURED
  - SILVERFISH_BLOCK
  - SHEARED
  - SHOULDER_ENTITY
entity-weight:
  WITHER_SKELETON: 5
  STRAY: 3
  HUSK: 3
  ZOMBIFIED_PIGLIN: 3
  MUSHROOM_COW: 1
  SNOWMAN: 1
  OCELOT: 1
  IRON_GOLEM: 100

  ```
