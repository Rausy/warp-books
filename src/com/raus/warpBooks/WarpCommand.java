package com.raus.warpBooks;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Lectern;
import org.bukkit.block.TileState;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import com.raus.shortUtils.ShortUtils;

public class WarpCommand implements CommandExecutor
{
	private final Main plugin = JavaPlugin.getPlugin(Main.class);
	private final Random rand = new Random();

	private boolean isWarpBook(ItemStack item)
	{
		// Check if the book is a warp book
		return ShortUtils.hasKey(item.getItemMeta(), plugin.warpBookKey);
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
	{
		// Sanity check
		if (!(sender instanceof Player) || args.length != 4) { return false; }

		// Check main hand
		Player ply = (Player) sender;
		ItemStack item = ply.getInventory().getItemInMainHand();

		if (!isWarpBook(item))
		{
			// Check if looking at lectern
			Block block = ply.getTargetBlockExact(5);
			if (block == null || block.getType() != Material.LECTERN) { return false; }

			// Check book
			Inventory inv = ((Lectern) block.getState()).getInventory();
			item = inv.getItem(0);
			if (item == null || !isWarpBook(item)) { return false; }
		}

		// Get data and block
		World world = Bukkit.getWorld(args[0]);
		Location loc = new Location(world, Integer.parseInt(args[1]), Integer.parseInt(args[2]), Integer.parseInt(args[3]));
		Block block = world.getBlockAt(loc);

		// Check if banner
		BlockState state = block.getState();
		if (!(state instanceof TileState))
		{
			ply.sendMessage("§cThe warp banner is missing");
			return false;
		}

		// Check if warp banner
		TileState banner = (TileState) state;
		if (!banner.getPersistentDataContainer().has(plugin.warpBannerKey, PersistentDataType.BYTE))
		{
			ply.sendMessage("§cThe warp banner is missing");
			return false;
		}

		// Find valid teleport locations
		List<Location> locs = new ArrayList<>();

		for (int x = -3; x <= 3; ++x)
		{
			for (int y = -1; y <= 1; ++y)
			{
				for (int z = -3; z <= 3; ++z)
				{
					// Skip corners
					if (Math.abs(x) == 3 && Math.abs(z) == 3) { continue; }

					// Check if area is safe
					Block root = block.getRelative(x, y, z);
					Block head = root.getRelative(0, 1, 0);
					Block plat = root.getRelative(0, -1, 0);

					if (!root.getType().isSolid() && root.getType() != Material.LAVA && root.getType() != Material.FIRE &&
							!head.getType().isSolid() && head.getType() != Material.LAVA && head.getType() != Material.FIRE &&
							plat.getType().isSolid() && plat.getType() != Material.MAGMA_BLOCK)
					{
						locs.add(root.getLocation());
					}
				}
			}
		}

		// Teleport (finally)
		loc = locs.get(rand.nextInt(locs.size()));
		loc.add(0.5, 0, 0.5);
		loc.setDirection(ply.getEyeLocation().getDirection());
		ply.teleport(loc);
		ply.playSound(loc, Sound.ENTITY_ENDERMAN_TELEPORT, 1, 1);

		ply.sendMessage("§6Warping to §cHome");

		return true;
	}
}