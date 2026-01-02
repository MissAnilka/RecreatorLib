package com.fembuncollective.recreatorlib.integration;

import com.fembuncollective.recreatorlib.RecreatorLib;

/**
 * Base interface for all plugin integrations.
 * Implement this interface to create a new integration.
 * 
 * @author MissAnilka
 */
public interface PluginIntegration {

    /**
     * Gets the name of the plugin this integration is for.
     * 
     * @return The plugin name
     */
    String getPluginName();

    /**
     * Loads the integration.
     * Called when the target plugin is detected and available.
     * 
     * @param plugin The RecreatorLib plugin instance
     * @return true if the integration loaded successfully
     */
    boolean load(RecreatorLib plugin);

    /**
     * Unloads the integration.
     * Called when RecreatorLib is being disabled.
     */
    void unload();

    /**
     * Checks if the integration is currently active.
     * 
     * @return true if the integration is active and working
     */
    boolean isActive();

    /**
     * Gets the version of the integration.
     * 
     * @return The integration version string
     */
    default String getVersion() {
        return "1.0.0";
    }
}
