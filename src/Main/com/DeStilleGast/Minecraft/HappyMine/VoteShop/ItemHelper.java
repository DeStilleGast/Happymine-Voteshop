package com.DeStilleGast.Minecraft.HappyMine.VoteShop;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by DeStilleGast 26-9-2020
 */
public class ItemHelper {

    private ItemStack item;
    private ItemMeta itemMeta;

    public ItemHelper(Material type) {
        this.item = new ItemStack(type);
        this.itemMeta = item.getItemMeta();
    }

    public ItemHelper setDisplayName(String name){
        itemMeta.setDisplayName(ChatColor.RESET + name);

        return this;
    }

    public ItemHelper setLore(List<String> lore){
        itemMeta.setLore(lore.stream().map(l -> ChatColor.translateAlternateColorCodes('&', l)).collect(Collectors.toList()));
        return this;
    }

    public ItemHelper addLore(List<String> lore){
        if(itemMeta.getLore() != null){
            List<String> current = itemMeta.getLore();

            for (String s : lore) {
                current.add(ChatColor.translateAlternateColorCodes('&', s));
            }

            itemMeta.setLore(current);
        }

        return this;
    }

    public ItemHelper applyTag(NamespacedKey key){
        this.itemMeta.getPersistentDataContainer().set(key, PersistentDataType.BYTE, (byte)1);
        return this;
    }

    public ItemHelper applyMeta(ItemMeta newMeta){
        this.itemMeta = newMeta;
        return this;
    }

    public ItemStack build(){
        item.setItemMeta(itemMeta);
        return this.item;
    }
}
