package com.fembuncollective.recreatorlib.hopper;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

/**
 * Represents the result of a hopper transfer operation.
 * 
 * @author MissAnilka
 */
public class TransferResult {

    private final TransferStatus status;
    private final ItemStack item;
    private final int slot;

    private TransferResult(TransferStatus status, @Nullable ItemStack item, int slot) {
        this.status = status;
        this.item = item;
        this.slot = slot;
    }

    /**
     * Creates a successful transfer result.
     * 
     * @param item The item that was/will be transferred
     * @param slot The slot involved in the transfer
     * @return A success result
     */
    public static TransferResult success(@Nullable ItemStack item, int slot) {
        return new TransferResult(TransferStatus.SUCCESS, item, slot);
    }

    /**
     * Creates a successful transfer result without specifying a slot.
     * 
     * @param item The item that was/will be transferred
     * @return A success result
     */
    public static TransferResult success(@Nullable ItemStack item) {
        return new TransferResult(TransferStatus.SUCCESS, item, -1);
    }

    /**
     * Creates a denied transfer result (transfer was blocked).
     * 
     * @return A denied result
     */
    public static TransferResult denied() {
        return new TransferResult(TransferStatus.DENIED, null, -1);
    }

    /**
     * Creates a result indicating no space available.
     * 
     * @return A no-space result
     */
    public static TransferResult noSpace() {
        return new TransferResult(TransferStatus.NO_SPACE, null, -1);
    }

    /**
     * Creates a result indicating nothing to transfer.
     * 
     * @return An empty result
     */
    public static TransferResult empty() {
        return new TransferResult(TransferStatus.EMPTY, null, -1);
    }

    /**
     * Creates a result to pass through to vanilla behavior.
     * 
     * @return A pass-through result
     */
    public static TransferResult passThrough() {
        return new TransferResult(TransferStatus.PASS_THROUGH, null, -1);
    }

    public TransferStatus getStatus() {
        return status;
    }

    @Nullable
    public ItemStack getItem() {
        return item;
    }

    public int getSlot() {
        return slot;
    }

    public boolean isSuccessful() {
        return status == TransferStatus.SUCCESS;
    }

    public boolean shouldCancel() {
        return status != TransferStatus.PASS_THROUGH;
    }

    /**
     * Status of a hopper transfer operation.
     */
    public enum TransferStatus {
        /**
         * Transfer was successful
         */
        SUCCESS,
        /**
         * Transfer was denied/blocked
         */
        DENIED,
        /**
         * No space available for the transfer
         */
        NO_SPACE,
        /**
         * Nothing to transfer (empty source)
         */
        EMPTY,
        /**
         * Pass through to vanilla hopper behavior
         */
        PASS_THROUGH
    }
}
