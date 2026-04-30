package com.enes.enesrtpzone.commands;

import com.enes.enesrtpzone.EnesRTPZone;
import com.enes.enesrtpzone.listeners.ZoneListener;
import com.enes.enesrtpzone.utils.ColorUtil;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

public class ZoneCommand implements CommandExecutor {

    EnesRTPZone plugin;

    public ZoneCommand(EnesRTPZone p) {
        this.plugin = p;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if(!(sender instanceof Player)) {
            sender.sendMessage("Sadece oyuncular kullanabilir!");
            return true;
        }
        
        Player player = (Player)sender;
        
        if(!player.hasPermission("rtpzone.admin")) {
            player.sendMessage(ColorUtil.color("&cYetkin yok!"));
            return true;
        }

        String prefix = ColorUtil.color(plugin.getConfig().getString("messages.prefix"));

        if(args.length == 0) {
            showHelp(player, prefix);
            return true;
        }

        String sub = args[0].toLowerCase();

        if(sub.equals("wand")) {
            ItemStack axe = new ItemStack(Material.WOODEN_AXE);
            ItemMeta meta = axe.getItemMeta();
            meta.setDisplayName(ColorUtil.color("&bRTPZone Wand"));
            meta.setLore(Arrays.asList(
                ColorUtil.color("&7Sol tik: 1. konum"), 
                ColorUtil.color("&7Sag tik: 2. konum")
            ));
            axe.setItemMeta(meta);
            
            player.getInventory().addItem(axe);
            player.sendMessage(prefix + ColorUtil.color(plugin.getConfig().getString("messages.wand-received")));
            return true;
        }

        if(sub.equals("delete")) {
            if(args.length < 2) {
                player.sendMessage(prefix + ColorUtil.color("&cKullanim: /rtpzone delete <isim>"));
                return true;
            }
            
            String name = args[1];
            
            if(!plugin.zoneManager.zoneExists(name)) {
                player.sendMessage(prefix + ColorUtil.color("&cBolge bulunamadi: " + name));
                return true;
            }
            
            plugin.zoneManager.deleteZone(name);
            player.sendMessage(prefix + ColorUtil.color("&aBolge silindi: " + name));
            return true;
        }

        if(sub.equals("reload")) {
            plugin.reloadConfig();
            plugin.zoneManager.reloadZones();
            player.sendMessage(prefix + ColorUtil.color("&aConfig yenilendi!"));
            return true;
        }

        if(sub.equals("reset") || sub.equals("clear")) {
            ZoneListener.pos1.remove(player.getUniqueId());
            ZoneListener.pos2.remove(player.getUniqueId());
            player.sendMessage(prefix + ColorUtil.color("&aSecimler temizlendi!"));
            return true;
        }

        if(sub.equals("create")) {
            if(args.length < 2) {
                player.sendMessage(prefix + ColorUtil.color("&cKullanim: /rtpzone create <isim>"));
                return true;
            }
            
            Location pos1 = ZoneListener.pos1.get(player.getUniqueId());
            Location pos2 = ZoneListener.pos2.get(player.getUniqueId());

            if(pos1 == null || pos2 == null) {
                player.sendMessage(prefix + ColorUtil.color(plugin.getConfig().getString("messages.select-first")));
                return true;
            }

            String zoneName = args[1];
            
            plugin.zoneManager.createZone(zoneName, pos1, pos2);
            
            String msg = plugin.getConfig().getString("messages.zone-created");
            msg = msg.replace("%name%", zoneName);
            player.sendMessage(prefix + ColorUtil.color(msg));
            
            ZoneListener.pos1.remove(player.getUniqueId());
            ZoneListener.pos2.remove(player.getUniqueId());
            return true;
        }

        showHelp(player, prefix);
        return true;
    }
    
    void showHelp(Player player, String prefix) {
        player.sendMessage(prefix + ColorUtil.color("&7Komutlar:"));
        player.sendMessage(ColorUtil.color("&7/rtpzone wand - Secim aleti al"));
        player.sendMessage(ColorUtil.color("&7/rtpzone create <isim> - Yeni bolge olustur"));
        player.sendMessage(ColorUtil.color("&7/rtpzone delete <isim> - Bolge sil"));
        player.sendMessage(ColorUtil.color("&7/rtpzone reset - Secimleri temizle"));
        player.sendMessage(ColorUtil.color("&7/rtpzone reload - Config yenile"));
    }
}
