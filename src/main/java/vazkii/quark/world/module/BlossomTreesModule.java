package vazkii.quark.world.module;

import java.util.HashMap;
import java.util.Map;

import com.google.common.base.Functions;

import net.minecraft.block.ComposterBlock;
import net.minecraft.block.MaterialColor;
import net.minecraft.item.Items;
import net.minecraft.world.gen.GenerationStep.Feature;
import net.minecraftforge.common.BiomeDictionary;
import vazkii.quark.base.handler.VariantHandler;
import vazkii.quark.base.module.LoadModule;
import vazkii.quark.base.module.Module;
import vazkii.quark.base.module.ModuleCategory;
import vazkii.quark.base.module.config.Config;
import vazkii.quark.base.world.WorldGenHandler;
import vazkii.quark.base.world.WorldGenWeights;
import vazkii.quark.world.block.BlossomLeavesBlock;
import vazkii.quark.world.block.BlossomSaplingBlock;
import vazkii.quark.world.block.BlossomSaplingBlock.BlossomTree;
import vazkii.quark.world.config.BlossomTreeConfig;
import vazkii.quark.world.gen.BlossomTreeGenerator;

@LoadModule(category = ModuleCategory.WORLD)
public class BlossomTreesModule extends Module {

	@Config BlossomTreeConfig blue = new BlossomTreeConfig(200, BiomeDictionary.Type.COLD);
	@Config BlossomTreeConfig lavender = new BlossomTreeConfig(100, BiomeDictionary.Type.SWAMP); 
	@Config BlossomTreeConfig orange = new BlossomTreeConfig(100, BiomeDictionary.Type.SAVANNA); 
	@Config BlossomTreeConfig pink = new BlossomTreeConfig(100, BiomeDictionary.Type.MOUNTAIN); 
	@Config BlossomTreeConfig yellow = new BlossomTreeConfig(200, BiomeDictionary.Type.PLAINS); 
	
	public static Map<BlossomTree, BlossomTreeConfig> trees = new HashMap<>();
	
	@Override
	public void construct() {
		add("blue", MaterialColor.LIGHT_BLUE, blue);
		add("lavender", MaterialColor.PINK, lavender);
		add("orange", MaterialColor.ORANGE_TERRACOTTA, orange);
		add("pink", MaterialColor.PINK, pink);
		add("yellow", MaterialColor.YELLOW, yellow);
	}
	
	@Override
	public void setup() {
		trees.forEach((tree, config) -> {
			ComposterBlock.ITEM_TO_LEVEL_INCREASE_CHANCE.put(tree.leaf.getBlock().asItem(), 0.3F);
			ComposterBlock.ITEM_TO_LEVEL_INCREASE_CHANCE.put(tree.sapling.asItem(), 0.3F);
			
			WorldGenHandler.addGenerator(this, new BlossomTreeGenerator(config, tree), Feature.TOP_LAYER_MODIFICATION, WorldGenWeights.BLOSSOM_TREES);
		});
	}
	
	private void add(String colorName, MaterialColor color, BlossomTreeConfig config) {
		BlossomLeavesBlock leaves = new BlossomLeavesBlock(colorName, this, color);
		BlossomTree tree = new BlossomTree(leaves);
		BlossomSaplingBlock sapling = new BlossomSaplingBlock(colorName, this, tree, leaves);
		VariantHandler.addFlowerPot(sapling, sapling.getRegistryName().getPath(), Functions.identity());
		
		trees.put(tree, config);
	}
	
}
