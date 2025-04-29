package com.nynsrulers.lorepowers;

import java.util.*;

import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.Event.Result;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.*;
import org.bukkit.event.vehicle.VehicleEnterEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

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
    public void onJoin_BeeFlight(PlayerJoinEvent e) {
        if (checkPower(e.getPlayer().getUniqueId(), Power.BEE_FLIGHT)) {
            e.getPlayer().setAllowFlight(true);
            e.getPlayer().setFlying(true);
            e.getPlayer().getAttribute(Attribute.SCALE).setBaseValue(0.5);
            e.getPlayer().getAttribute(Attribute.MAX_HEALTH).setBaseValue(16);
        }
    }
    @EventHandler
    public void onAttack_BeeFlight(EntityDamageByEntityEvent e) {
        if (e.isCancelled()) return;
        if (e.getDamager() instanceof Player player && checkPower(e.getDamager().getUniqueId(), Power.BEE_FLIGHT)) {
            if (player.getInventory().getItemInMainHand().getType().toString().endsWith("_SWORD") && Math.random() < 0.1) {
                ((LivingEntity) e.getEntity()).addPotionEffect(new PotionEffect(PotionEffectType.POISON, 200, 1, true, true, true));
                ((Player) e.getDamager()).damage(2);
                e.getEntity().sendMessage(CoreTools.getInstance().getPrefix() + ChatColor.RED + "You have been stung by " + e.getDamager().getName() + ChatColor.RED + "!");
                e.getDamager().sendMessage(CoreTools.getInstance().getPrefix() + ChatColor.GREEN + "You stung " + e.getEntity().getName() + ChatColor.GREEN + "!");
            }

        }
    }
    @EventHandler
    public void onDamage_BeeFlight(EntityDamageEvent e) {
        if (e.isCancelled()) return;
        if (e.getEntity() instanceof Player && checkPower(e.getEntity().getUniqueId(), Power.BEE_FLIGHT)) {
            ((Player) e.getEntity()).setFlying(false);
        }
    }

    @EventHandler
    public void onAttack_GlitchedPresence(EntityDamageByEntityEvent e) {
        if (e.isCancelled()) return;
        if (e.getEntity() instanceof Player && checkPower(e.getEntity().getUniqueId(), Power.GLITCHED_PRESENCE)) {
            Entity lastCause = e.getDamager();
            if (lastCause instanceof Player) {
                if (!((Player) lastCause).getInventory().getItemInMainHand().getType().toString().endsWith("_SWORD")) {
                    ((Player) lastCause).damage(e.getDamage(), e.getEntity());
                    e.setDamage(0);
                    lastCause.sendMessage(CoreTools.getInstance().getPrefix() + ChatColor.RED + "You were glitched by " + e.getEntity().getName() + "'s presence!");
                    e.getEntity().sendMessage(CoreTools.getInstance().getPrefix() + ChatColor.GREEN + "You glitched " + lastCause.getName() + "'s presence!");
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
        Player lastCause = e.getPlayer();
        if (checkPower(lastCause.getUniqueId(), Power.MAP_WARP)) {
            Material tool = lastCause.getInventory().getItemInMainHand().getType();
            Material tool2 = lastCause.getInventory().getItemInOffHand().getType();
           if (tool.toString().matches("^(NETHERITE|DIAMOND|GOLDEN|IRON)_(SWORD|PICKAXE|AXE|SHOVEL|HOE)$") ||
                   tool2.toString().matches("^(NETHERITE|DIAMOND|GOLDEN|IRON)_(SWORD|PICKAXE|AXE|SHOVEL|HOE)$")) {
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
                    if (armor == null) continue;
                    if (armor.getType().toString().startsWith("DIAMOND_") ||
                            armor.getType().toString().startsWith("NETHERITE_") ||
                            armor.getType().toString().startsWith("GOLDEN_") ||
                            armor.getType().toString().startsWith("IRON_") ||
                            armor.getType().toString().startsWith("CHAINMAIL_")) {
                        isWearingTooStrongArmor = true;
                        break;
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
    public void onRespawn_SpeedMine(PlayerRespawnEvent e) {
        if (checkPower(e.getPlayer().getUniqueId(), Power.SPEED_MINE)) {
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
    public void onRespawn_PiglinAvianTraits(PlayerRespawnEvent e) {
        if (checkPower(e.getPlayer().getUniqueId(), Power.PIGLIN_AVIAN_TRAITS)) {
            getServer().getScheduler().scheduleSyncDelayedTask(this, () -> {
                e.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 1, true, true, true));
                e.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.JUMP_BOOST, Integer.MAX_VALUE, 1, true, true, true));
                e.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, Integer.MAX_VALUE, 0, true, true, true));
                e.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, Integer.MAX_VALUE, 0, true, true, true));
            }, 20L);
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
    public void onDamageByEnemy_PiglinAid(EntityDamageByEntityEvent e) {
        if (e.isCancelled()) return;
        Entity causingEntity = e.getDamageSource().getCausingEntity();
        if (!(causingEntity instanceof LivingEntity)) return;
        if (!(e.getEntity() instanceof Player)) return;
        if (checkPower(e.getEntity().getUniqueId(), Power.PIGLIN_AID)) {
            for (Entity entity : e.getEntity().getNearbyEntities(20, 20, 20)) {
                if (entity instanceof PiglinAbstract) {
                    try {
                        ((PiglinAbstract) entity).setTarget((LivingEntity) causingEntity);
                        ((PiglinAbstract) entity).attack(causingEntity);
                    } catch (ClassCastException ignored) {}
                }
            }
        }
    }
    @EventHandler
    public void onDamageByWielder_PiglinAid(EntityDamageByEntityEvent e) {
        if (e.isCancelled()) return;
        Entity causingEntity = e.getDamageSource().getCausingEntity();
        if (!(e.getEntity() instanceof LivingEntity)) return;
        if (causingEntity == null) return;
        if (!(causingEntity instanceof Player)) return;
        if (checkPower(causingEntity.getUniqueId(), Power.PIGLIN_AID)) {
            for (Entity entity : e.getEntity().getNearbyEntities(20, 20, 20)) {
                if (entity instanceof PiglinAbstract) {
                    try {
                        ((PiglinAbstract) entity).setTarget((LivingEntity) e.getEntity());
                        ((PiglinAbstract) entity).attack(e.getEntity());
                    } catch (ClassCastException ignored) {}
                }
            }
        }
    }
    @EventHandler
    public void onPiglinDeath_PiglinAid(EntityDeathEvent e) {
        if (e.getEntity() instanceof PiglinAbstract) {
            for (Entity entity : e.getEntity().getNearbyEntities(20, 20, 20)) {
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
    public void onTarget_PiglinAid(EntityTargetLivingEntityEvent e) {
        if (e.isCancelled()) return;
        if (e.getEntity() instanceof PiglinAbstract) {
            if (e.getTarget() == null) return;
            if (checkPower(e.getTarget().getUniqueId(), Power.PIGLIN_AID)) {
                e.setCancelled(true);
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

    @EventHandler
    public void onDamage_FireBreath(EntityDamageByEntityEvent e) {
        if (e.isCancelled()) return;
        if (!checkPower(e.getDamager().getUniqueId(), Power.FIRE_BREATH)) return;
        if (!(e.getDamager() instanceof Player player)) return;
        if (player.isSneaking()) {
            player.setFireTicks(100);
            player.damage(2);
            Location playerLocation = player.getLocation();
            Vector direction = playerLocation.getDirection().normalize();
            for (int i = 1; i <= 5; i++) {
                Location checkLocation = playerLocation.clone().add(direction.clone().multiply(i));
                player.getWorld().spawnParticle(Particle.FLAME, checkLocation, 10, 0.2, 0.2, 0.2, 0.01);
                for (Entity entity : player.getWorld().getNearbyEntities(checkLocation, 1, 1, 1)) {
                    if (entity instanceof LivingEntity && !entity.equals(player)) {
                        entity.setFireTicks(100);
                        ((LivingEntity) entity).damage(3);
                    }
                }
            }
        }
    }

    @EventHandler
    public void onJoin_DragonForm(PlayerJoinEvent e) {
        if (checkPower(e.getPlayer().getUniqueId(), Power.DRAGON_FORM)) {
            if (e.getPlayer().getName().equals(".XxdeathflamexX1")) {
                e.getPlayer().getAttribute(Attribute.SCALE).setBaseValue(0.5);
            } else {
                e.getPlayer().getAttribute(Attribute.SCALE).setBaseValue(1.5);
            }
            e.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.JUMP_BOOST, Integer.MAX_VALUE, 0, true, true, true));
            e.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, Integer.MAX_VALUE, 0, true, true, true));
        }
    }
    @EventHandler
    public void onConsume_DragonForm(PlayerItemConsumeEvent e) {
        if (e.getItem().getType() == Material.MILK_BUCKET && checkPower(e.getPlayer().getUniqueId(), Power.DRAGON_FORM)) {
            getServer().getScheduler().scheduleSyncDelayedTask(this, () -> {
                e.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.JUMP_BOOST, Integer.MAX_VALUE, 0, true, true, true));
                e.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, Integer.MAX_VALUE, 0, true, true, true));
            }, 20L);
        }
    }
    @EventHandler
    public void onRespawn_DragonForm(PlayerRespawnEvent e) {
        if (checkPower(e.getPlayer().getUniqueId(), Power.DRAGON_FORM)) {
            getServer().getScheduler().scheduleSyncDelayedTask(this, () -> {
                e.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.JUMP_BOOST, Integer.MAX_VALUE, 0, true, true, true));
                e.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, Integer.MAX_VALUE, 0, true, true, true));
            }, 20L);
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
        if (checkPower(playerUUID, Power.DRAGON_FORM)) {
            if (player != null) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP_BOOST, Integer.MAX_VALUE, 0, true, true, true));
                player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, Integer.MAX_VALUE, 0, true, true, true));
                player.sendMessage(CoreTools.getInstance().getPrefix() + ChatColor.RED + "You have been given your Dragon Form buffs!");
            }
        } else {
            if (player != null) {
                boolean hasAllEffects = player.hasPotionEffect(PotionEffectType.JUMP_BOOST) && Objects.requireNonNull(player.getPotionEffect(PotionEffectType.JUMP_BOOST)).getAmplifier() == 0 &&
                        player.hasPotionEffect(PotionEffectType.SLOW_FALLING) && Objects.requireNonNull(player.getPotionEffect(PotionEffectType.SLOW_FALLING)).getAmplifier() == 0;

                if (hasAllEffects) {
                    player.removePotionEffect(PotionEffectType.JUMP_BOOST);
                    player.removePotionEffect(PotionEffectType.SLOW_FALLING);
                    player.sendMessage(CoreTools.getInstance().getPrefix() + ChatColor.RED + "You have lost your Dragon Form buffs!");
                }
            }
        }
        if (player != null) {
            if (checkPower(playerUUID, Power.ANKLE_BITER)) {
                player.getAttribute(Attribute.SCALE).setBaseValue(0.75);
            } else if (checkPower(playerUUID, Power.DRAGON_FORM)) {
                if (player.getName().equals(".XxdeathflamexX1")) {
                    player.getAttribute(Attribute.SCALE).setBaseValue(0.5);
                } else {
                    player.getAttribute(Attribute.SCALE).setBaseValue(1.5);
                }
            } else if (checkPower(playerUUID, Power.BEE_FLIGHT)) {
                player.getAttribute(Attribute.SCALE).setBaseValue(0.3);
            } else {
                player.getAttribute(Attribute.SCALE).setBaseValue(1.0);
            }
        }
        if (checkPower(playerUUID, Power.BEE_FLIGHT)) {
            if (player != null) {
                player.setAllowFlight(true);
                player.setFlying(true);
                player.getAttribute(Attribute.MAX_HEALTH).setBaseValue(16);
            }
        } else {
            if (player != null) {
                player.setAllowFlight(false);
                player.setFlying(false);
                player.getAttribute(Attribute.MAX_HEALTH).setBaseValue(20);
            }
        }
    }
}
