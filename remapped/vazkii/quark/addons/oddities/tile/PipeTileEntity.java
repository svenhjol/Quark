package vazkii.quark.addons.oddities.tile;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Random;

import javax.annotation.Nonnull;

import com.google.common.base.Predicate;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.Tickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.wrapper.InvWrapper;
import net.minecraftforge.items.wrapper.SidedInvWrapper;
import vazkii.arl.block.tile.TileSimpleInventory;
import vazkii.quark.addons.oddities.block.PipeBlock;
import vazkii.quark.addons.oddities.module.PipesModule;
import vazkii.quark.base.client.handler.NetworkProfilingHandler;
import vazkii.quark.base.handler.MiscUtil;
import vazkii.quark.base.handler.QuarkSounds;

public class PipeTileEntity extends TileSimpleInventory implements Tickable {

	public PipeTileEntity() {
		super(PipesModule.tileEntityType);
	}
	
	private static final String TAG_PIPE_ITEMS = "pipeItems";
	
	private boolean iterating = false;
	public final List<PipeItem> pipeItems = new LinkedList<>();
	public final List<PipeItem> queuedItems = new LinkedList<>();
	
	private boolean skipSync = false;

	public static boolean isTheGoodDay(World world) {
		Calendar calendar = Calendar.getInstance();
		return calendar.get(Calendar.MONTH) + 1 == 4 && calendar.get(Calendar.DAY_OF_MONTH) == 1;
	}

	@Override
	public void tick() {
		boolean enabled = isPipeEnabled();
		if(!enabled && world.getTime() % 10 == 0 && world instanceof ServerWorld) 
			((ServerWorld) world).spawnParticles(new DustParticleEffect(1.0F, 0.0F, 0.0F, 1.0F), pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 3, 0.2, 0.2, 0.2, 0);

		BlockState blockAt = world.getBlockState(pos);
		if(!world.isClient && enabled && blockAt.getBlock() instanceof PipeBlock) {
			for(Direction side : Direction.values()) {
				if(getConnectionTo(world, pos, side) == ConnectionType.OPENING) {
					double minX = pos.getX() + 0.25 + 0.5 * Math.min(0, side.getOffsetX());
					double minY = pos.getY() + 0.25 + 0.5 * Math.min(0, side.getOffsetY());
					double minZ = pos.getZ() + 0.25 + 0.5 * Math.min(0, side.getOffsetZ());
					double maxX = pos.getX() + 0.75 + 0.5 * Math.max(0, side.getOffsetX());
					double maxY = pos.getY() + 0.75 + 0.5 * Math.max(0, side.getOffsetY());
					double maxZ = pos.getZ() + 0.75 + 0.5 * Math.max(0, side.getOffsetZ());

					Direction opposite = side.getOpposite();

					boolean pickedItemsUp = false;
					Predicate<ItemEntity> predicate = entity -> {
						if(entity == null || !entity.isAlive())
							return false;
						
						Vec3d motion = entity.getVelocity();
						Direction dir = Direction.getFacing(motion.x, motion.y, motion.z);
						
						return dir == opposite;
					};
					
					for (ItemEntity item : world.getEntitiesByClass(ItemEntity.class, new Box(minX, minY, minZ, maxX, maxY, maxZ), predicate)) {
						passIn(item.getStack().copy(), side);
						
						if (PipesModule.doPipesWhoosh) { 
							if (isTheGoodDay(world))
								world.playSound(null, item.getX(), item.getY(), item.getZ(), QuarkSounds.BLOCK_PIPE_PICKUP_LENNY, SoundCategory.BLOCKS, 1f, 1f);
							else
								world.playSound(null, item.getX(), item.getY(), item.getZ(), QuarkSounds.BLOCK_PIPE_PICKUP, SoundCategory.BLOCKS, 1f, 1f);
						}

						pickedItemsUp = true;
						item.remove();
					}

					if(pickedItemsUp)
						sync();
				}
			}
		}

		int currentOut = getComparatorOutput();

		if(!pipeItems.isEmpty()) {
			if(PipesModule.maxPipeItems > 0 && pipeItems.size() > PipesModule.maxPipeItems && !world.isClient) {
				world.syncWorldEvent(2001, pos, Block.getRawIdFromState(world.getBlockState(pos)));
				dropItem(new ItemStack(getCachedState().getBlock()));
				world.removeBlock(getPos(), false);
			}

			ListIterator<PipeItem> itemItr = pipeItems.listIterator();
			iterating = true;
			while(itemItr.hasNext()) {
				PipeItem item = itemItr.next();
				Direction lastFacing = item.outgoingFace;
				if(item.tick(this)) {
					itemItr.remove();

					if (item.valid)
						passOut(item);
					else {
						dropItem(item.stack, lastFacing, true);
					}
				}
			}
			iterating = false;

			pipeItems.addAll(queuedItems);
			queuedItems.clear();
		}

		if(getComparatorOutput() != currentOut)
			world.updateComparators(getPos(), getCachedState().getBlock());
	}

	public int getComparatorOutput() {
		return Math.min(15, pipeItems.size());
	}

	public Iterator<PipeItem> getItemIterator() {
		return pipeItems.iterator(); 
	}

	public boolean passIn(ItemStack stack, Direction face, Direction backlog, long seed, int time) {
		PipeItem item = new PipeItem(stack, face, seed);
		item.backloggedFace = backlog;
		if(!iterating) {
			int currentOut = getComparatorOutput();
			pipeItems.add(item);
			item.timeInWorld = time;
			if(getComparatorOutput() != currentOut)
				world.updateComparators(getPos(), getCachedState().getBlock());
		} else queuedItems.add(item);

		return true;
	}

	public boolean passIn(ItemStack stack, Direction face) {
		return passIn(stack, face, null, world.random.nextLong(), 0);
	}

	protected void passOut(PipeItem item) {
		BlockPos targetPos = getPos().offset(item.outgoingFace);
		BlockEntity tile = world.getBlockEntity(targetPos);
		boolean did = false;
		if(tile != null) {
			if(tile instanceof PipeTileEntity)
				did = ((PipeTileEntity) tile).passIn(item.stack, item.outgoingFace.getOpposite(), null, item.rngSeed, item.timeInWorld);
			else {
				ItemStack result = putIntoInv(item.stack, tile, item.outgoingFace.getOpposite(), false);
				if(result.getCount() != item.stack.getCount()) {
					did = true;
					if(!result.isEmpty())
						bounceBack(item, result);
				}
			}
		}

		if(!did)
			bounceBack(item, null);
	}

	private void bounceBack(PipeItem item, ItemStack stack) {
		if(!world.isClient) {
			passIn(stack == null ? item.stack : stack, item.outgoingFace, item.incomingFace, item.rngSeed, item.timeInWorld);
			sync();
		}
	}

	public void dropItem(ItemStack stack) {
		dropItem(stack, null, false);
	}

	public void dropItem(ItemStack stack, Direction facing, boolean playSound) {
		if(!world.isClient) {
			double posX = pos.getX() + 0.5;
			double posY = pos.getY() + 0.25;
			double posZ = pos.getZ() + 0.5;

			if (facing != null) {
				posX -= facing.getOffsetX() * 0.4;
				posY -= facing.getOffsetY() * 0.65;
				posZ -= facing.getOffsetZ() * 0.4;
			}

			boolean shootOut = isPipeEnabled();

			float pitch = 1f;
			if (!shootOut)
				pitch = 0.025f;

			if (playSound && PipesModule.doPipesWhoosh) { 
				if (isTheGoodDay(world))
					world.playSound(null, posX, posY, posZ, QuarkSounds.BLOCK_PIPE_SHOOT_LENNY, SoundCategory.BLOCKS, 1f, pitch);
				else
					world.playSound(null, posX, posY, posZ, QuarkSounds.BLOCK_PIPE_SHOOT, SoundCategory.BLOCKS, 1f, pitch);
			}

			ItemEntity entity = new ItemEntity(world, posX, posY, posZ, stack);
			entity.setToDefaultPickupDelay();

			double velocityMod = 0.5;
			if (!shootOut)
				velocityMod = 0.125;

			if (facing != null) {
				double mx = -facing.getOffsetX() * velocityMod;
				double my = -facing.getOffsetY() * velocityMod;
				double mz = -facing.getOffsetZ() * velocityMod;
				entity.setVelocity(mx, my, mz);
			}
			
			world.spawnEntity(entity);
		}
	}
	
	@Override
	public void onDataPacket(ClientConnection net, BlockEntityUpdateS2CPacket packet) {
		super.onDataPacket(net, packet);
		NetworkProfilingHandler.receive("pipe");
	}

	public void dropAllItems() {
		for(PipeItem item : pipeItems)
			dropItem(item.stack);
		pipeItems.clear();
	}

	@Override
	public void readSharedNBT(CompoundTag cmp) {
		skipSync = true;
		super.readSharedNBT(cmp);
		skipSync = false;

		ListTag pipeItemList = cmp.getList(TAG_PIPE_ITEMS, cmp.getType());
		pipeItems.clear();
		pipeItemList.forEach(listCmp -> {
			PipeItem item = PipeItem.readFromNBT((CompoundTag) listCmp);
			pipeItems.add(item);
		});
	}

	@Override
	public void writeSharedNBT(CompoundTag cmp) {
		super.writeSharedNBT(cmp);

		ListTag pipeItemList = new ListTag();
		for(PipeItem item : pipeItems) {
			CompoundTag listCmp = new CompoundTag();
			item.writeToNBT(listCmp);
			pipeItemList.add(listCmp);
		}
		cmp.put(TAG_PIPE_ITEMS, pipeItemList);
	}

	protected boolean canFit(ItemStack stack, BlockPos pos, Direction face) {
		BlockEntity tile = world.getBlockEntity(pos);
		if(tile == null)
			return false;

		if(tile instanceof PipeTileEntity)
			return ((PipeTileEntity) tile).isPipeEnabled();
		else {
			ItemStack result = putIntoInv(stack, tile, face, true);
			return result.isEmpty();
		}
	}

	protected boolean isPipeEnabled() {
		BlockState state = world.getBlockState(pos);
		return state.getBlock() instanceof PipeBlock && !world.isReceivingRedstonePower(pos);
	}

	protected ItemStack putIntoInv(ItemStack stack, BlockEntity tile, Direction face, boolean simulate) {
		IItemHandler handler = null;
		
		LazyOptional<IItemHandler> opt = tile.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, face); 
		if(opt.isPresent())
			handler = opt.orElse(null);
		else if(tile instanceof SidedInventory)
			handler = new SidedInvWrapper((SidedInventory) tile, face);
		else if(tile instanceof Inventory)
			handler = new InvWrapper((Inventory) tile);

		if(handler != null)
			return simulate ? ItemStack.EMPTY : ItemHandlerHelper.insertItem(handler, stack, simulate);
		return stack;
	}

	@Override
	public boolean canInsert(int index, @Nonnull ItemStack itemStackIn, @Nonnull Direction direction) {
		return index == direction.ordinal() && isPipeEnabled();
	}

	@Override
	public void setStack(int i, @Nonnull ItemStack itemstack) {
		if(!itemstack.isEmpty()) {
			Direction side = Direction.values()[i];
			passIn(itemstack, side);
			
			if(!world.isClient && !skipSync)
				sync();
		}
	}

	@Override
	public int size() {
		return 6;
	}

	@Override
	protected boolean needsToSyncInventory() {
		return true;
	}
	
	@Override
	public void sync() {
		MiscUtil.syncTE(this);
	}


	public static ConnectionType getConnectionTo(BlockView world, BlockPos pos, Direction face) {
		return getConnectionTo(world, pos, face, false);
	}
	
	private static ConnectionType getConnectionTo(BlockView world, BlockPos pos, Direction face, boolean recursed) {
		BlockPos truePos = pos.offset(face);
		BlockEntity tile = world.getBlockEntity(truePos);
		
		if(tile != null) {
			if(tile instanceof PipeTileEntity)
				return ConnectionType.PIPE;
			else if(tile instanceof Inventory || tile.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, face.getOpposite()).isPresent())
				return tile instanceof ChestBlockEntity ? ConnectionType.TERMINAL_OFFSET : ConnectionType.TERMINAL;
		}
		
		checkSides: if(!recursed) {
			ConnectionType other = getConnectionTo(world, pos, face.getOpposite(), true);
			if(other.isSolid) {
				for(Direction d : Direction.values())
					if(d.getAxis() != face.getAxis()) {
						other = getConnectionTo(world, pos, d, true);
						if(other.isSolid)
							break checkSides;
					}
				
				return ConnectionType.OPENING;
			}
		}

		return ConnectionType.NONE;
	}
	
	public static class PipeItem {

		private static final String TAG_TICKS = "ticksInPipe";
		private static final String TAG_INCOMING = "incomingFace";
		private static final String TAG_OUTGOING = "outgoingFace";
		private static final String TAG_BACKLOGGED = "backloggedFace";
		private static final String TAG_RNG_SEED = "rngSeed";
		private static final String TAG_TIME_IN_WORLD = "timeInWorld";

		private static final List<Direction> HORIZONTAL_SIDES_LIST = Arrays.asList(MiscUtil.HORIZONTALS);

		public final ItemStack stack;
		public int ticksInPipe;
		public final Direction incomingFace;
		public Direction outgoingFace;
		public Direction backloggedFace;
		public long rngSeed;
		public int timeInWorld = 0;
		public boolean valid = true;

		public PipeItem(ItemStack stack, Direction face, long rngSeed) {
			this.stack = stack;
			ticksInPipe = 0;
			incomingFace = outgoingFace = face;
			this.rngSeed = rngSeed;
		}

		protected boolean tick(PipeTileEntity pipe) {
			ticksInPipe++;
			timeInWorld++;

			if(ticksInPipe == PipesModule.effectivePipeSpeed / 2 - 1) {
				Direction target = getTargetFace(pipe);
				outgoingFace = target;
			}

			if(outgoingFace == null) {
				valid = false;
				return true;
			}

			return ticksInPipe >= PipesModule.effectivePipeSpeed;
		}

		protected Direction getTargetFace(PipeTileEntity pipe) {
			BlockPos pipePos = pipe.getPos();
			if(incomingFace != Direction.DOWN && backloggedFace != Direction.DOWN && pipe.canFit(stack, pipePos.offset(Direction.DOWN), Direction.UP))
				return Direction.DOWN;

			Direction incomingOpposite = incomingFace; // init as same so it doesn't break in the remove later
			if(incomingFace.getAxis() != Axis.Y) {
				incomingOpposite = incomingFace.getOpposite();
				if(incomingOpposite != backloggedFace && pipe.canFit(stack, pipePos.offset(incomingOpposite), incomingFace))
					return incomingOpposite;
			}

			List<Direction> sides = new ArrayList<>(HORIZONTAL_SIDES_LIST);
			sides.remove(incomingFace);
			sides.remove(incomingOpposite);

			Random rng = new Random(rngSeed);
			rngSeed = rng.nextLong();
			Collections.shuffle(sides, rng);
			for(Direction side : sides) {
				if(side != backloggedFace && pipe.canFit(stack, pipePos.offset(side), side.getOpposite()))
					return side;
			}

			if(incomingFace != Direction.UP && backloggedFace != Direction.UP && pipe.canFit(stack, pipePos.offset(Direction.UP), Direction.DOWN))
				return Direction.UP;

			if(backloggedFace != null)
				return backloggedFace;
			
			return null;
		}

		public float getTimeFract(float partial) {
			return (ticksInPipe + partial) / PipesModule.effectivePipeSpeed;
		}

		public void writeToNBT(CompoundTag cmp) {
			stack.toTag(cmp);
			cmp.putInt(TAG_TICKS, ticksInPipe);
			cmp.putInt(TAG_INCOMING, incomingFace.ordinal());
			cmp.putInt(TAG_OUTGOING, outgoingFace.ordinal());
			cmp.putInt(TAG_BACKLOGGED, backloggedFace != null ? backloggedFace.ordinal() : -1);
			cmp.putLong(TAG_RNG_SEED, rngSeed);
			cmp.putInt(TAG_TIME_IN_WORLD, timeInWorld);
		}

		public static PipeItem readFromNBT(CompoundTag cmp) {
			ItemStack stack = ItemStack.fromTag(cmp);
			Direction inFace = Direction.values()[cmp.getInt(TAG_INCOMING)];
			long rngSeed = cmp.getLong(TAG_RNG_SEED);
			
			PipeItem item = new PipeItem(stack, inFace, rngSeed);
			item.ticksInPipe = cmp.getInt(TAG_TICKS);
			item.outgoingFace = Direction.values()[cmp.getInt(TAG_OUTGOING)];
			item.timeInWorld = cmp.getInt(TAG_TIME_IN_WORLD);
			
			int backloggedId = cmp.getInt(TAG_BACKLOGGED);
			item.backloggedFace = backloggedId == -1 ? null : Direction.values()[backloggedId];
			
			return item;
		}

	}

	public enum ConnectionType {

		NONE(false, false, false, 0),
		PIPE(true, true, false, 0),
		OPENING(false, true, true, -0.125),
		TERMINAL(true, true, true, 0.125),
		TERMINAL_OFFSET(true, true, true, 0.1875);

		ConnectionType(boolean isSolid, boolean allowsItems, boolean isFlared, double flareShift) {
			this.isSolid = isSolid;
			this.allowsItems = allowsItems;
			this.isFlared = isFlared;
			this.flareShift = flareShift;
		}

		public final boolean isSolid, allowsItems, isFlared;
		public final double flareShift;

	}
	
}

