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

public class RTPUtil {

    private static Random r = new Random();
    private static Map<UUID, Location> preloads = new HashMap<>();

    public static void teleport(EnesRTPZone pl, Player p) {
        int max = pl.getConfig().getInt("rtp.max-radius", 10000);
        int min = pl.getConfig().getInt("rtp.min-radius", 1000);
        
        String wname = pl.getConfig().getString("rtp.target-world", "");
        World w;
        if (wname != null && !wname.isEmpty()) {
            w = Bukkit.getWorld(wname);
            if (w == null) {
                w = p.getWorld();
            }
        } else {
            w = p.getWorld();
        }

        boolean folia = false;
        try {
            Bukkit.class.getMethod("getRegionScheduler");
            folia = true;
        } catch (NoSuchMethodException e) {
            folia = false;
        }

        if (folia) {
            doFoliaTeleport(pl, p, w, max, min, 0);
        } else {
            doNormalTeleport(pl, p, w, max, min);
        }
    }

    private static void doFoliaTeleport(EnesRTPZone pl, Player p, World w, int max, int min, int tryCount) {
        if (tryCount > 15) {
            p.sendMessage(ColorUtil.color(pl.getConfig().getString("messages.prefix") + "&cGuvenli alan bulunamadi"));
            return;
        }

        int x = (r.nextInt(max - min) + min) * (r.nextBoolean() ? 1 : -1);
        int z = (r.nextInt(max - min) + min) * (r.nextBoolean() ? 1 : -1);

        try {
            Method getChunk = World.class.getMethod("getChunkAtAsync", int.class, int.class);
            CompletableFuture<Chunk> future = (CompletableFuture<Chunk>) getChunk.invoke(w, x >> 4, z >> 4);
            
            future.thenAccept(chunk -> {
                int y = w.getMaxHeight() - 1;
                Block b = null;
                Material m = Material.AIR;
                
                while (y > w.getMinHeight() + 5) {
                    b = chunk.getBlock(x & 0xF, y, z & 0xF);
                    m = b.getType();
                    if (m.isSolid() || m == Material.WATER || m == Material.LAVA) {
                        break;
                    }
                    y--;
                }
                
                if (m == Material.LAVA || m == Material.WATER || m == Material.CACTUS || y <= w.getMinHeight() + 5) {
                    y += 10;
                }
                
                Location loc = new Location(w, x + 0.5, y + 1, z + 0.5);
                
                try {
                    Method getSched = Bukkit.class.getMethod("getRegionScheduler");
                    Object sched = getSched.invoke(null);
                    Method exec = sched.getClass().getMethod("execute", org.bukkit.plugin.Plugin.class, World.class, int.class, int.class, Runnable.class);
                    
                    exec.invoke(sched, pl, w, x, z, (Runnable) () -> {
                        try {
                            Method tpAsync = Player.class.getMethod("teleportAsync", Location.class);
                            tpAsync.invoke(p, loc);
                            p.sendMessage(ColorUtil.color(pl.getConfig().getString("messages.prefix") + pl.getConfig().getString("messages.teleporting")));
                        } catch (Exception ex) {
                            pl.getLogger().warning("Teleport hatasi: " + ex.getMessage());
                        }
                    });
                } catch (Exception ex) {
                    pl.getLogger().warning("Schedule hatasi: " + ex.getMessage());
                }
            });
        } catch (Exception ex) {
            doFoliaTeleport(pl, p, w, max, min, tryCount + 1);
        }
    }

    private static void doNormalTeleport(EnesRTPZone pl, Player p, World w, int max, int min) {
        for (int i = 0; i <= 15; i++) {
            int x = (r.nextInt(max - min) + min) * (r.nextBoolean() ? 1 : -1);
            int z = (r.nextInt(max - min) + min) * (r.nextBoolean() ? 1 : -1);

            try {
                Chunk c = w.getChunkAt(x, z);
                int y = w.getMaxHeight() - 1;
                Block b = null;
                Material m = Material.AIR;

                while (y > w.getMinHeight() + 5) {
                    b = c.getBlock(x & 0xF, y, z & 0xF);
                    m = b.getType();
                    if (m.isSolid() || m == Material.WATER || m == Material.LAVA) break;
                    y--;
                }

                if (m.isSolid() && m != Material.LAVA && m != Material.WATER && m != Material.CACTUS) {
                    Location loc = new Location(w, x + 0.5, y + 1, z + 0.5);
                    p.teleport(loc);
                    p.sendMessage(ColorUtil.color(pl.getConfig().getString("messages.prefix") + pl.getConfig().getString("messages.teleporting")));
                    return;
                }
            } catch (Exception ex) {
            }
        }

        p.sendMessage(ColorUtil.color(pl.getConfig().getString("messages.prefix") + "&cGuvenli alan bulunamadi"));
    }

    public static void preload(EnesRTPZone pl, Player p) {
        int max = pl.getConfig().getInt("rtp.max-radius", 10000);
        int min = pl.getConfig().getInt("rtp.min-radius", 1000);
        
        String wname = pl.getConfig().getString("rtp.target-world", "");
        World w;
        if (wname != null && !wname.isEmpty()) {
            w = Bukkit.getWorld(wname);
            if (w == null) w = p.getWorld();
        } else {
            w = p.getWorld();
        }

        int x = (r.nextInt(max - min) + min) * (r.nextBoolean() ? 1 : -1);
        int z = (r.nextInt(max - min) + min) * (r.nextBoolean() ? 1 : -1);
        
        final World fw = w;
        final int fx = x;
        final int fz = z;

        try {
            Method getChunk = World.class.getMethod("getChunkAtAsync", int.class, int.class);
            CompletableFuture<Chunk> future = (CompletableFuture<Chunk>) getChunk.invoke(w, x >> 4, z >> 4);
            
            future.thenAccept(chunk -> {
                int y = fw.getMaxHeight() - 1;
                Block b = null;
                Material m = Material.AIR;
                
                while (y > fw.getMinHeight() + 5) {
                    b = chunk.getBlock(fx & 0xF, y, fz & 0xF);
                    m = b.getType();
                    if (m.isSolid() || m == Material.WATER || m == Material.LAVA) break;
                    y--;
                }
                
                if (m == Material.LAVA || m == Material.WATER || y <= fw.getMinHeight() + 5) {
                    y += 10;
                }
                
                Location loc = new Location(fw, fx + 0.5, y + 1, fz + 0.5);
                preloads.put(p.getUniqueId(), loc);
            });
        } catch (Exception ex) {
        }
    }

    public static void instantTeleport(EnesRTPZone pl, Player p) {
        Location loc = preloads.remove(p.getUniqueId());
        
        if (loc == null) {
            teleport(pl, p);
            return;
        }

        try {
            boolean folia = false;
            try {
                Bukkit.class.getMethod("getRegionScheduler");
                folia = true;
            } catch (NoSuchMethodException e) {
                folia = false;
            }

            if (folia) {
                Method getSched = Bukkit.class.getMethod("getRegionScheduler");
                Object sched = getSched.invoke(null);
                Method exec = sched.getClass().getMethod("execute", org.bukkit.plugin.Plugin.class, World.class, int.class, int.class, Runnable.class);
                
                exec.invoke(sched, pl, loc.getWorld(), loc.getBlockX(), loc.getBlockZ(), (Runnable) () -> {
                    try {
                        Method tpAsync = Player.class.getMethod("teleportAsync", Location.class);
                        tpAsync.invoke(p, loc);
                        p.sendMessage(ColorUtil.color(pl.getConfig().getString("messages.prefix") + pl.getConfig().getString("messages.teleporting")));
                    } catch (Exception ex) {
                        pl.getLogger().warning("Teleport hatasi: " + ex.getMessage());
                    }
                });
            } else {
                p.teleport(loc);
                p.sendMessage(ColorUtil.color(pl.getConfig().getString("messages.prefix") + pl.getConfig().getString("messages.teleporting")));
            }
        } catch (Exception ex) {
            p.teleport(loc);
        }
    }
}
