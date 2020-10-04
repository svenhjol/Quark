package vazkii.quark.building.block;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.color.block.BlockColorProvider;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.color.item.ItemColorProvider;
import net.minecraft.client.color.item.ItemColors;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import vazkii.arl.interf.IBlockColorProvider;
import vazkii.quark.base.block.QuarkBlock;
import vazkii.quark.base.module.Module;

/**
 * @author WireSegal
 * Created at 11:23 AM on 10/4/19.
 */
public class TurfBlock extends QuarkBlock implements IBlockColorProvider {
    public TurfBlock(String regname, Module module, ItemGroup creativeTab, Settings properties) {
        super(regname, module, creativeTab, properties);
    }

    @Override
    @Environment(EnvType.CLIENT)
    public BlockColorProvider getBlockColor() {
        final BlockColors colors = MinecraftClient.getInstance().getBlockColors();
        final BlockState grass = Blocks.GRASS_BLOCK.getDefaultState();
        return (state, world, pos, tintIndex) -> colors.getColor(grass, world, pos, tintIndex);
    }

    @Override
    @Environment(EnvType.CLIENT)
    public ItemColorProvider getItemColor() {
        final ItemColors colors = MinecraftClient.getInstance().getItemColors();
        final ItemStack grass = new ItemStack(Items.GRASS_BLOCK);
        return (stack, tintIndex) -> colors.getColorMultiplier(grass, tintIndex);
    }
}
