package com.fembuncollective.recreatorlib.integration.placeholderapi;

import com.fembuncollective.recreatorlib.RecreatorLib;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

/**
 * Manages custom placeholder transformations.
 * Allows users to define placeholders that transform output from other placeholders.
 * 
 * @author MissAnilka
 */
public class CustomPlaceholderManager {

    private final RecreatorLib plugin;
    private final File configFile;
    private final Map<String, CustomPlaceholder> placeholders = new HashMap<>();

    public CustomPlaceholderManager(RecreatorLib plugin) {
        this.plugin = plugin;
        this.configFile = new File(plugin.getDataFolder(), "custom_placeholders.yml");
    }

    /**
     * Loads or reloads placeholders from the config file.
     */
    public void loadPlaceholders() {
        placeholders.clear();
        
        // Create default config if it doesn't exist
        if (!configFile.exists()) {
            createDefaultConfig();
        }

        YamlConfiguration config = YamlConfiguration.loadConfiguration(configFile);
        ConfigurationSection placeholdersSection = config.getConfigurationSection("placeholders");
        
        if (placeholdersSection == null) {
            return;
        }

        for (String key : placeholdersSection.getKeys(false)) {
            ConfigurationSection section = placeholdersSection.getConfigurationSection(key);
            if (section == null) continue;

            String source = section.getString("source");
            String defaultValue = section.getString("default", "");
            
            ConfigurationSection mappings = section.getConfigurationSection("mappings");
            Map<String, String> valueMap = new HashMap<>();
            
            if (mappings != null) {
                for (String mapKey : mappings.getKeys(false)) {
                    valueMap.put(mapKey, mappings.getString(mapKey, mapKey));
                }
            }

            // Check for regex mappings
            ConfigurationSection regexMappings = section.getConfigurationSection("regex_mappings");
            Map<String, String> regexMap = new HashMap<>();
            
            if (regexMappings != null) {
                for (String pattern : regexMappings.getKeys(false)) {
                    regexMap.put(pattern, regexMappings.getString(pattern, ""));
                }
            }

            placeholders.put(key.toLowerCase(), new CustomPlaceholder(source, defaultValue, valueMap, regexMap));
        }
    }

    /**
     * Creates the default config file with examples.
     */
    private void createDefaultConfig() {
        try {
            plugin.getDataFolder().mkdirs();
            
            // Try to copy from resources first
            InputStream is = plugin.getResource("custom_placeholders.yml");
            if (is != null) {
                Files.copy(is, configFile.toPath());
                is.close();
            } else {
                // Create manually if resource doesn't exist
                configFile.createNewFile();
                YamlConfiguration config = new YamlConfiguration();
                config.options().header("""
                    RecreatorLib Custom Placeholders
                    =================================
                    
                    Define custom placeholders that transform output from other placeholders.
                    All custom placeholders will be available as: %recreator_<name>%
                    
                    Configuration format:
                    placeholders:
                      <name>:                    # Your placeholder name (used as %recreator_<name>%)
                        source: "%placeholder%"  # The source placeholder to transform
                        default: "Unknown"       # Default value if no mapping matches
                        mappings:                # Exact value mappings
                          "input_value": "output_value"
                        regex_mappings:          # Regex pattern mappings (optional)
                          "pattern": "replacement"
                    
                    Example with server names from proxy:
                    You can use placeholders from plugins like:
                    - %server_name% (ServerUtils, requires velocity-side plugin)
                    - %redisbungee_server_name% (RedisBungee)
                    - %plan_server_name% (Plan)
                    
                    """);
                config.save(configFile);
            }
        } catch (IOException e) {
            plugin.getLogger().warning("Could not create custom_placeholders.yml: " + e.getMessage());
        }
    }

    /**
     * Gets a custom placeholder value for a player.
     * 
     * @param player The player
     * @param name The placeholder name (without recreator_ prefix)
     * @return The transformed value, or null if placeholder doesn't exist
     */
    public String getPlaceholder(OfflinePlayer player, String name) {
        CustomPlaceholder placeholder = placeholders.get(name.toLowerCase());
        if (placeholder == null) {
            return null;
        }
        return placeholder.getValue(player);
    }

    /**
     * Gets the number of loaded custom placeholders.
     */
    public int getPlaceholderCount() {
        return placeholders.size();
    }

    /**
     * Represents a custom placeholder with transformation rules.
     */
    private record CustomPlaceholder(
            String source,
            String defaultValue,
            Map<String, String> mappings,
            Map<String, String> regexMappings
    ) {
        /**
         * Gets the transformed value for a player.
         */
        public String getValue(OfflinePlayer player) {
            // Parse the source placeholder
            String rawValue = PlaceholderAPI.setPlaceholders(player, source);
            
            // Check exact mappings first
            if (mappings.containsKey(rawValue)) {
                return mappings.get(rawValue);
            }
            
            // Check case-insensitive
            for (Map.Entry<String, String> entry : mappings.entrySet()) {
                if (entry.getKey().equalsIgnoreCase(rawValue)) {
                    return entry.getValue();
                }
            }
            
            // Check regex mappings
            for (Map.Entry<String, String> entry : regexMappings.entrySet()) {
                if (rawValue.matches(entry.getKey())) {
                    return rawValue.replaceAll(entry.getKey(), entry.getValue());
                }
            }
            
            // Return default if no mapping matches
            return defaultValue.isEmpty() ? rawValue : defaultValue;
        }
    }
}
