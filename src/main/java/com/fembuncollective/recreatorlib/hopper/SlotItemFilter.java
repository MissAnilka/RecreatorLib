package com.fembuncollective.recreatorlib.hopper;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Predicate;

/**
 * Defines what items are allowed in a workstation slot.
 * The hopper system is PERMISSIVE - if an item COULD be valid for any recipe, it will be allowed.
 * 
 * @author MissAnilka
 */
public class SlotItemFilter {

    private final FilterMode mode;
    private final Set<Material> allowedMaterials;
    private final Set<Material> blockedMaterials;
    private final Predicate<ItemStack> customValidator;
    private final boolean allowVanillaFuel;
    private final boolean allowVanillaSmeltable;
    private final Set<Material> customFuelSources;
    private final Set<Material> customInputItems;

    private SlotItemFilter(Builder builder) {
        this.mode = builder.mode;
        this.allowedMaterials = builder.allowedMaterials == null ? null : Collections.unmodifiableSet(new HashSet<>(builder.allowedMaterials));
        this.blockedMaterials = Collections.unmodifiableSet(new HashSet<>(builder.blockedMaterials));
        this.customValidator = builder.customValidator;
        this.allowVanillaFuel = builder.allowVanillaFuel;
        this.allowVanillaSmeltable = builder.allowVanillaSmeltable;
        this.customFuelSources = Collections.unmodifiableSet(new HashSet<>(builder.customFuelSources));
        this.customInputItems = Collections.unmodifiableSet(new HashSet<>(builder.customInputItems));
    }

    public boolean test(@NotNull ItemStack item) {
        if (item == null || item.getType().isAir()) return false;
        Material mat = item.getType();
        if (blockedMaterials.contains(mat)) return false;
        if (customValidator != null) return customValidator.test(item);

        return switch (mode) {
            case ALLOW_ALL -> true;
            case BLOCK_ALL -> false;
            case WHITELIST -> allowedMaterials != null && allowedMaterials.contains(mat);
            case FUEL_SLOT -> (allowVanillaFuel && mat.isFuel()) || customFuelSources.contains(mat);
            case INPUT_SLOT -> (allowVanillaSmeltable && isVanillaSmeltable(mat)) || customInputItems.contains(mat);
            case PERMISSIVE_FUEL -> mat.isFuel() || customFuelSources.contains(mat);
            case PERMISSIVE_INPUT -> customInputItems.isEmpty() || customInputItems.contains(mat) || (allowVanillaSmeltable && isVanillaSmeltable(mat));
            case CUSTOM -> allowedMaterials != null && allowedMaterials.contains(mat);
        };
    }

    private boolean isVanillaSmeltable(Material material) {
        return material.isItem() && !material.isAir();
    }

    public static SlotItemFilter allowAll() { return new Builder().mode(FilterMode.ALLOW_ALL).build(); }
    public static SlotItemFilter blockAll() { return new Builder().mode(FilterMode.BLOCK_ALL).build(); }
    public static SlotItemFilter permissiveFuel() { return new Builder().mode(FilterMode.PERMISSIVE_FUEL).allowVanillaFuel(true).build(); }
    public static SlotItemFilter permissiveInput() { return new Builder().mode(FilterMode.PERMISSIVE_INPUT).allowVanillaSmeltable(true).build(); }
    public static Builder builder() { return new Builder(); }

    public enum FilterMode { ALLOW_ALL, BLOCK_ALL, WHITELIST, FUEL_SLOT, INPUT_SLOT, PERMISSIVE_FUEL, PERMISSIVE_INPUT, CUSTOM }

    public static class Builder {
        private FilterMode mode = FilterMode.ALLOW_ALL;
        private Set<Material> allowedMaterials = null;
        private Set<Material> blockedMaterials = new HashSet<>();
        private Predicate<ItemStack> customValidator = null;
        private boolean allowVanillaFuel = false;
        private boolean allowVanillaSmeltable = false;
        private Set<Material> customFuelSources = new HashSet<>();
        private Set<Material> customInputItems = new HashSet<>();

        public Builder mode(@NotNull FilterMode mode) { this.mode = mode; return this; }
        public Builder allow(@NotNull Material... materials) { if (allowedMaterials == null) allowedMaterials = new HashSet<>(); allowedMaterials.addAll(Arrays.asList(materials)); return this; }
        public Builder block(@NotNull Material... materials) { blockedMaterials.addAll(Arrays.asList(materials)); return this; }
        public Builder customValidator(@Nullable Predicate<ItemStack> validator) { this.customValidator = validator; return this; }
        public Builder allowVanillaFuel(boolean allow) { this.allowVanillaFuel = allow; return this; }
        public Builder allowVanillaSmeltable(boolean allow) { this.allowVanillaSmeltable = allow; return this; }
        public Builder addFuelSources(@NotNull Material... materials) { customFuelSources.addAll(Arrays.asList(materials)); return this; }
        public Builder addInputItems(@NotNull Material... materials) { customInputItems.addAll(Arrays.asList(materials)); return this; }
        public SlotItemFilter build() { return new SlotItemFilter(this); }
    }
}
