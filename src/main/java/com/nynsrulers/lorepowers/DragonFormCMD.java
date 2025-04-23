package com.nynsrulers.lorepowers;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.libraryaddict.disguise.DisguiseAPI;
import me.libraryaddict.disguise.disguisetypes.DisguiseType;
import me.libraryaddict.disguise.disguisetypes.MobDisguise;
import net.md_5.bungee.api.ChatColor;

public class DragonFormCMD implements CommandExecutor {
  private final LorePowers plugin;
  public DragonFormCMD(LorePowers plugin) {
    this.plugin = plugin;
  }
  @Override
  public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
    if (!(sender instanceof Player)) {
      sender.sendMessage(CoreTools.getInstance().getPrefix() + ChatColor.RED + "This command can only be used by players.");
      return false;
    }
    Player player = (Player) sender;
    if (plugin.checkPower(player.getUniqueId(), Power.DRAGON_FORM)) {
      sender.sendMessage(CoreTools.getInstance().getPrefix() + ChatColor.RED + "You do not have this power!.");
      if (plugin.dragonFormActive.contains(player.getUniqueId())) {
        plugin.dragonFormActive.remove(player.getUniqueId());
        DisguiseAPI.undisguiseToAll(player);
      }
      return false;
    }
    if (plugin.dragonFormActive.contains(player.getUniqueId())) {
      plugin.dragonFormActive.remove(player.getUniqueId());
      DisguiseAPI.undisguiseToAll(player);
      sender.sendMessage(CoreTools.getInstance().getPrefix() + ChatColor.GREEN + "You have returned to a human form!");
      return true;
    }
    DisguiseAPI.disguiseEntity(player, new MobDisguise(DisguiseType.ENDER_DRAGON));
    plugin.dragonFormActive.add(player.getUniqueId());
    sender.sendMessage(CoreTools.getInstance().getPrefix() + ChatColor.GREEN + "You have transformed into a Dragon form!");
    return true;
  }
}