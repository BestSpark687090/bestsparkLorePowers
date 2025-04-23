package com.nynsrulers.lorepowers;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.libraryaddict.disguise.DisguiseAPI;
import me.libraryaddict.disguise.LibsDisguises;
import me.libraryaddict.disguise.disguisetypes.Disguise;
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
    if (plugin.checkPower(((Player) sender).getUniqueId(), Power.DRAGON_FORM)) {
      sender.sendMessage(CoreTools.getInstance().getPrefix() + ChatColor.RED + "You do not have this power!.");
      return false;
    }
    DisguiseAPI.disguiseEntity(((Player) sender), getDragonDisguise());
    return true;
  }
  private Disguise getDragonDisguise() {
    return null; // in dev
  }
}