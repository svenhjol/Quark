package vazkii.quark.content.building.tile;

import vazkii.quark.content.building.module.VariantChestsModule;

public class VariantTrappedChestTileEntity extends VariantChestTileEntity {

	public VariantTrappedChestTileEntity() {
		super(VariantChestsModule.trappedChestTEType);
	}

	protected void onOpenOrClose() {
		super.onOpenOrClose();
		this.world.notifyNeighborsOfStateChange(this.pos.down(), this.getBlockState().getBlock());
	}

}
