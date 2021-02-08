package vazkii.quark.content.building.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.item.ItemGroup;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraftforge.common.ToolType;
import vazkii.quark.base.block.QuarkBlock;
import vazkii.quark.base.module.QuarkModule;

import javax.annotation.Nonnull;

public class PaperLanternBlock extends QuarkBlock {

	private static final VoxelShape POST_SHAPE = makeCuboidShape(6, 0, 6, 10, 16, 10);
	private static final VoxelShape LANTERN_SHAPE = makeCuboidShape(2, 2, 2, 14, 14, 14);
	private static final VoxelShape SHAPE = VoxelShapes.or(POST_SHAPE, LANTERN_SHAPE);

	public PaperLanternBlock(String regname, QuarkModule module) {
		super(regname, module, ItemGroup.DECORATIONS,
				Block.Properties.create(Material.WOOD, MaterialColor.SNOW)
						.sound(SoundType.WOOD)
						.harvestTool(ToolType.AXE)
						.harvestLevel(0)
						.setLightLevel(b -> 15)
						.hardnessAndResistance(1.5F));
	}

	@Nonnull
	@Override
	@SuppressWarnings("deprecation")
	public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
		return SHAPE;
	}

	@Override
	public int getFlammability(BlockState state, IBlockReader world, BlockPos pos, Direction face) {
		return 30;
	}

	@Override
	public int getFireSpreadSpeed(BlockState state, IBlockReader world, BlockPos pos, Direction face) {
		return 60;
	}
}
