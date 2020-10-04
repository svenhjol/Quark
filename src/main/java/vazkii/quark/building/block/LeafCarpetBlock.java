package vazkii.quark.building.block;

import javax.annotation.Nonnull;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Material;
import net.minecraft.block.ShapeContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.color.block.BlockColorProvider;
import net.minecraft.client.color.item.ItemColorProvider;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraftforge.common.ToolType;
import vazkii.arl.interf.IBlockColorProvider;
import vazkii.quark.base.block.QuarkBlock;
import vazkii.quark.base.handler.RenderLayerHandler;
import vazkii.quark.base.handler.RenderLayerHandler.RenderTypeSkeleton;
import vazkii.quark.base.module.Module;

public class LeafCarpetBlock extends QuarkBlock implements IBlockColorProvider {

	private static final VoxelShape SHAPE = createCuboidShape(0, 0, 0, 16, 1, 16);
	
	private final BlockState baseState;
	private ItemStack baseStack;
	
	public LeafCarpetBlock(String name, Block base, Module module) {
		super(name + "_leaf_carpet", module, ItemGroup.DECORATIONS, 
				Block.Properties.of(Material.CARPET)
				.strength(0.2F)
				.sounds(BlockSoundGroup.GRASS)
				.harvestTool(ToolType.HOE)
				.nonOpaque());
		
		baseState = base.getDefaultState();
		
		RenderLayerHandler.setRenderType(this, RenderTypeSkeleton.CUTOUT_MIPPED);
	}
	
	@Nonnull
	@Override
	public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
		return SHAPE;
	}

	@Nonnull
	@Override
	@SuppressWarnings("deprecation")
	public VoxelShape getCollisionShape(@Nonnull BlockState state, @Nonnull BlockView world, @Nonnull BlockPos pos, ShapeContext p_220071_4_) {
		return VoxelShapes.empty();
	}

	@Override
	public ItemColorProvider getItemColor() {
		if(baseStack == null)
			baseStack = new ItemStack(baseState.getBlock());

		return (stack, tintIndex) -> MinecraftClient.getInstance().getItemColors().getColorMultiplier(baseStack, tintIndex);
	}

	@Override
	public BlockColorProvider getBlockColor() {
		return (state, worldIn, pos, tintIndex) -> MinecraftClient.getInstance().getBlockColors().getColor(baseState, worldIn, pos, tintIndex);
	}

}
