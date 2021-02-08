package vazkii.quark.content.world.module;

import java.util.List;
import java.util.function.BiPredicate;
import java.util.function.BooleanSupplier;

import com.google.common.collect.Lists;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.GenerationStage;
import net.minecraft.world.gen.GenerationStage.Decoration;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.IFeatureConfig;
import net.minecraft.world.gen.feature.OreFeatureConfig;
import vazkii.quark.base.module.LoadModule;
import vazkii.quark.base.module.ModuleCategory;
import vazkii.quark.base.module.QuarkModule;
import vazkii.quark.base.module.config.Config;
import vazkii.quark.base.world.WorldGenHandler;
import vazkii.quark.base.world.WorldGenWeights;
import vazkii.quark.base.world.config.DimensionConfig;
import vazkii.quark.base.world.config.StrictBiomeConfig;
import vazkii.quark.content.world.config.AirStoneClusterConfig;
import vazkii.quark.content.world.config.BigStoneClusterConfig;
import vazkii.quark.content.world.gen.BigStoneClusterGenerator;

@LoadModule(category = ModuleCategory.WORLD)
public class BigStoneClustersModule extends QuarkModule {

	@Config public static BigStoneClusterConfig granite = new BigStoneClusterConfig(Biome.Category.EXTREME_HILLS);
	@Config public static BigStoneClusterConfig diorite = new BigStoneClusterConfig(Biome.Category.SAVANNA, Biome.Category.JUNGLE, Biome.Category.MUSHROOM);
	@Config public static BigStoneClusterConfig andesite = new BigStoneClusterConfig(Biome.Category.FOREST);
	@Config public static BigStoneClusterConfig marble = new BigStoneClusterConfig(Biome.Category.PLAINS);
	@Config public static BigStoneClusterConfig limestone = new BigStoneClusterConfig(Biome.Category.SWAMP, Biome.Category.OCEAN);
	@Config public static BigStoneClusterConfig jasper = new BigStoneClusterConfig(Biome.Category.MESA, Biome.Category.DESERT);
	@Config public static BigStoneClusterConfig slate = new BigStoneClusterConfig(Biome.Category.ICY);
	
	@Config public static BigStoneClusterConfig voidstone = new BigStoneClusterConfig(DimensionConfig.end(false), 19, 6, 20, 0, 40, 
			new StrictBiomeConfig(false, "minecraft:end_highlands", "minecraft:end_midlands", "minecraft:end_barrens"));
	
	@Config public static BigStoneClusterConfig myalite = new AirStoneClusterConfig(DimensionConfig.end(false), 20, 6, 100, 58, 62, 
			new StrictBiomeConfig(false, "minecraft:end_highlands"))
			.setVertical(40, 10);

	@Config(description = "Blocks that stone clusters can replace. If you want to make it so it only replaces in one dimension,\n"
			+ "do \"block|dimension\", as we do for netherrack and end stone by default.") 
	public static List<String> blocksToReplace = Lists.newArrayList(
			"minecraft:stone", "minecraft:andesite", "minecraft:diorite", "minecraft:granite",
			"minecraft:netherrack|minecraft:the_nether", "minecraft:end_stone|minecraft:the_end",
			"quark:marble", "quark:limestone", "quark:jasper", "quark:slate", "quark:basalt");
	
	public static BiPredicate<World, Block> blockReplacePredicate = (w, b) -> false;
	
	@Override
	public void setup() {
		BooleanSupplier alwaysTrue = () -> true;
		add(granite, Blocks.GRANITE, alwaysTrue);
		add(diorite, Blocks.DIORITE, alwaysTrue);
		add(andesite, Blocks.ANDESITE, alwaysTrue);
		
		add(marble, NewStoneTypesModule.marbleBlock, () -> NewStoneTypesModule.enabledWithMarble);
		add(limestone, NewStoneTypesModule.limestoneBlock, () -> NewStoneTypesModule.enabledWithLimestone);
		add(jasper, NewStoneTypesModule.jasperBlock, () -> NewStoneTypesModule.enabledWithJasper);
		add(slate, NewStoneTypesModule.slateBlock, () -> NewStoneTypesModule.enabledWithSlate);
		add(voidstone, NewStoneTypesModule.basaltBlock, () -> NewStoneTypesModule.enabledWithVoidstone);
		add(myalite, NewStoneTypesModule.myaliteBlock, () -> NewStoneTypesModule.enabledWithMyalite);

		conditionalize(Blocks.GRANITE, () -> (!enabled || !granite.enabled));
		conditionalize(Blocks.DIORITE, () -> (!enabled || !diorite.enabled));
		conditionalize(Blocks.ANDESITE, () -> (!enabled || !andesite.enabled));
	}
	
	private void add(BigStoneClusterConfig config, Block block, BooleanSupplier condition) {
		WorldGenHandler.addGenerator(this, new BigStoneClusterGenerator(config, block.getDefaultState(), condition), Decoration.UNDERGROUND_DECORATION, WorldGenWeights.BIG_STONE_CLUSTERS);
	}
	
	private void conditionalize(Block block, BooleanSupplier condition) {
		BiPredicate<Feature<? extends IFeatureConfig>, IFeatureConfig> pred = (feature, config) -> {
			if(config instanceof OreFeatureConfig) {
				OreFeatureConfig oconfig = (OreFeatureConfig) config;
				return oconfig.state.getBlock() == block;
			}
			
			return false;
		};
		
		WorldGenHandler.conditionalizeFeatures(GenerationStage.Decoration.UNDERGROUND_ORES, pred, condition);
	}
	
	@Override
	public void configChanged() {
		blockReplacePredicate = (b, w) -> false;
		
		for(String s : blocksToReplace) {
			String bname = s;
			String dimension = null;
			
			if(bname.contains("|")) {
				String[] toks = bname.split("\\|");
				bname = toks[0];
				dimension = toks[1];
			}
			
			String dimFinal = dimension;
			Registry.BLOCK.getOptional(new ResourceLocation(bname)).ifPresent(blockObj -> {
				if(blockObj != Blocks.AIR) {
					if(dimFinal == null)
						blockReplacePredicate = blockReplacePredicate.or((w, b) -> blockObj == b);
					else {
						blockReplacePredicate = blockReplacePredicate.or((w, b) -> {
							if(blockObj != b)
								return false;
							if(w == null)
								return false;
							
							return w.getDimensionKey().getLocation().toString().equals(dimFinal);
						});
					}
				}
			});
		}
	}
	
}
