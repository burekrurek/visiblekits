package org.burixton.visiblekits.configReader;

import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.burixton.visiblekits.events.InventoryEvents.getTimeLeft;

public class configReader {

    public static JavaPlugin plugin;

    public configReader(JavaPlugin plugin) {
        configReader.plugin = plugin;
    }

    public static String getKitGuiName() {
        FileConfiguration config = plugin.getConfig();
        return config.getString("kit-gui-name", "&6Default Kit GUI");
    }

    public static String getCooldownMessage(Player player, KitItem kitItem) {
        long timeLeft = getTimeLeft(player, kitItem);

        if (timeLeft > 0) {
            // Calculate the remaining time in minutes
            long remainingMinutes = TimeUnit.SECONDS.toMinutes(timeLeft);

            // Get the cooldown message from the config
            String cooldownMessage = configReader.plugin.getConfig().getString("cooldown-message", "&6&lYou need to wait another %formattedTime% to use this kit");

            // Format the remaining time and replace the placeholder in the message
            String formattedTime = formatTime(remainingMinutes);
            cooldownMessage = cooldownMessage
                    .replace("%days%", String.format("%02d", TimeUnit.SECONDS.toDays(remainingMinutes)))
                    .replace("%hours%", String.format("%02d", TimeUnit.SECONDS.toHours(remainingMinutes) % 24))
                    .replace("%minutes%", String.format("%02d", remainingMinutes % 60))
                    .replace("%seconds%", String.format("%02d", timeLeft % 60))
                    .replace("%formattedTime%", formattedTime);

            return cooldownMessage;
        }

        return "";  // Or some default message if there's no cooldown
    }



    public static String getCooldownFormat() {
        return plugin.getConfig().getString("cooldown-message", "&6&lYou need to wait another %formattedTime% to use this kit");
    }

    public static String getPermissionMessage() {
        return plugin.getConfig().getString("permission-message", "&cYou don't have permission to use this kit.");
    }

    public static List<KitItem> getKitItems() {
        List<KitItem> kitItems = new ArrayList<>();
        FileConfiguration config = plugin.getConfig();

        for (int i = 1; i <= 9; i++) {
            String path = "kit-items.item" + i + ".";
            Material material = Material.valueOf(config.getString(path + "material", "STONE"));
            String name = config.getString(path + "name", "Item" + i);
            String active = config.getString(path + "active", "Item" + i);
            List<String> command = config.getStringList(path + "command");
            List<String> lore = config.getStringList(path + "lore");
            int cooldownSeconds = config.getInt(path + "cooldown");  // Read as seconds
            String permission = config.getString(path + "permission", "");  // Retrieve permission

            if (lore == null) {
                lore = new ArrayList<>();
            }

            KitItem kitItem = new KitItem(material, name, lore, i, active, command, cooldownSeconds, permission);
            kitItems.add(kitItem);
        }

        return kitItems;
    }

    public static class KitItem {
        private Material material;
        private String name;
        private List<String> lore;
        private String active;
        private List<String> command;
        private Integer slot;
        private Integer cooldown;
        private String permission;

        public KitItem(Material material, String name, List<String> lore, Integer i, String active, List<String> command, Integer cooldown, String permission) {
            this.material = material;
            this.active = active;  // Set a default value if active is null
            this.command = command;
            this.name = name;
            this.lore = lore;
            this.slot = i;
            this.cooldown = cooldown;
            this.permission = permission;
        }

        public Integer getSlot() {
            return slot;
        }

        public Integer getCooldown() {
            return cooldown;
        }

        public String getActive() {
            return active;
        }

        public List<String> getCommand() {
            return command;
        }

        public Material getMaterial() {
            return material;
        }

        public String getName() {
            return name;
        }

        public List<String> getLore() {
            return lore;
        }

        public String getPermission() {
            return permission;
        }
    }

    public static String formatTime(long timeInSeconds) {
        long days = TimeUnit.SECONDS.toDays(timeInSeconds);
        long hours = TimeUnit.SECONDS.toHours(timeInSeconds) % 24;
        long minutes = TimeUnit.SECONDS.toMinutes(timeInSeconds) % 60;
        long seconds = timeInSeconds % 60;

        // Format the time
        return String.format("%02dd %02dh %02dm %02ds", days, hours, minutes, seconds);
    }

}
