package vazkii.quark.content.building.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Material;
import net.minecraft.block.MaterialColor;
import net.minecraft.block.ShapeContext;
import net.minecraft.item.ItemGroup;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraftforge.common.ToolType;
import vazkii.quark.base.block.QuarkBlock;
import vazkii.quark.base.module.QuarkModule;

import javax.annotation.Nonnull;

public class PaperLanternBlock extends QuarkBlock {

	private static final VoxelShape POST_SHAPE = createCuboidShape(6, 0, 6, 10, 16, 10);
	private static final VoxelShape LANTERN_SHAPE = createCuboidShape(2, 2, 2, 14, 14, 14);
	private static final VoxelShape SHAPE = VoxelShapes.union(POST_SHAPE, LANTERN_SHAPE);

	public PaperLanternBlock(String regname, QuarkModule module) {
		super(regname, module, ItemGroup.DECORATIONS,
				Block.Properties.of(Material.WOOD, MaterialColor.WHITE)
						.sounds(BlockSoundGroup.WOOD)
						.harvestTool(ToolType.AXE)
						.harvestLevel(0)
						.luminance(b -> 15)
						.strength(1.5F));
	}

	@Nonnull
	@Override
	@SuppressWarnings("deprecation")
	public VoxelShape getOutlineShape(BlockState state, BlockView worldIn, BlockPos pos, ShapeContext context) {
		return SHAPE;
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
