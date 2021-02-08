/**
 * This class was created by <WireSegal>. It's distributed as
 * part of the Quark Mod. Get the Source Code in github:
 * https://github.com/Vazkii/Quark
 * <p>
 * Quark is Open Source and distributed under the
 * CC-BY-NC-SA 3.0 License: https://creativecommons.org/licenses/by-nc-sa/3.0/deed.en_GB
 * <p>
 * File Created @ [May 20, 2019, 10:35 AM (EST)]
 */
package vazkii.quark.base.handler;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Map;

import javax.annotation.Nullable;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.Items;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.Biomes;
import net.minecraftforge.registries.ForgeRegistryEntry;
import net.minecraftforge.registries.GameData;
import net.minecraftforge.registries.IForgeRegistryEntry;
import vazkii.arl.util.RegistryHelper;
import vazkii.quark.base.Quark;

public final class OverrideRegistryHandler {
	
	public static void registerBlock(Block block, String baseName, @Nullable ItemGroup group) {
		register(block, Blocks.class, baseName);
		registerBlockItem(block, group);
	}

	private static void registerBlockItem(Block block, @Nullable ItemGroup group) {
		Item.Properties props = new Item.Properties();
		if(group != null)
			props = props.group(group);
		
		BlockItem item = new BlockItem(block, props);
		registerItem(item, block.getRegistryName().getPath());
	}
	
	public static void registerItem(Item item, String baseName) {
		register(item, Items.class, baseName);
	}

	public static <T extends ForgeRegistryEntry<T>> void register(T obj, Class<?> registryType, String baseName) {
		ResourceLocation regName = new ResourceLocation("minecraft", baseName);
		try {
			Field field = ForgeRegistryEntry.class.getDeclaredField("registryName");
			field.setAccessible(true);
			field.set(obj, regName);
		} catch (IllegalAccessException | NoSuchFieldException e) {
			obj.setRegistryName(regName);
		}

		RegistryHelper.register(obj);

		for (Field declared : registryType.getDeclaredFields()) {
			if (Modifier.isStatic(declared.getModifiers()) && obj.getClass().isAssignableFrom(declared.getType())) {
				try {
					IForgeRegistryEntry<?> fieldVal = (IForgeRegistryEntry) declared.get(null);
					if (regName.equals(fieldVal.getRegistryName())) {
						if (obj instanceof Block && fieldVal instanceof Block) {
							Map<Block, Item> itemMap = GameData.getBlockItemMap();
							itemMap.put((Block) obj, itemMap.get(fieldVal));
						} else if (obj instanceof BlockItem) {
							Map<Block, Item> itemMap = GameData.getBlockItemMap();
							itemMap.put(((BlockItem) obj).getBlock(), (Item) obj);
						}
						
						Quark.LOG.info("Overriding " + registryType + "." + declared + " with " + obj);
						MiscUtil.editFinalField(declared, null, obj);
					}
				} catch (IllegalAccessException e) {
					Quark.LOG.warn("Was unable to replace registry entry for " + regName + ", may cause issues", e);
				}
			}
		}
	}


}
