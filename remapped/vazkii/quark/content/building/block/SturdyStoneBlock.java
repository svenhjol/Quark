package vazkii.quark.content.building.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Material;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.item.ItemGroup;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraftforge.common.ToolType;
import vazkii.quark.base.block.QuarkBlock;
import vazkii.quark.base.module.QuarkModule;

public class SturdyStoneBlock extends QuarkBlock {

	public SturdyStoneBlock(QuarkModule module) {
		super("sturdy_stone", module, ItemGroup.BUILDING_BLOCKS,
				Block.Properties.of(Material.STONE)
				.requiresTool()
        		.harvestTool(ToolType.PICKAXE)
				.strength(4F, 10F)
				.sounds(BlockSoundGroup.STONE));
	}
	
	@Override
	@SuppressWarnings("deprecation")
	public PistonBehavior getPistonBehavior(BlockState state) {
		return PistonBehavior.BLOCK;
	}

}
