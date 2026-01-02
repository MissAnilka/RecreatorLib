package com.fembuncollective.recreatorlib.integration.placeholderapi;

import com.fembuncollective.recreatorlib.RecreatorLib;
import com.fembuncollective.recreatorlib.integration.IntegrationManager;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.Optional;

/**
 * Utility class for easy access to PlaceholderAPI functionality.
 * All methods are null-safe and will return sensible defaults if PlaceholderAPI is not present.
 * 
 * @author MissAnilka
 */
public final class PlaceholderUtil {

    private PlaceholderUtil() {
        // Utility class - no instantiation
    }

    /**
     * Checks if PlaceholderAPI integration is available.
     * 
     * @return true if PlaceholderAPI is installed and the integration is active
     */
    public static boolean isAvailable() {
        return getIntegration().map(PlaceholderAPIIntegration::isActive).orElse(false);
    }

    /**
     * Parses placeholders in a string for a player.
     * If PlaceholderAPI is not available, returns the original text.
     * 
     * @param player The player to parse for
     * @param text The text containing placeholders
     * @return The parsed text
     */
    public static String parse(Player player, String text) {
        return getIntegration()
                .map(p -> p.parse(player, text))
                .orElse(text);
    }

    /**
     * Parses placeholders in a string for an offline player.
     * If PlaceholderAPI is not available, returns the original text.
     * 
     * @param player The offline player to parse for
     * @param text The text containing placeholders
     * @return The parsed text
     */
    public static String parse(OfflinePlayer player, String text) {
        return getIntegration()
                .map(p -> p.parse(player, text))
                .orElse(text);
    }

    /**
     * Gets a custom RecreatorLib placeholder value.
     * 
     * @param player The player
     * @param placeholder The placeholder name (without %recreator_ prefix)
     * @return The placeholder value, or empty string if not found
     */
    public static String getCustomPlaceholder(OfflinePlayer player, String placeholder) {
        return getIntegration()
                .map(p -> p.getPlaceholderManager().getPlaceholder(player, placeholder))
                .orElse("");
    }

    /**
     * Reloads custom placeholder configurations.
     */
    public static void reload() {
        getIntegration().ifPresent(PlaceholderAPIIntegration::reload);
    }

    private static Optional<PlaceholderAPIIntegration> getIntegration() {
        IntegrationManager manager = IntegrationManager.getInstance();
        if (manager == null) return Optional.empty();
        return manager.getPlaceholderAPI();
    }
}
