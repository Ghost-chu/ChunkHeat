package com.bilicraft.chunkheat;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.entity.Mob;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.event.world.WorldUnloadEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public final class ChunkHeat extends JavaPlugin implements Listener {

    private final Cache<Chunk, LimitEntry> chunkHeapMap = CacheBuilder.newBuilder()
            .expireAfterWrite(30, TimeUnit.MINUTES)
            .maximumSize(50000)
            .build();

    private final Set<UUID> whitelistedWorld = new CopyOnWriteArraySet<>();
    private final Set<CreatureSpawnEvent.SpawnReason> whitelistedSpawnReason = new CopyOnWriteArraySet<>();
    private int limit;

    @Override
    public void onEnable() {
        // Plugin startup logic
        chunkHeapMap.invalidateAll(); // Reset data
        saveDefaultConfig();
        getConfig().getStringList("whitelist-worlds").forEach(world -> {
            World bukkitWorld = Bukkit.getWorld(world);
            if (bukkitWorld == null) return;
            whitelistedWorld.add(bukkitWorld.getUID());
        });
        getConfig().getStringList("whitelist-spawnreason").forEach(reason -> {
            try {
                CreatureSpawnEvent.SpawnReason bukkitReason = CreatureSpawnEvent.SpawnReason.valueOf(reason);
                whitelistedSpawnReason.add(bukkitReason);
            } catch (IllegalArgumentException ignored) {
            }
        });
        limit = getConfig().getInt("limit", 5000);
        Bukkit.getPluginManager().registerEvents(this, this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        chunkHeapMap.invalidateAll(); // Garbage collection
        whitelistedWorld.clear();
        whitelistedSpawnReason.clear();
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onWorldLoad(WorldLoadEvent event) {
        getConfig().getStringList("whitelist-worlds").forEach(world -> {
            World bukkitWorld = Bukkit.getWorld(world);
            if (bukkitWorld == null) return;
            whitelistedWorld.add(bukkitWorld.getUID());
        });
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onWorldUnload(WorldUnloadEvent event) {
        whitelistedWorld.remove(event.getWorld().getUID());
        chunkHeapMap.asMap().keySet().removeIf(chunk -> chunk.getWorld() == event.getWorld());
    }

    @EventHandler(ignoreCancelled = true)
    public void onMobSpawning(CreatureSpawnEvent event) {
        if (!(event.getEntity() instanceof Mob)) return;
        if (whitelistedSpawnReason.contains(event.getSpawnReason())) return;
        //noinspection ConstantConditions
        if (!whitelistedWorld.contains(event.getLocation().getWorld().getUID())) return;
        Chunk chunk = event.getLocation().getChunk();
        LimitEntry counter = chunkHeapMap.getIfPresent(chunk);
        if (counter == null) {
            counter = new LimitEntry(new AtomicInteger(0), System.currentTimeMillis());
            chunkHeapMap.put(chunk, counter);
        }
        int counts = counter.getAInteger().incrementAndGet();
        if (counts > limit) {
            event.setCancelled(true);
        }
    }
}
