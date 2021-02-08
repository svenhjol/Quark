package vazkii.quark.content.building.module;

import java.util.List;
import java.util.function.BooleanSupplier;

import com.google.common.collect.Lists;

import net.minecraft.block.Block;
import net.minecraft.block.ComposterBlock;
import net.minecraft.block.Material;
import net.minecraft.block.MaterialColor;
import net.minecraft.item.ItemGroup;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.DyeColor;
import net.minecraftforge.common.ToolType;
import vazkii.quark.base.block.QuarkBlock;
import vazkii.quark.base.block.QuarkFlammableBlock;
import vazkii.quark.base.block.QuarkFlammablePillarBlock;
import vazkii.quark.base.handler.FuelHandler;
import vazkii.quark.base.module.LoadModule;
import vazkii.quark.base.module.QuarkModule;
import vazkii.quark.base.module.ModuleCategory;
import vazkii.quark.base.module.config.Config;
import vazkii.quark.content.building.block.BurnForeverBlock;

@LoadModule(category = ModuleCategory.BUILDING)
public class CompressedBlocksModule extends QuarkModule {

	@Config(name = "Charcoal Block and Blaze Lantern Stay On Fire Forever")
	public static boolean burnsForever = true;

	@Config(name = "Charcoal Block Fuel Time")
	@Config.Min(0)
	public static int charcoalBlockFuelTime = 16000;

	@Config(name = "Blaze Lantern Fuel Time")
	@Config.Min(0)
	public static int blazeLanternFuelTime = 24000;

	@Config(name = "Stick Block Fuel Time")
	@Config.Min(0)
	public static int stickBlockFuelTime = 900;
	
	@Config(flag = "charcoal_block") public static boolean enableCharcoalBlock = true;
	@Config(flag = "sugar_cane_block") public static boolean enableSugarCaneBlock = true;
	@Config(flag = "bamboo_block") public static boolean enableBambooBlock = true;
	@Config(flag = "cactus_block") public static boolean enableCactusBlock = true;
	@Config(flag = "chorus_fruit_block") public static boolean enableChorusFruitBlock = true;
	@Config(flag = "stick_block") public static boolean enableStickBlock = true;

	@Config(flag = "apple_crate") public static boolean enableAppleCrate = true;
	@Config(flag = "golden_apple_crate") public static boolean enableGoldenAppleCrate = true;
	@Config(flag = "potato_crate") public static boolean enablePotatoCrate = true;
	@Config(flag = "carrot_crate") public static boolean enableCarrotCrate = true;
	@Config(flag = "beetroot_crate") public static boolean enableBeetrootCrate = true;

	@Config(flag = "cocoa_beans_sack") public static boolean enableCocoaBeanSack = true;
	@Config(flag = "nether_wart_sack") public static boolean enableNetherWartSack = true;
	@Config(flag = "gunpowder_sack") public static boolean enableGunpowderSack = true;
	@Config(flag = "berry_sack") public static boolean enableBerrySack = true;

	@Config(flag = "blaze_lantern") public static boolean enableBlazeLantern = true;
	@Config(flag = "bonded_leather") public static boolean enableBondedLeather = true;
	@Config(flag = "bonded_rabbit_hide") public static boolean enableBondedRabbitHide = true;

	public static Block charcoal_block, stick_block, blaze_lantern;

	private final List<Block> compostable = Lists.newArrayList();

	@Override
	public void construct() {
		charcoal_block = new BurnForeverBlock("charcoal_block", this, ItemGroup.BUILDING_BLOCKS,
				Block.Properties.of(Material.STONE, MaterialColor.BLACK)
						.requiresTool()
		        		.harvestTool(ToolType.PICKAXE)
						.strength(5F, 10F)
						.sounds(BlockSoundGroup.STONE), true)
				.setCondition(() -> enableCharcoalBlock);
		
		pillar("sugar_cane", MaterialColor.LIME, true, () -> enableSugarCaneBlock, 200);
		pillar("bamboo", MaterialColor.YELLOW, true, () -> enableBambooBlock, 200);
		pillar("cactus", MaterialColor.GREEN, true, () -> enableCactusBlock, 50);
		pillar("chorus_fruit", MaterialColor.PURPLE, false, () -> enableChorusFruitBlock, 10);
		stick_block = pillar("stick", MaterialColor.WOOD, false, () -> enableStickBlock, 300);

		crate("golden_apple", MaterialColor.GOLD, true, () -> enableGoldenAppleCrate);
		crate("apple", MaterialColor.RED, true, () -> enableAppleCrate);
		crate("potato", MaterialColor.ORANGE, true, () -> enablePotatoCrate);
		crate("carrot", MaterialColor.ORANGE_TERRACOTTA, true, () -> enableCarrotCrate);
		crate("beetroot", MaterialColor.RED, true, () -> enableBeetrootCrate);

		sack("cocoa_beans", MaterialColor.BROWN, true, () -> enableCocoaBeanSack);
		sack("nether_wart", MaterialColor.RED, true, () -> enableNetherWartSack);
		sack("gunpowder", MaterialColor.GRAY, false, () -> enableGunpowderSack);
		sack("berry", MaterialColor.RED, true, () -> enableBerrySack);

		blaze_lantern = new BurnForeverBlock("blaze_lantern", this, ItemGroup.BUILDING_BLOCKS,
				Block.Properties.of(Material.GLASS, DyeColor.YELLOW)
				.strength(0.3F)
				.sounds(BlockSoundGroup.GLASS)
				.luminance(b -> 15), false)
		.setCondition(() -> enableBlazeLantern);
		
		new QuarkBlock("bonded_leather", this, ItemGroup.BUILDING_BLOCKS, Block.Properties.of(Material.WOOL, DyeColor.ORANGE)
				.strength(0.4F)
				.sounds(BlockSoundGroup.WOOL))
		.setCondition(() -> enableBondedLeather);
		
		new QuarkBlock("bonded_rabbit_hide", this, ItemGroup.BUILDING_BLOCKS, Block.Properties.of(Material.WOOL, DyeColor.WHITE)
				.strength(0.4F)
				.sounds(BlockSoundGroup.WOOL))
		.setCondition(() -> enableBondedRabbitHide);
	}

	@Override
	public void loadComplete() {
		enqueue(() -> {
			for(Block block : compostable)
				if(block.asItem() != null)
					ComposterBlock.ITEM_TO_LEVEL_INCREASE_CHANCE.put(block.asItem(), 1F);
		});
			
		FuelHandler.addFuel(stick_block, stickBlockFuelTime);
		FuelHandler.addFuel(charcoal_block, charcoalBlockFuelTime);
		FuelHandler.addFuel(blaze_lantern, blazeLanternFuelTime);
	}

	private Block pillar(String name, MaterialColor color, boolean compost, BooleanSupplier cond, int flammability) {
		Block block = new QuarkFlammablePillarBlock(name + "_block", this, ItemGroup.BUILDING_BLOCKS, flammability,
				Block.Properties.of(Material.WOOD, color)
				.strength(0.5F)
				.sounds(BlockSoundGroup.WOOD))
		.setCondition(cond);

		if (compost)
			compostable.add(block);
		return block;
	}
	
	private Block crate(String name, MaterialColor color, boolean compost, BooleanSupplier cond) {
		Block block = new QuarkFlammableBlock(name + "_crate", this, ItemGroup.DECORATIONS, 150,
				Block.Properties.of(Material.WOOD, color)
				.strength(1.5F)
				.sounds(BlockSoundGroup.WOOD))
		.setCondition(cond);

		if (compost)
			compostable.add(block);
		return block;
	}

	private Block sack(String name, MaterialColor color, boolean compost, BooleanSupplier cond) {
		Block block = new QuarkFlammableBlock(name + "_sack", this, ItemGroup.DECORATIONS, 150,
				Block.Properties.of(Material.WOOL, color)
				.strength(0.5F)
				.sounds(BlockSoundGroup.WOOL))
		.setCondition(cond);

		if (compost)
			compostable.add(block);
		return block;
	}

}
