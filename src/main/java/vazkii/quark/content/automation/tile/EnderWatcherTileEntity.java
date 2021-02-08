package vazkii.quark.content.automation.tile;

import java.util.List;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particles.RedstoneParticleData;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceContext.BlockMode;
import net.minecraft.util.math.RayTraceContext.FluidMode;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import vazkii.arl.block.tile.TileMod;
import vazkii.quark.base.handler.RayTraceHandler;
import vazkii.quark.content.automation.block.EnderWatcherBlock;
import vazkii.quark.content.automation.module.EnderWatcherModule;

public class EnderWatcherTileEntity extends TileMod implements ITickableTileEntity {
	
	public EnderWatcherTileEntity() {
		super(EnderWatcherModule.enderWatcherTEType);
	}

	@Override
	public void tick() {
		BlockState state = getBlockState();
		boolean wasLooking = state.get(EnderWatcherBlock.WATCHED);
		int currWatch = state.get(EnderWatcherBlock.POWER);
		int range = 80;
		
		int newWatch = 0;
		List<PlayerEntity> players = world.getEntitiesWithinAABB(PlayerEntity.class, new AxisAlignedBB(pos.add(-range, -range, -range), pos.add(range, range, range)));
		
		boolean looking = false;
		for(PlayerEntity player : players) {
			ItemStack helm = player.getItemStackFromSlot(EquipmentSlotType.HEAD);
			if(!helm.isEmpty() && helm.getItem() == Items.PUMPKIN)
				continue;

			RayTraceResult result = RayTraceHandler.rayTrace(player, world, player, BlockMode.OUTLINE, FluidMode.NONE, 64);
			if(result != null && result instanceof BlockRayTraceResult && ((BlockRayTraceResult) result).getPos().equals(pos)) {
				looking = true;
				
				Vector3d vec = result.getHitVec();
				Direction dir = ((BlockRayTraceResult) result).getFace();
				double x = Math.abs(vec.x - pos.getX() - 0.5) * (1 - Math.abs(dir.getXOffset()));
				double y = Math.abs(vec.y - pos.getY() - 0.5) * (1 - Math.abs(dir.getYOffset()));
				double z = Math.abs(vec.z - pos.getZ() - 0.5) * (1 - Math.abs(dir.getZOffset()));
				
				// 0.7071067811865476 being the hypotenuse of an isosceles triangle with cathetus of length 0.5
				double fract = 1 - (Math.sqrt(x*x + y*y + z*z) / 0.7071067811865476);
				newWatch = Math.max(newWatch, (int) Math.ceil(fract * 15));
			}
		}
		
		if(!world.isRemote && (looking != wasLooking || currWatch != newWatch))
			world.setBlockState(pos, world.getBlockState(pos).with(EnderWatcherBlock.WATCHED, looking).with(EnderWatcherBlock.POWER, newWatch), 1 | 2);
		
		if(looking) {
			double x = pos.getX() - 0.1 + Math.random() * 1.2;
			double y = pos.getY() - 0.1 + Math.random() * 1.2;
			double z = pos.getZ() - 0.1 + Math.random() * 1.2;

			world.addParticle(new RedstoneParticleData(1.0F, 0.0F, 0.0F, 1.0F), x, y, z, 0.0D, 0.0D, 0.0D);
		}
	}

}
