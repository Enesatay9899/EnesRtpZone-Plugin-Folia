package com.enes.enesrtpzone.managers;

import com.enes.enesrtpzone.EnesRTPZone;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ZoneManager {

    EnesRTPZone plugin;
    Map<String, Kutu> bolgeler = new HashMap<>();

    public ZoneManager(EnesRTPZone p) {
        this.plugin = p;
        yukle();
    }

    public void reloadZones() {
        bolgeler.clear();
        yukle();
    }

    private void yukle() {
        ConfigurationSection s = plugin.getConfig().getConfigurationSection("zones");
        if(s == null) return;

        for(String isim : s.getKeys(false)) {
            String dunya = s.getString(isim + ".world");
            if(dunya == null) continue;
            World w = plugin.getServer().getWorld(dunya);
            if(w == null) continue;
            
            double x1 = s.getDouble(isim + ".minX");
            double y1 = s.getDouble(isim + ".minY");
            double z1 = s.getDouble(isim + ".minZ");
            double x2 = s.getDouble(isim + ".maxX");
            double y2 = s.getDouble(isim + ".maxY");
            double z2 = s.getDouble(isim + ".maxZ");
            
            bolgeler.put(isim, new Kutu(w, x1, y1, z1, x2, y2, z2));
        }
    }

    public void createZone(String isim, Location a, Location b) {
        World w = a.getWorld();
        
        double x1 = Math.min(a.getX(), b.getX());
        double y1 = Math.min(a.getY(), b.getY());
        double z1 = Math.min(a.getZ(), b.getZ());
        double x2 = Math.max(a.getX(), b.getX()) + 1;
        double y2 = Math.max(a.getY(), b.getY()) + 1;
        double z2 = Math.max(a.getZ(), b.getZ()) + 1;

        plugin.getConfig().set("zones." + isim + ".world", w.getName());
        plugin.getConfig().set("zones." + isim + ".minX", x1);
        plugin.getConfig().set("zones." + isim + ".minY", y1);
        plugin.getConfig().set("zones." + isim + ".minZ", z1);
        plugin.getConfig().set("zones." + isim + ".maxX", x2);
        plugin.getConfig().set("zones." + isim + ".maxY", y2);
        plugin.getConfig().set("zones." + isim + ".maxZ", z2);
        plugin.saveConfig();

        bolgeler.put(isim, new Kutu(w, x1, y1, z1, x2, y2, z2));
    }

    public boolean isInAnyZone(Location l) {
        for(Kutu k : bolgeler.values()) {
            if(k.icinde(l)) return true;
        }
        return false;
    }

    public boolean deleteZone(String isim) {
        if(!bolgeler.containsKey(isim)) return false;
        bolgeler.remove(isim);
        plugin.getConfig().set("zones." + isim, null);
        plugin.saveConfig();
        return true;
    }

    public boolean zoneExists(String isim) {
        return bolgeler.containsKey(isim);
    }

    public List<String> getZoneNames() {
        return new ArrayList<>(bolgeler.keySet());
    }

    public static class Kutu {
        World dunya;
        double kucukX, kucukY, kucukZ, buyukX, buyukY, buyukZ;

        public Kutu(World w, double x1, double y1, double z1, double x2, double y2, double z2) {
            dunya = w;
            kucukX = x1; kucukY = y1; kucukZ = z1;
            buyukX = x2; buyukY = y2; buyukZ = z2;
        }

        public boolean icinde(Location l) {
            if(!l.getWorld().getName().equals(dunya.getName())) return false;
            if(l.getX() < kucukX) return false;
            if(l.getX() > buyukX) return false;
            if(l.getY() < kucukY) return false;
            if(l.getY() > buyukY) return false;
            if(l.getZ() < kucukZ) return false;
            if(l.getZ() > buyukZ) return false;
            return true;
        }
    }
}
