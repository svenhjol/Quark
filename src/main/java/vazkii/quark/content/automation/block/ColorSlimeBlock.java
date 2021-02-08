package vazkii.quark.content.automation.block;

import java.util.Locale;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.SlimeBlock;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import vazkii.arl.util.RegistryHelper;
import vazkii.quark.api.IConditionalSticky;
import vazkii.quark.base.handler.RenderLayerHandler;
import vazkii.quark.base.handler.RenderLayerHandler.RenderTypeSkeleton;
import vazkii.quark.base.module.QuarkModule;

public class ColorSlimeBlock extends SlimeBlock implements IConditionalSticky {

	private final SlimeColor color;
	private final QuarkModule module;

	public ColorSlimeBlock(SlimeColor color, QuarkModule module) {
		super(Block.Properties.from(Blocks.SLIME_BLOCK));

		RegistryHelper.registerBlock(this, color.name().toLowerCase(Locale.ROOT) + "_slime_block");
		RegistryHelper.setCreativeTab(this, ItemGroup.DECORATIONS);

		this.color = color;
		this.module = module;
		
		RenderLayerHandler.setRenderType(this, RenderTypeSkeleton.TRANSLUCENT);
	}

	@Override
	public void fillItemGroup(ItemGroup group, NonNullList<ItemStack> items) {
		if (module.enabled || group == ItemGroup.SEARCH)
			super.fillItemGroup(group, items);
	}

	@Override
	public boolean canStickToBlock(World world, BlockPos pistonPos, BlockPos pos, BlockPos slimePos, BlockState state, BlockState slimeState, Direction direction) {
		SlimeColor ourColor = color;
		Block block = slimeState.getBlock();

		if (block instanceof ColorSlimeBlock) {
			SlimeColor otherColor = ((ColorSlimeBlock) block).color;
			if (!ourColor.sticksTo(otherColor) && ourColor != otherColor)
				return false;
		}

		if (block == Blocks.SLIME_BLOCK)
			return ourColor.sticksToGreen;

		return true;
	}

	@Override
	public boolean isStickyBlock(BlockState state) {
		return true;
	}

	public enum SlimeColor {

		RED(MaterialColor.RED, false, 3, 4), // 0
		BLUE(MaterialColor.BLUE, false, 2, 3), // 1
		CYAN(MaterialColor.CYAN, true, 1), // 2
		MAGENTA(MaterialColor.MAGENTA, false, 0, 1), // 3
		YELLOW(MaterialColor.YELLOW, true, 0); // 4

		SlimeColor(MaterialColor color, boolean sticksToGreen, int... sticksTo) {
			this.color = color;
			this.sticksToGreen = sticksToGreen;
			this.sticksTo = sticksTo;
		}

		public final MaterialColor color;
		public final boolean sticksToGreen;
		private final int[] sticksTo;

		public boolean sticksTo(SlimeColor otherVariant) {
			int ord = otherVariant.ordinal();
			for (int i : sticksTo)
				if (i == ord)
					return true;

			return false;
		}

	}

}
