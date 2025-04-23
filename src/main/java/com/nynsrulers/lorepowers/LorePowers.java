package com.nynsrulers.lorepowers;

import de.tr7zw.nbtapi.NBT;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.EnderPearl;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityResurrectEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.projectiles.ProjectileSource;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public final class LorePowers extends JavaPlugin implements Listener {
    @Override
    public void onEnable() {
        getConfig().options().copyDefaults();
        saveDefaultConfig();
        getServer().getPluginManager().registerEvents(this, this);
        CoreTools.getInstance().setPlugin(this);
        getCommand("lorepowers").setExecutor(new ManageCMD(this));
        getCommand("lorepowers").setTabCompleter(new ManageTabCompleter());
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
    public void onTotem_VoidTotems(EntityResurrectEvent e) {
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

    @EventHandler
    public void onAttack_GlitchedPresence(EntityDamageByEntityEvent e) {
        if (e.isCancelled()) return;
        if (e.getEntity() instanceof Player && checkPower(e.getEntity().getUniqueId(), Power.GLITCHED_PRESENCE)) {
            Entity lastCause = e.getDamager();
            if (lastCause instanceof Player) {
                if (!((Player) lastCause).getInventory().getItemInMainHand().getType().toString().endsWith("_SWORD")) {
                    lastCause.teleport(lastCause.getWorld().getHighestBlockAt(
                            lastCause.getLocation().add(
                                    (Math.random() - 0.5) * 200, 0, (Math.random() - 0.5) * 200
                            ).getBlockX(),
                            lastCause.getLocation().getBlockZ()
                    ).getLocation().add(0.5, 1, 0.5));
                    lastCause.sendMessage(CoreTools.getInstance().getPrefix() + ChatColor.RED + "You were glitched by " + e.getEntity().getName() + "'s presence!");
                    e.getEntity().sendMessage(CoreTools.getInstance().getPrefix() + ChatColor.RED + "You glitched " + lastCause.getName() + "'s presence!");
                } else {
                    e.setDamage(e.getDamage() * 1.5);
                }
            }
        }
    }

    @EventHandler
    public void onAttack_MapWarp(EntityDamageByEntityEvent e) {
        if (e.isCancelled()) return;
        Entity lastCause = e.getDamager();
        if (lastCause instanceof Player && checkPower(lastCause.getUniqueId(), Power.MAP_WARP)) {
            Material weapon = ((Player) lastCause).getInventory().getItemInMainHand().getType();
            if ((weapon.toString().endsWith("_SWORD") && e.getDamage() > 5) ||
                    (weapon.toString().endsWith("_AXE") && e.getDamage() > 9)) {
                e.setCancelled(true);
                lastCause.sendMessage(CoreTools.getInstance().getPrefix() + ChatColor.RED + "You cannot attack with this, as you are too weak!");
            }
        }
    }
    @EventHandler
    public void onHurt_MapWarp(EntityDamageEvent e) {
        if (e.isCancelled()) return;
        Entity hurtEntity = e.getEntity();
        if (hurtEntity instanceof Player) {
            if (checkPower(hurtEntity.getUniqueId(), Power.MAP_WARP)) {
               EntityEquipment equipment = ((Player) hurtEntity).getEquipment();
               if (equipment == null) return;
               boolean isWearingTooStrongArmor = false;
               for (ItemStack armor : equipment.getArmorContents()) {
                   if (armor.getType().toString().startsWith("DIAMOND_") ||
                           armor.getType().toString().startsWith("NETHERITE_") ||
                           armor.getType().toString().startsWith("GOLDEN_") ||
                           armor.getType().toString().startsWith("IRON_") ||
                           armor.getType().toString().startsWith("CHAINMAIL_")) {
                          isWearingTooStrongArmor = true;
                   }
               }
               if (isWearingTooStrongArmor) {
                   hurtEntity.sendMessage(CoreTools.getInstance().getPrefix() + ChatColor.RED + "You cannot wear this armor, as you are too weak!");
                   for (ItemStack armor : equipment.getArmorContents()) {
                       hurtEntity.getWorld().dropItem(hurtEntity.getLocation(), armor);
                   }
                   equipment.setArmorContents(new ItemStack[]{});
               }
            }
        }
    }
    @EventHandler
    public void onPearlThrow_NightPearls(ProjectileLaunchEvent e) {
        if (e.isCancelled()) return;
        ItemStack nightPearl = new ItemCreator(this).createNightPearl();
        if (!(e.getEntity().getShooter() instanceof Player player)) return;
        if (player.getItemInUse() == null) return;
        if (e.getEntity() instanceof EnderPearl && player.getItemInUse().isSimilar(nightPearl)) {
            if (!checkPower(player.getUniqueId(), Power.NIGHT_PEARLS)) {
                player.getInventory().remove(nightPearl);
                e.setCancelled(true);
                return;
            }
            if (player.getWorld().getEnvironment() != World.Environment.NORMAL) {
                player.sendMessage(CoreTools.getInstance().getPrefix() + ChatColor.RED + "You can only use this in the overworld!");
                e.setCancelled(true);
                return;
            }
            if (player.getWorld().getGameTime() % 24000 > 13000) {
                player.sendMessage(CoreTools.getInstance().getPrefix() + ChatColor.RED + "You can only use this at night!");
                e.setCancelled(true);
                return;
            }
            player.setCooldown(Material.ENDER_PEARL, 0);
            try {
                player.teleport(Objects.requireNonNull(player.getTargetBlockExact(40)).getLocation());
            } catch (NullPointerException ex) {
                player.sendMessage(CoreTools.getInstance().getPrefix() + ChatColor.RED + "You cannot teleport to that location!");
                e.setCancelled(true);
                return;
            }
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onJoin_SpeedMine(PlayerJoinEvent e) {
        if (checkPower(e.getPlayer().getUniqueId(), Power.SPEED_MINE)) {
            e.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.HASTE, Integer.MAX_VALUE, 1, true, true, true));
        }
    }
    @EventHandler
    public void onConsume_SpeedMine(PlayerItemConsumeEvent e) {
        if (e.getItem().getType() == Material.MILK_BUCKET && checkPower(e.getPlayer().getUniqueId(), Power.SPEED_MINE)) {
            e.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.HASTE, Integer.MAX_VALUE, 1, true, true, true));
        }
    }

    void reloadPlugin() {
        reloadConfig();
        CoreTools.getInstance().setPlugin(this);
        CoreTools.getInstance().checkForUpdates();
    }

    public void powerEditCallback(UUID playerUUID) {
        List<Power> playerPowers = new ArrayList<>();
        for (String power : getConfig().getStringList("PowerLinks." + playerUUID.toString())) {
            try {
                playerPowers.add(Power.valueOf(power));
            } catch (IllegalArgumentException e) {
                getLogger().warning("Power " + power + " is not a valid power.");
            }
        }
        if (playerPowers.isEmpty()) {
           return;
        }
        Player player = getServer().getPlayer(playerUUID);

        // we need to remove custom items before giving new copies (if player is online ofc)
        if (player != null) {
            for (ItemStack item : player.getInventory()) {
                NBT.get(item, nbt -> {
                    boolean isNightPearl = nbt.getBoolean("LorePowers_NightPearl");
                    if (isNightPearl) {
                        player.getInventory().remove(item);
                    }
                });
            }
        }

        if (checkPower(playerUUID, Power.NIGHT_PEARLS)) {
            ItemStack nightPearl = new ItemCreator(this).createNightPearl();
            if (player != null) {
                player.getInventory().addItem(nightPearl);
                player.sendMessage(CoreTools.getInstance().getPrefix() + ChatColor.RED + "You have been given a Night Pearl!");
            }
        }
        if (checkPower(playerUUID, Power.SPEED_MINE)) {
            if (player != null) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.HASTE, Integer.MAX_VALUE, 1, true, true, true));
                player.sendMessage(CoreTools.getInstance().getPrefix() + ChatColor.RED + "You have been given Speed Mine (Haste 2)!");
            }
        }
    }
}
