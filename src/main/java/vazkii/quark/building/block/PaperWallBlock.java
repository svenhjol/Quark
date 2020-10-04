/**
 * This class was created by <Vazkii>. It's distributed as
 * part of the Quark Mod. Get the Source Code in github:
 * https://github.com/Vazkii/Quark
 *
 * Quark is Open Source and distributed under the
 * CC-BY-NC-SA 3.0 License: https://creativecommons.org/licenses/by-nc-sa/3.0/deed.en_GB
 *
 * File Created @ [24/03/2016, 02:45:00 (GMT)]
 */
package vazkii.quark.building.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import vazkii.quark.base.block.IQuarkBlock;
import vazkii.quark.base.block.QuarkInheritedPaneBlock;

public class PaperWallBlock extends QuarkInheritedPaneBlock {

	public PaperWallBlock(IQuarkBlock parent, String name) {
		super(parent, name,
				Block.Properties.copy(parent.getBlock())
					.lightLevel(b -> 0)); // light level
	}

	@Override
	public int getFlammability(BlockState state, BlockView world, BlockPos pos, Direction face) {
		return 30;
	}

	@Override
	public int getFireSpreadSpeed(BlockState state, BlockView world, BlockPos pos, Direction face) {
		return 60;
	}
}
