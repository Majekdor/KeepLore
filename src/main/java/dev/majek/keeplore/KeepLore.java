package dev.majek.keeplore;

import net.md_5.bungee.api.ChatColor;
import org.apache.commons.lang.WordUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public final class KeepLore extends JavaPlugin implements Listener, CommandExecutor {

    /**
     * Map of locations of blocks with preserved lore and display name.
     */
    public static Map<Location, Pair<String, List<String>>> loreMap = new HashMap<>();

    public static KeepLore instance;
    private static Database db;
    public static KeepLore getInstance() { return instance; }
    public KeepLore() {
        instance = this;
    }

    @Override
    public void onEnable() {
        // Plugin startup logic
        this.getServer().getPluginManager().registerEvents(this, this);
        //Objects.requireNonNull(this.getCommand("testblock")).setExecutor(this);

        // Load hashmap from SQLite database
        db = new SQLite(this);
        db.load();
        db.getLocationLore();
        db.clearTable();
    }

    @Override
    public void onDisable() {
        // Save hashmap to SQLite database
        for (Location location : loreMap.keySet())
            db.addLocationLore(location, loreMap.get(location).getFirst(), loreMap.get(location).getSecond());
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("testblock")) {
            ItemStack testBlock = new ItemStack(Material.DIAMOND_BLOCK);
            ItemMeta meta = testBlock.getItemMeta();
            meta.setDisplayName(applyColorCodes("&b&lTest Block"));
            meta.setLore(Arrays.asList(applyColorCodes("&6&lLore line one."),
                    applyColorCodes("&6&lLore line two.")));
            testBlock.setItemMeta(meta);
            ((Player) sender).getLocation().getWorld().dropItemNaturally(((Player) sender).getLocation(), testBlock);
        }
        return true;
    }

    /**
     * Save any lore a block has when it is placed.
     * @param event The event.
     */
    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        ItemStack blockPlaced = event.getItemInHand();
        ItemMeta meta = blockPlaced.getItemMeta();
        if (!meta.hasLore())
            return;
        List<String> lore = meta.getLore();
        String displayName;
        if (blockPlaced.getItemMeta().getDisplayName().equals("")) {
            displayName = blockPlaced.getType().name().replace("_", " ");
            displayName = WordUtils.capitalizeFully(displayName);
        } else
            displayName = blockPlaced.getItemMeta().getDisplayName();
        loreMap.put(event.getBlockPlaced().getLocation(), new Pair<>(displayName, lore));
    }

    /**
     * Restore lore to broken block if it had lore on placement.
     * @param event The event.
     */
    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        // Check if the drops are equal to one so we don't mess with diamonds blocks and fortune, for example
        if (loreMap.containsKey(event.getBlock().getLocation()) && event.getBlock().getDrops().size() == 1) {
            ItemStack blockBroken = event.getBlock().getDrops().stream().findFirst().get();
            ItemMeta meta = blockBroken.getItemMeta();
            meta.setDisplayName(loreMap.get(event.getBlock().getLocation()).getFirst());
            meta.setLore(loreMap.get(event.getBlock().getLocation()).getSecond());
            blockBroken.setItemMeta(meta);
            event.setDropItems(false);
            loreMap.remove(event.getBlock().getLocation());
            event.getBlock().getLocation().getWorld().dropItemNaturally(event.getBlock().getLocation(), blockBroken);
        }
    }

    /**
     * Apply standard bukkit color codes
     * @param message String containing color codes.
     * @return Formatted message.
     */
    private String applyColorCodes(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }
}