package vazkii.quark.building.block;

import java.util.Random;

import javax.annotation.Nonnull;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.FallingBlock;
import net.minecraft.block.Material;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.Waterloggable;
import net.minecraft.entity.FallingBlockEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.DyeColor;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import vazkii.quark.api.IEnchantmentInfluencer;
import vazkii.quark.base.block.QuarkBlock;
import vazkii.quark.base.module.Module;
import vazkii.quark.building.module.TallowAndCandlesModule;

public class CandleBlock extends QuarkBlock implements Waterloggable, IEnchantmentInfluencer {

	private static final VoxelShape SHAPE = Block.createCuboidShape(6F, 0F, 6F, 10F, 8F, 10F);
	public static final BooleanProperty WATERLOGGED = Properties.WATERLOGGED;

	private final DyeColor color;
	
	public CandleBlock(String regname, Module module, DyeColor color) {
		super(regname, module, ItemGroup.DECORATIONS, 
				Block.Properties.of(Material.SUPPORTED, color.getMaterialColor())
				.strength(0.2F)
				.lightLevel((s) -> s.get(WATERLOGGED) ? 0 : 14)
				.sounds(BlockSoundGroup.WOOL));
		
		this.color = color;
		
		setDefaultState(getDefaultState().with(WATERLOGGED, false));
	}
	
	@Override
	public DyeColor getEnchantmentInfluenceColor(BlockView world, BlockPos pos, BlockState state) {
		return state.get(WATERLOGGED) ? null : color;
	}
	
	@Override
	protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
		builder.add(WATERLOGGED);
	}
	
	@Override
	public BlockState getPlacementState(ItemPlacementContext context) {
		return super.getPlacementState(context)
				.with(WATERLOGGED, context.getWorld().getFluidState(context.getBlockPos()).getFluid() == Fluids.WATER);
	}
	
	@Nonnull
	@Override
	@SuppressWarnings("deprecation")
	public FluidState getFluidState(BlockState state) {
		return state.get(WATERLOGGED) ? Fluids.WATER.getStill(false) : super.getFluidState(state);
	}
	
	@Nonnull
	@Override
	@SuppressWarnings("deprecation")
	public BlockState getStateForNeighborUpdate(@Nonnull BlockState stateIn, Direction facing, BlockState facingState, WorldAccess worldIn, BlockPos currentPos, BlockPos facingPos) {
		if(stateIn.get(WATERLOGGED))
			worldIn.getFluidTickScheduler().schedule(currentPos, Fluids.WATER, Fluids.WATER.getTickRate(worldIn));

		return super.getStateForNeighborUpdate(stateIn, facing, facingState, worldIn, currentPos, facingPos);
	}
	
	@Override
	public VoxelShape getOutlineShape(BlockState state, BlockView worldIn, BlockPos pos, ShapeContext context) {
		return SHAPE;
	}

	@Override
	public void onBlockAdded(BlockState state, World worldIn, BlockPos pos, BlockState oldState, boolean isMoving) {
	      worldIn.getBlockTickScheduler().schedule(pos, this, 2);
	}

	@Override
	public void neighborUpdate(BlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos, boolean isMoving) {		
	      worldIn.getBlockTickScheduler().schedule(pos, this, 2);
	}

	@Override
	public void scheduledTick(BlockState state, ServerWorld worldIn, BlockPos pos, Random random) {
		if(!worldIn.isClient && TallowAndCandlesModule.candlesFall)
			checkFallable(worldIn, pos);
	}

	@Override
	public float getEnchantPowerBonus(BlockState state, WorldView world, BlockPos pos) {
		return (float) TallowAndCandlesModule.enchantPower;
	}

	// Copypasta from FallingBlock
	private void checkFallable(World worldIn, BlockPos pos) {
		if (worldIn.isAir(pos.down()) || FallingBlock.canFallThrough(worldIn.getBlockState(pos.down())) && pos.getY() >= 0) {
			if (!worldIn.isClient) {
				FallingBlockEntity fallingblockentity = new FallingBlockEntity(worldIn, (double)pos.getX() + 0.5D, (double)pos.getY(), (double)pos.getZ() + 0.5D, worldIn.getBlockState(pos));
				worldIn.spawnEntity(fallingblockentity);
			}
		}
	}

	@Override
	@Environment(EnvType.CLIENT)
	public void randomDisplayTick(BlockState stateIn, World worldIn, BlockPos pos, Random rand) {
		if(!stateIn.get(WATERLOGGED)) {
			double d0 = pos.getX() + 0.5D;
			double d1 = pos.getY() + 0.7D;
			double d2 = pos.getZ() + 0.5D;

			worldIn.addParticle(ParticleTypes.SMOKE, d0, d1, d2, 0.0D, 0.0D, 0.0D);
			worldIn.addParticle(ParticleTypes.FLAME, d0, d1, d2, 0.0D, 0.0D, 0.0D);
		}
	}
	
}
