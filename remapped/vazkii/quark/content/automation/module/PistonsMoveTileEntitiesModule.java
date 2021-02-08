package vazkii.quark.content.automation.module;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import org.apache.commons.lang3.tuple.Pair;

import com.google.common.collect.Lists;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.JukeboxBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.enums.ChestType;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.WorldTickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import vazkii.quark.api.IPistonCallback;
import vazkii.quark.api.QuarkCapabilities;
import vazkii.quark.base.module.LoadModule;
import vazkii.quark.base.module.QuarkModule;
import vazkii.quark.base.module.ModuleCategory;
import vazkii.quark.base.module.ModuleLoader;
import vazkii.quark.base.module.config.Config;

@LoadModule(category = ModuleCategory.AUTOMATION, hasSubscriptions = true)
public class PistonsMoveTileEntitiesModule extends QuarkModule {

	private static final WeakHashMap<World, Map<BlockPos, CompoundTag>> movements = new WeakHashMap<>();
	private static final WeakHashMap<World, List<Pair<BlockPos, CompoundTag>>> delayedUpdates = new WeakHashMap<>();

	@Config
	public static List<String> renderBlacklist = Lists.newArrayList("psi:programmer", "botania:starfield");
	@Config
	public static List<String> movementBlacklist = Lists.newArrayList("minecraft:spawner", "integrateddynamics:cable", "randomthings:blockbreaker", "minecraft:ender_chest", "minecraft:enchanting_table", "minecraft:trapped_chest", "quark:spruce_trapped_chest", "quark:birch_trapped_chest", "quark:jungle_trapped_chest", "quark:acacia_trapped_chest", "quark:dark_oak_trapped_chest", "endergetic:bolloom_bud");
	@Config
	public static List<String> delayedUpdateList = Lists.newArrayList("minecraft:dispenser", "minecraft:dropper");

	@SubscribeEvent
	public void onWorldTick(WorldTickEvent event) {
		if (!delayedUpdates.containsKey(event.world) || event.phase == Phase.START)
			return;

		List<Pair<BlockPos, CompoundTag>> delays = delayedUpdates.get(event.world);
		if (delays.isEmpty())
			return;

		for (Pair<BlockPos, CompoundTag> delay : delays) {
			BlockPos pos = delay.getLeft();
			BlockState state = event.world.getBlockState(pos);
			BlockEntity tile = BlockEntity.createFromTag(state, delay.getRight());
			
			event.world.setBlockEntity(pos, tile);
			event.world.updateComparators(pos, state.getBlock());
			if (tile != null)
				tile.resetBlock();
		}

		delays.clear();
	}

	// This is called from injected code and subsequently flipped, so to make it move, we return false
	public static boolean shouldMoveTE(boolean te, BlockState state) {
		if (!ModuleLoader.INSTANCE.isModuleEnabled(PistonsMoveTileEntitiesModule.class))
			return te;

		return shouldMoveTE(state);
	}

	public static boolean shouldMoveTE(BlockState state) {
		// Jukeboxes that are playing can't be moved so the music can be stopped
		if (state.getEntries().containsKey(JukeboxBlock.HAS_RECORD) && state.get(JukeboxBlock.HAS_RECORD))
			return true;

		if (state.getBlock() == Blocks.PISTON_HEAD)
			return true;

		Identifier res = state.getBlock().getRegistryName();
		return res == null || PistonsMoveTileEntitiesModule.movementBlacklist.contains(res.toString()) || PistonsMoveTileEntitiesModule.movementBlacklist.contains(res.getNamespace());
	}

	public static void detachTileEntities(World world, PistonHandler helper, Direction facing, boolean extending) {
		if (!ModuleLoader.INSTANCE.isModuleEnabled(PistonsMoveTileEntitiesModule.class))
			return;

		if (!extending)
			facing = facing.getOpposite();

		List<BlockPos> moveList = helper.getMovedBlocks();

		for (BlockPos pos : moveList) {
			BlockState state = world.getBlockState(pos);
			if (state.getBlock().hasTileEntity(state)) {
				BlockEntity tile = world.getBlockEntity(pos);
				if (tile != null) {
					if (hasCallback(tile))
						getCallback(tile).onPistonMovementStarted();

					world.removeBlockEntity(pos);

					registerMovement(world, pos.offset(facing), tile);
				}
			}
		}
	}

	@SuppressWarnings("deprecation")
	public static boolean setPistonBlock(World world, BlockPos pos, BlockState state, int flags) {
		if (!ModuleLoader.INSTANCE.isModuleEnabled(PistonsMoveTileEntitiesModule.class)) {
			world.setBlockState(pos, state, flags);
			return false;
		}

		if (state.getEntries().containsKey(ChestBlock.CHEST_TYPE))
			state = state.with(ChestBlock.CHEST_TYPE, ChestType.SINGLE);

		Block block = state.getBlock();
		BlockEntity tile = getAndClearMovement(world, pos);
		boolean destroyed = false;

		if (tile != null) {
			BlockState currState = world.getBlockState(pos);
			BlockEntity currTile = world.getBlockEntity(pos);

			world.removeBlock(pos, false);
			if (!block.canPlaceAt(state, world, pos)) {
				world.setBlockState(pos, state, flags);
				world.setBlockEntity(pos, tile);
				Block.dropStacks(state, world, pos, tile);
				world.removeBlock(pos, false);
				destroyed = true;
			}

			if (!destroyed) {
				world.setBlockState(pos, currState);
				world.setBlockEntity(pos, currTile);
			}
		}

		if (!destroyed) {
			world.setBlockState(pos, state, flags);
			if (world.getBlockEntity(pos) != null)
				world.setBlockState(pos, state, 0);

			if (tile != null && !world.isClient) {
				if (delayedUpdateList.contains(block.getRegistryName().toString()))
					registerDelayedUpdate(world, pos, tile);
				else {
					world.setBlockEntity(pos, tile);
					world.getChunk(pos).setBlockEntity(pos, tile);
					tile.resetBlock();

				}
			}
			world.updateNeighborsAlways(pos, block);
		}

		return false; // the value is popped, doesn't matter what we return
	}

	private static void registerMovement(World world, BlockPos pos, BlockEntity tile) {
		if (!movements.containsKey(world))
			movements.put(world, new HashMap<>());

		movements.get(world).put(pos, tile.serializeNBT());
	}

	public static BlockEntity getMovement(World world, BlockPos pos) {
		return getMovement(world, pos, false);
	}

	private static BlockEntity getMovement(World world, BlockPos pos, boolean remove) {
		if (!movements.containsKey(world))
			return null;

		Map<BlockPos, CompoundTag> worldMovements = movements.get(world);
		if (!worldMovements.containsKey(pos))
			return null;

		CompoundTag ret = worldMovements.get(pos);
		if (remove)
			worldMovements.remove(pos);

		return BlockEntity.createFromTag(world.getBlockState(pos), ret);
	}

	private static BlockEntity getAndClearMovement(World world, BlockPos pos) {
		BlockEntity tile = getMovement(world, pos, true);

		if (tile != null) {
			if (hasCallback(tile))
				getCallback(tile).onPistonMovementFinished();

			tile.setLocation(world, pos);
			tile.cancelRemoval();
		}

		return tile;
	}

	private static void registerDelayedUpdate(World world, BlockPos pos, BlockEntity tile) {
		if (!delayedUpdates.containsKey(world))
			delayedUpdates.put(world, new ArrayList<>());

		delayedUpdates.get(world).add(Pair.of(pos, tile.serializeNBT()));
	}

	@SuppressWarnings("ConstantConditions")
	private static boolean hasCallback(BlockEntity tile) {
		return tile.getCapability(QuarkCapabilities.PISTON_CALLBACK).isPresent();
	}

	@SuppressWarnings("ConstantConditions")
	private static IPistonCallback getCallback(BlockEntity tile) {
		return tile.getCapability(QuarkCapabilities.PISTON_CALLBACK).orElse(() -> {});
	}

}
