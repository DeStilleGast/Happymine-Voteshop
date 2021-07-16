package com.DeStilleGast.Minecraft.HappyMine.VoteShop;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by DeStilleGast on 13-11-2016.
 */
public class ShopItem {

    private String name;
    private ItemStack itemStack;
    private ArrayList<String> commands;
    private int price;

    private List<String> showIfPermission;
    private List<String> hideIfPermission;

    private String group = "default";

    @Deprecated
    public ShopItem(ItemStack itemStack, ArrayList<String> commands, int price) {
        this.itemStack = itemStack;
        this.commands = commands;
        this.price = price;
    }

    public ShopItem(ItemStack item, YamlConfiguration cf, String filename){
        this.itemStack = item;
        this.commands = (ArrayList<String>) cf.getList("runCommands");
        this.price = 1; //cf.getInt("price", 100000);


        ItemMeta im = item.getItemMeta();
        ArrayList<String> l = new ArrayList<>();
        l.add("VotePoints: " + this.price);

        if (im.getLore() != null) {
            for (String existingLore : im.getLore()) {
                l.add(ChatColor.translateAlternateColorCodes('&', existingLore));
            }
        }

        im.setLore(l);

        item.setItemMeta(im);
        item.setAmount(1);


        showIfPermission = cf.getStringList("showIfAllPermission");
        hideIfPermission = cf.getStringList("hideIfAnyPermission");

        this.name = filename;
    }

    public ItemStack getItemStack() {
        return itemStack;
    }

    public ArrayList<String> getCommands() {
        return commands;
    }

    public int getPrice(){
        return this.price;
    }

    public boolean canSeeThisItem(Player player){
//        if(showIfPermission.size() == 0) return true;

        if(hideIfPermission.size() != 0){
            if(hideIfPermission.stream().anyMatch(player::hasPermission)){
                return false;
            }
        }

        return showIfPermission.stream().allMatch(player::hasPermission);
    }

    public String getName() {
        return name;
    }
}
