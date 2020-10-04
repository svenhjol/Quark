package vazkii.quark.building.tile;

import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.util.math.Box;
import vazkii.quark.building.module.VariantChestsModule;

public class VariantChestTileEntity extends ChestBlockEntity {

	protected VariantChestTileEntity(BlockEntityType<?> typeIn) {
		super(typeIn);
	}

	public VariantChestTileEntity() {
		super(VariantChestsModule.chestTEType);
	}

	@Override
	public Box getRenderBoundingBox() {
		return new Box(pos.getX() - 1, pos.getY(), pos.getZ() - 1, pos.getX() + 2, pos.getY() + 2, pos.getZ() + 2);
	}

}
