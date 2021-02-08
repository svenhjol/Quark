package vazkii.quark.content.building.module;

import net.minecraft.block.Block;
import net.minecraft.block.Material;
import net.minecraft.block.MaterialColor;
import net.minecraft.item.ItemGroup;
import net.minecraft.sound.BlockSoundGroup;
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
                Block.Properties.of(Material.STONE, MaterialColor.NETHER)
                .requiresTool()
        		.harvestTool(ToolType.PICKAXE)
                .sounds(BlockSoundGroup.NETHER_BRICKS)
                .strength(2.0F, 6.0F));
    }
}
