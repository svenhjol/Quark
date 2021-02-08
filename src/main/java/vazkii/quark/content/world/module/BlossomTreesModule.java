package vazkii.quark.content.world.module;

import java.util.HashMap;
import java.util.Map;

import com.google.common.base.Functions;

import net.minecraft.block.ComposterBlock;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.GenerationStage.Decoration;
import vazkii.quark.base.handler.VariantHandler;
import vazkii.quark.base.module.LoadModule;
import vazkii.quark.base.module.QuarkModule;
import vazkii.quark.base.module.ModuleCategory;
import vazkii.quark.base.module.config.Config;
import vazkii.quark.base.world.WorldGenHandler;
import vazkii.quark.base.world.WorldGenWeights;
import vazkii.quark.content.world.block.BlossomLeavesBlock;
import vazkii.quark.content.world.block.BlossomSaplingBlock;
import vazkii.quark.content.world.block.BlossomSaplingBlock.BlossomTree;
import vazkii.quark.content.world.config.BlossomTreeConfig;
import vazkii.quark.content.world.gen.BlossomTreeGenerator;

@LoadModule(category = ModuleCategory.WORLD)
public class BlossomTreesModule extends QuarkModule {

	@Config BlossomTreeConfig blue = new BlossomTreeConfig(200, Biome.Category.ICY);
	@Config BlossomTreeConfig lavender = new BlossomTreeConfig(100, Biome.Category.SWAMP); 
	@Config BlossomTreeConfig orange = new BlossomTreeConfig(100, Biome.Category.SAVANNA); 
	@Config BlossomTreeConfig pink = new BlossomTreeConfig(100, Biome.Category.EXTREME_HILLS); 
	@Config BlossomTreeConfig yellow = new BlossomTreeConfig(200, Biome.Category.PLAINS); 
	@Config BlossomTreeConfig red = new BlossomTreeConfig(30, Biome.Category.MESA); 

	public static Map<BlossomTree, BlossomTreeConfig> trees = new HashMap<>();
	
	@Override
	public void construct() {
		add("blue", MaterialColor.LIGHT_BLUE, blue);
		add("lavender", MaterialColor.PINK, lavender);
		add("orange", MaterialColor.ORANGE_TERRACOTTA, orange);
		add("pink", MaterialColor.PINK, pink);
		add("yellow", MaterialColor.YELLOW, yellow);
		add("red", MaterialColor.RED, red);
	}
	
	@Override
	public void setup() {
		for(BlossomTree tree : trees.keySet())
			WorldGenHandler.addGenerator(this, new BlossomTreeGenerator(trees.get(tree), tree), Decoration.TOP_LAYER_MODIFICATION, WorldGenWeights.BLOSSOM_TREES);

		enqueue(() -> {
			for(BlossomTree tree : trees.keySet()) {
				if(tree.leaf.getBlock().asItem() != null)
					ComposterBlock.CHANCES.put(tree.leaf.getBlock().asItem(), 0.3F);
				if(tree.sapling.asItem() != null)
					ComposterBlock.CHANCES.put(tree.sapling.asItem(), 0.3F);
			}
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
