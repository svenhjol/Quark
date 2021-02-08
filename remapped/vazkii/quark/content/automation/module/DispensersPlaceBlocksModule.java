package vazkii.quark.content.automation.module;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.annotation.Nonnull;

import com.google.common.collect.Lists;

import net.minecraft.block.Block;
import net.minecraft.block.DispenserBlock;
import net.minecraft.block.SlabBlock;
import net.minecraft.block.StairsBlock;
import net.minecraft.block.dispenser.DispenserBehavior;
import net.minecraft.block.dispenser.FallibleItemDispenserBehavior;
import net.minecraft.item.AutomaticItemPlacementContext;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPointer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.world.World;
import net.minecraftforge.registries.ForgeRegistries;
import vazkii.quark.base.module.LoadModule;
import vazkii.quark.base.module.ModuleCategory;
import vazkii.quark.base.module.QuarkModule;
import vazkii.quark.base.module.config.Config;

@LoadModule(category = ModuleCategory.AUTOMATION)
public class DispensersPlaceBlocksModule extends QuarkModule {

	@Config public static List<String> blacklist = Lists.newArrayList("minecraft:water", "minecraft:lava", "minecraft:fire");

	@Override
	public void setup() {
		if(!enabled)
			return;
		
		BlockBehaviour behavior = new BlockBehaviour();
		
		enqueue(() -> {
			Map<Item, DispenserBehavior> registry = DispenserBlock.BEHAVIORS;
			for(Block b : ForgeRegistries.BLOCKS) {
				Identifier res = b.getRegistryName();
				if(!blacklist.contains(Objects.toString(res))) {
					Item item = b.asItem();
					if(item instanceof BlockItem && !registry.containsKey(item))
						registry.put(item, behavior);
				}
			}
		});
	}

	public static class BlockBehaviour extends FallibleItemDispenserBehavior {

		@Nonnull
		@Override
		public ItemStack dispenseSilently(BlockPointer source, ItemStack stack) {
			success = false;

			Direction direction = source.getBlockState().get(DispenserBlock.FACING);
			Direction against = direction;
			BlockPos pos = source.getBlockPos().offset(direction);

			BlockItem item = (BlockItem) stack.getItem();
			Block block = item.getBlock();
			if(block instanceof StairsBlock && direction.getAxis() != Axis.Y)
				direction = direction.getOpposite();
			else if(block instanceof SlabBlock)
				against = Direction.UP;

			success = item.place(new NotStupidDirectionalPlaceContext(source.getWorld(), pos, direction, stack, against)) == ActionResult.SUCCESS;

			return stack;
		}

	}

	// DirectionPlaceContext results in infinite loops when using slabs
	private static class NotStupidDirectionalPlaceContext extends AutomaticItemPlacementContext {

		protected boolean replaceClicked = true;
		protected Direction direction;

		public NotStupidDirectionalPlaceContext(World worldIn, BlockPos p_i50051_2_, Direction p_i50051_3_, ItemStack p_i50051_4_, Direction against) {
			super(worldIn, p_i50051_2_, p_i50051_3_, p_i50051_4_, against);
			replaceClicked = worldIn.getBlockState(getHitResult().getBlockPos()).canReplace(this); // func_242401_i = getRayTraceResult
			direction = p_i50051_3_;
		}

		@Override
		public boolean canPlace() {
			return canReplaceExisting;
		}

		@Override
		public Direction getPlayerLookDirection() {
			return direction.getOpposite();
		}

	}

}
