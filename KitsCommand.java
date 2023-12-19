package org.burixton.visiblekits;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.burixton.visiblekits.configReader.configReader;
import org.burixton.visiblekits.events.InventoryEvents;



import java.util.ArrayList;
import java.util.List;

import static org.burixton.visiblekits.configReader.configReader.formatTime;

public class KitsCommand implements InventoryHolder {
    private final Inventory kitsInv;

    public KitsCommand(Player player) {
        kitsInv = Bukkit.createInventory(this, 9, ChatColor.translateAlternateColorCodes('&', configReader.getKitGuiName()));
        initObj(player);
    }

    private Inventory initObj(Player player) {
        List<configReader.KitItem> kitItems = configReader.getKitItems();
        for (int i = 0; i < kitItems.size() && i < 9; i++) {
            configReader.KitItem kitItem = kitItems.get(i);
            ItemStack itemStack = new ItemStack(kitItem.getMaterial());
            ItemMeta itemMeta = itemStack.getItemMeta();

            String translatedName = ChatColor.translateAlternateColorCodes('&', kitItem.getName());
            itemMeta.setDisplayName(translatedName);

            List<String> translatedLore = new ArrayList<>();
            for (String loreLine : kitItem.getLore()) {
                translatedLore.add(ChatColor.translateAlternateColorCodes('&', loreLine).replace("%timeleft%", formatTime(InventoryEvents.getTimeLeft(player, kitItem))));

            }
            itemMeta.setLore(translatedLore);

            itemStack.setItemMeta(itemMeta);

            kitsInv.setItem(i, itemStack);
        }
        return kitsInv;
    }

    @Override
    public Inventory getInventory() {
        return kitsInv;
    }



    private ItemStack createNamedLoreItem(Material material, String displayName, String lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(displayName);

        List<String> loreList = new ArrayList<>();
        loreList.add(lore);
        meta.setLore(loreList);

        item.setItemMeta(meta);
        return item;
    }
}