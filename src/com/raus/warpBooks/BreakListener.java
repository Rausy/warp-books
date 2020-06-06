package com.raus.warpBooks;

import java.util.Arrays;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.TileState;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import com.raus.shortUtils.ShortUtils;

public class BreakListener implements Listener
{
	private final Main plugin = JavaPlugin.getPlugin(Main.class);

	@EventHandler
	public void onBreak(BlockBreakEvent event)
	{
		// Check if banner
		Block block = event.getBlock();
		BlockState state = block.getState();
		if (!(state instanceof TileState)) { return; }

		// Check if warp banner
		TileState banner = (TileState) state;
		PersistentDataContainer c = banner.getPersistentDataContainer();
		if (!c.has(plugin.warpBannerKey, PersistentDataType.BYTE)) { return; }

		String name = c.get(plugin.nameKey, PersistentDataType.STRING);
		String lore = c.get(plugin.loreKey, PersistentDataType.STRING);

		// Seems unnecessary
		for (ItemStack item : block.getDrops())
		{
			// Give item some metadata
			ItemMeta meta = item.getItemMeta();
			ShortUtils.addKey(meta, plugin.warpBannerKey);
			c = meta.getPersistentDataContainer();
			c.set(plugin.nameKey, PersistentDataType.STRING, name);
			c.set(plugin.loreKey, PersistentDataType.STRING, lore);

			// Give default description if one is lacking
			if (lore == "") { lore = "No description"; }
			meta.setLore(Arrays.asList("§f" + name, "§7" + lore));
			item.setItemMeta(meta);

			// Drop warp banner
			block.getWorld().dropItemNaturally(block.getLocation(), item);
			block.setType(Material.AIR);
		}
	}
}
