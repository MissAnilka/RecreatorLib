package com.fembuncollective.recreatorlib.hopper;

import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Interface for handling custom hopper interactions with blocks.
 * Implement this interface to define how hoppers should interact with your custom blocks/inventories.
 * 
 * @author MissAnilka
 */
public interface HopperHandler {

    /**
     * Called when a hopper tries to push items INTO this block.
     * 
     * @param targetBlock The block receiving items
     * @param item The item being pushed
     * @param hopperBlock The hopper block pushing the item
     * @return The result of the transfer attempt
     */
    @NotNull
    TransferResult onHopperPush(@NotNull Block targetBlock, @NotNull ItemStack item, @NotNull Block hopperBlock);

    /**
     * Called when a hopper tries to pull items FROM this block.
     * 
     * @param sourceBlock The block items are being pulled from
     * @param hopperBlock The hopper block pulling items
     * @return The result containing the item to transfer, or null if nothing to pull
     */
    @Nullable
    TransferResult onHopperPull(@NotNull Block sourceBlock, @NotNull Block hopperBlock);

    /**
     * Check if this handler can handle the given block.
     * 
     * @param block The block to check
     * @return true if this handler manages the block
     */
    boolean canHandle(@NotNull Block block);

    /**
     * Gets the input slots for pushing items into this block.
     * 
     * @param block The block
     * @return Array of slot indices that accept input, or empty array if none
     */
    int[] getInputSlots(@NotNull Block block);

    /**
     * Gets the output slots for pulling items from this block.
     * 
     * @param block The block
     * @return Array of slot indices that allow output, or empty array if none
     */
    int[] getOutputSlots(@NotNull Block block);

    /**
     * Check if a specific item can be inserted into a specific slot.
     * 
     * @param block The block
     * @param slot The slot index
     * @param item The item to insert
     * @return true if the item can be inserted
     */
    boolean canInsert(@NotNull Block block, int slot, @NotNull ItemStack item);

    /**
     * Check if items can be extracted from a specific slot.
     * 
     * @param block The block
     * @param slot The slot index
     * @return true if items can be extracted
     */
    boolean canExtract(@NotNull Block block, int slot);
}
