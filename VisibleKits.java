package org.burixton.visiblekits;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.burixton.visiblekits.events.InventoryEvents;
import org.yaml.snakeyaml.Yaml;
import org.burixton.visiblekits.configReader.configReader;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import org.burixton.visiblekits.KitsCommand;

import static org.burixton.visiblekits.configReader.configReader.*;

public final class VisibleKits extends JavaPlugin {
    private boolean hideInventory = false;
    private File configFile;
    private Yaml yaml;
    private configReader kitConfigReader;
    @Override
    public void onEnable() {
        kitConfigReader = new configReader(this);
        String kitGuiName = kitConfigReader.getKitGuiName();
        List<KitItem> kitItems = kitConfigReader.getKitItems();

        // Use the values as needed
        getLogger().info("Kit GUI Name: " + kitGuiName);

        for (configReader.KitItem kitItem : kitItems) {
            getLogger().info("Kit Item: " + kitItem.getName() +
                    ", Material: " + kitItem.getMaterial() +
                    ", Lore: " + kitItem.getLore());}
        yaml = new Yaml();
        configFile = new File(getDataFolder(), "config.yml");
        if (!configFile.exists()) {
            saveDefaultConfig();
        }
        getServer().getPluginManager().registerEvents(new InventoryEvents(), this);
        getLogger().info("BasicCommands 1.0 loaded");
        readConfig();
    }
    private void readConfig() {
        try (FileReader reader = new FileReader(configFile)) {
            // Parse YAML to a Map
            Map<String, Object> configMap = yaml.load(reader);

            // Access values from the Map
            if (configMap != null) {
                Object someValue = configMap.get("someKey");
                if (someValue != null) {
                    // Do something with the value
                    getLogger().info("Value of someKey: " + someValue);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void onDisable() {
        getLogger().info("BasicCommands 1.0 shut down");


    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        //return super.onCommand(sender, command, label, args);
        getLogger().log(Level.INFO, sender.getName() + "Issued the command" + command.getName());
        Player player = (Player) sender;
        if (command.getName().equals("kit") && sender instanceof Player) {
                if (args.length == 0) {
                    KitsCommand gui = new KitsCommand(player);
                    player.openInventory(gui.getInventory());
                    hideInventory = true;

                }else{
                    sender.sendMessage("Invalid usage (/kit)");
                }
                return true;

        }
        return true;}}
