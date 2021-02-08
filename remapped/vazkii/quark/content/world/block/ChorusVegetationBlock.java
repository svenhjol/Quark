package vazkii.quark.content.world.block;

import java.util.Random;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.Fertilizable;
import net.minecraft.block.Material;
import net.minecraft.block.ShapeContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.entity.mob.EndermanEntity;
import net.minecraft.entity.mob.EndermiteEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;
import net.minecraftforge.common.IForgeShearable;
import vazkii.quark.base.block.QuarkBlock;
import vazkii.quark.base.handler.RenderLayerHandler;
import vazkii.quark.base.handler.RenderLayerHandler.RenderTypeSkeleton;
import vazkii.quark.base.module.QuarkModule;
import vazkii.quark.content.world.module.ChorusVegetationModule;

public class ChorusVegetationBlock extends QuarkBlock implements Fertilizable, IForgeShearable {

	protected static final VoxelShape SHAPE = Block.createCuboidShape(2, 0, 2, 14, 13, 14);

	private final boolean simple;

	public ChorusVegetationBlock(String regname, QuarkModule module, boolean simple) {
		super(regname, module, ItemGroup.DECORATIONS,
				AbstractBlock.Settings.of(Material.REPLACEABLE_PLANT)
				.noCollision()
				.breakInstantly()
				.sounds(BlockSoundGroup.GRASS)
				.ticksRandomly());

		this.simple = simple;
		RenderLayerHandler.setRenderType(this, RenderTypeSkeleton.CUTOUT);
	}
	
	@Override
	public void randomTick(BlockState state, ServerWorld worldIn, BlockPos pos, Random random) {
		if(random.nextDouble() < ChorusVegetationModule.passiveTeleportChance)
			teleport(pos, random, worldIn, state);
	}
	
	@Override
	public void randomDisplayTick(BlockState stateIn, World worldIn, BlockPos pos, Random rand) {
		worldIn.addParticle(ParticleTypes.PORTAL, pos.getX() + 0.2 + rand.nextDouble() * 0.6, pos.getY() + 0.3, pos.getZ() + 0.2 + rand.nextDouble() * 0.6, 0, 0, 0);
	}
	
	@Override
	public void onEntityCollision(BlockState state, World worldIn, BlockPos pos, Entity entity) {
		if(simple && worldIn instanceof ServerWorld && entity instanceof LivingEntity && !(entity instanceof EndermanEntity) && !(entity instanceof EndermiteEntity)) {
			BlockPos target = teleport(pos, worldIn.random, (ServerWorld) worldIn, state);
			
			if(target != null && worldIn.random.nextDouble() < ChorusVegetationModule.endermiteSpawnChance) {
				EndermiteEntity mite = new EndermiteEntity(EntityType.ENDERMITE, worldIn);
				mite.updatePosition(target.getX(), target.getY(), target.getZ());
				worldIn.spawnEntity(mite);
			}
		}
	}
	
	@Override
	public void neighborUpdate(BlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos, boolean isMoving) {
		super.neighborUpdate(state, worldIn, pos, blockIn, fromPos, isMoving);
		
		if(worldIn instanceof ServerWorld)
			runAwayFromWater(pos, worldIn.random, (ServerWorld) worldIn, state);
	}
	
	private void runAwayFromWater(BlockPos pos, Random random, ServerWorld worldIn, BlockState state) {
		for(Direction d : Direction.values()) {
			BlockPos test = pos.offset(d);
			FluidState fluid = worldIn.getFluidState(test);
			if(fluid.getFluid() == Fluids.WATER || fluid.getFluid() == Fluids.FLOWING_WATER) {
				teleport(pos, random, worldIn, state, 8, 1);
				return;
			}
		}
	}
	
	private BlockPos teleport(BlockPos pos, Random random, ServerWorld worldIn, BlockState state) {
		return teleport(pos, random, worldIn, state, 4, (1.0 - ChorusVegetationModule.teleportDuplicationChance));
	}
	
	private BlockPos teleport(BlockPos pos, Random random, ServerWorld worldIn, BlockState state, int range, double growthChance) {
		int xOff = 0;
		int zOff = 0;
		do {
			xOff = random.nextInt(range) - (range / 2);
			zOff = random.nextInt(range) - (range / 2);
		} while(xOff == 0 && zOff == 0);
		BlockPos newPos = pos.add(xOff, 10, zOff);
		
		for(int i = 0; i < 20; i++) {
			BlockState stateAt = worldIn.getBlockState(newPos);
			if(stateAt.getBlock() == Blocks.END_STONE)
				break;
			
			else newPos = newPos.down();
		}
		
		if(worldIn.getBlockState(newPos).getBlock() == Blocks.END_STONE && worldIn.getBlockState(newPos.up()).isAir()) {
			newPos = newPos.up();
			worldIn.setBlockState(newPos, state);
			
			if(random.nextDouble() < growthChance) {
				worldIn.setBlockState(pos, Blocks.AIR.getDefaultState());
				worldIn.spawnParticles(ParticleTypes.PORTAL, pos.getX() + 0.5, pos.getY() - 0.25, pos.getZ(), 50, 0.25, 0.25, 0.25, 1);
				worldIn.playSound(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, SoundEvents.ENTITY_ENDERMAN_TELEPORT, SoundCategory.BLOCKS, 0.1F, 5F + random.nextFloat());
			} 
			
			worldIn.spawnParticles(ParticleTypes.REVERSE_PORTAL, newPos.getX() + 0.5, newPos.getY() - 0.25, newPos.getZ(), 50, 0.25, 0.25, 0.25, 0.05);
			
			return newPos;
		}
		
		return null;
	}

	@Override
	public boolean isFertilizable(BlockView worldIn, BlockPos pos, BlockState state, boolean isClient) {
		return true;
	}

	@Override
	public boolean canGrow(World worldIn, Random rand, BlockPos pos, BlockState state) {
		return true;
	}

	public void grow(ServerWorld worldIn, Random rand, BlockPos pos, BlockState state) {
		for(int i = 0; i < (3 + rand.nextInt(3)); i++)
			teleport(pos, rand, worldIn, state, 10, 0);
		teleport(pos, rand, worldIn, state, 4, 1);
	}

	@Override
	public VoxelShape getOutlineShape(BlockState state, BlockView worldIn, BlockPos pos, ShapeContext context) {
		return SHAPE;
	}

	@Override
	@SuppressWarnings("deprecation")
	public BlockState getStateForNeighborUpdate(BlockState stateIn, Direction facing, BlockState facingState, WorldAccess worldIn, BlockPos currentPos, BlockPos facingPos) {
		return !stateIn.canPlaceAt(worldIn, currentPos) ? Blocks.AIR.getDefaultState() : super.getStateForNeighborUpdate(stateIn, facing, facingState, worldIn, currentPos, facingPos);
	}

	@Override
	public boolean canPlaceAt(BlockState state, WorldView worldIn, BlockPos pos) {
		return worldIn.getBlockState(pos.down()).getBlock() == Blocks.END_STONE;
	}

	@Override
	@SuppressWarnings("deprecation")
	public boolean canPathfindThrough(BlockState state, BlockView worldIn, BlockPos pos, NavigationType type) {
		return type == NavigationType.AIR && !this.collidable ? true : super.canPathfindThrough(state, worldIn, pos, type);
	}

	@Override
	public AbstractBlock.OffsetType getOffsetType() {
		return AbstractBlock.OffsetType.XZ;
	}

}
