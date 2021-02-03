package dev.majek.keeplore;

import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;

public abstract class Database {

    KeepLore plugin;
    Connection connection;
    public String table = "savedLore";

    public Database(KeepLore instance) {
        plugin = instance;
    }

    public abstract Connection getSQLConnection();

    public abstract void load();

    public void initialize() {
        connection = getSQLConnection();
    }

    public void clearTable() {
        Connection conn;
        PreparedStatement ps;
        try {
            conn = getSQLConnection();
            ps = conn.prepareStatement("DELETE FROM savedLore");
            ps.executeUpdate();
        } catch (SQLException ex) {
            plugin.getLogger().log(Level.SEVERE, "Couldn't execute MySQL statement: ", ex);
        }
    }

    public void addLocationLore(Location location, String displayName, List<String> lore) {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = getSQLConnection();
            ps = conn.prepareStatement("INSERT INTO " + table + " (locationString,displayName,serializedLore)" +
                    " VALUES(?,?,?)");
            String serializedLocation = location.getWorld().getName() + "," + location.getX() + "," + location.getY()
                    + "," + location.getZ() + "," + location.getPitch() + "," + location.getYaw();
            ps.setString(1, serializedLocation);
            ps.setString(2, displayName);
            StringBuilder serializedLore = new StringBuilder();
            for (String line : lore)
                serializedLore.append(line).append(",");
            ps.setString(3, serializedLore.toString());
            ps.executeUpdate();
        } catch (SQLException ex) {
            plugin.getLogger().log(Level.SEVERE, "Couldn't execute MySQL statement: ", ex);
        } finally {
            try {
                if (ps != null)
                    ps.close();
                if (conn != null)
                    conn.close();
            } catch (SQLException ex) {
                plugin.getLogger().log(Level.SEVERE, "Failed to close MySQL connection: ", ex);
            }
        }
    }

    public void getLocationLore() {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs;
        try {
            conn = getSQLConnection();
            ps = conn.prepareStatement("SELECT * FROM " + table);
            rs = ps.executeQuery();
            while (rs.next()) {
                Location location; String displayName; List<String> lore;
                String[] locationString = rs.getString("locationString").split(",");
                location = new Location(Bukkit.getWorld(locationString[0]), Double.parseDouble(locationString[1]),
                        Double.parseDouble(locationString[2]), Double.parseDouble(locationString[3]),
                        Float.parseFloat(locationString[4]), Float.parseFloat(locationString[5]));
                displayName = rs.getString("displayName");
                String loreString = rs.getString("serializedLore");
                lore = Arrays.asList(loreString.split(",").clone());
                KeepLore.loreMap.put(location, new Pair<>(displayName, lore));
            }
        } catch (SQLException ex) {
            plugin.getLogger().log(Level.SEVERE, "Couldn't execute MySQL statement: ", ex);
        } finally {
            try {
                if (ps != null)
                    ps.close();
                if (conn != null)
                    conn.close();
            } catch (SQLException ex) {
                plugin.getLogger().log(Level.SEVERE, "Failed to close MySQL connection: ", ex);
            }
        }
    }
}