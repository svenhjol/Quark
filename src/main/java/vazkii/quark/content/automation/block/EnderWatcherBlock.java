package vazkii.quark.content.automation.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.item.ItemGroup;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.IntegerProperty;
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import vazkii.quark.base.block.QuarkBlock;
import vazkii.quark.base.module.QuarkModule;
import vazkii.quark.content.automation.tile.EnderWatcherTileEntity;

public class EnderWatcherBlock extends QuarkBlock {
	
	public static final BooleanProperty WATCHED = BooleanProperty.create("watched");
	public static final IntegerProperty POWER = BlockStateProperties.POWER_0_15;

	public EnderWatcherBlock(QuarkModule module) {
		super("ender_watcher", module, ItemGroup.REDSTONE, 
				Block.Properties.create(Material.IRON, MaterialColor.GREEN)
				.hardnessAndResistance(3F, 10F)
				.sound(SoundType.METAL));
		
		setDefaultState(getDefaultState().with(WATCHED, false).with(POWER, 0));
	}
	
	@Override
	protected void fillStateContainer(Builder<Block, BlockState> builder) {
		builder.add(WATCHED, POWER);
	}

	@Override
	@SuppressWarnings("deprecation")
	public boolean canProvidePower(BlockState state) {
		return true;
	}
	
	@Override
	@SuppressWarnings("deprecation")
	public int getWeakPower(BlockState blockState, IBlockReader blockAccess, BlockPos pos, Direction side) {
		return blockState.get(POWER);
	}
	
	@Override
	public boolean hasTileEntity(BlockState state) {
		return true;
	}
	
	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
		return new EnderWatcherTileEntity();
	}

}
