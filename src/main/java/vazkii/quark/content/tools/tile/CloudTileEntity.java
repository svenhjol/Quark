package vazkii.quark.content.tools.tile;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.tileentity.ITickableTileEntity;
import vazkii.arl.block.tile.TileMod;
import vazkii.quark.content.tools.module.BottledCloudModule;

public class CloudTileEntity extends TileMod implements ITickableTileEntity {

	private static final String TAG_LIVE_TIME = "liveTime";
	
	public int liveTime = -10000;
	
	public CloudTileEntity() {
		super(BottledCloudModule.tileEntityType);
	}

	@Override
	public void tick() {
		if(liveTime < -1000)
			liveTime = 200;
		
		if(liveTime > 0) {
			liveTime--;
			
			if(world.isRemote && liveTime % 20 == 0)
				for(int i = 0; i < (10 - (200 - liveTime) / 20); i++)
					world.addParticle(ParticleTypes.CLOUD, pos.getX() + Math.random(), pos.getY() + Math.random(), pos.getZ() + Math.random(), 0, 0, 0);
		} else {
			if(!world.isRemote)
				world.removeBlock(getPos(), false);
		}
	}
	
	@Override
	public void writeSharedNBT(CompoundNBT cmp) {
		cmp.putInt(TAG_LIVE_TIME, liveTime);
	}
	
	@Override
	public void readSharedNBT(CompoundNBT cmp) {
		liveTime = cmp.getInt(TAG_LIVE_TIME);
	}

}
