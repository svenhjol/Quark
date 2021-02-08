package vazkii.quark.content.building.block;

import javax.annotation.Nonnull;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.color.IBlockColor;
import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraftforge.common.ToolType;
import vazkii.arl.interf.IBlockColorProvider;
import vazkii.quark.base.block.QuarkBlock;
import vazkii.quark.base.handler.RenderLayerHandler;
import vazkii.quark.base.handler.RenderLayerHandler.RenderTypeSkeleton;
import vazkii.quark.base.module.QuarkModule;

public class LeafCarpetBlock extends QuarkBlock implements IBlockColorProvider {

	private static final VoxelShape SHAPE = makeCuboidShape(0, 0, 0, 16, 1, 16);
	
	private final BlockState baseState;
	private ItemStack baseStack;
	
	public LeafCarpetBlock(String name, Block base, QuarkModule module) {
		super(name + "_leaf_carpet", module, ItemGroup.DECORATIONS, 
				Block.Properties.create(Material.CARPET)
				.hardnessAndResistance(0F)
				.sound(SoundType.PLANT)
				.harvestTool(ToolType.HOE)
				.notSolid());
		
		baseState = base.getDefaultState();
		
		RenderLayerHandler.setRenderType(this, RenderTypeSkeleton.CUTOUT_MIPPED);
	}
	
	@Override
	public boolean isReplaceable(BlockState state, BlockItemUseContext useContext) {
		return true;
	}
	
	@Nonnull
	@Override
	public VoxelShape getShape(BlockState state, IBlockReader world, BlockPos pos, ISelectionContext context) {
		return SHAPE;
	}

	@Nonnull
	@Override
	@SuppressWarnings("deprecation")
	public VoxelShape getCollisionShape(@Nonnull BlockState state, @Nonnull IBlockReader world, @Nonnull BlockPos pos, ISelectionContext p_220071_4_) {
		return VoxelShapes.empty();
	}

	@Override
	public IItemColor getItemColor() {
		if(baseStack == null)
			baseStack = new ItemStack(baseState.getBlock());

		return (stack, tintIndex) -> Minecraft.getInstance().getItemColors().getColor(baseStack, tintIndex);
	}

	@Override
	public IBlockColor getBlockColor() {
		return (state, worldIn, pos, tintIndex) -> Minecraft.getInstance().getBlockColors().getColor(baseState, worldIn, pos, tintIndex);
	}

}
