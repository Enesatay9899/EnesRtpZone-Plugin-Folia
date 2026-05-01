package com.enes.enesrtpzone.managers;

import com.enes.enesrtpzone.EnesRTPZone;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

public class MessageManager {

    private EnesRTPZone plugin;
    private FileConfiguration messages;
    private File messagesFile;

    public MessageManager(EnesRTPZone plugin) {
        this.plugin = plugin;
        loadMessages();
    }

    private void loadMessages() {
        messagesFile = new File(plugin.getDataFolder(), "messages.yml");
        if (!messagesFile.exists()) {
            plugin.saveResource("messages.yml", false);
        }
        messages = YamlConfiguration.loadConfiguration(messagesFile);
    }

    public void reloadMessages() {
        messages = YamlConfiguration.loadConfiguration(messagesFile);
    }

    public String getMessage(String path, String defaultValue) {
        String msg = messages.getString(path, defaultValue);
        return msg != null ? msg : defaultValue;
    }

    public String getMessage(String path) {
        return getMessage(path, "&cMessage not found: " + path);
    }

    public String getPrefix() {
        return getMessage("prefix", "&8[&b&lʀᴛᴘ&f&lᴢᴏɴᴇ&8] &7");
    }
}
