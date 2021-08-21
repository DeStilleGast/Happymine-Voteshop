package com.DeStilleGast.Minecraft.HappyMine.VoteShop.sign;

import com.DeStilleGast.Minecraft.HappyMine.VoteShop.ShopCore;
import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import xyz.destillegast.dsgutils.helpers.SignHelper;
import xyz.destillegast.dsgutils.signs.SignActions;

/**
 * Created by DeStilleGast 16-7-2021
 */
public class CurrencySign implements SignActions {

    private final ShopCore shopCore;

    public CurrencySign(ShopCore shopCore) {
        this.shopCore = shopCore;
    }


    @Override
    public boolean onSignPlace(Player player, Block block, String[] lines) {
        return true;
    }

    @Override
    public void onSignUpdate(Player player, Block block) {
        SignHelper.sendSignUpdate(player, block, new String[]{
                ChatColor.GREEN + "[Vote points]",
                "Votepoints: " + shopCore.getCurrency(player),
                "",
                ""
        });
    }

    @Override
    public boolean onSignRemove(Player player, Block block) {
        return true;
    }

    @Override
    public void onSignInteract(Player player, Block block, Action action) {

    }
}
