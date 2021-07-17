package com.DeStilleGast.Minecraft.HappyMine.VoteShop.sign;

import com.DeStilleGast.Minecraft.HappyMine.VoteShop.ShopCore;
import com.DeStilleGast.Minecraft.HappyMine.VoteShop.ShopItem;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.inventory.ItemStack;
import xyz.destillegast.dsgutils.helpers.ColorHelper;
import xyz.destillegast.dsgutils.helpers.SignHelper;
import xyz.destillegast.dsgutils.signs.SignActions;

import java.util.Optional;

import static org.bukkit.Bukkit.getServer;

/**
 * Created by DeStilleGast 16-7-2021
 */
public class SignShop implements SignActions {

    private final ShopCore shopCore;

    public SignShop(ShopCore shopCore) {
        this.shopCore = shopCore;
    }

    @Override
    public boolean onSignPlace(Player player, Block block, String[] lines) {
        Bukkit.getScheduler().runTask(shopCore, () -> {
            SignHelper.sendSignUpdate(block, getLines(player, lines));
        });

        return true;
    }

    @Override
    public void onSignUpdate(Player player, Block block) {
        SignHelper.sendSignUpdate(block, getLines(player, ((Sign) block.getState()).getLines()));
    }

    @Override
    public boolean onSignRemove(Player player, Block block) {
        return true;
    }

    @Override
    public void onSignInteract(Player player, Block block, Action action) {
        Optional<ShopItem> shopItemOptional = shopCore.getShopItems().stream().filter(si -> si.getName().equalsIgnoreCase(((Sign) block.getState()).getLine(3))).findFirst();

        shopItemOptional.ifPresent(shopItem -> {
            int playerPoints = shopCore.getCurrency(player);
            if (playerPoints >= shopItem.getPrice()) {
                shopCore.pay(player, shopItem.getPrice());

                for (String cmd : shopItem.getCommands()) {
                    cmd = cmd.replace("{player}", player.getName());
                    getServer().dispatchCommand(getServer().getConsoleSender(), cmd);
                }

                String itemName = "no name";
                ItemStack itemStack = shopItem.getItemStack();
                if (itemStack.hasItemMeta() & itemStack.getItemMeta() != null /* to get rid of annoying build warnings */) {
                    if (itemStack.getItemMeta().hasDisplayName()) {
                        itemName = itemStack.getItemMeta().getDisplayName();
                    } else if (itemStack.getItemMeta().hasLocalizedName()) {
                        itemName = itemStack.getItemMeta().getLocalizedName();
                    }
                }

                player.sendMessage(String.format("%s %s", shopCore.prefix, shopCore.boughtMessage.replace("{item}", itemName).replace("{price}", shopItem.getPrice() + "")));
                shopCore.getLogger().info(String.format("%s player %s has bought '%s'", shopCore.prefix, player.getName(), itemName));
            }
        });
    }

    private String[] getLines(Player player, String[] lines) {
        Optional<ShopItem> shopItem = shopCore.getShopItems().stream().filter(si -> si.getName().equalsIgnoreCase(lines[3])).findFirst();


        return new String[]{
                (shopItem.filter(item -> shopCore.getCurrency(player) > item.getPrice()).map(item -> ChatColor.GREEN).orElse(ChatColor.RED)) + "[VoteShop]",
                ColorHelper.translate(lines[1]),
                "Prijs: " + (shopItem.isPresent() ? shopItem.get().getPrice() : "?"),
                ""
        };
    }

}
