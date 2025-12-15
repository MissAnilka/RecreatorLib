package com.fembuncollective.recreatorlib.hopper;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * Configuration for a workstation with complex recipe support.
 * Supports vanilla recipes, general custom recipes, and recipe groups with fuel restrictions.
 * 
 * @author MissAnilka
 */
public class CustomWorkstationConfig {

    private final String id;
    private final Set<Material> materials;
    private final int inventorySize;
    private final Map<Integer, SlotDefinition> slots;
    private final boolean vanillaRecipesEnabled;
    private final boolean vanillaFuelEnabled;
    private final Set<Material> generalFuelSources;
    private final Set<Material> generalInputItems;
    private final List<RecipeGroup> recipeGroups;
    private boolean enabled = true;

    private CustomWorkstationConfig(Builder builder) {
        this.id = builder.id;
        this.materials = Collections.unmodifiableSet(new HashSet<>(builder.materials));
        this.inventorySize = builder.inventorySize;
        this.slots = Collections.unmodifiableMap(new HashMap<>(builder.slots));
        this.vanillaRecipesEnabled = builder.vanillaRecipesEnabled;
        this.vanillaFuelEnabled = builder.vanillaFuelEnabled;
        this.generalFuelSources = Collections.unmodifiableSet(new HashSet<>(builder.generalFuelSources));
        this.generalInputItems = Collections.unmodifiableSet(new HashSet<>(builder.generalInputItems));
        this.recipeGroups = Collections.unmodifiableList(new ArrayList<>(builder.recipeGroups));
    }

    public String getId() { return id; }
    public Set<Material> getMaterials() { return materials; }
    public int getInventorySize() { return inventorySize; }
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public boolean isVanillaRecipesEnabled() { return vanillaRecipesEnabled; }
    public boolean isVanillaFuelEnabled() { return vanillaFuelEnabled; }
    public Set<Material> getGeneralFuelSources() { return generalFuelSources; }
    public Set<Material> getGeneralInputItems() { return generalInputItems; }
    public List<RecipeGroup> getRecipeGroups() { return recipeGroups; }

    @Nullable
    public SlotDefinition getSlot(int slot) { return slots.get(slot); }

    public int[] getInputSlots() {
        return slots.entrySet().stream()
                .filter(e -> e.getValue().getType() == SlotType.INPUT)
                .mapToInt(Map.Entry::getKey).toArray();
    }

    public int[] getFuelSlots() {
        return slots.entrySet().stream()
                .filter(e -> e.getValue().getType() == SlotType.FUEL)
                .mapToInt(Map.Entry::getKey).toArray();
    }

    public int[] getOutputSlots() {
        return slots.entrySet().stream()
                .filter(e -> e.getValue().getType() == SlotType.OUTPUT)
                .mapToInt(Map.Entry::getKey).toArray();
    }

    public boolean canAcceptInput(@NotNull ItemStack item) {
        Material mat = item.getType();
        if (vanillaRecipesEnabled) return true;
        if (generalInputItems.contains(mat)) return true;
        for (RecipeGroup group : recipeGroups) {
            if (group.getInputItems().contains(mat)) return true;
        }
        return generalInputItems.isEmpty() && recipeGroups.isEmpty();
    }

    public boolean canAcceptFuel(@NotNull ItemStack item) {
        Material mat = item.getType();
        if (vanillaFuelEnabled && mat.isFuel()) return true;
        if (generalFuelSources.contains(mat)) return true;
        for (RecipeGroup group : recipeGroups) {
            if (group.getFuelSources().contains(mat)) return true;
        }
        return false;
    }

    public static Builder builder(@NotNull String id) { return new Builder(id); }

    public static class SlotDefinition {
        private final int slot;
        private final SlotType type;
        private final SlotItemFilter filter;

        public SlotDefinition(int slot, SlotType type, @Nullable SlotItemFilter filter) {
            this.slot = slot;
            this.type = type;
            this.filter = filter;
        }

        public int getSlot() { return slot; }
        public SlotType getType() { return type; }
        @Nullable public SlotItemFilter getFilter() { return filter; }
        public boolean acceptsHopperInput() { return type == SlotType.INPUT || type == SlotType.FUEL || type == SlotType.STORAGE; }
        public boolean allowsHopperOutput() { return type == SlotType.OUTPUT || type == SlotType.STORAGE; }
    }

    public enum SlotType { INPUT, FUEL, OUTPUT, STORAGE, LOCKED }

    public static class RecipeGroup {
        private final String name;
        private final Set<Material> inputItems;
        private final Set<Material> fuelSources;

        public RecipeGroup(@NotNull String name, @NotNull Set<Material> inputItems, @NotNull Set<Material> fuelSources) {
            this.name = name;
            this.inputItems = Collections.unmodifiableSet(new HashSet<>(inputItems));
            this.fuelSources = Collections.unmodifiableSet(new HashSet<>(fuelSources));
        }

        public String getName() { return name; }
        public Set<Material> getInputItems() { return inputItems; }
        public Set<Material> getFuelSources() { return fuelSources; }

        public static RecipeGroupBuilder builder(@NotNull String name) { return new RecipeGroupBuilder(name); }
    }

    public static class RecipeGroupBuilder {
        private final String name;
        private final Set<Material> inputItems = new HashSet<>();
        private final Set<Material> fuelSources = new HashSet<>();

        private RecipeGroupBuilder(String name) { this.name = name; }

        public RecipeGroupBuilder input(@NotNull Material... materials) {
            inputItems.addAll(Arrays.asList(materials));
            return this;
        }

        public RecipeGroupBuilder fuel(@NotNull Material... materials) {
            fuelSources.addAll(Arrays.asList(materials));
            return this;
        }

        public RecipeGroup build() { return new RecipeGroup(name, inputItems, fuelSources); }
    }

    public static class Builder {
        private final String id;
        private final Set<Material> materials = new HashSet<>();
        private int inventorySize = 27;
        private final Map<Integer, SlotDefinition> slots = new HashMap<>();
        private boolean vanillaRecipesEnabled = false;
        private boolean vanillaFuelEnabled = false;
        private final Set<Material> generalFuelSources = new HashSet<>();
        private final Set<Material> generalInputItems = new HashSet<>();
        private final List<RecipeGroup> recipeGroups = new ArrayList<>();

        private Builder(String id) { this.id = id; }

        public Builder material(@NotNull Material material) { materials.add(material); return this; }
        public Builder materials(@NotNull Material... mats) { materials.addAll(Arrays.asList(mats)); return this; }
        public Builder inventorySize(int size) { this.inventorySize = size; return this; }

        public Builder inputSlot(int slot) {
            slots.put(slot, new SlotDefinition(slot, SlotType.INPUT, SlotItemFilter.permissiveInput()));
            return this;
        }

        public Builder fuelSlot(int slot) {
            slots.put(slot, new SlotDefinition(slot, SlotType.FUEL, SlotItemFilter.permissiveFuel()));
            return this;
        }

        public Builder outputSlot(int slot) {
            slots.put(slot, new SlotDefinition(slot, SlotType.OUTPUT, SlotItemFilter.blockAll()));
            return this;
        }

        public Builder enableVanillaRecipes(boolean enabled) { this.vanillaRecipesEnabled = enabled; return this; }
        public Builder enableVanillaFuel(boolean enabled) { this.vanillaFuelEnabled = enabled; return this; }
        public Builder addGeneralFuel(@NotNull Material... mats) { generalFuelSources.addAll(Arrays.asList(mats)); return this; }
        public Builder addGeneralInput(@NotNull Material... mats) { generalInputItems.addAll(Arrays.asList(mats)); return this; }
        public Builder addRecipeGroup(@NotNull RecipeGroup group) { recipeGroups.add(group); return this; }

        public CustomWorkstationConfig build() {
            if (id == null || id.isEmpty()) throw new IllegalStateException("Workstation ID is required");
            if (materials.isEmpty()) throw new IllegalStateException("At least one material is required");
            return new CustomWorkstationConfig(this);
        }
    }
}
