package vazkii.quark.base.item;

import net.minecraft.entity.EntityType;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.util.collection.DefaultedList;
import vazkii.arl.util.RegistryHelper;
import vazkii.quark.base.module.Module;

import javax.annotation.Nonnull;
import java.util.function.BooleanSupplier;

public class QuarkSpawnEggItem extends SpawnEggItem {

	private final Module module;
	private BooleanSupplier enabledSupplier = () -> true;

	public QuarkSpawnEggItem(EntityType<?> type, int primaryColor, int secondaryColor, String regname, Module module, Settings properties) {
		super(type, primaryColor, secondaryColor, properties);

		RegistryHelper.registerItem(this, regname);
		this.module = module;
	}

	@Override
	public void appendStacks(@Nonnull ItemGroup group, @Nonnull DefaultedList<ItemStack> items) {
		if(isEnabled() || group == ItemGroup.SEARCH)
			super.appendStacks(group, items);
	}

	public QuarkSpawnEggItem setCondition(BooleanSupplier enabledSupplier) {
		this.enabledSupplier = enabledSupplier;
		return this;
	}


	public boolean isEnabled() {
		return module != null && module.enabled && enabledSupplier.getAsBoolean();
	}

}
