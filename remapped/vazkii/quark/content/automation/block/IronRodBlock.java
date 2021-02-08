package vazkii.quark.content.automation.block;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.EndRodBlock;
import net.minecraft.block.Material;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.state.StateManager.Builder;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.util.DyeColor;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import vazkii.arl.util.RegistryHelper;
import vazkii.quark.api.ICollateralMover;
import vazkii.quark.base.module.QuarkModule;

public class IronRodBlock extends EndRodBlock implements ICollateralMover {

	private final QuarkModule module;
	
	public static final BooleanProperty CONNECTED = BooleanProperty.of("connected");
	
	public IronRodBlock(QuarkModule module) {
		super(Block.Properties.of(Material.METAL, DyeColor.GRAY)
				.strength(5F, 10F)
				.sounds(BlockSoundGroup.METAL));
		
		RegistryHelper.registerBlock(this, "iron_rod");
		RegistryHelper.setCreativeTab(this, ItemGroup.DECORATIONS);
		
		this.module = module;
	}
	
	@Override
	public void addStacksForDisplay(ItemGroup group, DefaultedList<ItemStack> items) {
		if(module.enabled || group == ItemGroup.SEARCH)
			super.addStacksForDisplay(group, items);
	}
	
	@Override
	protected void appendProperties(Builder<Block, BlockState> builder) {
		super.appendProperties(builder);
		builder.add(CONNECTED);
	}

	@Override
	public boolean isCollateralMover(World world, BlockPos source, Direction moveDirection, BlockPos pos) {
		return moveDirection == world.getBlockState(pos).get(FACING);
	}
	
	@Override
	public MoveResult getCollateralMovement(World world, BlockPos source, Direction moveDirection, Direction side, BlockPos pos) {
		return side == moveDirection ? MoveResult.BREAK : MoveResult.SKIP;
	}
	
	@Override
	public void randomDisplayTick(BlockState stateIn, World worldIn, BlockPos pos, Random rand) {
		// NO-OP
	}


}
