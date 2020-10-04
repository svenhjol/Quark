package vazkii.quark.building.block;

import java.util.function.BooleanSupplier;

import javax.annotation.Nullable;

import com.google.common.base.Supplier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.api.EnvironmentInterface;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.stat.Stat;
import net.minecraft.stat.Stats;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.BlockView;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.ModList;
import vazkii.arl.interf.IBlockItemProvider;
import vazkii.arl.util.RegistryHelper;
import vazkii.quark.base.Quark;
import vazkii.quark.base.block.IQuarkBlock;
import vazkii.quark.base.module.Module;
import vazkii.quark.building.block.VariantChestBlock.Compat;
import vazkii.quark.building.module.VariantChestsModule.IChestTextureProvider;
import vazkii.quark.building.tile.VariantTrappedChestTileEntity;

@EnvironmentInterface(value = EnvType.CLIENT, itf = IBlockItemProvider.class)
public class VariantTrappedChestBlock extends ChestBlock implements IBlockItemProvider, IQuarkBlock, IChestTextureProvider {

	public final String type;
	private final Module module;
	private BooleanSupplier enabledSupplier = () -> true;

	private String path;
	
	public VariantTrappedChestBlock(String type, Module module, Supplier<BlockEntityType<? extends ChestBlockEntity>> supplier, Settings props) {
		super(props, supplier);
		RegistryHelper.registerBlock(this, type + "_trapped_chest");
		RegistryHelper.setCreativeTab(this, ItemGroup.REDSTONE);

		this.type = type;
		this.module = module;

		path = (this instanceof vazkii.quark.building.block.VariantTrappedChestBlock.Compat ? "compat/" : "") + type + "/";
	}

	@Override
	public boolean isFlammable(BlockState state, BlockView world, BlockPos pos, Direction face) {
		return false;
	}
	
	@Override
	public void addStacksForDisplay(ItemGroup group, DefaultedList<ItemStack> items) {
		if(module.enabled || group == ItemGroup.SEARCH)
			super.addStacksForDisplay(group, items);
	}

	@Override
	public VariantTrappedChestBlock setCondition(BooleanSupplier enabledSupplier) {
		this.enabledSupplier = enabledSupplier;
		return this;
	}

	@Override
	public boolean doesConditionApply() {
		return enabledSupplier.getAsBoolean();
	}

	@Nullable
	@Override
	public Module getModule() {
		return module;
	}

	@Override
	public BlockEntity createBlockEntity(BlockView worldIn) {
		return new VariantTrappedChestTileEntity();
	}

	@Override
	@Environment(EnvType.CLIENT)
	public BlockItem provideItemBlock(Block block, Item.Settings props) {
		VariantChestBlock.setISTER(props, block);
		return new BlockItem(block, props);
	}

	public static class Compat extends VariantTrappedChestBlock {

		public Compat(String type, String mod, Module module, Supplier<BlockEntityType<? extends ChestBlockEntity>> supplier, Settings props) {
			super(type, module, supplier, props);
			setCondition(() -> ModList.get().isLoaded(mod));
		}

	}
	
	@Override
	public String getChestTexturePath() {
		return "model/chest/" + path;
	}

	@Override
	public boolean isTrap() {
		return true;
	}

	// VANILLA TrappedChestBlock copy

	@Override
	protected Stat<Identifier> getOpenStat() {
		return Stats.CUSTOM.getOrCreateStat(Stats.TRIGGER_TRAPPED_CHEST);
	}
	
	@Override
	public boolean emitsRedstonePower(BlockState p_149744_1_) {
		return true;
	}

	@Override
	public int getWeakRedstonePower(BlockState p_180656_1_, BlockView p_180656_2_, BlockPos p_180656_3_, Direction p_180656_4_) {
		return MathHelper.clamp(ChestBlockEntity.getPlayersLookingInChestCount(p_180656_2_, p_180656_3_), 0, 15);
	}

	@Override
	public int getStrongRedstonePower(BlockState p_176211_1_, BlockView p_176211_2_, BlockPos p_176211_3_, Direction p_176211_4_) {
		return p_176211_4_ == Direction.UP ? p_176211_1_.getWeakRedstonePower(p_176211_2_, p_176211_3_, p_176211_4_) : 0;
	}

}
