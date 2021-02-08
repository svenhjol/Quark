package vazkii.quark.base.block;

import java.util.Objects;
import java.util.function.BooleanSupplier;

import javax.annotation.Nullable;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.StairsBlock;
import net.minecraft.client.renderer.color.IBlockColor;
import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorldReader;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import vazkii.arl.interf.IBlockColorProvider;
import vazkii.arl.interf.IItemColorProvider;
import vazkii.arl.util.RegistryHelper;
import vazkii.quark.base.handler.RenderLayerHandler;
import vazkii.quark.base.handler.VariantHandler;
import vazkii.quark.base.module.QuarkModule;

public class QuarkStairsBlock extends StairsBlock implements IQuarkBlock, IBlockColorProvider {

	private final IQuarkBlock parent;
    private BooleanSupplier enabledSupplier = () -> true;

    public QuarkStairsBlock(IQuarkBlock parent) {
		super(parent.getBlock()::getDefaultState, VariantHandler.realStateCopy(parent));
		
		this.parent = parent;
		RegistryHelper.registerBlock(this, Objects.toString(parent.getBlock().getRegistryName()) + "_stairs");
		RegistryHelper.setCreativeTab(this, ItemGroup.BUILDING_BLOCKS);
		
		RenderLayerHandler.setInherited(this, parent.getBlock());
	}
	
	@Override
	public void fillItemGroup(ItemGroup group, NonNullList<ItemStack> items) {
		if(group == ItemGroup.SEARCH || isEnabled())
			super.fillItemGroup(group, items);
	}

    @Nullable
    @Override
    public QuarkModule getModule() {
        return parent.getModule();
    }

    @Override
    public QuarkStairsBlock setCondition(BooleanSupplier enabledSupplier) {
        this.enabledSupplier = enabledSupplier;
        return this;
    }

    @Override
    public boolean doesConditionApply() {
        return enabledSupplier.getAsBoolean();
    }

    @Nullable
    @Override
    public float[] getBeaconColorMultiplier(BlockState state, IWorldReader world, BlockPos pos, BlockPos beaconPos) {
        return parent.getBlock().getBeaconColorMultiplier(parent.getBlock().getDefaultState(), world, pos, beaconPos);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public IBlockColor getBlockColor() {
        return parent instanceof IBlockColorProvider ? ((IBlockColorProvider) parent).getBlockColor() : null;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public IItemColor getItemColor() {
        return parent instanceof IItemColorProvider ? ((IItemColorProvider) parent).getItemColor() : null;
    }
}
