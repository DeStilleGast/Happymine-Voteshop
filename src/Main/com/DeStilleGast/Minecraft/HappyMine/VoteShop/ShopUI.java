package com.DeStilleGast.Minecraft.HappyMine.VoteShop;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;

import static org.bukkit.Bukkit.getServer;

/**
 * Created by DeStilleGast 13-9-2020
 */
public class ShopUI implements InventoryHolder, Listener {

    private final ShopCore core;
    private final Inventory inventory;

    private final NamespacedKey shopDecoration, pageLeft, pageRight, closeIt;

    public ShopUI(ShopCore core, List<ItemStack> items, Player player) {
        this.core = core;
        int currency = core.getCurrency(player);
        this.inventory = Bukkit.createInventory(this, ((int)Math.ceil(items.size() / 9D) * 9) + 18, "-=[ Vote shop ]=-");
        items.forEach(inventory::addItem);

        shopDecoration = new NamespacedKey(core, "ShopDecorationItem");
        pageLeft = new NamespacedKey(core, "ShopLeftPageItem");
        pageRight = new NamespacedKey(core, "ShopRightPageItem");
        closeIt = new NamespacedKey(core, "ShopCloseItem");


        // Border
        ItemStack placeHolder = new ItemHelper(Material.BLACK_STAINED_GLASS_PANE)
                .setDisplayName("")
                .build();

        applyShopDecoration(placeHolder, shopDecoration);
        for (int i = 0; i < 9; i++) {
            inventory.setItem(inventory.getSize() - 18 + i, placeHolder);
        }

        // money
        setMoneyItem(player);

        // close
        ItemStack closeItem = new ItemHelper(Material.BARRIER)
            .setDisplayName(ChatColor.RED + "Sluit shop")
            .applyTag(closeIt)
            .build();
        inventory.setItem(inventory.getSize() - 1, closeItem);

        // page L
//        ItemStack arrowLeftItem = new ItemHelper(Material.ARROW)
//            .setDisplayName(ChatColor.BLUE + "<-")
//            .applyTag(pageLeft)
//            .build();
//        inventory.setItem(inventory.getSize() - 6, arrowLeftItem);
//
        // page R
//        ItemStack arrowRightItem = new ItemHelper(Material.ARROW)
//                .setDisplayName(ChatColor.BLUE + "->")
//                .applyTag(pageRight)
//                .build();
//        inventory.setItem(inventory.getSize() - 4, arrowRightItem);


    }

    private void setMoneyItem(Player p) {
        int currency = core.getCurrency(p);
        ItemStack moneyHolder = new ItemStack(Material.GOLD_INGOT);
        ItemMeta itemMeta = moneyHolder.getItemMeta();
        itemMeta.setDisplayName(ChatColor.RESET +""+ ChatColor.GOLD + "Je hebt " + currency + " VotePoints");
        moneyHolder.setItemMeta(itemMeta);

        applyShopDecoration(moneyHolder, shopDecoration);
        inventory.setItem(inventory.getSize() - 9, moneyHolder);
    }

    private void applyShopDecoration(ItemStack item, NamespacedKey key){
        ItemMeta itemMeta = item.getItemMeta();
        itemMeta.getPersistentDataContainer().set(key, PersistentDataType.BYTE, (byte)1);
        item.setItemMeta(itemMeta);
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

        if(itemStack.hasItemMeta()){
            if(itemStack.getItemMeta().getPersistentDataContainer().has(shopDecoration, PersistentDataType.BYTE)) {
                return;
            }
            if(itemStack.getItemMeta().getPersistentDataContainer().has(closeIt, PersistentDataType.BYTE)) {
                p.closeInventory();
                return;
            }
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

        setMoneyItem(p);
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent e){
        if (e.getInventory().getHolder() != this) return;

        HandlerList.unregisterAll(this);
    }
}
