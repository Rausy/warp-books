package com.raus.warpBooks;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.TileState;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import com.raus.shortUtils.ShortUtils;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;

public class UseListener implements Listener
{
	private final Main plugin = JavaPlugin.getPlugin(Main.class);

	private final NamespacedKey xKey = new NamespacedKey(plugin, "x");
	private final NamespacedKey yKey = new NamespacedKey(plugin, "y");
	private final NamespacedKey zKey = new NamespacedKey(plugin, "z");
	private final NamespacedKey yawKey = new NamespacedKey(plugin, "yaw");
	private final NamespacedKey pitchKey = new NamespacedKey(plugin, "pitch");
	private final NamespacedKey worldKey = new NamespacedKey(plugin, "world");

	private String getWorldName(World world)
	{
		switch (world.getEnvironment())
		{
		case NORMAL:
			return "§aOverworld";

		case NETHER:
			return "§cNether";

		case THE_END:
			return "§fThe End";

		default:
			return "§f???";
		}
	}

	private final int maxPageWidth = 113;
	private final String one = "i!|;:,.";
	private final String two = "l'`";
	private final String three = "tI*()[] ";
	private final String four = "fk{}<>\"";
	private final String six = "@~";

	private int getStringWidth(String str)
	{
		int width = 0;

		for (char c : str.toCharArray())
		{
			if (one.contains(""+c))			{ width += 1 + 1; }
			else if (two.contains(""+c))	{ width += 2 + 1; }
			else if (three.contains(""+c))	{ width += 3 + 1; }
			else if (four.contains(""+c))	{ width += 4 + 1; }
			else if (six.contains(""+c))	{ width += 6 + 1; }
			else							{ width += 5 + 1; }
		}

		return width - 1;
	}

	private int getStringWidth(String str, boolean bold)
	{
		double width = 0;

		for (char c : str.toCharArray())
		{
			if (one.contains(""+c))			{ width += 1.5 + 1; }
			else if (two.contains(""+c))	{ width += 2.5 + 1; }
			else if (three.contains(""+c))	{ width += 3.5 + 1; }
			else if (four.contains(""+c))	{ width += 4.5 + 1; }
			else if (six.contains(""+c))	{ width += 6.5 + 1; }
			else							{ width += 5.5 + 1; }
		}

		return (int) (width - 1);
	}

	private String getPadding(String str, boolean bold)
	{
		int width = bold ? getStringWidth(str, bold) : getStringWidth(str);
		int half = (maxPageWidth - width) / 2;
		String space = "";

		while (half > 1)
		{
			if (half >= 3 + 1)
			{
				space += " ";
				half -= 3 + 1;
			}
			else if (half >=2 + 1)
			{
				space += "'";
				half -= 2 + 1;
			}
			else if (half >= 1 + 1)
			{
				space += ".";
				half -= 1 + 1;
			}
		}

		return "§f" + space + "§r";
	}

	private List<String> splitLore(String str)
	{
		List<String> lore = new ArrayList<>();

		int pointer = 1;
		String line = "";
		int width = 0;

		char[] arr = str.toCharArray();
		for (int i = 0; i < arr.length; ++i)
		{
			char c = arr[i];
			line += c;

			if (one.contains(""+c))			{ width += 1 + 1; }
			else if (two.contains(""+c))	{ width += 2 + 1; }
			else if (three.contains(""+c))	{ width += 3 + 1; }
			else if (four.contains(""+c))	{ width += 4 + 1; }
			else if (six.contains(""+c))	{ width += 6 + 1; }
			else							{ width += 5 + 1; }

			// If exceeded page width
			if (width > maxPageWidth)
			{
				// Rewind if cutting off a letter
				if (arr[i] != ' ')
				{
					int trim = 1;
					while (i >= 0 && arr[--i] != ' ')
					{
						++trim;
					}
					line = line.substring(0, line.length() - trim);
				}

				lore.add(line.trim());
				line = "";
				width = 0;

				// Quit at 4 lines
				if (pointer++ == 4)
				{
					break;
				}
			}
		}

		// Add remainder
		lore.add(line.trim());

		// Pad up
		while (lore.size() < 4)
		{
			lore.add("");
		}

		return lore;
	}

	@EventHandler
	public void onPlayerUse(PlayerInteractEvent event)
	{
		// Cancel if interacting with environment
		if (ShortUtils.interacting(event)) { return; }

		// Get item
		ItemStack item = event.getItem();
		if (item == null) { return; }

		// Get stuff
		Player ply = event.getPlayer();
		Action act = event.getAction();

		// Is it the item we want?
		if (act == Action.RIGHT_CLICK_AIR || act == Action.RIGHT_CLICK_BLOCK)
		{
			// Get meta
			ItemMeta meta = item.getItemMeta();
			PersistentDataContainer container = meta.getPersistentDataContainer();

			if (ShortUtils.hasKey(meta, plugin.warpPageKey) && ply.getCooldown(Material.PAPER) <= 0)
			{
				// Check if it's bound
				boolean bound = container.get(plugin.boundKey, PersistentDataType.BYTE) == 1;

				if (bound && !ply.isSneaking())
				{
					// Teleport
					double x = container.get(xKey, PersistentDataType.DOUBLE);
					double y = container.get(yKey, PersistentDataType.DOUBLE);
					double z = container.get(zKey, PersistentDataType.DOUBLE);
					float yaw = container.get(yawKey, PersistentDataType.FLOAT);
					float pitch = container.get(pitchKey, PersistentDataType.FLOAT);
					String world = container.get(worldKey, PersistentDataType.STRING);
					Location loc = new Location(Bukkit.getWorld(world), x, y, z, yaw, pitch);

					item.setAmount(item.getAmount() - 1);
					ply.teleport(loc);
					ply.playSound(loc, Sound.ENTITY_ENDERMAN_TELEPORT, 1, 1);

					String str = loc.getBlockX() + " " + loc.getBlockY() + " " + loc.getBlockZ() + " " + getWorldName(loc.getWorld());
					ply.sendMessage("§6Warping to §c" + str);
				}
				else if (bound && ply.isSneaking())
				{
					// Unbind
					List<String> list = new ArrayList<>();
					list.add("§7Unbound");
					meta.setLore(list);
					meta.removeEnchant(Enchantment.LURE);

					container.set(plugin.boundKey, PersistentDataType.BYTE, (byte) 0);
					ply.playSound(ply.getLocation(), Sound.ITEM_BOOK_PAGE_TURN, 1, 1);

					ply.sendMessage("§6Unbound");
				}
				else if (!bound)
				{
					// Bind
					Location loc = ply.getLocation();
					String str = loc.getBlockX() + " " + loc.getBlockY() + " " + loc.getBlockZ();
					ply.playSound(loc, Sound.ITEM_BOOK_PAGE_TURN, 1, 1);

					List<String> list = new ArrayList<>();
					list.add("§7Bound");
					list.add("§2" + str);
					list.add("§2" + getWorldName(loc.getWorld()));
					meta.setLore(list);
					meta.addEnchant(Enchantment.LURE, 1, false);

					container.set(plugin.boundKey, PersistentDataType.BYTE, (byte) 1);
					container.set(xKey, PersistentDataType.DOUBLE, loc.getX());
					container.set(yKey, PersistentDataType.DOUBLE, loc.getY());
					container.set(zKey, PersistentDataType.DOUBLE, loc.getZ());
					container.set(yawKey, PersistentDataType.FLOAT, loc.getYaw());
					container.set(pitchKey, PersistentDataType.FLOAT, loc.getPitch());
					container.set(worldKey, PersistentDataType.STRING, loc.getWorld().getName());

					ply.sendMessage("§6Bound to §c" + str + " " + getWorldName(loc.getWorld()));
				}

				// Update item
				item.setItemMeta(meta);
				ply.setCooldown(Material.PAPER, 20);
			}
			else if (ShortUtils.hasKey(meta, plugin.warpBookKey) && act == Action.RIGHT_CLICK_BLOCK)
			{
				// Check if banner
				Block block = event.getClickedBlock();
				BlockState state = block.getState();
				if (!(state instanceof TileState)) { return; }

				// Check if warp banner
				TileState banner = (TileState) state;
				if (!banner.getPersistentDataContainer().has(plugin.warpBannerKey, PersistentDataType.BYTE)) { return; }

				// Get warp banner info
				String name = banner.getPersistentDataContainer().get(plugin.nameKey, PersistentDataType.STRING);
				List<String> lore = splitLore(banner.getPersistentDataContainer().get(plugin.loreKey, PersistentDataType.STRING));
				int x = block.getX();
				int y = block.getY();
				int z = block.getZ();
				String coords = x + " " + y + " " + z;
				String world = getWorldName(block.getWorld());

				// Check for duplicate
				BookMeta book = (BookMeta) meta;
				for (String page : book.getPages())
				{
					if (page.contains("§2" + coords))
					{
						ply.sendMessage("§6Your warp book is already attuned to §c" + name);
						event.setCancelled(true);
						return;
					}
				}

				// Add page
				TextComponent warp = new TextComponent("§5§l> Warp <");
				warp.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/warpto " +
						block.getWorld().getName() + " " + x + " " + y + " " + z));

				BaseComponent[] page = {
						new TextComponent("===================\n\n"),
						new TextComponent(getPadding(name, true) + "§l" + name + "\n"),
						new TextComponent("§8§o" + lore.get(0) + "\n"),
						new TextComponent("§8§o" + lore.get(1) + "\n"),
						new TextComponent("§8§o" + lore.get(2) + "\n"),
						new TextComponent("§8§o" + lore.get(3) + "\n\n"),
						new TextComponent(getPadding(coords, false) + "§2" + coords + "\n"),
						new TextComponent(getPadding(world.substring(2), false) + world + "\n\n"),
						new TextComponent(getPadding("> Warp <", true)), warp, new TextComponent("§r\n\n"),
						new TextComponent("===================\n")
				};

				// BLOCK_END_PORTAL_FRAME_FILL
				// BLOCK_BEACON_ACTIVATE

				book.spigot().addPage(page);
				ply.playSound(ply.getLocation(), Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1, 1);
				ply.sendMessage("§6You have attuned your warp book to §c" + name);

				// Update item and prevent opening book
				item.setItemMeta(book);
				event.setCancelled(true);
			}
		}
	}
}