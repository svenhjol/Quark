package vazkii.quark.building.module;

import java.util.LinkedList;
import java.util.List;

import com.google.common.collect.ImmutableSet;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.ComposterBlock;
import vazkii.quark.base.module.LoadModule;
import vazkii.quark.base.module.Module;
import vazkii.quark.base.module.ModuleCategory;
import vazkii.quark.building.block.LeafCarpetBlock;
import vazkii.quark.world.block.BlossomLeavesBlock;
import vazkii.quark.world.module.BlossomTreesModule;

@LoadModule(category = ModuleCategory.BUILDING)
public class LeafCarpetModule extends Module {

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
	public void setup() {
		carpets.forEach(c -> ComposterBlock.ITEM_TO_LEVEL_INCREASE_CHANCE.put(c.asItem(), 0.2F));
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
