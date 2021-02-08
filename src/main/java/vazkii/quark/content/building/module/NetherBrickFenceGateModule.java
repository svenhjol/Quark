package vazkii.quark.content.building.module;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.item.ItemGroup;
import net.minecraftforge.common.ToolType;
import vazkii.quark.base.block.QuarkFenceGateBlock;
import vazkii.quark.base.module.LoadModule;
import vazkii.quark.base.module.QuarkModule;
import vazkii.quark.base.module.ModuleCategory;

/**
 * @author WireSegal
 * Created at 10:51 AM on 10/9/19.
 */
@LoadModule(category = ModuleCategory.BUILDING)
public class NetherBrickFenceGateModule extends QuarkModule {
    @Override
    public void construct() {
        new QuarkFenceGateBlock("nether_brick_fence_gate", this, ItemGroup.REDSTONE,
                Block.Properties.create(Material.ROCK, MaterialColor.NETHERRACK)
                .setRequiresTool()
        		.harvestTool(ToolType.PICKAXE)
                .sound(SoundType.NETHER_BRICK)
                .hardnessAndResistance(2.0F, 6.0F));
    }
}
