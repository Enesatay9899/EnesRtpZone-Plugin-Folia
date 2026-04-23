package com.enes.enesrtpzone.commands;

import com.enes.enesrtpzone.EnesRTPZone;
import com.enes.enesrtpzone.listeners.ZoneListener;
import com.enes.enesrtpzone.utils.ColorUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Collections;

public class ZoneCommand implements CommandExecutor {

    private final EnesRTPZone plugin;

    public ZoneCommand(EnesRTPZone plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) return true;
        Player player = (Player) sender;
        if (!player.hasPermission("rtpzone.admin")) return true;

        String prefix = ColorUtils.format(plugin.getConfig().getString("messages.prefix"));

        if (args.length == 0) {
            player.sendMessage(prefix + ColorUtils.format(plugin.getConfig().getString("messages.usage")));
            return true;
        }

        if (args[0].equalsIgnoreCase("wand")) {
            ItemStack wand = new ItemStack(Material.WOODEN_AXE);
            ItemMeta meta = wand.getItemMeta();
            meta.setDisplayName(ColorUtils.format("&b&lʀᴛᴘᴢᴏɴᴇ &7ᴡᴀɴᴅ"));
            meta.setLore(Collections.singletonList(ColorUtils.format("&7ꜱᴏʟ ᴛɪᴋ: 1. ᴋᴏɴᴜᴍ, ꜱᴀɢ ᴛɪᴋ: 2. ᴋᴏɴᴜᴍ")));
            wand.setItemMeta(meta);
            
            player.getInventory().addItem(wand);
            player.sendMessage(prefix + ColorUtils.format(plugin.getConfig().getString("messages.wand-received")));
            return true;
        }

        if (args[0].equalsIgnoreCase("delete")) {
            if (args.length < 2) {
                player.sendMessage(prefix + ColorUtils.format("&cᴋᴜʟʟᴀɴɪᴍ: /ʀᴛᴘᴢᴏɴᴇ ᴅᴇʟᴇᴛᴇ <ɪꜱɪᴍ>"));
                return true;
            }
            String zoneName = args[1];
            if (!plugin.getZoneManager().zoneExists(zoneName)) {
                player.sendMessage(prefix + ColorUtils.format("&cʙᴏʟɢᴇ ʙᴜʟᴜɴᴀᴍᴀᴅɪ: &e" + zoneName));
                return true;
            }
            plugin.getZoneManager().deleteZone(zoneName);
            player.sendMessage(prefix + ColorUtils.format("&aʙᴏʟɢᴇ ꜱɪʟɪɴᴅɪ: &e" + zoneName));
            return true;
        }

        if (args[0].equalsIgnoreCase("reload")) {
            plugin.reloadConfig();
            plugin.getZoneManager().reloadZones();
            player.sendMessage(prefix + ColorUtils.format("&aᴋᴏɴꜰɪɢᴜʀᴀꜱʏᴏɴ ʏᴇɴɪʟᴇɴᴅɪ!"));
            return true;
        }

        if (args[0].equalsIgnoreCase("reset") || args[0].equalsIgnoreCase("clear")) {
            ZoneListener.pos1.remove(player.getUniqueId());
            ZoneListener.pos2.remove(player.getUniqueId());
            player.sendMessage(prefix + ColorUtils.format("&aꜱᴇᴄɪʟᴇɴ ᴘᴏᴢɪꜱʏᴏɴʟᴀʀ ꜱɪꜰɪʀʟᴀɴᴅɪ!"));
            return true;
        }

        if (args[0].equalsIgnoreCase("create")) {
            if (args.length < 2) {
                player.sendMessage(prefix + ColorUtils.format("&cᴋᴜʟʟᴀɴɪᴍ: /ʀᴛᴘᴢᴏɴᴇ ᴄʀᴇᴀᴛᴇ <ɪꜱɪᴍ>"));
                return true;
            }
            Location p1 = ZoneListener.pos1.get(player.getUniqueId());
            Location p2 = ZoneListener.pos2.get(player.getUniqueId());

            if (p1 == null || p2 == null) {
                player.sendMessage(prefix + ColorUtils.format(plugin.getConfig().getString("messages.select-first")));
                return true;
            }

            plugin.getZoneManager().createZone(args[1], p1, p2);
            player.sendMessage(prefix + ColorUtils.format(plugin.getConfig().getString("messages.zone-created").replace("%name%", args[1])));
            
            ZoneListener.pos1.remove(player.getUniqueId());
            ZoneListener.pos2.remove(player.getUniqueId());
            return true;
        }

        
        player.sendMessage(prefix + ColorUtils.format(plugin.getConfig().getString("messages.usage")));
        player.sendMessage(ColorUtils.format("&7- &b/ʀᴛᴘᴢᴏɴᴇ ᴡᴀɴᴅ &7- ꜱᴇᴄɪᴍ ᴡᴀɴᴅ'ɪ ᴀʟ"));
        player.sendMessage(ColorUtils.format("&7- &b/ʀᴛᴘᴢᴏɴᴇ ᴄʀᴇᴀᴛᴇ <ɪꜱɪᴍ> &7- ʙᴏʟɢᴇ ᴏʟᴜꜱᴛᴜʀ"));
        player.sendMessage(ColorUtils.format("&7- &b/ʀᴛᴘᴢᴏɴᴇ ᴅᴇʟᴇᴛᴇ <ɪꜱɪᴍ> &7- ʙᴏʟɢᴇ ꜱɪʟ"));
        player.sendMessage(ColorUtils.format("&7- &b/ʀᴛᴘᴢᴏɴᴇ ʀᴇꜱᴇᴛ &7- ꜱᴇᴄɪᴍʟᴇʀɪ ꜱɪꜰɪʀʟᴀ"));
        player.sendMessage(ColorUtils.format("&7- &b/ʀᴛᴘᴢᴏɴᴇ ʀᴇʟᴏᴀᴅ &7- ᴋᴏɴꜰɪɢᴜʀᴀꜱʏᴏɴ ʏᴇɴɪʟᴇ"));
        return true;
    }
}
