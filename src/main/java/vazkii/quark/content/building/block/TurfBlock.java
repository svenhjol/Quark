package vazkii.quark.content.building.block;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.color.BlockColors;
import net.minecraft.client.renderer.color.IBlockColor;
import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.client.renderer.color.ItemColors;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import vazkii.arl.interf.IBlockColorProvider;
import vazkii.quark.base.block.QuarkBlock;
import vazkii.quark.base.module.QuarkModule;

/**
 * @author WireSegal
 * Created at 11:23 AM on 10/4/19.
 */
public class TurfBlock extends QuarkBlock implements IBlockColorProvider {
    public TurfBlock(String regname, QuarkModule module, ItemGroup creativeTab, Properties properties) {
        super(regname, module, creativeTab, properties);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public IBlockColor getBlockColor() {
        final BlockColors colors = Minecraft.getInstance().getBlockColors();
        final BlockState grass = Blocks.GRASS_BLOCK.getDefaultState();
        return (state, world, pos, tintIndex) -> colors.getColor(grass, world, pos, tintIndex);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public IItemColor getItemColor() {
        final ItemColors colors = Minecraft.getInstance().getItemColors();
        final ItemStack grass = new ItemStack(Items.GRASS_BLOCK);
        return (stack, tintIndex) -> colors.getColor(grass, tintIndex);
    }
}
