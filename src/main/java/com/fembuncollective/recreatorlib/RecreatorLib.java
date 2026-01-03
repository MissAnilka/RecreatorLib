package com.fembuncollective.recreatorlib;

import com.fembuncollective.recreatorlib.hopper.CustomWorkstationRegistry;
import com.fembuncollective.recreatorlib.hopper.HopperAPI;
import com.fembuncollective.recreatorlib.hopper.HopperManager;
import com.fembuncollective.recreatorlib.hopper.WorkstationRegistry;
import com.fembuncollective.recreatorlib.hopper.listener.HopperListener;
import com.fembuncollective.recreatorlib.integration.IntegrationManager;
import org.bstats.bukkit.Metrics;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * RecreatorLib - A library plugin for MissAnilka's projects
 * 
 * @author MissAnilka
 * @version 1.0.0
 */
public final class RecreatorLib extends JavaPlugin {

    private static RecreatorLib instance;
    private HopperManager hopperManager;
    private WorkstationRegistry workstationRegistry;
    private CustomWorkstationRegistry customWorkstationRegistry;
    private HopperAPI hopperAPI;
    private IntegrationManager integrationManager;

    @Override
    public void onEnable() {
        instance = this;
        
        // Print startup banner first
        printStartupBanner();
        
        // Initialize managers
        hopperManager = HopperManager.getInstance();
        workstationRegistry = WorkstationRegistry.getInstance();
        customWorkstationRegistry = CustomWorkstationRegistry.getInstance();
        hopperAPI = new HopperAPI();
        
        // Register listeners
        getServer().getPluginManager().registerEvents(new HopperListener(), this);
        
        // Initialize integrations
        integrationManager = IntegrationManager.init(this);
        integrationManager.loadIntegrations();
        
        // Initialize bStats metrics
        int pluginId = 28692;
        Metrics metrics = new Metrics(this, pluginId);
    }
    
    private void printStartupBanner() {
        String version = getPluginMeta().getVersion();
        String versionText = "RecreatorLib v" + version;
        int boxWidth = 52; // inner width between ║ characters
        int padding = (boxWidth - versionText.length()) / 2;
        String paddedVersion = " ".repeat(padding) + versionText + " ".repeat(boxWidth - padding - versionText.length());
        
        String authorText = "Made by MissAnilka & The Fembun Collective";
        int authorPadding = (boxWidth - authorText.length()) / 2;
        String paddedAuthor = " ".repeat(authorPadding) + authorText + " ".repeat(boxWidth - authorPadding - authorText.length());
        
        String[] lines = {
                "",
                "  ╔════════════════════════════════════════════════════╗",
                "  ║                                                    ║",
                "  ║" + paddedVersion + "║",
                "  ║                                                    ║",
                "  ║" + paddedAuthor + "║",
                "  ║                                                    ║",
                "  ╚════════════════════════════════════════════════════╝",
                ""
        };
        
        for (String line : lines) {
            getLogger().info(line);
        }
    }

    @Override
    public void onDisable() {
        // Cleanup
        if (hopperManager != null) {
            hopperManager.clear();
        }
        if (workstationRegistry != null) {
            workstationRegistry.clear();
        }
        if (customWorkstationRegistry != null) {
            customWorkstationRegistry.clear();
        }
        if (integrationManager != null) {
            integrationManager.unloadAll();
        }
        
        getLogger().info("RecreatorLib has been disabled!");
        instance = null;
    }

    /**
     * Gets the instance of RecreatorLib
     * 
     * @return The plugin instance
     */
    public static RecreatorLib getInstance() {
        return instance;
    }

    /**
     * Gets the HopperAPI for registering custom workstations.
     * This is the main API you should use for hopper integration.
     * 
     * @return The hopper API
     */
    public HopperAPI getHopperAPI() {
        return hopperAPI;
    }

    /**
     * Gets the HopperManager instance (advanced usage)
     * 
     * @return The hopper manager
     */
    public HopperManager getHopperManager() {
        return hopperManager;
    }

    /**
     * Gets the WorkstationRegistry instance (simple workstations)
     * 
     * @return The workstation registry
     */
    public WorkstationRegistry getWorkstationRegistry() {
        return workstationRegistry;
    }

    /**
     * Gets the CustomWorkstationRegistry instance (complex workstations with recipe groups)
     * 
     * @return The custom workstation registry
     */
    public CustomWorkstationRegistry getCustomWorkstationRegistry() {
        return customWorkstationRegistry;
    }

    /**
     * Gets the IntegrationManager for accessing plugin integrations.
     * 
     * @return The integration manager
     */
    public IntegrationManager getIntegrationManager() {
        return integrationManager;
    }
}
