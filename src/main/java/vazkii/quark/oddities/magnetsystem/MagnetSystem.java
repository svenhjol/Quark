package vazkii.quark.oddities.magnetsystem;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import com.google.common.collect.Streams;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.PistonBlock;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeManager;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.TickEvent.ClientTickEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.WorldTickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.registries.ForgeRegistries;
import vazkii.quark.api.IMagnetMoveAction;
import vazkii.quark.api.IMagnetTracker;
import vazkii.quark.api.QuarkCapabilities;
import vazkii.quark.base.Quark;
import vazkii.quark.base.handler.MiscUtil;
import vazkii.quark.base.module.ModuleLoader;
import vazkii.quark.oddities.module.MagnetsModule;
import vazkii.quark.oddities.tile.MagnetTileEntity;

@EventBusSubscriber(bus = Bus.FORGE, modid = Quark.MOD_ID)
public class MagnetSystem {
	
	private static HashSet<Block> magnetizableBlocks = new HashSet<>();
	
	private static final HashMap<Block, IMagnetMoveAction> BLOCK_MOVE_ACTIONS = new HashMap<>();
	
	static {
		DefaultMoveActions.addActions(BLOCK_MOVE_ACTIONS);
	}

	public static IMagnetMoveAction getMoveAction(Block block) {
		return BLOCK_MOVE_ACTIONS.get(block);
	}

    public static LazyOptional<IMagnetTracker> getCapability(World world) {
        return world.getCapability(QuarkCapabilities.MAGNET_TRACKER_CAPABILITY);
    }
	
	@SubscribeEvent
	public static void tick(WorldTickEvent event) {
		if(!ModuleLoader.INSTANCE.isModuleEnabled(MagnetsModule.class))
			return;
		
		if (event.phase == Phase.START) {
			getCapability(event.world).ifPresent(IMagnetTracker::clear);
		} else {
			if (magnetizableBlocks.isEmpty())
				loadMagnetizableBlocks(event.world);
			getCapability(event.world).ifPresent(magnetTracker -> {
				for (BlockPos pos : magnetTracker.getTrackedPositions())
					magnetTracker.actOnForces(pos);
				magnetTracker.clear();
			});
		}
	}

	@SubscribeEvent
	@Environment(EnvType.CLIENT)
	public static void tick(ClientTickEvent event) {
		if(!ModuleLoader.INSTANCE.isModuleEnabled(MagnetsModule.class))
			return;
		
		if (MinecraftClient.getInstance().world == null) {
			magnetizableBlocks.clear();
		}
	}


	public static void applyForce(World world, BlockPos pos, int magnitude, boolean pushing, Direction dir, int distance, BlockPos origin) {
		getCapability(world).ifPresent(magnetTracker ->
				magnetTracker.applyForce(pos, magnitude, pushing, dir, distance, origin));
	}
	
	public static PistonBehavior getPushAction(MagnetTileEntity magnet, BlockPos pos, BlockState state, Direction moveDir) {
		World world = magnet.getWorld();
		if(world != null && isBlockMagnetic(state)) {
			BlockPos targetLocation = pos.offset(moveDir);
			BlockState stateAtTarget = world.getBlockState(targetLocation);
			if (stateAtTarget.isAir(world, targetLocation))
				return PistonBehavior.IGNORE;
			else if (stateAtTarget.getPistonBehavior() == PistonBehavior.DESTROY)
				return PistonBehavior.DESTROY;
		}

		return PistonBehavior.BLOCK;
	}
	
	public static boolean isBlockMagnetic(BlockState state) {
		Block block = state.getBlock();

		if (block == Blocks.PISTON || block == Blocks.STICKY_PISTON) {
			if (state.get(PistonBlock.EXTENDED))
				return false;
		}
		
		return block != MagnetsModule.magnet && (magnetizableBlocks.contains(block) || BLOCK_MOVE_ACTIONS.containsKey(block) || block instanceof IMagnetMoveAction);
	}
	
	private static void loadMagnetizableBlocks(World world) {
		RecipeManager manager = world.getRecipeManager();
		if(!manager.values().isEmpty()) {
			Collection<Recipe<?>> recipes = manager.values();

			Multimap<Item, Item> recipeDigestion = HashMultimap.create();

			for(Recipe<?> recipe : recipes) {
				Item out = recipe.getOutput().getItem();

				DefaultedList<Ingredient> ingredients = recipe.getPreviewInputs();
				for(Ingredient ingredient : ingredients) {
					for (ItemStack inStack : ingredient.getMatchingStacksClient())
						recipeDigestion.put(inStack.getItem(), out);
				}
			}


			List<Item> magneticDerivationList = MiscUtil.massRegistryGet(MagnetsModule.magneticDerivationList, ForgeRegistries.ITEMS);
			List<Item> magneticWhitelist = MiscUtil.massRegistryGet(MagnetsModule.magneticWhitelist, ForgeRegistries.ITEMS);
			List<Item> magneticBlacklist = MiscUtil.massRegistryGet(MagnetsModule.magneticBlacklist, ForgeRegistries.ITEMS);
			
			Streams.concat(magneticDerivationList.stream(), magneticWhitelist.stream())
				.filter(i -> i instanceof BlockItem)
				.map(i -> ((BlockItem) i).getBlock())
				.forEach(magnetizableBlocks::add);
			
			Set<Item> scanned = Sets.newHashSet(magneticDerivationList);
			List<Item> magnetizableToScan = Lists.newArrayList(magneticDerivationList);

			while (!magnetizableToScan.isEmpty()) {
				Item scan = magnetizableToScan.remove(0);

				if (recipeDigestion.containsKey(scan)) {
					for (Item candidate : recipeDigestion.get(scan)) {
						if (!scanned.contains(candidate)) {
							scanned.add(candidate);
							magnetizableToScan.add(candidate);

							if(candidate instanceof BlockItem && !magneticBlacklist.contains(candidate))
								magnetizableBlocks.add(((BlockItem) candidate).getBlock());
						}
					}
				}
			}
		}
	}
}
