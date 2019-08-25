package com.DeStilleGast.Minecraft.HappyMine.database;

import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.logging.Level;

/**
 * Created by DeStilleGast 23-8-2019
 */
public class PlayerLogger implements Listener {

    private MySQLConnector connector;

    public PlayerLogger(MySQLConnector sqlConnector) throws SQLException {
        connector = sqlConnector;

        String query = "CREATE TABLE IF NOT EXISTS `KnownPlayers` ( `ID` INT NOT NULL AUTO_INCREMENT , `UUID` VARCHAR(36) NOT NULL , `Username` VARCHAR(20) NOT NULL , PRIMARY KEY (`ID`), UNIQUE (`UUID`))";
        PreparedStatement ps = connector.prepareStatement(query);
        connector.update(ps);
    }

    @EventHandler
    public void onPlayerJoin(PostLoginEvent event){
        try{
            TryAndRegisterPlayer(event.getPlayer());
        }catch (SQLException ex){
            ProxyServer.getInstance().getLogger().log(Level.FINER, "Could not save player information:");
            ex.printStackTrace();
        }
    }

    private void TryAndRegisterPlayer(ProxiedPlayer player) throws SQLException {
        String Username = player.getName();
        String UUID = player.getUniqueId().toString();

        PreparedStatement ps = connector.prepareStatement("INSERT INTO `KnownPlayers` (`Username`, `UUID`) VALUES (?, ?) ON DUPLICATE KEY UPDATE Username = ?");
        ps.setString(1, Username);  // username
        ps.setString(2, UUID);      // UUID

        ps.setString(3, Username);  // username

        connector.update(ps);
    }


}
