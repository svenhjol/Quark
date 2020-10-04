package vazkii.quark.automation.module;

import net.minecraft.block.Block;
import net.minecraft.block.Material;
import net.minecraft.block.MaterialColor;
import net.minecraft.item.ItemGroup;
import net.minecraftforge.common.ToolType;
import vazkii.quark.automation.block.ObsidianPressurePlateBlock;
import vazkii.quark.base.module.LoadModule;
import vazkii.quark.base.module.Module;
import vazkii.quark.base.module.ModuleCategory;

/**
 * @author WireSegal
 * Created at 9:51 PM on 10/8/19.
 */
@LoadModule(category = ModuleCategory.AUTOMATION)
public class ObsidianPlateModule extends Module {
    @Override
    public void construct() {
        new ObsidianPressurePlateBlock("obsidian_pressure_plate", this, ItemGroup.REDSTONE,
                Block.Properties.of(Material.STONE, MaterialColor.BLACK)
                		.requiresTool() // needs tool
                		.harvestTool(ToolType.PICKAXE)
                        .noCollision()
                        .strength(2F, 1200.0F));
    }
}
