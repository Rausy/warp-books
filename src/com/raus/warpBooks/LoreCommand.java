package com.raus.warpBooks;

import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import com.raus.shortUtils.ShortUtils;

public class LoreCommand implements CommandExecutor
{
	private final Main plugin = JavaPlugin.getPlugin(Main.class);

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
	{
		// Sanity check
		if (!(sender instanceof Player) || args.length == 0) { return false; }

		// Check if holding banner
		Player ply = (Player) sender;
		ItemStack item = ply.getInventory().getItemInMainHand();
		ItemMeta meta = item.getItemMeta();

		// Check if warp banner
		if (ShortUtils.hasKey(meta, plugin.warpBannerKey))
		{
			// Modify
			String lore = String.join(" ", args);
			meta.getPersistentDataContainer().set(plugin.loreKey, PersistentDataType.STRING, lore);

			List<String> list = meta.getLore();
			list.set(1, "§7" + lore);
			meta.setLore(list);

			item.setItemMeta(meta);
			ply.sendMessage("§6Warp description changed");
		}
		return true;
	}
}