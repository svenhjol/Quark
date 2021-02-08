package vazkii.quark.content.building.block;

import net.minecraft.item.ItemGroup;
import vazkii.quark.base.block.QuarkPillarBlock;
import vazkii.quark.base.module.QuarkModule;
import vazkii.quark.content.world.block.IMyaliteColorProvider;

public class MyalitePillarBlock extends QuarkPillarBlock implements IMyaliteColorProvider {

	public MyalitePillarBlock(String regname, QuarkModule module, ItemGroup creativeTab, Properties properties) {
		super(regname, module, creativeTab, properties);
	}

}
