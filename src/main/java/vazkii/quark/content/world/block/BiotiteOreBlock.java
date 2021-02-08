package vazkii.quark.content.world.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.item.ItemGroup;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.IWorldReader;
import net.minecraftforge.common.ToolType;
import vazkii.quark.base.block.QuarkBlock;
import vazkii.quark.base.module.QuarkModule;

public class BiotiteOreBlock extends QuarkBlock {

	public BiotiteOreBlock(QuarkModule module) {
		super("biotite_ore", module, ItemGroup.BUILDING_BLOCKS, 
				Block.Properties.create(Material.ROCK, MaterialColor.SAND)
				.setRequiresTool() // needs tool
        		.harvestTool(ToolType.PICKAXE)
				.hardnessAndResistance(3.2F, 15F)
				.sound(SoundType.STONE));
	}
	
	@Override
	public int getExpDrop(BlockState state, IWorldReader world, BlockPos pos, int fortune, int silktouch) {
		return silktouch == 0 ? MathHelper.nextInt(RANDOM, 2, 5) : 0;
	}
	
}
