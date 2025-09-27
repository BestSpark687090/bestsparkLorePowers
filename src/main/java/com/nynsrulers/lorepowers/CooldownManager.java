package com.nynsrulers.lorepowers;

import java.util.Set;
import java.util.HashSet;
import java.util.UUID;

public class CooldownManager {
    private LorePowers plugin;
    private static CooldownManager instance;
    public static CooldownManager getInstance() {
        if (instance == null) {
            instance = new CooldownManager();
        }
        return instance;
    }
    public void setPlugin(LorePowers plugin) {
        this.plugin = plugin;
    }
    private final Set<CooldownKey> activeCooldowns = new HashSet<>();

    public void addCooldown(UUID player, Power power, Long duration) {
        activeCooldowns.add(new CooldownKey(player, power));
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            activeCooldowns.remove(new CooldownKey(player, power));
        }, duration);
    }
    public boolean checkCooldown(UUID player, Power power) {
        return activeCooldowns.contains(new CooldownKey(player, power));
    }
}