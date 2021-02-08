package vazkii.quark.content.building.block;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.color.BlockColors;
import net.minecraft.client.renderer.color.IBlockColor;
import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.client.renderer.color.ItemColors;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IBlockReader;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import vazkii.arl.interf.IBlockColorProvider;
import vazkii.quark.base.block.QuarkVineBlock;
import vazkii.quark.base.module.QuarkModule;

public class BurntVineBlock extends QuarkVineBlock implements IBlockColorProvider {

	public BurntVineBlock(QuarkModule module) {
		super(module, "burnt_vine", false);
	}

	@Override
	public ItemStack getPickBlock(BlockState state, RayTraceResult target, IBlockReader world, BlockPos pos, PlayerEntity player) {
		return new ItemStack(Items.VINE);
	}
	
    @Override
    @OnlyIn(Dist.CLIENT)
    public IBlockColor getBlockColor() {
        final BlockColors colors = Minecraft.getInstance().getBlockColors();
        final BlockState grass = Blocks.VINE.getDefaultState();
        return (state, world, pos, tintIndex) -> colors.getColor(grass, world, pos, tintIndex);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public IItemColor getItemColor() {
        final ItemColors colors = Minecraft.getInstance().getItemColors();
        final ItemStack grass = new ItemStack(Items.VINE);
        return (stack, tintIndex) -> colors.getColor(grass, tintIndex);
    }

}
