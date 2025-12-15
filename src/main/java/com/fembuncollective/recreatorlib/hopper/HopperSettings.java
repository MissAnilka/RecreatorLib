package com.fembuncollective.recreatorlib.hopper;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Predicate;

/**
 * Settings for customizing hopper behavior.
 * 
 * @author MissAnilka
 */
public class HopperSettings {

    /**
     * Default hopper settings (vanilla behavior).
     */
    public static final HopperSettings DEFAULT = new Builder().build();

    private final boolean enabled;
    private final int transferSpeed;
    private final int transferAmount;
    private final Predicate<ItemStack> itemFilter;
    private final boolean pushEnabled;
    private final boolean pullEnabled;
    private final int[] customInputSlots;
    private final int[] customOutputSlots;

    private HopperSettings(Builder builder) {
        this.enabled = builder.enabled;
        this.transferSpeed = builder.transferSpeed;
        this.transferAmount = builder.transferAmount;
        this.itemFilter = builder.itemFilter;
        this.pushEnabled = builder.pushEnabled;
        this.pullEnabled = builder.pullEnabled;
        this.customInputSlots = builder.customInputSlots;
        this.customOutputSlots = builder.customOutputSlots;
    }

    /**
     * Creates a new settings builder.
     * 
     * @return A new builder instance
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Check if the hopper is enabled.
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Gets the transfer speed in ticks.
     * Default is 8 ticks (vanilla).
     */
    public int getTransferSpeed() {
        return transferSpeed;
    }

    /**
     * Gets the amount of items to transfer per operation.
     * Default is 1 (vanilla).
     */
    public int getTransferAmount() {
        return transferAmount;
    }

    /**
     * Gets the item filter for this hopper.
     * 
     * @return The filter, or null if no filter
     */
    @Nullable
    public Predicate<ItemStack> getItemFilter() {
        return itemFilter;
    }

    /**
     * Check if an item passes the filter.
     * 
     * @param item The item to check
     * @return true if the item passes or no filter is set
     */
    public boolean passesFilter(@NotNull ItemStack item) {
        return itemFilter == null || itemFilter.test(item);
    }

    /**
     * Check if pushing items is enabled.
     */
    public boolean isPushEnabled() {
        return pushEnabled;
    }

    /**
     * Check if pulling items is enabled.
     */
    public boolean isPullEnabled() {
        return pullEnabled;
    }

    /**
     * Gets custom input slots, or null for default.
     */
    @Nullable
    public int[] getCustomInputSlots() {
        return customInputSlots;
    }

    /**
     * Gets custom output slots, or null for default.
     */
    @Nullable
    public int[] getCustomOutputSlots() {
        return customOutputSlots;
    }

    /**
     * Builder for HopperSettings.
     */
    public static class Builder {
        private boolean enabled = true;
        private int transferSpeed = 8;
        private int transferAmount = 1;
        private Predicate<ItemStack> itemFilter = null;
        private boolean pushEnabled = true;
        private boolean pullEnabled = true;
        private int[] customInputSlots = null;
        private int[] customOutputSlots = null;

        /**
         * Sets whether the hopper is enabled.
         */
        public Builder enabled(boolean enabled) {
            this.enabled = enabled;
            return this;
        }

        /**
         * Sets the transfer speed in ticks.
         * 
         * @param ticks Ticks between transfers (vanilla is 8)
         */
        public Builder transferSpeed(int ticks) {
            this.transferSpeed = Math.max(1, ticks);
            return this;
        }

        /**
         * Sets the amount of items to transfer per operation.
         * 
         * @param amount Items per transfer (vanilla is 1)
         */
        public Builder transferAmount(int amount) {
            this.transferAmount = Math.max(1, Math.min(64, amount));
            return this;
        }

        /**
         * Sets an item filter for the hopper.
         * 
         * @param filter Predicate that returns true for allowed items
         */
        public Builder itemFilter(@Nullable Predicate<ItemStack> filter) {
            this.itemFilter = filter;
            return this;
        }

        /**
         * Sets whether pushing items is enabled.
         */
        public Builder pushEnabled(boolean enabled) {
            this.pushEnabled = enabled;
            return this;
        }

        /**
         * Sets whether pulling items is enabled.
         */
        public Builder pullEnabled(boolean enabled) {
            this.pullEnabled = enabled;
            return this;
        }

        /**
         * Sets custom input slots for the target inventory.
         * 
         * @param slots Array of slot indices
         */
        public Builder customInputSlots(int... slots) {
            this.customInputSlots = slots;
            return this;
        }

        /**
         * Sets custom output slots for the source inventory.
         * 
         * @param slots Array of slot indices
         */
        public Builder customOutputSlots(int... slots) {
            this.customOutputSlots = slots;
            return this;
        }

        /**
         * Builds the HopperSettings instance.
         */
        public HopperSettings build() {
            return new HopperSettings(this);
        }
    }
}
