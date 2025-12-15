package com.fembuncollective.recreatorlib.hopper.listener;

import com.fembuncollective.recreatorlib.hopper.*;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Hopper;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.inventory.InventoryPickupItemEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.util.Arrays;
import java.util.logging.Logger;

/**
 * Listener for hopper events to integrate with custom handlers.
 * Cancels vanilla transfers and lets CustomWorkstations handle it via scheduled tasks.
 *
 * @author MissAnilka
 */
public class HopperListener implements Listener {

    private final HopperManager hopperManager;
    private final CustomWorkstationRegistry customWorkstationRegistry;
    private final Logger logger;
    private static final boolean DEBUG = true;

    public HopperListener() {
        this.hopperManager = HopperManager.getInstance();
        this.customWorkstationRegistry = CustomWorkstationRegistry.getInstance();
        this.logger = Bukkit.getLogger();
    }

    private void debug(String message) {
        if (DEBUG) {
            logger.info("[RecreatorLib DEBUG] " + message);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onInventoryMoveItem(InventoryMoveItemEvent event) {
        Inventory source = event.getSource();
        Inventory destination = event.getDestination();

        // === HOPPER PUSHING TO CUSTOM WORKSTATION - Just cancel, let task handle it ===
        if (source.getHolder() instanceof Hopper hopper) {
            Block hopperBlock = hopper.getBlock();
            Block targetBlock = getBlockInFront(hopperBlock);

            CustomWorkstationConfig config = customWorkstationRegistry.getConfig(targetBlock);
            if (config != null && config.isEnabled()) {
                // Cancel vanilla transfer - CustomWorkstations task will handle it
                event.setCancelled(true);
                return;
            }
        }

        // === HOPPER PULLING FROM CUSTOM WORKSTATION - Just cancel, let task handle it ===
        if (destination.getHolder() instanceof Hopper hopper) {
            Block hopperBlock = hopper.getBlock();
            Block sourceBlock = hopperBlock.getRelative(BlockFace.UP);

            CustomWorkstationConfig config = customWorkstationRegistry.getConfig(sourceBlock);
            if (config != null && config.isEnabled()) {
                // Cancel vanilla transfer - CustomWorkstations task will handle it
                event.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onInventoryPickupItem(InventoryPickupItemEvent event) {
        // Allow hoppers to pick up items from the ground normally
    }

    private Block getBlockInFront(Block block) {
        if (block.getBlockData() instanceof org.bukkit.block.data.type.Hopper hopperData) {
            return block.getRelative(hopperData.getFacing());
        }
        return block.getRelative(BlockFace.DOWN);
    }
}