package com.enes.enesrtpzone;

import com.enes.enesrtpzone.commands.*;
import com.enes.enesrtpzone.listeners.*;
import com.enes.enesrtpzone.managers.*;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class EnesRTPZone extends JavaPlugin {

    public ZoneManager zoneManager;
    public int globalCountdown;
    
    String RESET = "\u001B[0m";
    String CYAN = "\u001B[36m";
    String WHITE = "\u001B[97m";
    String B_CYAN = "\u001B[96m";

    @Override
    public void onEnable() {
        saveDefaultConfig();
        
        printLogo();

        zoneManager = new ZoneManager(this);
        globalCountdown = getConfig().getInt("rtp.countdown", 30);

        getCommand("rtpzone").setExecutor(new ZoneCommand(this));
        getServer().getPluginManager().registerEvents(new ZoneListener(this), this);

        Bukkit.getGlobalRegionScheduler().runAtFixedRate(this, task -> {
            if(globalCountdown <= 0){
                globalCountdown = getConfig().getInt("rtp.countdown", 30);
            }else{
                globalCountdown--;
            }
        }, 20L, 20L);
    }
    
    private void printLogo() {
        org.bukkit.command.ConsoleCommandSender console = getServer().getConsoleSender();

        console.sendMessage(CYAN  + "  ███████╗ ███╗   ██╗ ███████╗ ███████╗ " + RESET);
        console.sendMessage(WHITE + "  ██╔════╝ ████╗  ██║ ██╔════╝ ██╔════╝ " + RESET);
        console.sendMessage(CYAN  + "  █████╗   ██╔██╗ ██║ █████╗   ███████╗ " + RESET);
        console.sendMessage(WHITE + "  ██╔══╝   ██║╚██╗██║ ██╔══╝   ╚════██║ " + RESET);
        console.sendMessage(CYAN  + "  ███████╗ ██║ ╚████║ ███████╗ ███████║ " + RESET);
        console.sendMessage(WHITE + "  ╚══════╝ ╚═╝  ╚═══╝ ╚══════╝ ╚══════╝ " + RESET);

        console.sendMessage(CYAN  + " ██████╗  ████████╗ ██████╗ ███████╗  ██████╗  ███╗   ██╗ ███████╗" + RESET);
        console.sendMessage(WHITE + " ██╔══██╗ ╚══██╔══╝ ██╔══██╗╚══███╔╝ ██╔═══██╗ ████╗  ██║ ██╔════╝" + RESET);
        console.sendMessage(CYAN  + " ██████╔╝    ██║    ██████╔╝  ███╔╝  ██║   ██║ ██╔██╗ ██║ █████╗  " + RESET);
        console.sendMessage(WHITE + " ██╔══██╗    ██║    ██╔═══╝  ███╔╝   ██║   ██║ ██║╚██╗██║ ██╔══╝  " + RESET);
        console.sendMessage(CYAN  + " ██║  ██║    ██║    ██║     ███████╗ ╚██████╔╝ ██║ ╚████║ ███████╗" + RESET);
        console.sendMessage(WHITE + " ╚═╝  ╚═╝    ╚═╝    ╚═╝     ╚══════╝  ╚═════╝  ╚═╝  ╚═══╝ ╚══════╝" + RESET);

        console.sendMessage("");
        console.sendMessage(CYAN + " ╔══════════════════════════════════════════════════════════════╗" + RESET);
        console.sendMessage(CYAN + " ║" + WHITE + "                     EnesRTPZone Aktif!                 " + CYAN + " ║" + RESET);
        console.sendMessage(CYAN + " ╠══════════════════════════════════════════════════════════════╣" + RESET);
        console.sendMessage(CYAN + " ║ " + WHITE + "Durum: Aktif                                                 " + CYAN + "║" + RESET);
        console.sendMessage(CYAN + " ║ " + B_CYAN + "Enes9899 Keyifli Oyunlar Diler!                               " + CYAN + "║" + RESET);
        console.sendMessage(CYAN + " ╚══════════════════════════════════════════════════════════════╝" + RESET);
    }

    public ZoneManager getZoneManager() {
        return zoneManager;
    }

    public int getGlobalCountdown() {
        return globalCountdown;
    }
}
