package vazkii.quark.content.building.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.LadderBlock;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraftforge.common.ToolType;
import vazkii.arl.util.RegistryHelper;
import vazkii.quark.base.handler.RenderLayerHandler;
import vazkii.quark.base.handler.RenderLayerHandler.RenderTypeSkeleton;
import vazkii.quark.base.module.QuarkModule;

public class VariantLadderBlock extends LadderBlock {

	private final QuarkModule module;
	private final boolean flammable;
	
	public VariantLadderBlock(String type, QuarkModule module, Block.Properties props, boolean flammable) {
		super(props);
		
		RegistryHelper.registerBlock(this, type + "_ladder");
		RegistryHelper.setCreativeTab(this, ItemGroup.DECORATIONS);
		
		this.module = module;
		RenderLayerHandler.setRenderType(this, RenderTypeSkeleton.CUTOUT);
		
		this.flammable = flammable;
	}
	
	public VariantLadderBlock(String type, QuarkModule module, boolean flammable) {
		this(type, module, 
				Block.Properties.from(Blocks.LADDER)
				.harvestTool(ToolType.AXE), 
			flammable);
	}
	
	@Override
	public boolean isFlammable(BlockState state, IBlockReader world, BlockPos pos, Direction face) {
		return flammable;
	}
	
	@Override
	public void fillItemGroup(ItemGroup group, NonNullList<ItemStack> items) {
		if(isEnabled() || group == ItemGroup.SEARCH)
			super.fillItemGroup(group, items);
	}
	
	public boolean isEnabled() {
		return module.enabled;
	}

}
