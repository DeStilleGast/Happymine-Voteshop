package com.DeStilleGast.Minecraft.HappyMine.VoteSytem;

import com.DeStilleGast.Minecraft.HappyMine.database.MySQLConnector;
import com.DeStilleGast.Minecraft.HappyMine.database.MySqlConnectorHelper;
import com.DeStilleGast.Minecraft.HappyMine.database.PlayerLogger;
import com.google.common.io.ByteStreams;
import com.vexsoftware.votifier.bungee.events.VotifierEvent;
import com.vexsoftware.votifier.model.Vote;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;
import net.md_5.bungee.event.EventHandler;

import java.io.*;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Random;
import java.util.UUID;

/**
 * Created by DeStilleGast on 12-11-2016.
 */
public class VoteCore extends Plugin implements Listener {

    private MySQLConnector sql;

    private Configuration config;


    private String LogTable = "VoteLogs";
    private String VoteCurrency = "VoteCurrency";

    private String voteAnnounce = "&8[&bVote&8] &r{player} heeft gevote en heeft {points} VotePoints gekregen, wil je ook VotePoints, kijk dan hier https://www.happymine.nl/vote";

    @Override
    public void onEnable() {
        this.getProxy().getPluginManager().registerListener(this, this);

        try {
            try {
                config = loadConfig("voteannounce.yml");
                voteAnnounce = config.getString("announce", voteAnnounce);
            } catch (IOException e) {
                e.printStackTrace();
            }

            File configFile = new File(this.getDataFolder(), "database.yml");
            sql = MySqlConnectorHelper.Connect(configFile, this.getLogger());

            this.getProxy().getPluginManager().registerListener(this, new PlayerLogger(sql));

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        try {
            if (sql != null) {
                sql.open();
                String voteCurrenyCreate = "CREATE TABLE IF NOT EXISTS `VoteCurrency` ( `id` INT NOT NULL AUTO_INCREMENT , `UUID` VARCHAR(36) NOT NULL , `VotePoints` INT NOT NULL , PRIMARY KEY (`id`), UNIQUE (`UUID`))";
                sql.update(sql.prepareStatement(voteCurrenyCreate));

                String voteLogCreate = "CREATE TABLE IF NOT EXISTS `VoteLogs` ( `id` INT NOT NULL AUTO_INCREMENT, `username` VARCHAR(30) NOT NULL , `uuid` VARCHAR(36) NOT NULL , `service` VARCHAR(100) NOT NULL , `date` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP, PRIMARY KEY (`id`))";
                sql.update(sql.prepareStatement(voteLogCreate));

            }
        } catch (SQLException ex) {
            getLogger().finest("Failed to create tables");
            ex.printStackTrace();
        } finally {
            if (sql != null)
                if (sql.hasConnection())
                    sql.close();
        }


        this.getProxy().getPluginManager().registerCommand(this, new Command("vote") {
            @Override
            public void execute(CommandSender commandSender, String[] strings) {
                commandSender.sendMessage(TextComponent.fromLegacyText(ChatColor.translateAlternateColorCodes('&', config.getString("voteShowCurrency").replace("{points}", "" + getCurrency((ProxiedPlayer) commandSender)))));
                commandSender.sendMessage(TextComponent.fromLegacyText(ChatColor.translateAlternateColorCodes('&', config.getString("voteCommand"))));
            }
        });
    }

    @EventHandler
    public void onVote(VotifierEvent e) {
        Vote v = e.getVote();

        String stringUUID = MySqlConnectorHelper.getOfflinePlayerUUID(this.sql, v.getUsername());
        if (stringUUID == null || stringUUID.isEmpty()) return;
        UUID myUUID = UUID.fromString(stringUUID);
        String service = v.getServiceName();


        try {
            this.sql.open();
            PreparedStatement ps = this.sql.prepareStatement("INSERT INTO `" + LogTable + "` (`Username`, `UUID`, `Service`) VALUES (?, ?, ?)");

            ps.setString(1, v.getUsername());
            ps.setString(2, myUUID.toString());
//            }else{
//                ps.setString(2, "Player never online ?");
//            }
            ps.setString(3, service);

            ps.execute();

        } catch (Exception ex) {
            ex.printStackTrace();
            return;
        }finally {
            if(this.sql.hasConnection()){
                this.sql.close();
            }
        }

        // Give player minimal 5 points
        int toAdd = 5;
        if ((new Random()).nextBoolean()) { // With luck people get a extra 1 or 2 points
            toAdd += ((new Random()).nextInt(3));
        }

        try {
            sql.open();
            PreparedStatement ps1 = this.sql.prepareStatement("UPDATE `" + VoteCurrency + "` SET VotePoints=VotePoints+" + toAdd + " WHERE `UUID`=?");

            ps1.setString(1, myUUID.toString());
            ps1.execute();

        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            if (sql.hasConnection())
                sql.close();
        }

        String toAnnounce = this.voteAnnounce.replace("{player}", v.getUsername()).replace("{service}", v.getServiceName()).replace("{points}", toAdd + "");
        getProxy().broadcast(TextComponent.fromLegacyText(ChatColor.translateAlternateColorCodes('&', toAnnounce)));
    }

    @EventHandler
    public void onPlayerJoin(PostLoginEvent e) {

        try {
            sql.open();
            PreparedStatement ps = sql.prepareStatement("INSERT IGNORE INTO `" + VoteCurrency + "` (`UUID`, `VotePoints`) VALUES (?, 0)");

            ps.setString(1, e.getPlayer().getUniqueId().toString());
            ps.execute();

        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            if (sql.hasConnection())
                sql.close();
        }
    }

    public int getCurrency(ProxiedPlayer p) {

        try {
            sql.open();
            PreparedStatement ps = sql.prepareStatement("SELECT `VotePoints` FROM `VoteCurrency` WHERE `UUID`=?");

            ps.setString(1, p.getUniqueId().toString());

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getInt("VotePoints");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (sql.hasConnection())
                sql.close();
        }

        return 0;
    }

    private Configuration loadConfig(String resource) throws IOException {
        if (!getDataFolder().exists()) getDataFolder().mkdir();

        File file = new File(getDataFolder(), resource);
        if (!file.exists()) {
            try {
                InputStream in = getResourceAsStream(resource);
                OutputStream os = new FileOutputStream(file);
                ByteStreams.copy(in, os);
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }
        return ConfigurationProvider.getProvider(YamlConfiguration.class).load(new File(getDataFolder(), resource));
    }
}
