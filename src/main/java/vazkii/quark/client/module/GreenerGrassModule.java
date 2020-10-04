package vazkii.quark.client.module;

import com.google.common.collect.Lists;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Block;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.color.block.BlockColorProvider;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.util.Identifier;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IRegistryDelegate;
import vazkii.quark.base.module.LoadModule;
import vazkii.quark.base.module.Module;
import vazkii.quark.base.module.ModuleCategory;
import vazkii.quark.base.module.config.Config;

import java.util.List;
import java.util.Map;

@LoadModule(category = ModuleCategory.CLIENT)
public class GreenerGrassModule extends Module {

	@Config public static boolean affectLeaves = true;
	@Config public static boolean alphaGrass = false;
	@Config public static boolean absoluteValues = false;

	@Config public static int redShift = -30;
	@Config public static int greenShift = 30;
	@Config public static int blueShift = -30;

	@Config public static List<String> blockList = Lists.newArrayList(
			"minecraft:large_fern", 
			"minecraft:tall_grass",
			"minecraft:grass_block",
			"minecraft:fern",
			"minecraft:grass",
			"minecraft:potted_fern",
			"minecraft:sugar_cane");

	@Config public static List<String> leavesList = Lists.newArrayList(
			"minecraft:spruce_leaves", 
			"minecraft:birch_leaves",
			"minecraft:oak_leaves",
			"minecraft:jungle_leaves",
			"minecraft:acacia_leaves",
			"minecraft:dark_oak_leaves",
			"minecraft:vine");
	
	@Override
	public void clientSetup() {
		registerGreenerColor(blockList, false);
		registerGreenerColor(leavesList, true);
	}
	
	@Environment(EnvType.CLIENT)
	private void registerGreenerColor(Iterable<String> ids, boolean leaves) {
		BlockColors colors = MinecraftClient.getInstance().getBlockColors();

		Map<IRegistryDelegate<Block>, BlockColorProvider> map = ObfuscationReflectionHelper.getPrivateValue(BlockColors.class, colors, "field_186725_a"); // colors.colors;

		for(String id : ids) {
			Block b = ForgeRegistries.BLOCKS.getValue(new Identifier(id));
			if(b == null || b.delegate == null)
				return;
			
			BlockColorProvider color = map.get(b.delegate);
			if(color != null)
				colors.registerColorProvider(getGreenerColor(color, leaves), b);
		}
	}

	@Environment(EnvType.CLIENT)
	private BlockColorProvider getGreenerColor(BlockColorProvider color, boolean leaves) {
		return (state, world, pos, tintIndex) -> {
			int originalColor = color.getColor(state, world, pos, tintIndex);
			if(!enabled || (leaves && !affectLeaves))
				return originalColor;

			int r = originalColor >> 16 & 0xFF;
			int g = originalColor >> 8 & 0xFF;
			int b = originalColor & 0xFF;

			int shiftRed = alphaGrass ? 30 : redShift;
			int shiftGreen = alphaGrass ? 120 : greenShift;
			int shiftBlue = alphaGrass ? 30 : blueShift;

			if(absoluteValues)
				return (Math.max(0, Math.min(0xFF, redShift)) << 16) + Math.max(0, Math.min(0xFF, greenShift) << 8) + Math.max(0, Math.min(0xFF, blueShift));
			return (Math.max(0, Math.min(0xFF, r + shiftRed)) << 16) + Math.max(0, Math.min(0xFF, g + shiftGreen) << 8) + Math.max(0, Math.min(0xFF, b + shiftBlue));
		};
	}

}
