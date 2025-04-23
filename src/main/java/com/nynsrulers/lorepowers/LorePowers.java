package com.nynsrulers.lorepowers;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityResurrectEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.UUID;

public final class LorePowers extends JavaPlugin implements Listener {

    @Override
    public void onEnable() {
        getConfig().options().copyDefaults();
        saveDefaultConfig();
        getServer().getPluginManager().registerEvents(this, this);
        CoreTools.getInstance().setPlugin(this);
        getCommand("lorepowers").setExecutor(new ManageCMD(this));
        CoreTools.getInstance().checkForUpdates();
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public boolean checkPower(UUID playerUUID, Power power) {
        return getConfig().getStringList("PowerLinks." + playerUUID.toString()).contains(power.toString());
    }

    @EventHandler
    public void onTotem(EntityResurrectEvent e) {
        if (e.isCancelled()) return;
        Entity lastCause = e.getEntity().getLastDamageCause().getDamageSource().getCausingEntity();
        if (lastCause instanceof Player && checkPower(lastCause.getUniqueId(), Power.VOID_TOTEMS)) {
            e.setCancelled(true);
            EntityEquipment killedItems = e.getEntity().getEquipment();
            if (killedItems != null) {
                if (killedItems.getItemInMainHand().getType() == Material.TOTEM_OF_UNDYING) {
                    killedItems.setItemInMainHand(null);
                } else if (killedItems.getItemInOffHand().getType() == Material.TOTEM_OF_UNDYING) {
                    killedItems.setItemInOffHand(null);
                }
            }
            e.getEntity().sendMessage(CoreTools.getInstance().getPrefix() + ChatColor.RED + "Your totem was voided by the powers of " + lastCause.getName() + "!");
            lastCause.sendMessage(CoreTools.getInstance().getPrefix() + ChatColor.RED + "You voided " + e.getEntity().getName() + "'s totem!");
            ((Player) lastCause).addPotionEffect(new PotionEffect(PotionEffectType.DARKNESS, 400, 2, true, true, true));
            ((Player) lastCause).addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 400, 2, true, true, true));
        }
    }

    void reloadPlugin() {
        reloadConfig();
        CoreTools.getInstance().setPlugin(this);
        CoreTools.getInstance().checkForUpdates();
    }
}
