package vazkii.quark.world.block;

import java.util.OptionalInt;
import java.util.Random;
import java.util.function.BooleanSupplier;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.SaplingBlock;
import net.minecraft.block.sapling.SaplingGenerator;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.world.Heightmap;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.TreeFeatureConfig;
import net.minecraft.world.gen.feature.size.TwoLayersFeatureSize;
import net.minecraft.world.gen.foliage.LargeOakFoliagePlacer;
import net.minecraft.world.gen.stateprovider.SimpleBlockStateProvider;
import net.minecraft.world.gen.trunk.LargeOakTrunkPlacer;
import vazkii.arl.util.RegistryHelper;
import vazkii.quark.base.block.IQuarkBlock;
import vazkii.quark.base.handler.RenderLayerHandler;
import vazkii.quark.base.handler.RenderLayerHandler.RenderTypeSkeleton;
import vazkii.quark.base.module.Module;

public class BlossomSaplingBlock extends SaplingBlock implements IQuarkBlock {

	private static final BlockState SPRUCE_LOG = Blocks.SPRUCE_LOG.getDefaultState();

	private final Module module;
	private BooleanSupplier enabledSupplier = () -> true;

	public BlossomSaplingBlock(String colorName, Module module, BlossomTree tree, Block leaf) {
		super(tree, Block.Properties.copy(Blocks.OAK_SAPLING));
		this.module = module;

		RegistryHelper.registerBlock(this, colorName + "_blossom_sapling");
		RegistryHelper.setCreativeTab(this, ItemGroup.DECORATIONS);
		tree.sapling = this;
		
		RenderLayerHandler.setRenderType(this, RenderTypeSkeleton.CUTOUT);
	}

	@Override
	public void addStacksForDisplay(ItemGroup group, DefaultedList<ItemStack> items) {
		if(isEnabled() || group == ItemGroup.SEARCH)
			super.addStacksForDisplay(group, items);
	}

	@Override
	public Module getModule() {
		return module;
	}

	@Override
	public BlossomSaplingBlock setCondition(BooleanSupplier enabledSupplier) {
		this.enabledSupplier = enabledSupplier;
		return this;
	}

	@Override
	public boolean doesConditionApply() {
		return enabledSupplier.getAsBoolean();
	}

	public static class BlossomTree extends SaplingGenerator {

		public final TreeFeatureConfig config;
		public final BlockState leaf;
		public BlossomSaplingBlock sapling;

		public BlossomTree(Block leafBlock) {
			config = (new TreeFeatureConfig.Builder(
					new SimpleBlockStateProvider(Blocks.SPRUCE_LOG.getDefaultState()),
					new SimpleBlockStateProvider(leafBlock.getDefaultState()), 
					new LargeOakFoliagePlacer(2, 0, 4, 0, 4), 
					new LargeOakTrunkPlacer(3, 11, 0), 
					new TwoLayersFeatureSize(0, 0, 0, OptionalInt.of(4))))
					.ignoreVines().heightmap(Heightmap.Type.MOTION_BLOCKING)
					.build();
			
			leaf = leafBlock.getDefaultState();
		}

		@Override
		protected ConfiguredFeature<TreeFeatureConfig, ?> createTreeFeature(Random rand, boolean hjskfsd) {
			return Feature.TREE.configure(config); // tree
		}
		
	}

}
