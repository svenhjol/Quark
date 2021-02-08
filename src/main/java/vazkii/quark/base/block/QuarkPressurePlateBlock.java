package vazkii.quark.base.block;

import net.minecraft.block.AbstractPressurePlateBlock;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import vazkii.arl.util.RegistryHelper;
import vazkii.quark.base.module.QuarkModule;

import javax.annotation.Nullable;
import java.util.function.BooleanSupplier;

/**
 * @author WireSegal
 * Created at 9:41 PM on 10/8/19.
 */
public abstract class QuarkPressurePlateBlock extends AbstractPressurePlateBlock implements IQuarkBlock {

    private final QuarkModule module;
    private BooleanSupplier enabledSupplier = () -> true;

    public QuarkPressurePlateBlock(String regname, QuarkModule module, ItemGroup creativeTab, Properties properties) {
        super(properties);
        this.module = module;

        RegistryHelper.registerBlock(this, regname);
        if(creativeTab != null)
            RegistryHelper.setCreativeTab(this, creativeTab);
    }

    @Override
    public void fillItemGroup(ItemGroup group, NonNullList<ItemStack> items) {
        if(isEnabled() || group == ItemGroup.SEARCH)
            super.fillItemGroup(group, items);
    }

    @Override
    public QuarkPressurePlateBlock setCondition(BooleanSupplier enabledSupplier) {
        this.enabledSupplier = enabledSupplier;
        return this;
    }

    @Override
    public boolean doesConditionApply() {
        return enabledSupplier.getAsBoolean();
    }

    @Nullable
    @Override
    public QuarkModule getModule() {
        return module;
    }

}
