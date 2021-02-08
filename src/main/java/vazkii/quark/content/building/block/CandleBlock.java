package vazkii.quark.content.building.block;

import java.util.Random;

import javax.annotation.Nonnull;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.FallingBlock;
import net.minecraft.block.IWaterLoggable;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.entity.item.FallingBlockEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.DyeColor;
import net.minecraft.item.ItemGroup;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import vazkii.quark.api.IEnchantmentInfluencer;
import vazkii.quark.base.block.QuarkBlock;
import vazkii.quark.base.module.QuarkModule;
import vazkii.quark.content.building.module.TallowAndCandlesModule;

public class CandleBlock extends QuarkBlock implements IWaterLoggable, IEnchantmentInfluencer {

	private static final VoxelShape SHAPE = Block.makeCuboidShape(6F, 0F, 6F, 10F, 8F, 10F);
	public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

	private final DyeColor color;
	
	public CandleBlock(String regname, QuarkModule module, DyeColor color) {
		super(regname, module, ItemGroup.DECORATIONS, 
				Block.Properties.create(Material.MISCELLANEOUS, color.getMapColor())
				.hardnessAndResistance(0.2F)
				.setLightLevel((s) -> s.get(WATERLOGGED) ? 0 : 14)
				.sound(SoundType.CLOTH));
		
		this.color = color;
		
		setDefaultState(getDefaultState().with(WATERLOGGED, false));
	}
	
	@Override
	public DyeColor getEnchantmentInfluenceColor(IBlockReader world, BlockPos pos, BlockState state) {
		return state.get(WATERLOGGED) ? null : color;
	}
	
	@Override
	protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
		builder.add(WATERLOGGED);
	}
	
	@Override
	public BlockState getStateForPlacement(BlockItemUseContext context) {
		return super.getStateForPlacement(context)
				.with(WATERLOGGED, context.getWorld().getFluidState(context.getPos()).getFluid() == Fluids.WATER);
	}
	
	@Nonnull
	@Override
	@SuppressWarnings("deprecation")
	public FluidState getFluidState(BlockState state) {
		return state.get(WATERLOGGED) ? Fluids.WATER.getStillFluidState(false) : super.getFluidState(state);
	}
	
	@Nonnull
	@Override
	@SuppressWarnings("deprecation")
	public BlockState updatePostPlacement(@Nonnull BlockState stateIn, Direction facing, BlockState facingState, IWorld worldIn, BlockPos currentPos, BlockPos facingPos) {
		if(stateIn.get(WATERLOGGED))
			worldIn.getPendingFluidTicks().scheduleTick(currentPos, Fluids.WATER, Fluids.WATER.getTickRate(worldIn));

		return super.updatePostPlacement(stateIn, facing, facingState, worldIn, currentPos, facingPos);
	}
	
	@Override
	public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
		return SHAPE;
	}

	@Override
	public void onBlockAdded(BlockState state, World worldIn, BlockPos pos, BlockState oldState, boolean isMoving) {
	      worldIn.getPendingBlockTicks().scheduleTick(pos, this, 2);
	}

	@Override
	public void neighborChanged(BlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos, boolean isMoving) {		
	      worldIn.getPendingBlockTicks().scheduleTick(pos, this, 2);
	}

	@Override
	public void tick(BlockState state, ServerWorld worldIn, BlockPos pos, Random random) {
		if(!worldIn.isRemote && TallowAndCandlesModule.candlesFall)
			checkFallable(worldIn, pos);
	}

	@Override
	public float getEnchantPowerBonus(BlockState state, IWorldReader world, BlockPos pos) {
		return (float) TallowAndCandlesModule.enchantPower;
	}

	// Copypasta from FallingBlock
	private void checkFallable(World worldIn, BlockPos pos) {
		if (worldIn.isAirBlock(pos.down()) || FallingBlock.canFallThrough(worldIn.getBlockState(pos.down())) && pos.getY() >= 0) {
			if (!worldIn.isRemote) {
				FallingBlockEntity fallingblockentity = new FallingBlockEntity(worldIn, (double)pos.getX() + 0.5D, (double)pos.getY(), (double)pos.getZ() + 0.5D, worldIn.getBlockState(pos));
				worldIn.addEntity(fallingblockentity);
			}
		}
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void animateTick(BlockState stateIn, World worldIn, BlockPos pos, Random rand) {
		if(!stateIn.get(WATERLOGGED)) {
			double d0 = pos.getX() + 0.5D;
			double d1 = pos.getY() + 0.7D;
			double d2 = pos.getZ() + 0.5D;

			worldIn.addParticle(ParticleTypes.SMOKE, d0, d1, d2, 0.0D, 0.0D, 0.0D);
			worldIn.addParticle(ParticleTypes.FLAME, d0, d1, d2, 0.0D, 0.0D, 0.0D);
		}
	}
	
}
