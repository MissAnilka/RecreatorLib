package com.fembuncollective.recreatorlib.hopper;

import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * Configuration for a custom workstation's hopper integration.
 * Allows server owners to define which slots accept input and output.
 * 
 * @author MissAnilka
 */
public class WorkstationConfig {

    private final String id;
    private final Set<Material> materials;
    private final List<SlotConfig> slots;
    private final int inventorySize;
    private boolean enabled = true;

    private WorkstationConfig(Builder builder) {
        this.id = builder.id;
        this.materials = Collections.unmodifiableSet(new HashSet<>(builder.materials));
        this.slots = Collections.unmodifiableList(new ArrayList<>(builder.slots));
        this.inventorySize = builder.inventorySize;
        this.enabled = builder.enabled;
    }

    public String getId() {
        return id;
    }

    public Set<Material> getMaterials() {
        return materials;
    }

    public List<SlotConfig> getSlots() {
        return slots;
    }

    public int getInventorySize() {
        return inventorySize;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * Gets all slots that accept hopper input.
     */
    public int[] getInputSlots() {
        return slots.stream()
                .filter(SlotConfig::acceptsHopperInput)
                .mapToInt(SlotConfig::getSlot)
                .toArray();
    }

    /**
     * Gets all slots that allow hopper output.
     */
    public int[] getOutputSlots() {
        return slots.stream()
                .filter(SlotConfig::allowsHopperOutput)
                .mapToInt(SlotConfig::getSlot)
                .toArray();
    }

    /**
     * Gets the slot configuration for a specific slot.
     */
    @Nullable
    public SlotConfig getSlotConfig(int slot) {
        return slots.stream()
                .filter(s -> s.getSlot() == slot)
                .findFirst()
                .orElse(null);
    }

    /**
     * Check if a slot can accept a specific item type.
     */
    public boolean canSlotAccept(int slot, @NotNull Material itemType) {
        SlotConfig config = getSlotConfig(slot);
        if (config == null) return false;
        if (!config.acceptsHopperInput()) return false;
        return config.canAcceptMaterial(itemType);
    }

    public static Builder builder(@NotNull String id) {
        return new Builder(id);
    }

    /**
     * Configuration for a single slot in a workstation.
     */
    public static class SlotConfig {
        private final int slot;
        private final SlotFunction function;
        private final boolean acceptsHopperInput;
        private final boolean allowsHopperOutput;
        private final Set<Material> allowedMaterials;
        private final Set<Material> blockedMaterials;

        private SlotConfig(SlotBuilder builder) {
            this.slot = builder.slot;
            this.function = builder.function;
            this.acceptsHopperInput = builder.acceptsHopperInput;
            this.allowsHopperOutput = builder.allowsHopperOutput;
            this.allowedMaterials = builder.allowedMaterials == null ? null : 
                    Collections.unmodifiableSet(new HashSet<>(builder.allowedMaterials));
            this.blockedMaterials = builder.blockedMaterials == null ? Collections.emptySet() :
                    Collections.unmodifiableSet(new HashSet<>(builder.blockedMaterials));
        }

        public int getSlot() {
            return slot;
        }

        public SlotFunction getFunction() {
            return function;
        }

        public boolean acceptsHopperInput() {
            return acceptsHopperInput;
        }

        public boolean allowsHopperOutput() {
            return allowsHopperOutput;
        }

        public boolean canAcceptMaterial(@NotNull Material material) {
            if (blockedMaterials.contains(material)) return false;
            if (allowedMaterials == null) return true;
            return allowedMaterials.contains(material);
        }

        public static SlotBuilder builder(int slot) {
            return new SlotBuilder(slot);
        }
    }

    /**
     * Builder for SlotConfig.
     */
    public static class SlotBuilder {
        private final int slot;
        private SlotFunction function = SlotFunction.STORAGE;
        private boolean acceptsHopperInput = true;
        private boolean allowsHopperOutput = true;
        private Set<Material> allowedMaterials = null;
        private Set<Material> blockedMaterials = null;

        private SlotBuilder(int slot) {
            this.slot = slot;
        }

        public SlotBuilder function(@NotNull SlotFunction function) {
            this.function = function;
            switch (function) {
                case INPUT -> {
                    this.acceptsHopperInput = true;
                    this.allowsHopperOutput = false;
                }
                case OUTPUT -> {
                    this.acceptsHopperInput = false;
                    this.allowsHopperOutput = true;
                }
                case FUEL -> {
                    this.acceptsHopperInput = true;
                    this.allowsHopperOutput = false;
                }
                case STORAGE -> {
                    this.acceptsHopperInput = true;
                    this.allowsHopperOutput = true;
                }
                case LOCKED, DECORATION -> {
                    this.acceptsHopperInput = false;
                    this.allowsHopperOutput = false;
                }
            }
            return this;
        }

        public SlotBuilder acceptsHopperInput(boolean accepts) {
            this.acceptsHopperInput = accepts;
            return this;
        }

        public SlotBuilder allowsHopperOutput(boolean allows) {
            this.allowsHopperOutput = allows;
            return this;
        }

        public SlotBuilder allowedMaterials(Material... materials) {
            this.allowedMaterials = new HashSet<>(Arrays.asList(materials));
            return this;
        }

        public SlotBuilder allowedMaterials(Set<Material> materials) {
            this.allowedMaterials = materials;
            return this;
        }

        public SlotBuilder blockedMaterials(Material... materials) {
            this.blockedMaterials = new HashSet<>(Arrays.asList(materials));
            return this;
        }

        public SlotBuilder fuelOnly() {
            this.function = SlotFunction.FUEL;
            this.acceptsHopperInput = true;
            this.allowsHopperOutput = false;
            return this;
        }

        public SlotConfig build() {
            return new SlotConfig(this);
        }
    }

    /**
     * Builder for WorkstationConfig.
     */
    public static class Builder {
        private final String id;
        private final Set<Material> materials = new HashSet<>();
        private final List<SlotConfig> slots = new ArrayList<>();
        private int inventorySize = 27;
        private boolean enabled = true;

        private Builder(String id) {
            this.id = id;
        }

        public Builder material(@NotNull Material material) {
            this.materials.add(material);
            return this;
        }

        public Builder materials(@NotNull Material... materials) {
            this.materials.addAll(Arrays.asList(materials));
            return this;
        }

        public Builder inventorySize(int size) {
            this.inventorySize = size;
            return this;
        }

        public Builder slot(@NotNull SlotConfig slot) {
            this.slots.add(slot);
            return this;
        }

        public Builder slot(int slotIndex, @NotNull SlotFunction function) {
            this.slots.add(SlotConfig.builder(slotIndex).function(function).build());
            return this;
        }

        public Builder inputSlot(int slotIndex) {
            this.slots.add(SlotConfig.builder(slotIndex).function(SlotFunction.INPUT).build());
            return this;
        }

        public Builder outputSlot(int slotIndex) {
            this.slots.add(SlotConfig.builder(slotIndex).function(SlotFunction.OUTPUT).build());
            return this;
        }

        public Builder fuelSlot(int slotIndex) {
            this.slots.add(SlotConfig.builder(slotIndex).fuelOnly().build());
            return this;
        }

        public Builder storageSlot(int slotIndex) {
            this.slots.add(SlotConfig.builder(slotIndex).function(SlotFunction.STORAGE).build());
            return this;
        }

        public Builder enabled(boolean enabled) {
            this.enabled = enabled;
            return this;
        }

        public WorkstationConfig build() {
            if (id == null || id.isEmpty()) {
                throw new IllegalStateException("Workstation ID is required");
            }
            if (materials.isEmpty()) {
                throw new IllegalStateException("At least one material is required");
            }
            return new WorkstationConfig(this);
        }
    }

    /**
     * The function/purpose of a slot.
     */
    public enum SlotFunction {
        INPUT,
        OUTPUT,
        FUEL,
        STORAGE,
        LOCKED,
        DECORATION
    }
}
