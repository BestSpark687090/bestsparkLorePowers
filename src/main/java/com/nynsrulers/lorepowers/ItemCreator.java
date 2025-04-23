package com.nynsrulers.lorepowers;

import de.tr7zw.nbtapi.NBT;
import de.tr7zw.nbtapi.NBTItem;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class ItemCreator {
    private final LorePowers plugin;
    public ItemCreator(LorePowers plugin) {
        this.plugin = plugin;
    }
    public ItemStack createNightPearl() {
        ItemStack pearl = new ItemStack(Material.ENDER_PEARL);
        NBT.modify(pearl, nbt -> {
            nbt.setBoolean("LorePowers_NightPearl", true);
        });
        ItemMeta pearlMeta = pearl.getItemMeta();
        assert pearlMeta != null;
        pearlMeta.setDisplayName(ChatColor.BLUE + "Night Pearl");
        pearlMeta.setLore(nightPearlLore());
        pearlMeta.addEnchant(Enchantment.UNBREAKING, 1, true);
        pearlMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        pearl.setItemMeta(pearlMeta);
        return pearl;
    }
    private List<String> nightPearlLore() {
        List<String> lore = new ArrayList<>();
        lore.add("");
        lore.add(ChatColor.DARK_AQUA + "An infinite ender pearl with no cooldown.");
        lore.add(ChatColor.GRAY + "Only usable in the overworld, at night.");
        return lore;
    }
}
