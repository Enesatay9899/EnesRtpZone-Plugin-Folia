package com.enes.enesrtpzone.listeners;

import com.enes.enesrtpzone.EnesRTPZone;
import com.enes.enesrtpzone.utils.ColorUtil;
import com.enes.enesrtpzone.utils.RTPUtil;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@SuppressWarnings("deprecation")
public class ZoneListener implements Listener {

    EnesRTPZone plugin;
    public static Map<UUID, Location> pos1 = new HashMap<>();
    public static Map<UUID, Location> pos2 = new HashMap<>();
    Map<UUID, ScheduledTask> tasks = new HashMap<>();
    Map<UUID, Location> preloadedLocations = new HashMap<>();

    public ZoneListener(EnesRTPZone p) {
        this.plugin = p;
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        if(event.getItem() == null) return;
        if(event.getItem().getType() != Material.WOODEN_AXE) return;
        if(!event.getItem().hasItemMeta()) return;

        String name = event.getItem().getItemMeta().getDisplayName();
        if(name == null) return;

        String expectedWandName = ColorUtil.color(plugin.getMessageManager().getMessage("wand-name", "&bRTPZone Wand"));
        String plainExpected = expectedWandName.replaceAll("§[0-9a-fk-or]", "").replaceAll("&[0-9a-fk-or]", "").trim().toLowerCase();
        String plainActual = name.replaceAll("§[0-9a-fk-or]", "").trim().toLowerCase();

        if(!plainActual.contains(plainExpected) && !plainActual.contains("wand")) return;

        if(!player.hasPermission("rtpzone.admin")) return;

        if(event.getAction() == Action.LEFT_CLICK_BLOCK && event.getClickedBlock() != null) {
            event.setCancelled(true);
            pos1.put(player.getUniqueId(), event.getClickedBlock().getLocation());

            player.sendMessage(ColorUtil.color(plugin.getMessageManager().getPrefix() + plugin.getMessageManager().getMessage("pos1-set")));

        } else if(event.getAction() == Action.RIGHT_CLICK_BLOCK && event.getClickedBlock() != null) {
            event.setCancelled(true);
            pos2.put(player.getUniqueId(), event.getClickedBlock().getLocation());

            player.sendMessage(ColorUtil.color(plugin.getMessageManager().getPrefix() + plugin.getMessageManager().getMessage("pos2-set")));
        }
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        if(event.getTo() == null) return;
        
        if(event.getFrom().getBlockX() == event.getTo().getBlockX() && 
           event.getFrom().getBlockY() == event.getTo().getBlockY() && 
           event.getFrom().getBlockZ() == event.getTo().getBlockZ()) return;

        Player player = event.getPlayer();
        UUID id = player.getUniqueId();
        boolean inside = plugin.zoneManager.isInAnyZone(event.getTo());

        if(inside && !tasks.containsKey(id)) {
            player.sendMessage(ColorUtil.color(plugin.getMessageManager().getPrefix() + plugin.getMessageManager().getMessage("entered-zone")));
            startCountdown(player);
        } else if(!inside && tasks.containsKey(id)) {
            player.sendMessage(ColorUtil.color(plugin.getMessageManager().getPrefix() + plugin.getMessageManager().getMessage("left-zone")));
            stopTask(player);
        }
    }

    void startCountdown(Player player) {
        UUID id = player.getUniqueId();
        preloadedLocations.remove(id);

        preloadWithChunkLoad(player);

        ScheduledTask task = player.getScheduler().runAtFixedRate(plugin, (t) -> {
            int time = plugin.getGlobalCountdown();

            if(time <= 0) {
                Bukkit.getGlobalRegionScheduler().execute(plugin, () -> {
                    player.sendTitle("", "", 5, 10, 5);
                });

                if(plugin.zoneManager.isInAnyZone(player.getLocation())) {
                    teleportWithChunkLoad(player);
                }

                stopTask(player);
                return;
            }

            String titleText = plugin.getMessageManager().getMessage("countdown-title").replace("%time%", String.valueOf(time));
            String subtitleText = plugin.getMessageManager().getMessage("countdown-subtitle");
            
            String title = ColorUtil.color(titleText);
            String subtitle = ColorUtil.color(subtitleText);
            
            int fadeIn = 5;
            int stay = 20;
            int fadeOut = 5;
            
            Bukkit.getGlobalRegionScheduler().execute(plugin, () -> {
                player.sendTitle(title, subtitle, fadeIn, stay, fadeOut);
            });

        }, null, 1L, 20L);

        tasks.put(id, task);
    }

    void preloadWithChunkLoad(Player player) {
        int max = plugin.getConfig().getInt("rtp.max-radius", 10000);
        int min = plugin.getConfig().getInt("rtp.min-radius", 1000);

        String wname = plugin.getConfig().getString("rtp.target-world", "");
        final org.bukkit.World w;
        if (wname != null && !wname.isEmpty()) {
            org.bukkit.World targetWorld = Bukkit.getWorld(wname);
            w = (targetWorld != null) ? targetWorld : player.getWorld();
        } else {
            w = player.getWorld();
        }

        java.util.Random r = new java.util.Random();
        int x = (r.nextInt(max - min) + min) * (r.nextBoolean() ? 1 : -1);
        int z = (r.nextInt(max - min) + min) * (r.nextBoolean() ? 1 : -1);

        int cx = x >> 4;
        int cz = z >> 4;

        final int fx = x;
        final int fz = z;
        final UUID playerId = player.getUniqueId();

        w.getChunkAtAsync(cx, cz).thenAccept(chunk -> {
            chunk.setForceLoaded(true);

            w.getChunkAtAsync(cx + 1, cz).thenAccept(c -> c.setForceLoaded(true));
            w.getChunkAtAsync(cx - 1, cz).thenAccept(c -> c.setForceLoaded(true));
            w.getChunkAtAsync(cx, cz + 1).thenAccept(c -> c.setForceLoaded(true));
            w.getChunkAtAsync(cx, cz - 1).thenAccept(c -> c.setForceLoaded(true));

            Bukkit.getRegionScheduler().execute(plugin, w, cx, cz, () -> {
                int y = w.getMaxHeight() - 1;
                org.bukkit.block.Block b = null;
                org.bukkit.Material m = org.bukkit.Material.AIR;

                while (y > w.getMinHeight() + 5) {
                    b = chunk.getBlock(fx & 0xF, y, fz & 0xF);
                    m = b.getType();
                    if (m.isSolid() || m == org.bukkit.Material.WATER || m == org.bukkit.Material.LAVA) {
                        break;
                    }
                    y--;
                }

                if (m == org.bukkit.Material.LAVA || m == org.bukkit.Material.WATER || y <= w.getMinHeight() + 5) {
                    y += 10;
                }

                Location loc = new Location(w, fx + 0.5, y + 1, fz + 0.5);
                preloadedLocations.put(playerId, loc);
            });
        });
    }

    void teleportWithChunkLoad(Player player) {
        Location loc = preloadedLocations.remove(player.getUniqueId());

        if (loc == null) {
            RTPUtil.teleport(plugin, player);
            return;
        }

        org.bukkit.World w = loc.getWorld();
        int cx = loc.getBlockX() >> 4;
        int cz = loc.getBlockZ() >> 4;

        Bukkit.getRegionScheduler().execute(plugin, w, cx, cz, () -> {
            player.teleport(loc);
            player.sendMessage(ColorUtil.color(plugin.getMessageManager().getPrefix() + plugin.getMessageManager().getMessage("teleporting")));
        });
    }

    void stopTask(Player player) {
        UUID id = player.getUniqueId();
        ScheduledTask task = tasks.remove(id);
        if(task != null) task.cancel();
        player.sendTitle("", "", 5, 10, 5);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        stopTask(event.getPlayer());
    }
}
