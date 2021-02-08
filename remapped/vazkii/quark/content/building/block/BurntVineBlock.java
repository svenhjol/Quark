package vazkii.quark.content.building.block;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.color.block.BlockColorProvider;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.color.item.ItemColorProvider;
import net.minecraft.client.color.item.ItemColors;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
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
	public ItemStack getPickBlock(BlockState state, HitResult target, BlockView world, BlockPos pos, PlayerEntity player) {
		return new ItemStack(Items.VINE);
	}
	
    @Override
    @Environment(EnvType.CLIENT)
    public BlockColorProvider getBlockColor() {
        final BlockColors colors = MinecraftClient.getInstance().getBlockColors();
        final BlockState grass = Blocks.VINE.getDefaultState();
        return (state, world, pos, tintIndex) -> colors.getColor(grass, world, pos, tintIndex);
    }

    @Override
    @Environment(EnvType.CLIENT)
    public ItemColorProvider getItemColor() {
        final ItemColors colors = MinecraftClient.getInstance().getItemColors();
        final ItemStack grass = new ItemStack(Items.VINE);
        return (stack, tintIndex) -> colors.getColorMultiplier(grass, tintIndex);
    }

}
