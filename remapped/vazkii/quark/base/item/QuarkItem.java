package vazkii.quark.base.item;

import java.util.function.BooleanSupplier;

import javax.annotation.Nonnull;

import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.DefaultedList;
import vazkii.arl.item.BasicItem;
import vazkii.quark.base.module.QuarkModule;

public class QuarkItem extends BasicItem implements IQuarkItem {

	private final QuarkModule module;
	private BooleanSupplier enabledSupplier = () -> true;

	public QuarkItem(String regname, QuarkModule module, Settings properties) {
		super(regname, properties);
		this.module = module;
	}

	@Override
	public void appendStacks(@Nonnull ItemGroup group, @Nonnull DefaultedList<ItemStack> items) {
		if(isEnabled() || group == ItemGroup.SEARCH)
			super.appendStacks(group, items);
	}

	@Override
	public QuarkItem setCondition(BooleanSupplier enabledSupplier) {
		this.enabledSupplier = enabledSupplier;
		return this;
	}

	@Override
	public QuarkModule getModule() {
		return module;
	}

	@Override
	public boolean doesConditionApply() {
		return enabledSupplier.getAsBoolean();
	}

}
