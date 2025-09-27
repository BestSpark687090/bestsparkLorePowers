package com.nynsrulers.lorepowers;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;

public class TimedEffectManager {
    private LorePowers plugin;
    private static TimedEffectManager instance;
    public static TimedEffectManager getInstance() {
        if (instance == null) {
            instance = new TimedEffectManager();
        }
        return instance;
    }
    public void setPlugin(LorePowers plugin) {
        this.plugin = plugin;
    }

    private final Map<Player, BukkitTask> activeTasks = new HashMap<>();

    public void startTimedPower(Player player) {
        if (activeTasks.containsKey(player)) return;
        BukkitTask task = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            World world = player.getWorld();
            long time = world.getTime();
            if (plugin.checkPower(player.getUniqueId(), Power.FOX_MAGIC)) {
                if (time >= 0 && time < 12300) {
                    // day effects
                    player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 320, 0, true, false));
                    player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 320, 1, true, false));
                } else {
                    // night effects
                    player.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 320, 0, true, false));
                }
            }
        }, 0L, 180L);

        activeTasks.put(player, task);
    }

    public void stopTimedPower(Player player) {
        BukkitTask task = activeTasks.remove(player);
        if (task != null) {
            task.cancel();
        }
    }

    public void stopAll() {
        activeTasks.values().forEach(BukkitTask::cancel);
        activeTasks.clear();
    }
}
