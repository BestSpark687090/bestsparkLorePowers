package com.nynsrulers.lorepowers;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.md_5.bungee.api.ChatColor;

public class BestSparksIdea implements CommandExecutor {
  private final LorePowers plugin;
  public BestSparksIdea(LorePowers plugin) {
    this.plugin = plugin;
  }
  /// PLEASE NOTE I COPIED OFF OF THE DRAGON FORM COMMAND IF SOME OF THIS DOESNT WORK THEN MY B
  @Override
  public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
    // sender.sendMessage(CoreTools.getInstance().getPrefix() + ChatColor.RED + "This command has been forcibly disabled, as it breaks things and is not ready.");
    // sender.sendMessage(ChatColor.RED + "Please give Aelithron a few days, they ran out of time before the server launch.");
    // return false;
   if (!(sender instanceof Player)) {
     sender.sendMessage(CoreTools.getInstance().getPrefix() + ChatColor.RED + "This command can only be used by players.");
     return false;
   }
   if (!plugin.libsDisguisesInstalled) {
       sender.sendMessage(CoreTools.getInstance().getPrefix() + ChatColor.RED + "This power is not enabled, as Lib's Disguises is not installed!");
       return false;
   }
   Player player = (Player) sender;
   if (!plugin.checkPower(player.getUniqueId(), Power.BESTSPARKS_IDEA)) {
     sender.sendMessage(CoreTools.getInstance().getPrefix() + ChatColor.RED + "You do not have this power!");
     if (plugin.sparksIdeaActive.contains(player.getUniqueId())) {
       plugin.sparksIdeaActive.remove(player.getUniqueId());
       DisguiseAPI.undisguiseToAll(player);
     }
     return false;
   }
   if (plugin.sparksIdeaActive.contains(player.getUniqueId())) {
     plugin.sparksIdeaActive.remove(player.getUniqueId());
     DisguiseAPI.undisguiseToAll(player);
     sender.sendMessage(CoreTools.getInstance().getPrefix() + ChatColor.GREEN + "You have become normal :(");
     return true;
   }
   DisguiseAPI.disguiseEntity(player, new MobDisguise(DisguiseType.CREEPER));
   plugin.sparksIdeaActive.add(player.getUniqueId());
   sender.sendMessage(CoreTools.getInstance().getPrefix() + ChatColor.GREEN + "You have transformed into "+ChatColor.GREEN+"a creeper!");
   return true;
  }
}
