package org.burixton.visiblekits.events;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.burixton.visiblekits.KitsCommand;
import org.burixton.visiblekits.configReader.configReader;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class InventoryEvents implements Listener {
    private static final HashMap<Player, Map<Integer, Long>> cooldowns = new HashMap<>();
    private final File cooldownsFile = new File("plugins/VisibleKits/cooldowns.yml");

    public InventoryEvents() {
        loadCooldowns();
    }

    @EventHandler
    public boolean onClick(InventoryClickEvent e) {
        if (e.getClickedInventory() == null) {
            return false;
        }
        if (e.getClickedInventory().getHolder() instanceof KitsCommand) {
            e.setCancelled(true);
            int clickedSlot = e.getSlot() + 1; // Convert to 1-based index

            // Check if the clicked slot corresponds to a kit item
            for (configReader.KitItem kitItem : configReader.getKitItems()) {
                int kitSlot = kitItem.getSlot();
                if (kitSlot == clickedSlot && kitItem.getActive().equalsIgnoreCase("true")) {
                    Player player = (Player) e.getWhoClicked();

                    // Check cooldown
                    long timeLeft = getTimeLeft(player, kitItem);
                    if (timeLeft > 0) {
                        // Send a message to the player indicating the remaining cooldown time
                        player.sendMessage(ChatColor.translateAlternateColorCodes('&', configReader.getCooldownMessage(player, kitItem)));
                        return true; // Cancel the event since there's a cooldown
                    }

                    // Check permission
                    if (!player.hasPermission(kitItem.getPermission())) {
                        player.sendMessage(ChatColor.translateAlternateColorCodes('&', configReader.getPermissionMessage()));
                        return true; // Cancel the event due to lack of permission
                    }

                    // Activate the command associated with the kit item
                    List<String> command = kitItem.getCommand();
                    if (command != null && !command.isEmpty()) {
                        for (String s : command) {
                            // Replace placeholders and execute command
                            s = s.replace("%player%", player.getName());
                            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), s);
                        }

                        // Set cooldown for the player and kitItem
                        setCooldown(player, kitItem);
                    }
                    break; // Stop iterating once a match is found
                }
            }
        }
        return false;
    }

    public boolean hasCooldown(Player player, configReader.KitItem kitItem) {
        Map<Integer, Long> playerCooldowns = cooldowns.getOrDefault(player, new HashMap<>());
        long cooldown = kitItem.getCooldown();
        int slot = kitItem.getSlot();

        if (playerCooldowns.containsKey(slot)) {
            long lastUsage = playerCooldowns.get(slot);
            long currentTime = System.currentTimeMillis();
            long timeLeft = cooldown - (currentTime - lastUsage);

            if (timeLeft > 0) {
                // Send a message to the player indicating the remaining cooldown time
                player.sendMessage(configReader.formatTime(timeLeft));
                return true;
            }
        }

        return false;
    }


    public static long getTimeLeft(Player player, configReader.KitItem kitItem) {
        Map<Integer, Long> playerCooldowns = cooldowns.getOrDefault(player, new HashMap<>());
        long cooldown = kitItem.getCooldown();
        int slot = kitItem.getSlot();

        if (playerCooldowns.containsKey(slot)) {
            long lastUsage = playerCooldowns.get(slot);
            long currentTime = System.currentTimeMillis();
            long timeLeft = cooldown - (currentTime - lastUsage);

            if (timeLeft > 0) {
                // Convert milliseconds to seconds
                timeLeft /= 1000;
            }

            return timeLeft;
        }

        return 0;
    }

    private void setCooldown(Player player, configReader.KitItem kitItem) {
        Map<Integer, Long> playerCooldowns = cooldowns.computeIfAbsent(player, k -> new HashMap<>());
        long cooldown = kitItem.getCooldown();
        int slot = kitItem.getSlot();
        playerCooldowns.put(slot, System.currentTimeMillis() + cooldown * 1000L);

        saveCooldowns(); // Save cooldowns to the file
    }

    private void loadCooldowns() {
        if (cooldownsFile.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(cooldownsFile))) {
                Yaml yaml = new Yaml();
                Map<String, Map<String, Object>> data = yaml.load(reader.lines().collect(Collectors.joining()));

                if (data != null) {
                    for (Map.Entry<String, Map<String, Object>> entry : data.entrySet()) {
                        Player player = Bukkit.getPlayerExact(entry.getKey());
                        if (player != null) {
                            Map<Integer, Long> playerCooldowns = new HashMap<>();
                            for (Map.Entry<String, Object> kitEntry : entry.getValue().entrySet()) {
                                int slot = Integer.parseInt(kitEntry.getKey());
                                long cooldown = ((Number) kitEntry.getValue()).longValue();
                                playerCooldowns.put(slot, cooldown);
                            }
                            cooldowns.put(player, playerCooldowns);
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void saveCooldowns() {
        Map<String, Map<String, Long>> data = new HashMap<>();
        for (Map.Entry<Player, Map<Integer, Long>> entry : cooldowns.entrySet()) {
            String playerName = entry.getKey().getName();
            Map<String, Long> playerCooldowns = new HashMap<>();
            for (Map.Entry<Integer, Long> kitEntry : entry.getValue().entrySet()) {
                playerCooldowns.put(String.valueOf(kitEntry.getKey()), kitEntry.getValue());
            }
            data.put(playerName, playerCooldowns);
        }

        try (Writer writer = new FileWriter(cooldownsFile)) {
            Yaml yaml = new Yaml();
            yaml.dump(data, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
