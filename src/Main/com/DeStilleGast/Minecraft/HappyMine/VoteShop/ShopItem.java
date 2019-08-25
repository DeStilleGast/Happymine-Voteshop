package com.DeStilleGast.Minecraft.HappyMine.VoteShop;

import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;

/**
 * Created by DeStilleGast on 13-11-2016.
 */
public class ShopItem {

    private ItemStack itemStack;
    private ArrayList<String> commands;
    private int price;

    public ShopItem(ItemStack itemStack, ArrayList<String> commands, int price) {
        this.itemStack = itemStack;
        this.commands = commands;
        this.price = price;
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
}
