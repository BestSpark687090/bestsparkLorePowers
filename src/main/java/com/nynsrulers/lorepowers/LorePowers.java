package com.nynsrulers.lorepowers;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.Event.Result;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.vehicle.VehicleEnterEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public final class LorePowers extends JavaPlugin implements Listener {
    public List<UUID> dragonFormActive = new ArrayList<>();

    @Override
    public void onEnable() {
        getConfig().options().copyDefaults();
        saveDefaultConfig();
        getServer().getPluginManager().registerEvents(this, this);
        CoreTools.getInstance().setPlugin(this);
        getCommand("lorepowers").setExecutor(new ManageCMD(this));
        getCommand("lorepowers").setTabCompleter(new ManageTabCompleter());
        getCommand("dragonform").setExecutor(new DragonFormCMD(this));
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
    public void onToolUse_MapWarp(PlayerInteractEvent e) {
        if (e.useInteractedBlock() == Result.DENY || e.useItemInHand() == Result.DENY) return;
        Entity lastCause = e.getPlayer();
        if (lastCause instanceof Player && checkPower(lastCause.getUniqueId(), Power.MAP_WARP)) {
            Material tool = ((Player) lastCause).getInventory().getItemInMainHand().getType();
            Material tool2 = ((Player) lastCause).getInventory().getItemInOffHand().getType();
            if (tool.toString().startsWith("NETHERITE_") ||
                    tool.toString().startsWith("DIAMOND_") ||
                    tool.toString().startsWith("GOLDEN_") ||
                    tool.toString().startsWith("IRON_") ||
                    tool2.toString().startsWith("NETHERITE_") ||
                    tool2.toString().startsWith("DIAMOND_") ||
                    tool2.toString().startsWith("GOLDEN_") ||
                    tool2.toString().startsWith("IRON_")
            ) {
                e.setCancelled(true);
                lastCause.sendMessage(CoreTools.getInstance().getPrefix() + ChatColor.RED + "You cannot use this tool, as you are too weak!");
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
    public void onDrop_MapWarp(PlayerDropItemEvent e) {
        if (e.isCancelled()) return;
        if (e.getItemDrop().getItemStack().getType() != Material.FILLED_MAP) return;
        MapMeta mapMeta = (MapMeta) e.getItemDrop().getItemStack().getItemMeta();
        if (mapMeta == null) return;
        if (checkPower(e.getPlayer().getUniqueId(), Power.MAP_WARP)) {
            if (mapMeta.getMapView().getWorld().getEnvironment() == World.Environment.NETHER) {
                e.getPlayer().sendMessage(CoreTools.getInstance().getPrefix() + ChatColor.RED + "You cannot use this in the Nether!");
                return;
            }
            Location tpLocation = new Location(mapMeta.getMapView().getWorld(), mapMeta.getMapView().getCenterX(), 0, mapMeta.getMapView().getCenterZ());
            tpLocation.setY(tpLocation.getWorld().getHighestBlockYAt(tpLocation));
            e.getPlayer().teleport(tpLocation);
            e.getPlayer().sendMessage(CoreTools.getInstance().getPrefix() + ChatColor.RED + "You have been warped to the map's center!");
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onPearlThrow_NightPearls(ProjectileLaunchEvent e) {
        if (e.isCancelled()) return;
        if (!(e.getEntity().getShooter() instanceof Player player)) return;
        if (!checkPower(player.getUniqueId(), Power.NIGHT_PEARLS)) return;
        if (e.getEntityType() == EntityType.ENDER_PEARL && player.getInventory().getItemInMainHand().getType() == Material.ENDER_PEARL) {
            if (player.getWorld().getEnvironment() != World.Environment.NORMAL) return;
            if (player.getWorld().getGameTime() % 24000 >= 13000) return;
            Block locToTP = player.getTargetBlockExact(40, FluidCollisionMode.NEVER);
            if (locToTP != null) {
                e.setCancelled(true);
                player.teleport(locToTP.getLocation());
            } else {
                e.setCancelled(false);
                e.getEntity().setVelocity(player.getLocation().getDirection().multiply(3));
            }
        }
    }

    @EventHandler
    public void onJoin_SpeedMine(PlayerJoinEvent e) {
        if (checkPower(e.getPlayer().getUniqueId(), Power.SPEED_MINE)) {
            e.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.HASTE, Integer.MAX_VALUE, 2, true, true, true));
        }
    }

    @EventHandler
    public void onConsume_SpeedMine(PlayerItemConsumeEvent e) {
        if (e.getItem().getType() == Material.MILK_BUCKET && checkPower(e.getPlayer().getUniqueId(), Power.SPEED_MINE)) {
            getServer().getScheduler().scheduleSyncDelayedTask(this, () -> e.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.HASTE, Integer.MAX_VALUE, 2, true, true, true)), 20L);
        }
    }

    @EventHandler
    public void onBoat_SpeedMine(VehicleEnterEvent e) {
        if (e.getVehicle() instanceof Boat && e.getEntered() instanceof Player) {
            if (checkPower(((Player) e.getEntered()).getUniqueId(), Power.SPEED_MINE)) {
                e.setCancelled(true);
                e.getEntered().sendMessage(CoreTools.getInstance().getPrefix() + ChatColor.RED + "You cannot ride in boats, as your arms too strong!");
                e.getVehicle().remove();
            }
        }
    }

    @EventHandler
    public void onJoin_PiglinAvianTraits(PlayerJoinEvent e) {
        Player player = e.getPlayer();
        if (checkPower(player.getUniqueId(), Power.PIGLIN_AVIAN_TRAITS)) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 1, true, true, true));
            player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP_BOOST, Integer.MAX_VALUE, 1, true, true, true));
            player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, Integer.MAX_VALUE, 0, true, true, true));
            player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, Integer.MAX_VALUE, 0, true, true, true));
        }
    }

    @EventHandler
    public void onConsume_PiglinAvianTraits(PlayerItemConsumeEvent e) {
        if (e.getItem().getType() == Material.MILK_BUCKET && checkPower(e.getPlayer().getUniqueId(), Power.PIGLIN_AVIAN_TRAITS)) {
            getServer().getScheduler().scheduleSyncDelayedTask(this, () -> {
                e.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 1, true, true, true));
                e.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.JUMP_BOOST, Integer.MAX_VALUE, 1, true, true, true));
                e.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, Integer.MAX_VALUE, 0, true, true, true));
                e.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, Integer.MAX_VALUE, 0, true, true, true));
            }, 20L);
        }
    }

    @EventHandler
    public void onHit_PiglinAvianTraits(EntityDamageByEntityEvent e) {
        if (e.isCancelled()) return;
        if (e.getEntity() instanceof Player && checkPower(e.getEntity().getUniqueId(), Power.PIGLIN_AVIAN_TRAITS)) {
            Entity lastCause = e.getDamager();
            if (lastCause instanceof Player) {
                if (((Player) lastCause).getInventory().getItemInMainHand().getType().toString().endsWith("_AXE")) {
                    e.setDamage(e.getDamage() * 1.5);
                }
            }
        }
    }

    @EventHandler
    public void onDamage_HeatResistance(EntityDamageEvent e) {
        if (e.isCancelled()) return;
        if (e.getEntity() instanceof Player && checkPower(e.getEntity().getUniqueId(), Power.HEAT_RESISTANCE)) {
            if (e.getCause() == EntityDamageEvent.DamageCause.FIRE ||
                    e.getCause() == EntityDamageEvent.DamageCause.FIRE_TICK ||
                    e.getCause() == EntityDamageEvent.DamageCause.HOT_FLOOR) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onDamage_PiglinAid(EntityDamageEvent e) {
        if (e.isCancelled()) return;
        if (e.getEntity() instanceof Player && checkPower(e.getEntity().getUniqueId(), Power.PIGLIN_AID)) {
            for (Entity entity : e.getEntity().getNearbyEntities(10, 10, 10)) {
                if (entity instanceof PiglinAbstract) {
                    try {
                        ((PiglinAbstract) entity).setTarget((LivingEntity) e.getDamageSource().getCausingEntity());
                    } catch (ClassCastException ignored) {
                    }
                }
            }
        }
    }

    @EventHandler
    public void onPiglinDeath_PiglinAid(EntityDeathEvent e) {
        if (e.getEntity() instanceof PiglinAbstract) {
            for (Entity entity : e.getEntity().getNearbyEntities(10, 10, 10)) {
                if (entity instanceof Player) {
                    if (checkPower(entity.getUniqueId(), Power.PIGLIN_AID)) {
                        entity.sendMessage(CoreTools.getInstance().getPrefix() + ChatColor.RED + "A piglin has died near you!");
                        ((Player) entity).addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 400, 1, true, true, true));
                        ((Player) entity).damage(5);
                    }
                }
            }
        }
    }

    @EventHandler
    public void onAttack_AnkleBiter(EntityDamageByEntityEvent e) {
        if (e.isCancelled()) return;
        if (e.getDamager() instanceof Player player && checkPower(e.getDamager().getUniqueId(), Power.ANKLE_BITER)) {
            if (player.getInventory().getItemInMainHand().getType() == Material.AIR && Math.random() < 0.1) {
                ((LivingEntity) e.getEntity()).addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 200, 9, true, true, true));
                e.getEntity().sendMessage(CoreTools.getInstance().getPrefix() + ChatColor.RED + "Your ankles have been bitten by " + e.getDamager().getName() + ChatColor.RED + ", so you are immobilized!");
                e.getDamager().sendMessage(CoreTools.getInstance().getPrefix() + ChatColor.GREEN + "You have bitten the ankles of " + e.getEntity().getName() + ChatColor.GREEN + "!");
            }
        }
    }
    @EventHandler
    public void onJoin_AnkleBiter(PlayerJoinEvent e) {
        if (checkPower(e.getPlayer().getUniqueId(), Power.ANKLE_BITER)) {
            e.getPlayer().getAttribute(Attribute.SCALE).setBaseValue(0.75);
        }
    }

    void reloadPlugin() {
        reloadConfig();
        CoreTools.getInstance().setPlugin(this);
        CoreTools.getInstance().checkForUpdates();
        for (Player player : getServer().getOnlinePlayers()) {
            powerEditCallback(player.getUniqueId());
        }
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

        if (checkPower(playerUUID, Power.SPEED_MINE)) {
            if (player != null) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.HASTE, Integer.MAX_VALUE, 2, true, true, true));
                player.sendMessage(CoreTools.getInstance().getPrefix() + ChatColor.RED + "You have been given Speed Mine (Haste 3)!");
            }
        } else {
            if (player != null && player.hasPotionEffect(PotionEffectType.HASTE) && Objects.requireNonNull(player.getPotionEffect(PotionEffectType.HASTE)).getAmplifier() == 2) {
                player.removePotionEffect(PotionEffectType.HASTE);
                player.sendMessage(CoreTools.getInstance().getPrefix() + ChatColor.RED + "You have lost Speed Mine (Haste 3)!");
            }
        }
        if (checkPower(playerUUID, Power.PIGLIN_AVIAN_TRAITS)) {
            if (player != null) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 1, true, true, true));
                player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP_BOOST, Integer.MAX_VALUE, 1, true, true, true));
                player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, Integer.MAX_VALUE, 0, true, true, true));
                player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, Integer.MAX_VALUE, 0, true, true, true));
                player.sendMessage(CoreTools.getInstance().getPrefix() + ChatColor.RED + "You have been given your Piglin and Avian traits!");
            }
        } else {
            if (player != null) {
                boolean hasAllEffects = player.hasPotionEffect(PotionEffectType.SPEED) && Objects.requireNonNull(player.getPotionEffect(PotionEffectType.SPEED)).getAmplifier() == 1 &&
                        player.hasPotionEffect(PotionEffectType.JUMP_BOOST) && Objects.requireNonNull(player.getPotionEffect(PotionEffectType.JUMP_BOOST)).getAmplifier() == 1 &&
                        player.hasPotionEffect(PotionEffectType.SLOW_FALLING) && Objects.requireNonNull(player.getPotionEffect(PotionEffectType.SLOW_FALLING)).getAmplifier() == 0 &&
                        player.hasPotionEffect(PotionEffectType.STRENGTH) && Objects.requireNonNull(player.getPotionEffect(PotionEffectType.STRENGTH)).getAmplifier() == 0;

                if (hasAllEffects) {
                    player.removePotionEffect(PotionEffectType.SPEED);
                    player.removePotionEffect(PotionEffectType.JUMP_BOOST);
                    player.removePotionEffect(PotionEffectType.SLOW_FALLING);
                    player.removePotionEffect(PotionEffectType.STRENGTH);
                    player.sendMessage(CoreTools.getInstance().getPrefix() + ChatColor.RED + "You have lost your Piglin and Avian traits!");
                }
            }
        }
        if (player != null) {
            if (checkPower(playerUUID, Power.ANKLE_BITER)) {
                player.getAttribute(Attribute.SCALE).setBaseValue(0.75);
            } else {
                player.getAttribute(Attribute.SCALE).setBaseValue(1.0);
            }
        }
    }
}
