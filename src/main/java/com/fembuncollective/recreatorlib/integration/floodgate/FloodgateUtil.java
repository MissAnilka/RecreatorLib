package com.fembuncollective.recreatorlib.integration.floodgate;

import com.fembuncollective.recreatorlib.integration.IntegrationManager;
import org.bukkit.entity.Player;

import java.util.Optional;
import java.util.UUID;

/**
 * Utility class for easy access to Floodgate functionality.
 * All methods are null-safe and will return sensible defaults if Floodgate is not present.
 * 
 * @author MissAnilka
 */
public final class FloodgateUtil {

    private FloodgateUtil() {
        // Utility class - no instantiation
    }

    /**
     * Checks if Floodgate integration is available.
     * 
     * @return true if Floodgate is installed and the integration is active
     */
    public static boolean isAvailable() {
        return getIntegration().map(FloodgateIntegration::isActive).orElse(false);
    }

    /**
     * Checks if a player is playing on Bedrock Edition.
     * 
     * @param player The player to check
     * @return true if the player is on Bedrock, false if on Java or Floodgate isn't available
     */
    public static boolean isBedrock(Player player) {
        return getIntegration().map(f -> f.isBedrockPlayer(player)).orElse(false);
    }

    /**
     * Checks if a UUID belongs to a Bedrock player.
     * 
     * @param uuid The UUID to check
     * @return true if the UUID is a Bedrock player
     */
    public static boolean isBedrock(UUID uuid) {
        return getIntegration().map(f -> f.isBedrockPlayer(uuid)).orElse(false);
    }

    /**
     * Checks if a player is playing on Java Edition.
     * 
     * @param player The player to check
     * @return true if the player is on Java Edition
     */
    public static boolean isJava(Player player) {
        return !isBedrock(player);
    }

    /**
     * Gets the original Bedrock username (without the server prefix).
     * 
     * @param player The player to get the username for
     * @return The Bedrock username, or the player's name if not Bedrock
     */
    public static String getBedrockName(Player player) {
        return getIntegration()
                .map(f -> f.getBedrockUsername(player))
                .orElse(player.getName());
    }

    /**
     * Gets the Xbox UID for a Bedrock player.
     * 
     * @param player The player
     * @return Optional containing the XUID if available
     */
    public static Optional<String> getXuid(Player player) {
        return getIntegration().map(f -> f.getXuid(player));
    }

    /**
     * Gets the device OS the player is using.
     * 
     * @param player The player
     * @return Optional containing the device OS (Android, iOS, Windows, etc.)
     */
    public static Optional<String> getDeviceOs(Player player) {
        return getIntegration().map(f -> f.getDeviceOs(player));
    }

    /**
     * Gets the input mode the player is using.
     * 
     * @param player The player
     * @return Optional containing the input mode (Touch, Controller, Keyboard)
     */
    public static Optional<String> getInputMode(Player player) {
        return getIntegration().map(f -> f.getInputMode(player));
    }

    /**
     * Checks if the Bedrock player is using touch controls.
     * 
     * @param player The player
     * @return true if using touch controls
     */
    public static boolean isUsingTouch(Player player) {
        return getInputMode(player).map(m -> m.equalsIgnoreCase("TOUCH")).orElse(false);
    }

    /**
     * Checks if the Bedrock player is using a controller.
     * 
     * @param player The player
     * @return true if using a controller
     */
    public static boolean isUsingController(Player player) {
        return getInputMode(player).map(m -> m.equalsIgnoreCase("CONTROLLER")).orElse(false);
    }

    /**
     * Checks if the Bedrock player is using keyboard/mouse.
     * 
     * @param player The player
     * @return true if using keyboard/mouse
     */
    public static boolean isUsingKeyboard(Player player) {
        return getInputMode(player).map(m -> m.equalsIgnoreCase("KEYBOARD_MOUSE")).orElse(false);
    }

    /**
     * Checks if the player has a linked Java account.
     * 
     * @param player The player
     * @return true if the player has linked accounts
     */
    public static boolean hasLinkedAccount(Player player) {
        return getIntegration().map(f -> f.hasLinkedAccount(player)).orElse(false);
    }

    /**
     * Gets the linked Java UUID if available.
     * 
     * @param player The player
     * @return Optional containing the linked Java UUID
     */
    public static Optional<UUID> getLinkedJavaUuid(Player player) {
        return getIntegration().map(f -> f.getLinkedJavaUuid(player));
    }

    /**
     * Gets the username prefix used for Bedrock players.
     * 
     * @return The prefix string (typically ".")
     */
    public static String getPrefix() {
        return getIntegration().map(FloodgateIntegration::getUsernamePrefix).orElse("");
    }

    private static Optional<FloodgateIntegration> getIntegration() {
        IntegrationManager manager = IntegrationManager.getInstance();
        if (manager == null) return Optional.empty();
        return manager.getFloodgate();
    }
}
