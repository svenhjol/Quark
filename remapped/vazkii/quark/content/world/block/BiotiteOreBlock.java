package vazkii.quark.content.world.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Material;
import net.minecraft.block.MaterialColor;
import net.minecraft.item.ItemGroup;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.WorldView;
import net.minecraftforge.common.ToolType;
import vazkii.quark.base.block.QuarkBlock;
import vazkii.quark.base.module.QuarkModule;

public class BiotiteOreBlock extends QuarkBlock {

	public BiotiteOreBlock(QuarkModule module) {
		super("biotite_ore", module, ItemGroup.BUILDING_BLOCKS, 
				Block.Properties.of(Material.STONE, MaterialColor.SAND)
				.requiresTool() // needs tool
        		.harvestTool(ToolType.PICKAXE)
				.strength(3.2F, 15F)
				.sounds(BlockSoundGroup.STONE));
	}
	
	@Override
	public int getExpDrop(BlockState state, WorldView world, BlockPos pos, int fortune, int silktouch) {
		return silktouch == 0 ? MathHelper.nextInt(RANDOM, 2, 5) : 0;
	}
	
}
