package vazkii.quark.content.automation.block;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.EndRodBlock;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.item.DyeColor;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.util.Direction;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import vazkii.arl.util.RegistryHelper;
import vazkii.quark.api.ICollateralMover;
import vazkii.quark.base.module.QuarkModule;

public class IronRodBlock extends EndRodBlock implements ICollateralMover {

	private final QuarkModule module;
	
	public static final BooleanProperty CONNECTED = BooleanProperty.create("connected");
	
	public IronRodBlock(QuarkModule module) {
		super(Block.Properties.create(Material.IRON, DyeColor.GRAY)
				.hardnessAndResistance(5F, 10F)
				.sound(SoundType.METAL));
		
		RegistryHelper.registerBlock(this, "iron_rod");
		RegistryHelper.setCreativeTab(this, ItemGroup.DECORATIONS);
		
		this.module = module;
	}
	
	@Override
	public void fillItemGroup(ItemGroup group, NonNullList<ItemStack> items) {
		if(module.enabled || group == ItemGroup.SEARCH)
			super.fillItemGroup(group, items);
	}
	
	@Override
	protected void fillStateContainer(Builder<Block, BlockState> builder) {
		super.fillStateContainer(builder);
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
	public void animateTick(BlockState stateIn, World worldIn, BlockPos pos, Random rand) {
		// NO-OP
	}


}
