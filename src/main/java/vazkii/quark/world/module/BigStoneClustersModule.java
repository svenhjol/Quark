package vazkii.quark.world.module;

import java.util.List;
import java.util.function.BiPredicate;
import java.util.function.BooleanSupplier;

import com.google.common.base.Predicates;
import com.google.common.collect.Lists;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.FeatureConfig;
import net.minecraft.world.gen.feature.OreFeatureConfig;
import net.minecraftforge.common.BiomeDictionary.Type;
import net.minecraftforge.registries.ForgeRegistries;
import vazkii.quark.base.module.LoadModule;
import vazkii.quark.base.module.Module;
import vazkii.quark.base.module.ModuleCategory;
import vazkii.quark.base.module.config.Config;
import vazkii.quark.base.world.WorldGenHandler;
import vazkii.quark.base.world.WorldGenWeights;
import vazkii.quark.base.world.config.DimensionConfig;
import vazkii.quark.world.config.BigStoneClusterConfig;
import vazkii.quark.world.gen.BigStoneClusterGenerator;

@LoadModule(category = ModuleCategory.WORLD)
public class BigStoneClustersModule extends Module {

	@Config public static BigStoneClusterConfig granite = new BigStoneClusterConfig(Type.MOUNTAIN, Type.HILLS);
	@Config public static BigStoneClusterConfig diorite = new BigStoneClusterConfig(Type.SAVANNA, Type.JUNGLE, Type.MUSHROOM);
	@Config public static BigStoneClusterConfig andesite = new BigStoneClusterConfig(Type.FOREST);
	@Config public static BigStoneClusterConfig marble = new BigStoneClusterConfig(Type.PLAINS);
	@Config public static BigStoneClusterConfig limestone = new BigStoneClusterConfig(Type.SWAMP, Type.OCEAN);
	@Config public static BigStoneClusterConfig jasper = new BigStoneClusterConfig(Type.MESA, Type.SANDY);
	@Config public static BigStoneClusterConfig slate = new BigStoneClusterConfig(Type.COLD);
	@Config public static BigStoneClusterConfig voidstone = new BigStoneClusterConfig(DimensionConfig.end(false), 19, 6, 20, 0, 40, Type.END);
	
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
		
		conditionalize(Blocks.GRANITE, () -> (!enabled || !granite.enabled));
		conditionalize(Blocks.DIORITE, () -> (!enabled || !diorite.enabled));
		conditionalize(Blocks.ANDESITE, () -> (!enabled || !andesite.enabled));
	}
	
	private void add(BigStoneClusterConfig config, Block block, BooleanSupplier condition) {
		WorldGenHandler.addGenerator(this, new BigStoneClusterGenerator(config, block.getDefaultState(), condition), net.minecraft.world.gen.GenerationStep.Feature.UNDERGROUND_DECORATION, WorldGenWeights.BIG_STONE_CLUSTERS);
	}
	
	private void conditionalize(Block block, BooleanSupplier condition) {
		BiPredicate<Feature<? extends FeatureConfig>, FeatureConfig> pred = (feature, config) -> {
			if(config instanceof OreFeatureConfig) {
				OreFeatureConfig oconfig = (OreFeatureConfig) config;
				return oconfig.state.getBlock() == block;
			}
			
			return false;
		};
		
		WorldGenHandler.conditionalizeFeatures(GenerationStep.Feature.UNDERGROUND_ORES, pred, condition);
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
			Block blockObj = ForgeRegistries.BLOCKS.getValue(new Identifier(bname));
			
			if(blockObj != null && blockObj != Blocks.AIR) {
				if(dimension == null)
					blockReplacePredicate = blockReplacePredicate.or((w, b) -> blockObj == b);
				else {
					blockReplacePredicate = blockReplacePredicate.or((w, b) -> {
						if(blockObj != b)
							return false;
						if(w == null)
							return false;

						return ((World) w).getRegistryKey().getValue().toString().equals(dimFinal);
					});
				}
			}
		}
	}
	
}
