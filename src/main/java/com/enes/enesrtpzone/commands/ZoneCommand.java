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
import java.util.List;
import java.util.ArrayList;

@SuppressWarnings("deprecation")
public class ZoneCommand implements CommandExecutor, org.bukkit.command.TabCompleter {

    EnesRTPZone plugin;

    public ZoneCommand(EnesRTPZone p) {
        this.plugin = p;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if(!(sender instanceof Player)) {
            sender.sendMessage(ColorUtil.color(plugin.getMessageManager().getMessage("player-only")));
            return true;
        }

        Player player = (Player)sender;

        if(!player.hasPermission("rtpzone.admin")) {
            player.sendMessage(ColorUtil.color(plugin.getMessageManager().getPrefix() + plugin.getMessageManager().getMessage("no-permission")));
            return true;
        }

        String prefix = ColorUtil.color(plugin.getMessageManager().getPrefix());

        if(args.length == 0) {
            showHelp(player, prefix);
            return true;
        }

        String sub = args[0].toLowerCase();

        if(sub.equals("wand")) {
            ItemStack axe = new ItemStack(Material.WOODEN_AXE);
            ItemMeta meta = axe.getItemMeta();
            meta.setDisplayName(ColorUtil.color(plugin.getMessageManager().getMessage("wand-name")));
            meta.setLore(Arrays.asList(
                ColorUtil.color(plugin.getMessageManager().getMessage("wand-lore-left")),
                ColorUtil.color(plugin.getMessageManager().getMessage("wand-lore-right"))
            ));
            axe.setItemMeta(meta);

            player.getInventory().addItem(axe);
            player.sendMessage(prefix + ColorUtil.color(plugin.getMessageManager().getMessage("wand-received")));
            return true;
        }

        if(sub.equals("delete")) {
            if(args.length < 2) {
                player.sendMessage(prefix + ColorUtil.color(plugin.getMessageManager().getMessage("usage-delete")));
                return true;
            }

            String name = args[1];

            if(!plugin.zoneManager.zoneExists(name)) {
                String msg = plugin.getMessageManager().getMessage("zone-not-found").replace("%name%", name);
                player.sendMessage(prefix + ColorUtil.color(msg));
                return true;
            }

            plugin.zoneManager.deleteZone(name);
            String msg = plugin.getMessageManager().getMessage("zone-deleted").replace("%name%", name);
            player.sendMessage(prefix + ColorUtil.color(msg));
            return true;
        }

        if(sub.equals("reload")) {
            plugin.reloadConfig();
            plugin.getMessageManager().reloadMessages();
            plugin.zoneManager.reloadZones();
            player.sendMessage(prefix + ColorUtil.color(plugin.getMessageManager().getMessage("config-reloaded")));
            return true;
        }

        if(sub.equals("reset") || sub.equals("clear")) {
            ZoneListener.pos1.remove(player.getUniqueId());
            ZoneListener.pos2.remove(player.getUniqueId());
            player.sendMessage(prefix + ColorUtil.color(plugin.getMessageManager().getMessage("selections-cleared")));
            return true;
        }

        if(sub.equals("create")) {
            if(args.length < 2) {
                player.sendMessage(prefix + ColorUtil.color(plugin.getMessageManager().getMessage("usage-create")));
                return true;
            }

            Location pos1 = ZoneListener.pos1.get(player.getUniqueId());
            Location pos2 = ZoneListener.pos2.get(player.getUniqueId());

            if(pos1 == null || pos2 == null) {
                player.sendMessage(prefix + ColorUtil.color(plugin.getMessageManager().getMessage("select-first")));
                return true;
            }

            String zoneName = args[1];

            plugin.zoneManager.createZone(zoneName, pos1, pos2);

            String msg = plugin.getMessageManager().getMessage("zone-created").replace("%name%", zoneName);
            player.sendMessage(prefix + ColorUtil.color(msg));

            ZoneListener.pos1.remove(player.getUniqueId());
            ZoneListener.pos2.remove(player.getUniqueId());
            return true;
        }

        showHelp(player, prefix);
        return true;
    }
    
    void showHelp(Player player, String prefix) {
        player.sendMessage(ColorUtil.color("&8&m━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"));
        player.sendMessage(prefix + ColorUtil.color(plugin.getMessageManager().getMessage("help-header")));
        player.sendMessage(ColorUtil.color("&8"));
        player.sendMessage(ColorUtil.color(plugin.getMessageManager().getMessage("help-wand")));
        player.sendMessage(ColorUtil.color(plugin.getMessageManager().getMessage("help-create")));
        player.sendMessage(ColorUtil.color(plugin.getMessageManager().getMessage("help-delete")));
        player.sendMessage(ColorUtil.color(plugin.getMessageManager().getMessage("help-reset")));
        player.sendMessage(ColorUtil.color("&7/rtpzone clear &8- &7Clear selections (alias)"));
        player.sendMessage(ColorUtil.color(plugin.getMessageManager().getMessage("help-reload")));
        player.sendMessage(ColorUtil.color("&8&m━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"));
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
        List<String> suggestions = new ArrayList<>();

        if(args.length == 1) {
            String[] subcommands = {"wand", "create", "delete", "reset", "clear", "reload"};
            for(String sub : subcommands) {
                if(sub.startsWith(args[0].toLowerCase())) {
                    suggestions.add(sub);
                }
            }
        } else if(args.length == 2 && args[0].equalsIgnoreCase("delete")) {
            for(String zoneName : plugin.zoneManager.getZoneNames()) {
                if(zoneName.toLowerCase().startsWith(args[1].toLowerCase())) {
                    suggestions.add(zoneName);
                }
            }
        }

        return suggestions;
    }
}
