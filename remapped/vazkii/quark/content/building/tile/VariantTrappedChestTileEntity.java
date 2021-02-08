package vazkii.quark.content.building.tile;

import vazkii.quark.content.building.module.VariantChestsModule;

public class VariantTrappedChestTileEntity extends VariantChestTileEntity {

	public VariantTrappedChestTileEntity() {
		super(VariantChestsModule.trappedChestTEType);
	}

	protected void onInvOpenOrClose() {
		super.onInvOpenOrClose();
		this.world.updateNeighborsAlways(this.pos.down(), this.getCachedState().getBlock());
	}

}
