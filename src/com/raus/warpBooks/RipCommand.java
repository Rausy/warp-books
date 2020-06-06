package com.raus.warpBooks;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import com.raus.shortUtils.ShortUtils;

public class RipCommand implements CommandExecutor
{
	private final Main plugin = JavaPlugin.getPlugin(Main.class);

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
	{
		// Sanity check
		if (!(sender instanceof Player) || args.length != 1) { return false; }

		// Get stuff
		Player ply = (Player) sender;
		ItemStack item = ply.getInventory().getItemInMainHand();
		ItemMeta meta = item.getItemMeta();

		// Check if warp book
		if (ShortUtils.hasKey(meta, plugin.warpBookKey))
		{
			// Remove page
			int page = Integer.parseInt(args[0]);

			BookMeta book = (BookMeta) meta;
			List<String> pages = book.getPages();
			List<String> newPages = new ArrayList<>();
			for (int i = 0; i < pages.size(); ++i)
			{
				if (i == page - 1) { continue; }
				newPages.add(pages.get(i));
			}
			book.setPages(newPages);

			item.setItemMeta(book);
			ply.sendMessage("§6Ripped out page §c" + page);
		}
		return true;
	}
}