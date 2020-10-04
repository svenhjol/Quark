package vazkii.quark.building.tile;

import vazkii.quark.building.module.VariantChestsModule;

public class VariantTrappedChestTileEntity extends VariantChestTileEntity {

	public VariantTrappedChestTileEntity() {
		super(VariantChestsModule.trappedChestTEType);
	}

	protected void onInvOpenOrClose() {
		super.onInvOpenOrClose();
		this.world.updateNeighborsAlways(this.pos.down(), this.getCachedState().getBlock());
	}

}
