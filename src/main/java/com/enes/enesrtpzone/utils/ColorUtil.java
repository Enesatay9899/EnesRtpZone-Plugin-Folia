package com.enes.enesrtpzone.utils;

import org.bukkit.ChatColor;

@SuppressWarnings("deprecation")
public class ColorUtil {
    
    public static String color(String text) {
        if(text == null) return "";
        return ChatColor.translateAlternateColorCodes('&', text);
    }
}
