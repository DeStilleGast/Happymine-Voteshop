package com.DeStilleGast.Minecraft.HappyMine.database;

import java.sql.*;
import java.util.TimeZone;

/**
 * Created by DeStilleGast 23-8-2019
 */
public class MySQLConnector {

    private final String host, database, user, password;
    private Connection con;

    /**
     * Create a MySQL Connector and attempt to connect
     * @param host Host of server
     * @param database Database/scheme to connect
     * @param user Username to login
     * @param password Password for user to connect with
     */
    public MySQLConnector(String host, String database, String user, String password) {
        this.host = host;
        this.database = database;
        this.user = user;
        this.password = password;

        open();

        if(hasConnection())
            close();
        else
            System.out.println("[MySQL] No database connection");
    }

    /**
     * Attempt to open a connection to the database
     */
    public void open() {
        try {
            int port = 3306;

//            System.out.println("[MySQL] Attempting to login to database");
            con = DriverManager.getConnection("jdbc:mysql://" + host + ":"+port+"/" + database + "?autoReconnect=true&maxReconnects=2&maxTimeout=5&serverTimezone=" + TimeZone.getDefault().getID(),
                    user, password);


//            System.out.println("[MySQL] The connection to MySQL has been made!");
        } catch (SQLException e) {
            System.out.println("[MySQL] The connection to MySQL couldn't be made!\nreason: " + e.getMessage() + ",\nCaused by " + e.getCause().getMessage());
        }
    }

    /**
     * Close connection with database
     */
    public void close() {
        try {
            if (con != null) {
                con.close();
//                System.out.println("[MySQL] The connection to MySQL has ended successfully!");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Request a prepare statement with the given query
     * @param qry Query
     * @return PreparedStatedment with given query
     */
    public PreparedStatement prepareStatement(String qry) throws SQLException {
        return con.prepareStatement(qry);
    }

    /**
     * Execute a update/insert statement
     * @param statement PrepareStatement with Update/Insert statement
     */
    public int update(PreparedStatement statement) throws SQLException {
        try {
            return statement.executeUpdate();
        } catch (SQLException e) {
            open();
            e.printStackTrace();
            throw e; // ook al wordt het hier opgevangen, het dingetje is, we kunnen anders niet zien of het gelukt of gefaald heeft
        } finally {
            statement.close();
        }
    }

    /**
     * Check if there is a connection to the MySQL server
     * @return true if there is a connection
     */
    public boolean hasConnection() {
        return con != null;
    }

    /**
     * Execute SELECT statement
     * @param statement PrepareStatement with SELECT query
     * @return ResultSet from given PrepareStatement/query
     */
    public ResultSet query(PreparedStatement statement) throws SQLException {
        return statement.executeQuery();
    }
}
