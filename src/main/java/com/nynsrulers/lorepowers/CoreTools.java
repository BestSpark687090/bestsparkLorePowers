package com.nynsrulers.lorepowers;

import org.bukkit.ChatColor;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Objects;

public class CoreTools {
    private LorePowers plugin;
    private static CoreTools instance;

    public static CoreTools getInstance() {
        if (instance == null) {
            instance = new CoreTools();
        }
        return instance;
    }

    public void setPlugin(LorePowers plugin) {
        this.plugin = plugin;
    }

    public String getPrefix() {
        return (ChatColor.translateAlternateColorCodes('&', Objects.requireNonNull(plugin.getConfig().getString("Prefix"))) + " ");
    }

    public void checkForUpdates() {
        if (plugin.getConfig().getBoolean("CheckForUpdates")) {
            HttpRequest check = HttpRequest.newBuilder()
                    .GET()
                    .uri(URI.create("https://api.github.com/repos/nynsrulers/LorePowers/releases/latest"))
                    .build();
            HttpResponse<String> response = null;
            try {
                response = HttpClient.newHttpClient().send(check, HttpResponse.BodyHandlers.ofString());
            } catch (IOException | InterruptedException e) {
                plugin.getLogger().info("Failed to check for updates.");
                e.printStackTrace();
            }
            assert response != null;
            String json = response.body();
            String latestVersion = json.substring(json.indexOf("tag_name") + 11, json.indexOf('"', json.indexOf("tag_name") + 11));
            String version = "v" + plugin.getDescription().getVersion();
            if (!version.equals(latestVersion)) {
                plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> {
                    plugin.getLogger().info("An update is available!");
                    plugin.getLogger().info("Your version: " + version + " - Latest version: " + latestVersion);
                    plugin.getLogger().info("Please update at https://github.com/nynsrulers/LorePowers/releases!");
                }, 60L);
            }
        }
    }
}
