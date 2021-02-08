package vazkii.quark.content.world.block;

import net.minecraft.item.ItemGroup;
import vazkii.quark.base.block.QuarkBlock;
import vazkii.quark.base.module.QuarkModule;

public class MyaliteBlock extends QuarkBlock implements IMyaliteColorProvider {

	public MyaliteBlock(String regname, QuarkModule module, ItemGroup creativeTab, Settings properties) {
		super(regname, module, creativeTab, properties);
	}

}
