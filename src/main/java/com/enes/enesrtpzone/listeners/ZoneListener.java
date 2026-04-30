package com.enes.enesrtpzone.listeners;

import com.enes.enesrtpzone.EnesRTPZone;
import com.enes.enesrtpzone.utils.ColorUtil;
import com.enes.enesrtpzone.utils.RTPUtil;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
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

public class ZoneListener implements Listener {

    EnesRTPZone plugin;
    public static Map<UUID, Location> pos1 = new HashMap<>();
    public static Map<UUID, Location> pos2 = new HashMap<>();
    Map<UUID, ScheduledTask> tasks = new HashMap<>();

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
        if(!name.contains("Wand")) return;
        
        if(!player.hasPermission("rtpzone.admin")) return;

        if(event.getAction() == Action.LEFT_CLICK_BLOCK && event.getClickedBlock() != null) {
            event.setCancelled(true);
            pos1.put(player.getUniqueId(), event.getClickedBlock().getLocation());
            
            String prefix = plugin.getConfig().getString("messages.prefix");
            String msg = plugin.getConfig().getString("messages.pos1-set");
            player.sendMessage(ColorUtil.color(prefix + msg));
            
        } else if(event.getAction() == Action.RIGHT_CLICK_BLOCK && event.getClickedBlock() != null) {
            event.setCancelled(true);
            pos2.put(player.getUniqueId(), event.getClickedBlock().getLocation());
            
            String prefix = plugin.getConfig().getString("messages.prefix");
            String msg = plugin.getConfig().getString("messages.pos2-set");
            player.sendMessage(ColorUtil.color(prefix + msg));
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
            String prefix = plugin.getConfig().getString("messages.prefix");
            String msg = plugin.getConfig().getString("messages.entered-zone");
            player.sendMessage(ColorUtil.color(prefix + msg));
            
            startCountdown(player);
        } else if(!inside && tasks.containsKey(id)) {
            stopTask(player);
        }
    }

    void startCountdown(Player player) {
        UUID id = player.getUniqueId();

        ScheduledTask task = player.getScheduler().runAtFixedRate(plugin, (t) -> {
            int time = plugin.getGlobalCountdown();

            if(time == 5) {
                if(plugin.zoneManager.isInAnyZone(player.getLocation())) {
                    RTPUtil.preload(plugin, player);
                }
            }

            if(time <= 0) {
                player.sendTitle("", "", 0, 1, 0);
                
                if(plugin.zoneManager.isInAnyZone(player.getLocation())) {
                    RTPUtil.instantTeleport(plugin, player);
                }
                
                stopTask(player);
                return;
            }

            String title = plugin.getConfig().getString("messages.countdown-title");
            title = title.replace("%time%", String.valueOf(time));
            String subtitle = plugin.getConfig().getString("messages.countdown-subtitle");
            
            player.sendTitle(ColorUtil.color(title), ColorUtil.color(subtitle), 0, 25, 0);

        }, null, 1L, 20L);

        tasks.put(id, task);
    }

    void stopTask(Player player) {
        UUID id = player.getUniqueId();
        ScheduledTask task = tasks.remove(id);
        if(task != null) task.cancel();
        player.sendTitle("", "", 0, 5, 0);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        stopTask(event.getPlayer());
    }
}
