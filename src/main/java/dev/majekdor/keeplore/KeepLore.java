package dev.majekdor.keeplore;

import net.md_5.bungee.api.ChatColor;
import org.apache.commons.lang.WordUtils;
import org.bukkit.Bukkit;
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
     * Map of locations of blocks with preserved lore.
     */
    public static Map<Location, Pair<String, List<String>>> loreMap = new HashMap<>();

    @Override
    public void onEnable() {
        // Plugin startup logic
        this.getServer().getPluginManager().registerEvents(this, this);
        Objects.requireNonNull(this.getCommand("testblock")).setExecutor(this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
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
        Bukkit.getConsoleSender().sendMessage(event.getBlockPlaced().getLocation().toString());
        loreMap.put(event.getBlockPlaced().getLocation(), new Pair<>(displayName, lore));
    }

    /**
     * Restore lore to broken block if it had lore on placement.
     * @param event The event.
     */
    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (loreMap.containsKey(event.getBlock().getLocation()) && event.getBlock().getDrops().size() == 1) {
            ItemStack blockBroken = event.getBlock().getDrops().stream().findFirst().get();
            ItemMeta meta = blockBroken.getItemMeta();
            meta.setDisplayName(loreMap.get(event.getBlock().getLocation()).getFirst());
            meta.setLore(loreMap.get(event.getBlock().getLocation()).getSecond());
            blockBroken.setItemMeta(meta);
            event.setDropItems(false);
            Bukkit.getConsoleSender().sendMessage("DID IT BEACHES ");
            event.getBlock().getLocation().getWorld().dropItemNaturally(event.getBlock().getLocation(), blockBroken);
        }
    }

    /**
     * Apply standard bukkit color codes
     * @param message String containing color codes.
     * @return Formatted message.
     */
    public static String applyColorCodes(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }
}