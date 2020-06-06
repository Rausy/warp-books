package com.raus.warpBooks;

import java.util.Arrays;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import com.raus.craftLib.CraftLib;
import com.raus.shortUtils.ShortUtils;

public class Main extends JavaPlugin
{
	private final Plugin craftLib = getServer().getPluginManager().getPlugin("CraftLib");

	// NamespacedKeys
	public final NamespacedKey boundKey = new NamespacedKey(this, "bound");
	public final NamespacedKey nameKey = new NamespacedKey(this, "warp_name");
	public final NamespacedKey loreKey = new NamespacedKey(this, "warp_lore");

	public final NamespacedKey warpPageKey = new NamespacedKey(this, "warp_page");
	public final NamespacedKey warpBookKey = new NamespacedKey(this, "warp_book");
	public final NamespacedKey warpBannerKey = new NamespacedKey(this, "warp_banner");

	private final Material[] banners = {
			Material.WHITE_BANNER, Material.ORANGE_BANNER, Material.MAGENTA_BANNER, Material.LIGHT_BLUE_BANNER,
			Material.YELLOW_BANNER, Material.LIME_BANNER, Material.PINK_BANNER, Material.GRAY_BANNER,
			Material.LIGHT_GRAY_BANNER, Material.CYAN_BANNER, Material.PURPLE_BANNER, Material.BLUE_BANNER,
			Material.BROWN_BANNER, Material.GREEN_BANNER, Material.RED_BANNER, Material.BLACK_BANNER
	};

	@Override
	public void onEnable()
	{
		// Listeners
		getServer().getPluginManager().registerEvents(new UseListener(), this);
		getServer().getPluginManager().registerEvents(new PlaceListener(), this);
		getServer().getPluginManager().registerEvents(new BreakListener(), this);

		// Register command
		getCommand("warpto").setExecutor(new WarpCommand());
		getCommand("rip").setExecutor(new RipCommand());
		getCommand("rename").setExecutor(new RenameCommand());
		getCommand("lore").setExecutor(new LoreCommand());

		// Warp page
		ItemStack item = new ItemStack(Material.PAPER);
		ItemMeta meta = item.getItemMeta();
		meta.setDisplayName("§rWarp Page");
		meta.setLore(Arrays.asList("§7Unbound"));
		meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
		ShortUtils.addKey(meta, warpPageKey);
		meta.getPersistentDataContainer().set(boundKey, PersistentDataType.BYTE, (byte) 0);
		item.setItemMeta(meta);

		ShapelessRecipe recipe = new ShapelessRecipe(warpPageKey, item);
		recipe.addIngredient(Material.PAPER);
		recipe.addIngredient(Material.ENDER_PEARL);
		Bukkit.addRecipe(recipe);

		// Warp book
		item = new ItemStack(Material.WRITTEN_BOOK);
		meta = item.getItemMeta();
		meta.setDisplayName("§eWarp Book");
		ShortUtils.addKey(meta, warpBookKey);

		BookMeta book = (BookMeta) meta;
		book.setGeneration(BookMeta.Generation.TATTERED);
		book.setTitle("");
		book.setAuthor("");
		item.setItemMeta(book);

		recipe = new ShapelessRecipe(warpBookKey, item);
		recipe.addIngredient(Material.BOOK);
		recipe.addIngredient(Material.DIAMOND);
		recipe.addIngredient(Material.GHAST_TEAR);
		recipe.addIngredient(Material.NAUTILUS_SHELL);
		recipe.addIngredient(Material.DRAGON_BREATH);
		Bukkit.addRecipe(recipe);

		// Iterate through all kinds of banners
		for (Material mat : banners)
		{
			// Create warp banner item
			item = new ItemStack(mat);
			meta = item.getItemMeta();
			meta.setDisplayName("§dWarp Banner");
			meta.setLore(Arrays.asList("§fUnnamed", "§7No description"));
			ShortUtils.addKey(meta, warpBannerKey);
			meta.getPersistentDataContainer().set(nameKey, PersistentDataType.STRING, "Unnamed");
			meta.getPersistentDataContainer().set(loreKey, PersistentDataType.STRING, "");
			item.setItemMeta(meta);

			// Create and add recipe
			recipe = new ShapelessRecipe(new NamespacedKey(this, "warp_" + mat.name()), item);
			recipe.addIngredient(Material.DRAGON_EGG);
			recipe.addIngredient(mat);
			Bukkit.addRecipe(recipe);
		}

		// Add keys to CraftLib if installed
		if (craftLib != null)
		{
			CraftLib cl = (CraftLib) craftLib;
			cl.addKey(warpPageKey);
			cl.addKey(warpBookKey);
			cl.addKey(warpBannerKey);
		}
	}

	@Override
	public void onDisable()
	{

	}
}