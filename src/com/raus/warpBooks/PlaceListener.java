package com.raus.warpBooks;

import org.bukkit.block.Block;
import org.bukkit.block.TileState;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import com.raus.shortUtils.ShortUtils;

public class PlaceListener implements Listener
{
	private final Main plugin = JavaPlugin.getPlugin(Main.class);

	@EventHandler
	public void onPlace(BlockPlaceEvent event)
	{
		// Get data
		ItemStack item = event.getItemInHand();
		ItemMeta meta = item.getItemMeta();
		PersistentDataContainer container = meta.getPersistentDataContainer();

		// Check if warp banner
		if (ShortUtils.hasKey(meta, plugin.warpBannerKey))
		{
			// Give persistent data
			Block block = event.getBlockPlaced();
			TileState banner = (TileState) block.getState();

			PersistentDataContainer c = banner.getPersistentDataContainer();
			c.set(plugin.warpBannerKey, PersistentDataType.BYTE, (byte) 0);
			c.set(plugin.nameKey, PersistentDataType.STRING, container.get(plugin.nameKey, PersistentDataType.STRING));
			c.set(plugin.loreKey, PersistentDataType.STRING, container.get(plugin.loreKey, PersistentDataType.STRING));
			banner.update();
		}
	}
}
