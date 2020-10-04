package vazkii.quark.automation.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Material;
import net.minecraft.block.MaterialColor;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.item.ItemGroup;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.state.StateManager.Builder;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import vazkii.quark.automation.tile.EnderWatcherTileEntity;
import vazkii.quark.base.block.QuarkBlock;
import vazkii.quark.base.module.Module;

public class EnderWatcherBlock extends QuarkBlock {
	
	public static final BooleanProperty WATCHED = BooleanProperty.of("watched");
	public static final IntProperty POWER = Properties.POWER;

	public EnderWatcherBlock(Module module) {
		super("ender_watcher", module, ItemGroup.REDSTONE, 
				Block.Properties.of(Material.METAL, MaterialColor.GREEN)
				.strength(3F, 10F)
				.sounds(BlockSoundGroup.METAL));
		
		setDefaultState(getDefaultState().with(WATCHED, false).with(POWER, 0));
	}
	
	@Override
	protected void appendProperties(Builder<Block, BlockState> builder) {
		builder.add(WATCHED, POWER);
	}

	@Override
	@SuppressWarnings("deprecation")
	public boolean emitsRedstonePower(BlockState state) {
		return true;
	}
	
	@Override
	@SuppressWarnings("deprecation")
	public int getWeakRedstonePower(BlockState blockState, BlockView blockAccess, BlockPos pos, Direction side) {
		return blockState.get(POWER);
	}
	
	@Override
	public boolean hasTileEntity(BlockState state) {
		return true;
	}
	
	@Override
	public BlockEntity createTileEntity(BlockState state, BlockView world) {
		return new EnderWatcherTileEntity();
	}

}
