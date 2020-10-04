package vazkii.quark.automation.module;

import net.minecraft.block.Block;
import net.minecraft.block.Material;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.ItemGroup;
import net.minecraft.sound.BlockSoundGroup;
import vazkii.arl.util.RegistryHelper;
import vazkii.quark.automation.block.ChuteBlock;
import vazkii.quark.automation.tile.ChuteTileEntity;
import vazkii.quark.base.module.LoadModule;
import vazkii.quark.base.module.Module;
import vazkii.quark.base.module.ModuleCategory;

/**
 * @author WireSegal
 * Created at 10:25 AM on 9/29/19.
 */
@LoadModule(category = ModuleCategory.AUTOMATION)
public class ChuteModule extends Module {

    public static BlockEntityType<ChuteTileEntity> tileEntityType;

    @Override
    public void construct() {
        Block chute = new ChuteBlock("chute", this, ItemGroup.REDSTONE,
                Block.Properties.of(Material.WOOD)
                        .strength(2.5F)
                        .sounds(BlockSoundGroup.WOOD));

        tileEntityType = BlockEntityType.Builder.create(ChuteTileEntity::new, chute).build(null);
        RegistryHelper.register(tileEntityType, "chute");
    }
}
