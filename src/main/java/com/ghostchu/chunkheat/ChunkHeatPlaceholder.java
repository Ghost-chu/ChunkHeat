package com.ghostchu.chunkheat;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.Chunk;
import org.bukkit.entity.Player;

public class ChunkHeatPlaceholder extends PlaceholderExpansion {
    private final ChunkHeat plugin;

    public ChunkHeatPlaceholder(ChunkHeat plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean canRegister() {
        return true;
    }

    @Override
    public String getIdentifier() {
        return "chunkheat";
    }

    @Override
    public String getAuthor() {
        return "T14D3";
    }

    @Override
    public String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public String onPlaceholderRequest(Player player, String identifier) {
        if (player == null) {
            return "";
        }

        if (identifier.equals("value")) {
            Chunk chunk = player.getLocation().getChunk();
            LimitEntry entry = plugin.chunkHeapMap.getIfPresent(chunk);
            return String.valueOf(entry == null ? "0" : entry.getCounter().get());
        }

        return null;
    }
}
