package vazkii.quark.content.building.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.PushReaction;
import net.minecraft.item.ItemGroup;
import net.minecraftforge.common.ToolType;
import vazkii.quark.base.block.QuarkBlock;
import vazkii.quark.base.module.QuarkModule;

public class SturdyStoneBlock extends QuarkBlock {

	public SturdyStoneBlock(QuarkModule module) {
		super("sturdy_stone", module, ItemGroup.BUILDING_BLOCKS,
				Block.Properties.create(Material.ROCK)
				.setRequiresTool()
        		.harvestTool(ToolType.PICKAXE)
				.hardnessAndResistance(4F, 10F)
				.sound(SoundType.STONE));
	}
	
	@Override
	@SuppressWarnings("deprecation")
	public PushReaction getPushReaction(BlockState state) {
		return PushReaction.BLOCK;
	}

}
