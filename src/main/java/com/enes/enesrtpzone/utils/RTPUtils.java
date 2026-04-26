package com.enes.enesrtpzone.utils;

import com.enes.enesrtpzone.EnesRTPZone;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class RTPUtils {

    private static final Random random = new Random();
    private static final Map<UUID, Location> preloadedLocations = new HashMap<>();

    public static void teleportSafely(EnesRTPZone plugin, Player player) {
        int max = plugin.getConfig().getInt("rtp.max-radius", 10000);
        int min = plugin.getConfig().getInt("rtp.min-radius", 1000);
        
        
        String targetWorldName = plugin.getConfig().getString("rtp.target-world", "");
        World world;
        if (targetWorldName != null && !targetWorldName.isEmpty()) {
            world = Bukkit.getWorld(targetWorldName);
            if (world == null) {
                plugin.getLogger().warning("Target world '" + targetWorldName + "' not found! Using player's current world.");
                world = player.getWorld();
            }
        } else {
            world = player.getWorld();
        }

        // Create final reference for lambda
        final World targetWorld = world;

        // Check if Folia's getRegionScheduler exists
        boolean isFolia = false;
        try {
            Bukkit.class.getMethod("getRegionScheduler");
            isFolia = true;
        } catch (NoSuchMethodException e) {
            isFolia = false;
        }

        if (isFolia) {
            plugin.getLogger().info("Folia detected, using async chunk loading for " + player.getName());
            teleportAsyncFolia(plugin, player, targetWorld, max, min, 0);
        } else {
            plugin.getLogger().info("Using Bukkit scheduler for " + player.getName());
            Bukkit.getScheduler().runTask(plugin, () -> {
                performTeleport(plugin, player, targetWorld, max, min);
            });
        }
    }

    private static void teleportAsyncFolia(EnesRTPZone plugin, Player player, World world, int max, int min, int attempt) {
        if (attempt > 15) {
            plugin.getLogger().warning("Failed to find safe location after 16 attempts");
            player.sendMessage(ColorUtils.format(plugin.getConfig().getString("messages.prefix") + "&cGuvenli alan bulunamadi, yeniden deneyin."));
            return;
        }

        int x = (random.nextInt(max - min) + min) * (random.nextBoolean() ? 1 : -1);
        int z = (random.nextInt(max - min) + min) * (random.nextBoolean() ? 1 : -1);

        
        scheduleTeleport(plugin, player, world, x, z, max, min, attempt);
    }

    private static void scheduleTeleport(EnesRTPZone plugin, Player player, World world, int x, int z, int max, int min, int attempt) {
        try {
            // Use getChunkAtAsync to avoid blocking the region thread
            Method getChunkAtAsync = World.class.getMethod("getChunkAtAsync", int.class, int.class);
            CompletableFuture<Chunk> chunkFuture = (CompletableFuture<Chunk>) getChunkAtAsync.invoke(world, x >> 4, z >> 4);
            
            chunkFuture.thenAccept(chunk -> {
                try {
                    int y = world.getMaxHeight() - 1;
                    Block block = null;
                    Material type = Material.AIR;
                    
                    while (y > world.getMinHeight() + 5) {
                        block = chunk.getBlock(x & 0xF, y, z & 0xF);
                        type = block.getType();
                        if (type.isSolid() || type == Material.WATER || type == Material.LAVA) {
                            break;
                        }
                        y--;
                    }
                    
                    if (type == Material.LAVA || type == Material.WATER || type == Material.CACTUS || type == Material.MAGMA_BLOCK || y <= world.getMinHeight() + 5) {
                        y += 10; 
                    }
                    
                    Location loc = new Location(world, x + 0.5, y + 1, z + 0.5);
                    
                    // Schedule teleport on region scheduler
                    Method getRegionScheduler = Bukkit.class.getMethod("getRegionScheduler");
                    Object scheduler = getRegionScheduler.invoke(null);
                    Method execute = scheduler.getClass().getMethod("execute", org.bukkit.plugin.Plugin.class, World.class, int.class, int.class, Runnable.class);
                    execute.invoke(scheduler, plugin, world, x, z, (Runnable) () -> {
                        try {
                            Method teleportAsync = Player.class.getMethod("teleportAsync", Location.class);
                            teleportAsync.invoke(player, loc);
                            player.sendMessage(ColorUtils.format(plugin.getConfig().getString("messages.prefix") + plugin.getConfig().getString("messages.teleporting")));
                            plugin.getLogger().info("SUCCESS: Teleported to " + loc);
                        } catch (Exception ex) {
                            plugin.getLogger().severe("Teleport invoke failed: " + ex.getMessage());
                        }
                    });
                } catch (Exception teleEx) {
                    plugin.getLogger().severe("Chunk processing failed: " + teleEx.getMessage());
                    teleportAsyncFolia(plugin, player, world, max, min, attempt + 1);
                }
            }).exceptionally(ex -> {
                plugin.getLogger().severe("Async chunk load failed: " + ex.getMessage());
                teleportAsyncFolia(plugin, player, world, max, min, attempt + 1);
                return null;
            });
        } catch (Exception ex) {
            plugin.getLogger().severe("Schedule teleport failed: " + ex.getMessage());
            // Fallback: retry
            teleportAsyncFolia(plugin, player, world, max, min, attempt + 1);
        }
    }

    private static void performTeleport(EnesRTPZone plugin, Player player, World world, int max, int min) {
        for (int attempt = 0; attempt <= 15; attempt++) {
            int x = (random.nextInt(max - min) + min) * (random.nextBoolean() ? 1 : -1);
            int z = (random.nextInt(max - min) + min) * (random.nextBoolean() ? 1 : -1);

            try {
                Chunk chunk = world.getChunkAt(x, z);

                
                int y = world.getMaxHeight() - 1;
                Block block = null;
                Material type = Material.AIR;

                while (y > world.getMinHeight() + 5) {
                    block = chunk.getBlock(x & 0xF, y, z & 0xF);
                    type = block.getType();
                    if (type.isSolid() || type == Material.WATER || type == Material.LAVA) {
                        break;
                    }
                    y--;
                }

                if (type.isSolid() && type != Material.LAVA && type != Material.WATER &&
                    type != Material.MAGMA_BLOCK && type != Material.CACTUS && y > world.getMinHeight() + 5) {

                    Location loc = new Location(world, x + 0.5, y + 1, z + 0.5);
                    player.teleport(loc);
                    player.sendMessage(ColorUtils.format(plugin.getConfig().getString("messages.prefix") + plugin.getConfig().getString("messages.teleporting")));
                    plugin.getLogger().info("Paper/Purpur teleport success to " + loc);
                    return;
                }
            } catch (Exception ex) {
                plugin.getLogger().warning("Paper attempt " + attempt + " error: " + ex.getMessage());
            }
        }

        player.sendMessage(ColorUtils.format(plugin.getConfig().getString("messages.prefix") + "&cGuvenli alan bulunamadi, yeniden deneyin."));
    }

    // Preload teleport location 1 second before countdown ends
    public static void preloadTeleport(EnesRTPZone plugin, Player player) {
        int max = plugin.getConfig().getInt("rtp.max-radius", 10000);
        int min = plugin.getConfig().getInt("rtp.min-radius", 1000);
        
        String targetWorldName = plugin.getConfig().getString("rtp.target-world", "");
        World world;
        if (targetWorldName != null && !targetWorldName.isEmpty()) {
            world = Bukkit.getWorld(targetWorldName);
            if (world == null) {
                world = player.getWorld();
            }
        } else {
            world = player.getWorld();
        }

        // Generate random location and preload chunk
        int x = (random.nextInt(max - min) + min) * (random.nextBoolean() ? 1 : -1);
        int z = (random.nextInt(max - min) + min) * (random.nextBoolean() ? 1 : -1);
        
        final World targetWorld = world;
        final int finalX = x;
        final int finalZ = z;

        try {
            Method getChunkAtAsync = World.class.getMethod("getChunkAtAsync", int.class, int.class);
            CompletableFuture<Chunk> chunkFuture = (CompletableFuture<Chunk>) getChunkAtAsync.invoke(world, x >> 4, z >> 4);
            
            chunkFuture.thenAccept(chunk -> {
                int y = targetWorld.getMaxHeight() - 1;
                Block block = null;
                Material type = Material.AIR;
                
                while (y > targetWorld.getMinHeight() + 5) {
                    block = chunk.getBlock(finalX & 0xF, y, finalZ & 0xF);
                    type = block.getType();
                    if (type.isSolid() || type == Material.WATER || type == Material.LAVA) {
                        break;
                    }
                    y--;
                }
                
                if (type == Material.LAVA || type == Material.WATER || type == Material.CACTUS || type == Material.MAGMA_BLOCK || y <= targetWorld.getMinHeight() + 5) {
                    y += 10;
                }
                
                Location loc = new Location(targetWorld, finalX + 0.5, y + 1, finalZ + 0.5);
                preloadedLocations.put(player.getUniqueId(), loc);
                plugin.getLogger().info("Preloaded teleport location for " + player.getName() + ": " + loc);
            });
        } catch (Exception ex) {
            plugin.getLogger().warning("Preload failed for " + player.getName() + ": " + ex.getMessage());
        }
    }

    // Complete instant teleport using preloaded location
    public static void completeInstantTeleport(EnesRTPZone plugin, Player player) {
        Location loc = preloadedLocations.remove(player.getUniqueId());
        
        if (loc == null) {
            // Fallback if preload failed
            plugin.getLogger().warning("No preloaded location for " + player.getName() + ", using fallback");
            teleportSafely(plugin, player);
            return;
        }

        try {
            // Check if Folia
            boolean isFolia = false;
            try {
                Bukkit.class.getMethod("getRegionScheduler");
                isFolia = true;
            } catch (NoSuchMethodException e) {
                isFolia = false;
            }

            if (isFolia) {
                // Use teleportAsync on Folia
                Method getRegionScheduler = Bukkit.class.getMethod("getRegionScheduler");
                Object scheduler = getRegionScheduler.invoke(null);
                Method execute = scheduler.getClass().getMethod("execute", org.bukkit.plugin.Plugin.class, World.class, int.class, int.class, Runnable.class);
                
                final Location finalLoc = loc;
                execute.invoke(scheduler, plugin, loc.getWorld(), loc.getBlockX(), loc.getBlockZ(), (Runnable) () -> {
                    try {
                        Method teleportAsync = Player.class.getMethod("teleportAsync", Location.class);
                        teleportAsync.invoke(player, finalLoc);
                        player.sendMessage(ColorUtils.format(plugin.getConfig().getString("messages.prefix") + plugin.getConfig().getString("messages.teleporting")));
                        plugin.getLogger().info("INSTANT teleport for " + player.getName() + " to " + finalLoc);
                    } catch (Exception ex) {
                        plugin.getLogger().severe("Instant teleport failed: " + ex.getMessage());
                    }
                });
            } else {
                // Direct teleport on Paper/Purpur
                player.teleport(loc);
                player.sendMessage(ColorUtils.format(plugin.getConfig().getString("messages.prefix") + plugin.getConfig().getString("messages.teleporting")));
                plugin.getLogger().info("INSTANT teleport for " + player.getName() + " to " + loc);
            }
        } catch (Exception ex) {
            plugin.getLogger().severe("Complete teleport failed: " + ex.getMessage());
            // Fallback
            player.teleport(loc);
        }
    }
}
