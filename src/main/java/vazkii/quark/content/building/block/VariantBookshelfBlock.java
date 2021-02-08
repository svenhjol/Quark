package vazkii.quark.content.building.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.item.ItemGroup;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;
import vazkii.quark.base.block.QuarkBlock;
import vazkii.quark.base.module.QuarkModule;

public class VariantBookshelfBlock extends QuarkBlock {

	private final boolean flammable;
	
    public VariantBookshelfBlock(String type, QuarkModule module, boolean flammable) {
        super(type + "_bookshelf", module, ItemGroup.BUILDING_BLOCKS, Block.Properties.from(Blocks.BOOKSHELF));
        this.flammable = flammable;
    }
    
    @Override
    public boolean isFlammable(BlockState state, IBlockReader world, BlockPos pos, Direction face) {
    	return flammable;
    }
    
    @Override
    public float getEnchantPowerBonus(BlockState state, IWorldReader world, BlockPos pos) {
        return 1;
    }
}
