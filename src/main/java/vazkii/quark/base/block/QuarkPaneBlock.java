package vazkii.quark.base.block;

import java.util.function.BooleanSupplier;

import javax.annotation.Nullable;

import net.minecraft.block.Block;
import net.minecraft.block.PaneBlock;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import vazkii.arl.util.RegistryHelper;
import vazkii.quark.base.handler.RenderLayerHandler;
import vazkii.quark.base.handler.RenderLayerHandler.RenderTypeSkeleton;
import vazkii.quark.base.module.QuarkModule;

public class QuarkPaneBlock extends PaneBlock implements IQuarkBlock {
	
	public final QuarkModule module;
	private BooleanSupplier enabledSupplier = () -> true;

	public QuarkPaneBlock(String name, QuarkModule module, Block.Properties properties, RenderTypeSkeleton renderType) {
		super(properties);

		this.module = module;
		RegistryHelper.registerBlock(this, name);
		RegistryHelper.setCreativeTab(this, ItemGroup.DECORATIONS);
		
		if(renderType != null)
			RenderLayerHandler.setRenderType(this, renderType);
	}

	@Override
	public void fillItemGroup(ItemGroup group, NonNullList<ItemStack> items) {	
		if(group == ItemGroup.SEARCH || isEnabled())
			super.fillItemGroup(group, items);
	}

	@Nullable
	@Override
	public QuarkModule getModule() {
		return module;
	}

	@Override
	public QuarkPaneBlock setCondition(BooleanSupplier enabledSupplier) {
		this.enabledSupplier = enabledSupplier;
		return this;
	}

	@Override
	public boolean doesConditionApply() {
		return enabledSupplier.getAsBoolean();
	}


}