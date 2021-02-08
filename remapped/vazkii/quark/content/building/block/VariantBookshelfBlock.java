package vazkii.quark.content.building.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.item.ItemGroup;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import net.minecraft.world.WorldView;
import vazkii.quark.base.block.QuarkBlock;
import vazkii.quark.base.module.QuarkModule;

public class VariantBookshelfBlock extends QuarkBlock {

	private final boolean flammable;
	
    public VariantBookshelfBlock(String type, QuarkModule module, boolean flammable) {
        super(type + "_bookshelf", module, ItemGroup.BUILDING_BLOCKS, Block.Properties.copy(Blocks.BOOKSHELF));
        this.flammable = flammable;
    }
    
    @Override
    public boolean isFlammable(BlockState state, BlockView world, BlockPos pos, Direction face) {
    	return flammable;
    }
    
    @Override
    public float getEnchantPowerBonus(BlockState state, WorldView world, BlockPos pos) {
        return 1;
    }
}
