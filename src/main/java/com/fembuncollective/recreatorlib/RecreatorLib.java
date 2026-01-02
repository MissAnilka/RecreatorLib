package com.fembuncollective.recreatorlib;

import com.fembuncollective.recreatorlib.hopper.CustomWorkstationRegistry;
import com.fembuncollective.recreatorlib.hopper.HopperAPI;
import com.fembuncollective.recreatorlib.hopper.HopperManager;
import com.fembuncollective.recreatorlib.hopper.WorkstationRegistry;
import com.fembuncollective.recreatorlib.hopper.listener.HopperListener;
import com.fembuncollective.recreatorlib.integration.IntegrationManager;
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
        
        // Print startup banner
        printStartupBanner();
    }
    
    private void printStartupBanner() {
        String version = getDescription().getVersion();
        String[] lines = {
                "",
                "  ╔════════════════════════════════════════════════════╗",
                "  ║                                                    ║",
                "  ║             RecreatorLib v" + version + centerPad(version, 23) + "║",
                "  ║                                                    ║",
                "  ║  Made by MissAnilka & The Fembun Collective        ║",
                "  ║                                                    ║",
                "  ╚════════════════════════════════════════════════════╝",
                ""
        };
        
        for (String line : lines) {
            getLogger().info(line);
        }
    }
    
    private String centerPad(String version, int totalWidth) {
        int padding = totalWidth - version.length();
        return " ".repeat(Math.max(0, padding));
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
