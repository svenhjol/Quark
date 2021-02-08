package vazkii.quark.content.building.module;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.DispenserBlock;
import net.minecraft.block.Material;
import net.minecraft.block.MaterialColor;
import net.minecraft.block.dispenser.FallibleItemDispenserBehavior;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.math.BlockPointer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import vazkii.quark.base.module.LoadModule;
import vazkii.quark.base.module.QuarkModule;
import vazkii.quark.base.module.ModuleCategory;
import vazkii.quark.base.module.config.Config;
import vazkii.quark.content.building.block.RopeBlock;

import javax.annotation.Nonnull;

@LoadModule(category = ModuleCategory.BUILDING)
public class RopeModule extends QuarkModule {

	public static Block rope;

	@Config(description = "Set to true to allow ropes to move Tile Entities even if Pistons Push TEs is disabled.\nNote that ropes will still use the same blacklist.")
	public static boolean forceEnableMoveTileEntities = false;

	@Config
	public static boolean enableDispenserBehavior = true;

	@Override
	public void construct() {
		rope = new RopeBlock("rope", this, ItemGroup.DECORATIONS,
				Block.Properties.of(Material.WOOL, MaterialColor.BROWN)
						.strength(0.5f)
						.sounds(BlockSoundGroup.WOOL));
	}
	
	@Override
	public void configChanged() {
		if(enableDispenserBehavior)
			DispenserBlock.BEHAVIORS.put(rope.asItem(), new BehaviourRope());
		else
			DispenserBlock.BEHAVIORS.remove(rope.asItem());
	}
	
	public static class BehaviourRope extends FallibleItemDispenserBehavior {
		
		@Nonnull
		@Override
		protected ItemStack dispenseSilently(BlockPointer source, ItemStack stack) {
			Direction facing = source.getBlockState().get(DispenserBlock.FACING);
			BlockPos pos = source.getBlockPos().offset(facing);
			World world = source.getWorld();
			this.success = false;
			
			BlockState state = world.getBlockState(pos);
			if(state.getBlock() == rope) {
				if(((RopeBlock) rope).pullDown(world, pos)) {
					this.success = true;
					stack.decrement(1);
					return stack;
				}
			} else if(world.isAir(pos) && rope.getDefaultState().canPlaceAt(world, pos)) {
				BlockSoundGroup soundtype = rope.getSoundType(state, world, pos, null);
				world.setBlockState(pos, rope.getDefaultState());
				world.playSound(null, pos, soundtype.getPlaceSound(), SoundCategory.BLOCKS, (soundtype.getVolume() + 1.0F) / 2.0F, soundtype.getPitch() * 0.8F);
				this.success = true;
				stack.decrement(1);
				
				return stack;
			}
			
			return stack;
		}
		
	}
	
}
