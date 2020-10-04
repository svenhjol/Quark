package vazkii.quark.base.item;

import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.DefaultedList;
import vazkii.arl.item.BasicItem;
import vazkii.quark.base.module.Module;

import javax.annotation.Nonnull;
import java.util.function.BooleanSupplier;

public class QuarkItem extends BasicItem {

	private final Module module;
	private BooleanSupplier enabledSupplier = () -> true;

	public QuarkItem(String regname, Module module, Settings properties) {
		super(regname, properties);
		this.module = module;
	}

	@Override
	public void appendStacks(@Nonnull ItemGroup group, @Nonnull DefaultedList<ItemStack> items) {
		if(isEnabled() || group == ItemGroup.SEARCH)
			super.appendStacks(group, items);
	}

	public QuarkItem setCondition(BooleanSupplier enabledSupplier) {
		this.enabledSupplier = enabledSupplier;
		return this;
	}

	public boolean isEnabled() {
		return module != null && module.enabled && enabledSupplier.getAsBoolean();
	}

}
