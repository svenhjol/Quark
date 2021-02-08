package vazkii.quark.content.building.block;

import net.minecraft.item.ItemGroup;
import vazkii.quark.base.block.QuarkGlassBlock;
import vazkii.quark.base.handler.RenderLayerHandler;
import vazkii.quark.base.handler.RenderLayerHandler.RenderTypeSkeleton;
import vazkii.quark.base.module.QuarkModule;

public class FramedGlassBlock extends QuarkGlassBlock {

	public FramedGlassBlock(String regname, QuarkModule module, ItemGroup creativeTab, Properties properties) {
		super(regname, module, creativeTab, properties);
		
		RenderLayerHandler.setRenderType(this, RenderTypeSkeleton.CUTOUT);
	}
	
}
