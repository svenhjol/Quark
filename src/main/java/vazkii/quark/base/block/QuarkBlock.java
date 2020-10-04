package vazkii.quark.base.block;

import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.DefaultedList;
import vazkii.arl.block.BasicBlock;
import vazkii.arl.util.RegistryHelper;
import vazkii.quark.base.module.Module;

import javax.annotation.Nullable;
import java.util.function.BooleanSupplier;

public class QuarkBlock extends BasicBlock implements IQuarkBlock {
	
	private final Module module;
	private BooleanSupplier enabledSupplier = () -> true;

	public QuarkBlock(String regname, Module module, ItemGroup creativeTab, Settings properties) {
		super(regname, properties);
		this.module = module;
		
		if(creativeTab != null)
			RegistryHelper.setCreativeTab(this, creativeTab);
	}

	@Override
	public void addStacksForDisplay(ItemGroup group, DefaultedList<ItemStack> items) {
		if(isEnabled() || group == ItemGroup.SEARCH)
			super.addStacksForDisplay(group, items);
	}

	@Override
	public QuarkBlock setCondition(BooleanSupplier enabledSupplier) {
		this.enabledSupplier = enabledSupplier;
		return this;
	}

	@Override
	public boolean doesConditionApply() {
		return enabledSupplier.getAsBoolean();
	}

	@Nullable
	@Override
	public Module getModule() {
		return module;
	}

}
