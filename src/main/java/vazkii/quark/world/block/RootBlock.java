package vazkii.quark.world.block;

import java.util.Random;
import java.util.function.BooleanSupplier;

import javax.annotation.Nullable;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Fertilizable;
import net.minecraft.block.Material;
import net.minecraft.block.VineBlock;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;
import vazkii.arl.util.RegistryHelper;
import vazkii.quark.base.block.IQuarkBlock;
import vazkii.quark.base.handler.MiscUtil;
import vazkii.quark.base.handler.RenderLayerHandler;
import vazkii.quark.base.handler.RenderLayerHandler.RenderTypeSkeleton;
import vazkii.quark.base.module.Module;

public class RootBlock extends VineBlock implements IQuarkBlock, Fertilizable {

	private final Module module;
	private BooleanSupplier enabledSupplier = () -> true;

	public RootBlock(Module module) {
		super(Block.Properties.of(Material.REPLACEABLE_PLANT).noCollision().ticksRandomly().strength(0.2F).sounds(BlockSoundGroup.GRASS));
		this.module = module;

		RegistryHelper.registerBlock(this, "root");
		RegistryHelper.setCreativeTab(this, ItemGroup.DECORATIONS);
		RenderLayerHandler.setRenderType(this, RenderTypeSkeleton.CUTOUT);
	}
	
	@Override
	public boolean canReplace(BlockState state, ItemPlacementContext useContext) {
		return true;
	}
	
	@Override
	public boolean isLadder(BlockState state, WorldView world, BlockPos pos, LivingEntity entity) {
		return false;
	}
	
	@Override
	public boolean isFlammable(BlockState state, BlockView world, BlockPos pos, Direction face) {
		return true;
	}
	
	@Override
	public int getFlammability(BlockState state, BlockView world, BlockPos pos, Direction face) {
		return 300;
	}
	
	@Override
	public void scheduledTick(BlockState state, ServerWorld worldIn, BlockPos pos, Random random) {
		if(!worldIn.isClient && worldIn.random.nextInt(2) == 0)
			grow(worldIn, random, pos, state);
	}
	
	public static void growMany(WorldAccess world, Random rand, BlockPos pos, BlockState state, float stopChance) {
		BlockPos next = pos;
		
		do {
			next = growAndReturnLastPos(world, next, state);
		} while(next != null && rand.nextFloat() >= stopChance);
	}

	public static BlockPos growAndReturnLastPos(WorldAccess world, BlockPos pos, BlockState state) {
		BlockPos down = pos.down();
		
		for(Direction facing : MiscUtil.HORIZONTALS) {
			BooleanProperty prop = getFacingProperty(facing);
			if(state.get(prop)) {
				BlockPos ret = growInFacing(world, down, facing);
				if(ret != null) {
					BlockState setState = state.getBlock().getDefaultState().with(prop, true);
					world.setBlockState(ret, setState, 2);
					return ret;
				}
				
				break;
			}
		}
		
		return null;
	}
	
	public static BlockPos growInFacing(WorldAccess world, BlockPos pos, Direction facing) {
		if(!world.isAir(pos))
			return null;
		
		BlockPos check = pos.offset(facing);
		if(isAcceptableNeighbor(world, check, facing.getOpposite()))
			return pos;
		
		pos = check;
		if(!world.isAir(check))
			return null;
		
		check = pos.offset(facing);
		if(isAcceptableNeighbor(world, check, facing.getOpposite()))
			return pos;
		
		return null;
	}

	public static boolean isAcceptableNeighbor(WorldAccess world, BlockPos pos, Direction side) {
		BlockState iblockstate = world.getBlockState(pos);
		return Block.isSideSolidFullSquare(iblockstate, world, pos, side) && iblockstate.getMaterial() == Material.STONE;
	}
	
	@Override
	public void addStacksForDisplay(ItemGroup group, DefaultedList<ItemStack> items) {
		if(isEnabled() || group == ItemGroup.SEARCH)
			super.addStacksForDisplay(group, items);
	}

	@Nullable
	@Override
	public Module getModule() {
		return module;
	}

	@Override
	public RootBlock setCondition(BooleanSupplier enabledSupplier) {
		this.enabledSupplier = enabledSupplier;
		return this;
	}

	@Override
	public boolean doesConditionApply() {
		return enabledSupplier.getAsBoolean();
	}

	@Override
	public boolean isFertilizable(BlockView world, BlockPos pos, BlockState state, boolean client) {
		return world.getLuminance(pos) < 7;
	}

	@Override
	public boolean canGrow(World world, Random rand, BlockPos pos, BlockState state) {
		return rand.nextFloat() < 0.4;
	}
	
	@Override
	public void grow(ServerWorld world, Random rand, BlockPos pos, BlockState state) {
		growAndReturnLastPos(world, pos, state);
	}
	
}
