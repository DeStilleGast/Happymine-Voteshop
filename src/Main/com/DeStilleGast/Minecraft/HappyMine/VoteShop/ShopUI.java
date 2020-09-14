package com.DeStilleGast.Minecraft.HappyMine.VoteShop;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.util.List;

import static org.bukkit.Bukkit.getServer;

/**
 * Created by DeStilleGast 13-9-2020
 */
public class ShopUI implements InventoryHolder, Listener {

    private final ShopCore core;
    private final Inventory inventory;

    public ShopUI(ShopCore core, List<ItemStack> items, Player player) {
        this.core = core;
        this.inventory = Bukkit.createInventory(this, (int)Math.ceil(items.size() / 9D) * 9, "[" + core.getCurrency(player) + " votepoints] Vote shop: ");// + getCurrency(p) + " VotePoints"););
        items.forEach(inventory::addItem);
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        if (e.getInventory().getHolder() != this) return;

        Player p = (Player) e.getWhoClicked();
        ItemStack itemStack = e.getCurrentItem();

        e.setCancelled(true);


        if (itemStack == null) {
            return;
        }


        for (ShopItem si : core.shopItems) {
            ItemStack shopItm = si.getItemStack();

            if (itemStack.isSimilar(shopItm)) {
                int myPoints = core.getCurrency(p);
                if (myPoints >= si.getPrice()) {
                    core.pay(p, si.getPrice());

                    for (String cmd : si.getCommands()) {
                        cmd = cmd.replace("{player}", p.getName());
                        getServer().dispatchCommand(getServer().getConsoleSender(), cmd);
                    }

                    String itemName = "no name";
                    if (itemStack.hasItemMeta() & itemStack.getItemMeta() != null /* to get rid of annoying build warnings */) {
                        if (itemStack.getItemMeta().hasDisplayName()) {
                            itemName = itemStack.getItemMeta().getDisplayName();
                        } else if (itemStack.getItemMeta().hasLocalizedName()) {
                            itemName = itemStack.getItemMeta().getLocalizedName();
                        }
                    }

                    p.sendMessage(String.format("%s %s", core.prefix, core.boughtMessage.replace("{item}", itemName).replace("{price}", si.getPrice() + "")));
                    core.getLogger().info(String.format("%s player %s has bought '%s'", core.prefix, p.getName(), itemName));
                } else {
                    p.sendMessage(core.prefix + " " + core.poorMessage.replace("{coins}", myPoints + ""));
                }
            }
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent e){
        if (e.getInventory().getHolder() != this) return;

        HandlerList.unregisterAll(this);
    }
}
