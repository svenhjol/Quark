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
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher;
import net.minecraft.client.render.item.BuiltinModelItemRenderer;
import net.minecraft.client.render.model.json.ModelTransformation.Mode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.ModList;
import vazkii.arl.interf.IBlockItemProvider;
import vazkii.arl.util.RegistryHelper;
import vazkii.quark.base.block.IQuarkBlock;
import vazkii.quark.base.module.Module;
import vazkii.quark.building.client.render.VariantChestTileEntityRenderer;
import vazkii.quark.building.module.VariantChestsModule.IChestTextureProvider;
import vazkii.quark.building.tile.VariantChestTileEntity;

@EnvironmentInterface(value = EnvType.CLIENT, itf = IBlockItemProvider.class)
public class VariantChestBlock extends ChestBlock implements IBlockItemProvider, IQuarkBlock, IChestTextureProvider {

	public final String type;
	private final Module module;
	private BooleanSupplier enabledSupplier = () -> true;
	
	private String path;

	public VariantChestBlock(String type, Module module, Supplier<BlockEntityType<? extends ChestBlockEntity>> supplier, Settings props) {
		super(props, supplier);
		RegistryHelper.registerBlock(this, type + "_chest");
		RegistryHelper.setCreativeTab(this, ItemGroup.DECORATIONS);
		
		this.type = type;
		this.module = module;
		
		path = (this instanceof Compat ? "compat/" : "") + type + "/";
	}
	
	@Override
	public boolean isFlammable(BlockState state, BlockView world, BlockPos pos, Direction face) {
		return false;
	}
	
	@Override
	public void addStacksForDisplay(ItemGroup group, DefaultedList<ItemStack> items) {
		if(isEnabled() || group == ItemGroup.SEARCH)
			super.addStacksForDisplay(group, items);
	}

	@Override
	public VariantChestBlock setCondition(BooleanSupplier enabledSupplier) {
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
		return new VariantChestTileEntity();
	}
	
	@Environment(EnvType.CLIENT)
	public static void setISTER(Item.Settings props, Block block) {
		props.setISTER(() -> () -> new BuiltinModelItemRenderer() {
			private final BlockEntity tile = new VariantChestTileEntity();
			//render
			public void render(ItemStack stack, Mode transformType, MatrixStack matrix, VertexConsumerProvider buffer, int x, int y) {
				VariantChestTileEntityRenderer.invBlock = block;
	            BlockEntityRenderDispatcher.INSTANCE.renderEntity(tile, matrix, buffer, x, y);
	            VariantChestTileEntityRenderer.invBlock = null;
			}
			
		});
	}

	@Override
	@Environment(EnvType.CLIENT)
	public BlockItem provideItemBlock(Block block, Item.Settings props) {
		setISTER(props, block);
		return new BlockItem(block, props);
	}
	
	public static class Compat extends VariantChestBlock {

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
		return false;
	}
	
}
