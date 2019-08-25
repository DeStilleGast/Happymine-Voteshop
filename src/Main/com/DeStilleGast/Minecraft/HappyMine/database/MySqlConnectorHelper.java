package com.DeStilleGast.Minecraft.HappyMine.database;

import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.logging.Logger;

/**
 * Created by DeStilleGast 23-8-2019
 */
public class MySqlConnectorHelper {


    public static MySQLConnector Connect(File configFile, Logger logger) throws IOException {
//        File configFile = new File(this.getDataFolder(), "database.yml");
        Configuration cf = null;
        boolean alreadyExist = configFile.exists();

        if(alreadyExist) {
            cf = YamlConfiguration.getProvider(YamlConfiguration.class).load(configFile);
        }else{
            cf = new Configuration();
            cf.set("host", "");
            cf.set("database", "Voteshop");
            cf.set("Username", "root");
            cf.set("Password", "toor");
            try {
                YamlConfiguration.getProvider(YamlConfiguration.class).save(cf, configFile);
            } catch (IOException e) {
                e.printStackTrace();
            }

            logger.info("Config file created");
        }
        String mysqlHost = cf.getString("host");
        String mysqlDatabase = cf.getString("database", "Voteshop");
        String mysqlUser = cf.getString("Username", "root");
        String mysqlPass = cf.getString("Password", "toor");

        return new MySQLConnector(mysqlHost, mysqlDatabase, mysqlUser, mysqlPass);
    }

    public static String getOfflinePlayerUUID(MySQLConnector mysql, String name){
        try{
            mysql.open();
            PreparedStatement ps = mysql.prepareStatement("SELECT `UUID` FROM KnownPlayers WHERE `Username`=? ORDER BY `ID` DESC");

            ps.setString(1, name);
            ResultSet rs = ps.executeQuery();

            if(rs.next()){
                return rs.getString("UUID");
            }

        }catch (Exception ex){
            ex.printStackTrace();
        }finally {
            if(mysql.hasConnection())
                mysql.close();
        }

        return null;
    }
}
