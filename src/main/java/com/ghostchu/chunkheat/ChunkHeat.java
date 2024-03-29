package com.ghostchu.chunkheat;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.event.world.WorldUnloadEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;
import java.util.concurrent.TimeUnit;

public final class ChunkHeat extends JavaPlugin implements Listener {
    Cache<Chunk, LimitEntry> chunkHeapMap;
    private final Set<String> whitelistedWorld = new HashSet<>();
    private final Set<CreatureSpawnEvent.SpawnReason> whitelistedSpawnReason = new HashSet<>();
    private int limit;
    private final Map<EntityType, Integer> entityWeight = new HashMap<>();
    private boolean preventDropXp; // New config option to prevent XP dropping
    private boolean preventDropItems; // New config option to prevent item dropping
    private boolean increaseHeatOnSpawn; // New config option to increase "heat" on mob spawn
    private int minPlayersToIncreaseHeat; // New config option for minimum player count to increase "heat"


    @Override
    public void onEnable() {
        saveDefaultConfig();
        CacheBuilder<Object, Object> cacheBuilder = CacheBuilder.newBuilder()
                .initialCapacity(10000)
                .maximumSize(10000);
        if (getConfig().getInt("reset-mode", 0) == 1) {
            cacheBuilder.expireAfterAccess(getConfig().getInt("reset-time", 60), TimeUnit.MINUTES);
        } else {
            cacheBuilder.expireAfterWrite(getConfig().getInt("reset-time", 60), TimeUnit.MINUTES);
        }
        this.chunkHeapMap = cacheBuilder.build();

        ConfigurationSection entityWeightSection = getConfig().getConfigurationSection("entity-weight");
        if (entityWeightSection == null) {
            entityWeightSection = getConfig().createSection("entity-weight");
        }
        for (EntityType value : EntityType.values()) {
            if (!value.isAlive()) continue;
            if (entityWeightSection.get(value.name()) == null)
                entityWeightSection.set(value.name(), 1);
            entityWeight.put(value, entityWeightSection.getInt(value.name(), 1));
        }

        getConfig().getStringList("whitelist-worlds").forEach(world -> {
            World bukkitWorld = Bukkit.getWorld(world);
            if (bukkitWorld == null) return;
            whitelistedWorld.add(bukkitWorld.getName());
        });
        getConfig().getStringList("whitelist-spawnreason").forEach(reason -> {
            try {
                CreatureSpawnEvent.SpawnReason bukkitReason = CreatureSpawnEvent.SpawnReason.valueOf(reason);
                whitelistedSpawnReason.add(bukkitReason);
            } catch (IllegalArgumentException ignored) {
            }
        });
        limit = getConfig().getInt("limit", 5000);
        preventDropXp = getConfig().getBoolean("prevent-drop-xp", false);
        preventDropItems = getConfig().getBoolean("prevent-drop-items", false);
        Bukkit.getPluginManager().registerEvents(this, this);
        saveConfig();

        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new ChunkHeatPlaceholder(this).register();
        }

        increaseHeatOnSpawn = getConfig().getBoolean("increase-heat-on-spawn", true);
        minPlayersToIncreaseHeat = getConfig().getInt("min-players-to-increase-heat", -1);

        updateConfig();
    }

    private void loadConfiguration() {
        CacheBuilder<Object, Object> cacheBuilder = CacheBuilder.newBuilder()
                .initialCapacity(10000)
                .maximumSize(10000);
        if (getConfig().getInt("reset-mode", 0) == 1) {
            cacheBuilder.expireAfterAccess(getConfig().getInt("reset-time", 60), TimeUnit.MINUTES);
        } else {
            cacheBuilder.expireAfterWrite(getConfig().getInt("reset-time", 60), TimeUnit.MINUTES);
        }
        this.chunkHeapMap = cacheBuilder.build();

        ConfigurationSection entityWeightSection = getConfig().getConfigurationSection("entity-weight");
        if (entityWeightSection == null) {
            entityWeightSection = getConfig().createSection("entity-weight");
        }
        entityWeight.clear();
        for (EntityType value : EntityType.values()) {
            if (!value.isAlive()) continue;
            if (entityWeightSection.get(value.name()) == null)
                entityWeightSection.set(value.name(), 1);
            entityWeight.put(value, entityWeightSection.getInt(value.name(), 1));
        }

        whitelistedWorld.clear();
        getConfig().getStringList("whitelist-worlds").forEach(world -> {
            World bukkitWorld = Bukkit.getWorld(world);
            if (bukkitWorld != null) {
                whitelistedWorld.add(bukkitWorld.getName());
            }
        });

        whitelistedSpawnReason.clear();
        getConfig().getStringList("whitelist-spawnreason").forEach(reason -> {
            try {
                CreatureSpawnEvent.SpawnReason bukkitReason = CreatureSpawnEvent.SpawnReason.valueOf(reason);
                whitelistedSpawnReason.add(bukkitReason);
            } catch (IllegalArgumentException ignored) {
            }
        });

        limit = getConfig().getInt("limit", 5000);

        preventDropXp = getConfig().getBoolean("prevent-drop-xp", false);
        preventDropItems = getConfig().getBoolean("prevent-drop-items", false);
    }

    private void updateConfig() {
        InputStream defaultConfigStream = getResource("config.yml");
        if (defaultConfigStream != null) {
            YamlConfiguration defaultConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(defaultConfigStream));

            // Get the live configuration
            File configFile = new File(getDataFolder(), "config.yml");
            YamlConfiguration liveConfig = YamlConfiguration.loadConfiguration(configFile);

            // Compare and add missing keys
            for (String key : defaultConfig.getKeys(true)) {
                if (!liveConfig.isSet(key)) {
                    liveConfig.set(key, defaultConfig.get(key));
                }
            }

            // Save the updated live configuration
            try {
                liveConfig.save(configFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        chunkHeapMap.invalidateAll();
        whitelistedWorld.clear();
        whitelistedSpawnReason.clear();
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onWorldLoad(WorldLoadEvent event) {
        getConfig().getStringList("whitelist-worlds").forEach(world -> {
            World bukkitWorld = Bukkit.getWorld(world);
            if (bukkitWorld == null) return;
            whitelistedWorld.add(bukkitWorld.getName());
        });
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onWorldUnload(WorldUnloadEvent event) {
        whitelistedWorld.remove(event.getWorld().getName());
        chunkHeapMap.asMap().keySet().removeIf(chunk -> chunk.getWorld() == event.getWorld());
    }

    @EventHandler(ignoreCancelled = true)
    public void onMobSpawning(CreatureSpawnEvent event) {
        if (!(event.getEntity() instanceof Mob)) return;
        if (whitelistedSpawnReason.contains(event.getSpawnReason())) return;
        if (whitelistedWorld.contains(event.getLocation().getWorld().getName())) return;
        Chunk chunk = event.getLocation().getChunk();

        // Check if the min player count condition is met
        if (minPlayersToIncreaseHeat < 0 || Bukkit.getOnlinePlayers().size() >= minPlayersToIncreaseHeat) {
            if (increaseHeatOnSpawn) {
                // Increase the "heat" score in the chunk where the mob spawns based on entity type weight
                LimitEntry counter = chunkHeapMap.getIfPresent(chunk);
                if (counter == null) {
                    counter = new LimitEntry();
                    chunkHeapMap.put(chunk, counter);
                }
                int counts = counter.getCounter().getAndAdd(entityWeight.getOrDefault(event.getEntityType(), 1));
                if (counts > limit) {
                    event.setCancelled(true);
                }
            }
        }

        // Check if the mob spawning should also increase the "heat" score in the chunk where it dies
        if (increaseHeatOnSpawn || event.getSpawnReason() != CreatureSpawnEvent.SpawnReason.CUSTOM) {
            // Increase the "heat" score in the chunk where the mob dies
            event.getEntity().setMetadata("chunkheat-spawned-in-chunk", new FixedMetadataValue(this, chunk));
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onMobDeath(EntityDeathEvent event) {
        if (!(event.getEntity() instanceof Mob)) return;
        if (whitelistedWorld.contains(event.getEntity().getWorld().getName())) return;
        if (event.getEntity().getLastDamageCause() != null && event.getEntity().getLastDamageCause().getEntity() instanceof Player)
            return;

        // Check if the mob died in a different chunk than where it spawned
        if (event.getEntity().hasMetadata("chunkheat-spawned-in-chunk")) {
            Chunk spawnedInChunk = (Chunk) event.getEntity().getMetadata("chunkheat-spawned-in-chunk").get(0).value();

            if (!spawnedInChunk.equals(event.getEntity().getLocation().getChunk())) {
                // Increase the "heat" score in the chunk where the mob dies based on entity type weight
                LimitEntry deathChunkCounter = chunkHeapMap.getIfPresent(event.getEntity().getLocation().getChunk());
                if (deathChunkCounter == null) {
                    deathChunkCounter = new LimitEntry();
                    chunkHeapMap.put(event.getEntity().getLocation().getChunk(), deathChunkCounter);
                }
                int deathChunkCounts = deathChunkCounter.getCounter().getAndAdd(entityWeight.getOrDefault(event.getEntityType(), 1));
                if (deathChunkCounts > limit) {
                    // Check configuration options to determine whether to prevent XP and items from dropping
                    if (preventDropXp) {
                        event.setDroppedExp(0);
                    }
                    if (preventDropItems) {
                        event.getDrops().clear();
                    }
                }

                // Increase the "heat" score in the chunk where the mob spawns
                LimitEntry spawnChunkCounter = chunkHeapMap.getIfPresent(spawnedInChunk);
                if (spawnChunkCounter == null) {
                    spawnChunkCounter = new LimitEntry();
                    chunkHeapMap.put(spawnedInChunk, spawnChunkCounter);
                }
                int spawnChunkCounts = spawnChunkCounter.getCounter().getAndAdd(entityWeight.getOrDefault(event.getEntityType(), 1));
                if (spawnChunkCounts > limit) {
                    // You can optionally choose to prevent something in the spawn chunk as well if needed.
                    // For example, you might want to prevent spawning in the spawn chunk too.
                    // spawnChunkCounter.setCounter(limit); // Set the counter to the limit to prevent spawning in the spawn chunk.
                }
            }
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("chunkheat.admin")) {
            sender.sendMessage("No permission");
            return true;
        }

        if (args.length < 1) {
            sender.sendMessage("Reading all data... " + this.chunkHeapMap.size());
            //this.chunkHeapMap.asMap().entrySet().forEach(set->sender.sendMessage(set.toString()));
            new LinkedHashMap<>(chunkHeapMap.asMap()).entrySet().stream().sorted(Comparator.comparingInt(o -> o.getValue().getCounter().get())).forEach(data -> {
                String color = ChatColor.GREEN.toString();
                if (data.getValue().getCounter().get() > limit) {
                    color = ChatColor.YELLOW.toString();
                    sender.sendMessage(color + "[Suppressed] " + data.getKey().getWorld().getName() + "," + data.getKey().getX() + "," + data.getKey().getX()
                            + " => " + data.getValue().toString() + "(" + data.getKey().getBlock(0, 0, 0).getLocation() + ")");
                } else {
                    sender.sendMessage(color + data.getKey().getWorld().getName() + "," + data.getKey().getX() + "," + data.getKey().getX()
                            + " => " + data.getValue().toString());
                }


            });
        } else {
            //noinspection SwitchStatementWithTooFewBranches
            switch (args[0]) {
                case "get":
                    if (sender instanceof Player) {
                        Chunk chunk = ((Player) sender).getLocation().getChunk();
                        LimitEntry entry = chunkHeapMap.getIfPresent(chunk);
                        sender.sendMessage(ChatColor.BLUE + "This Chunk heat value is: " + ChatColor.YELLOW + (entry == null ? "0" : entry.getCounter().get()));
                    } else {
                        sender.sendMessage("This command only can be executed by Player.");
                    }
                    break;
                case "reload":
                    reloadConfig(); // Reload the configuration from config.yml
                    loadConfiguration(); // Load the new configuration settings into the plugin
                    sender.sendMessage(ChatColor.GREEN + "ChunkHeat configuration reloaded.");
            }

        }


        return true;
    }
}
