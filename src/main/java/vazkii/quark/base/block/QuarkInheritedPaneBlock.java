package vazkii.quark.base.block;

import java.util.Objects;
import java.util.function.BooleanSupplier;

import javax.annotation.Nullable;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.PaneBlock;
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
import vazkii.quark.base.module.QuarkModule;

/**
 * @author WireSegal
 * Created at 1:09 PM on 9/19/19.
 */
public class QuarkInheritedPaneBlock extends QuarkPaneBlock implements IQuarkBlock, IBlockColorProvider {

	public final IQuarkBlock parent;

	public QuarkInheritedPaneBlock(IQuarkBlock parent, String name, Block.Properties properties) {
		super(name, parent.getModule(), properties, null);

		this.parent = parent;
		RenderLayerHandler.setInherited(this, parent.getBlock());
	}

	public QuarkInheritedPaneBlock(IQuarkBlock parent, Block.Properties properties) {
		this(parent, Objects.toString(parent.getBlock().getRegistryName()) + "_pane", properties);
	}

	public QuarkInheritedPaneBlock(IQuarkBlock parent) {
		this(parent, Block.Properties.from(parent.getBlock()));
	}

	@Override
	public boolean isEnabled() {
		return super.isEnabled() && parent.isEnabled();
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
