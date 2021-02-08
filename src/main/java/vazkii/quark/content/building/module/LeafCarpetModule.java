package vazkii.quark.content.building.module;

import java.util.LinkedList;
import java.util.List;

import com.google.common.collect.ImmutableSet;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.ComposterBlock;
import vazkii.quark.base.module.LoadModule;
import vazkii.quark.base.module.QuarkModule;
import vazkii.quark.content.building.block.LeafCarpetBlock;
import vazkii.quark.content.world.block.BlossomLeavesBlock;
import vazkii.quark.content.world.module.BlossomTreesModule;
import vazkii.quark.base.module.ModuleCategory;

@LoadModule(category = ModuleCategory.BUILDING)
public class LeafCarpetModule extends QuarkModule {

	public static List<LeafCarpetBlock> carpets = new LinkedList<>();
	
	@Override
	public void construct() {
		ImmutableSet.of(Blocks.OAK_LEAVES, Blocks.SPRUCE_LEAVES, Blocks.BIRCH_LEAVES, Blocks.JUNGLE_LEAVES, Blocks.ACACIA_LEAVES, Blocks.DARK_OAK_LEAVES).forEach(this::carpet);
	}
	
	@Override
	public void modulesStarted() {
		BlossomTreesModule.trees.keySet().stream().map(t -> (BlossomLeavesBlock) t.leaf.getBlock()).forEach(this::blossomCarpet);
	}
	
	@Override
	public void loadComplete() {
		enqueue(() -> {
			for(LeafCarpetBlock c : carpets) {
				if(c.asItem() != null)
					ComposterBlock.CHANCES.put(c.asItem(), 0.2F);
			}
		});
	}
	
	private void carpet(Block base) {
		carpetBlock(base);
	}
	
	private void blossomCarpet(BlossomLeavesBlock base) {
		carpetBlock(base).setCondition(() -> base.isEnabled());
	}
	
	private LeafCarpetBlock carpetBlock(Block base) {
		LeafCarpetBlock carpet = new LeafCarpetBlock(base.getRegistryName().getPath().replaceAll("_leaves", ""), base, this);
		carpets.add(carpet);
		return carpet;
	}
	
}
