package vazkii.quark.content.world.block;

import java.util.Random;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.IGrowable;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.command.impl.SetBlockCommand;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.monster.EndermanEntity;
import net.minecraft.entity.monster.EndermiteEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.pathfinding.PathType;
import net.minecraft.util.Direction;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.IForgeShearable;
import vazkii.quark.base.block.QuarkBlock;
import vazkii.quark.base.handler.RenderLayerHandler;
import vazkii.quark.base.handler.RenderLayerHandler.RenderTypeSkeleton;
import vazkii.quark.base.module.QuarkModule;
import vazkii.quark.content.world.module.ChorusVegetationModule;

public class ChorusVegetationBlock extends QuarkBlock implements IGrowable, IForgeShearable {

	protected static final VoxelShape SHAPE = Block.makeCuboidShape(2, 0, 2, 14, 13, 14);

	private final boolean simple;

	public ChorusVegetationBlock(String regname, QuarkModule module, boolean simple) {
		super(regname, module, ItemGroup.DECORATIONS,
				AbstractBlock.Properties.create(Material.TALL_PLANTS)
				.doesNotBlockMovement()
				.zeroHardnessAndResistance()
				.sound(SoundType.PLANT)
				.tickRandomly());

		this.simple = simple;
		RenderLayerHandler.setRenderType(this, RenderTypeSkeleton.CUTOUT);
	}
	
	@Override
	public void randomTick(BlockState state, ServerWorld worldIn, BlockPos pos, Random random) {
		if(random.nextDouble() < ChorusVegetationModule.passiveTeleportChance)
			teleport(pos, random, worldIn, state);
	}
	
	@Override
	public void animateTick(BlockState stateIn, World worldIn, BlockPos pos, Random rand) {
		worldIn.addParticle(ParticleTypes.PORTAL, pos.getX() + 0.2 + rand.nextDouble() * 0.6, pos.getY() + 0.3, pos.getZ() + 0.2 + rand.nextDouble() * 0.6, 0, 0, 0);
	}
	
	@Override
	public void onEntityCollision(BlockState state, World worldIn, BlockPos pos, Entity entity) {
		if(simple && worldIn instanceof ServerWorld && entity instanceof LivingEntity && !(entity instanceof EndermanEntity) && !(entity instanceof EndermiteEntity)) {
			BlockPos target = teleport(pos, worldIn.rand, (ServerWorld) worldIn, state);
			
			if(target != null && worldIn.rand.nextDouble() < ChorusVegetationModule.endermiteSpawnChance) {
				EndermiteEntity mite = new EndermiteEntity(EntityType.ENDERMITE, worldIn);
				mite.setPosition(target.getX(), target.getY(), target.getZ());
				worldIn.addEntity(mite);
			}
		}
	}
	
	@Override
	public void neighborChanged(BlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos, boolean isMoving) {
		super.neighborChanged(state, worldIn, pos, blockIn, fromPos, isMoving);
		
		if(worldIn instanceof ServerWorld)
			runAwayFromWater(pos, worldIn.rand, (ServerWorld) worldIn, state);
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
				worldIn.spawnParticle(ParticleTypes.PORTAL, pos.getX() + 0.5, pos.getY() - 0.25, pos.getZ(), 50, 0.25, 0.25, 0.25, 1);
				worldIn.playSound(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, SoundEvents.ENTITY_ENDERMAN_TELEPORT, SoundCategory.BLOCKS, 0.1F, 5F + random.nextFloat());
			} 
			
			worldIn.spawnParticle(ParticleTypes.REVERSE_PORTAL, newPos.getX() + 0.5, newPos.getY() - 0.25, newPos.getZ(), 50, 0.25, 0.25, 0.25, 0.05);
			
			return newPos;
		}
		
		return null;
	}

	@Override
	public boolean canGrow(IBlockReader worldIn, BlockPos pos, BlockState state, boolean isClient) {
		return true;
	}

	@Override
	public boolean canUseBonemeal(World worldIn, Random rand, BlockPos pos, BlockState state) {
		return true;
	}

	public void grow(ServerWorld worldIn, Random rand, BlockPos pos, BlockState state) {
		for(int i = 0; i < (3 + rand.nextInt(3)); i++)
			teleport(pos, rand, worldIn, state, 10, 0);
		teleport(pos, rand, worldIn, state, 4, 1);
	}

	@Override
	public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
		return SHAPE;
	}

	@Override
	@SuppressWarnings("deprecation")
	public BlockState updatePostPlacement(BlockState stateIn, Direction facing, BlockState facingState, IWorld worldIn, BlockPos currentPos, BlockPos facingPos) {
		return !stateIn.isValidPosition(worldIn, currentPos) ? Blocks.AIR.getDefaultState() : super.updatePostPlacement(stateIn, facing, facingState, worldIn, currentPos, facingPos);
	}

	@Override
	public boolean isValidPosition(BlockState state, IWorldReader worldIn, BlockPos pos) {
		return worldIn.getBlockState(pos.down()).getBlock() == Blocks.END_STONE;
	}

	@Override
	@SuppressWarnings("deprecation")
	public boolean allowsMovement(BlockState state, IBlockReader worldIn, BlockPos pos, PathType type) {
		return type == PathType.AIR && !this.canCollide ? true : super.allowsMovement(state, worldIn, pos, type);
	}

	@Override
	public AbstractBlock.OffsetType getOffsetType() {
		return AbstractBlock.OffsetType.XZ;
	}

}
