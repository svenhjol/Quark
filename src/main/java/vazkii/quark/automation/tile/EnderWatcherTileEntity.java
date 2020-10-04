package vazkii.quark.automation.tile;

import java.util.List;

import net.minecraft.block.BlockState;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.util.Tickable;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RayTraceContext.FluidHandling;
import net.minecraft.world.RayTraceContext.ShapeType;
import vazkii.arl.block.tile.TileMod;
import vazkii.quark.automation.block.EnderWatcherBlock;
import vazkii.quark.automation.module.EnderWatcherModule;
import vazkii.quark.base.handler.RayTraceHandler;

public class EnderWatcherTileEntity extends TileMod implements Tickable {
	
	public EnderWatcherTileEntity() {
		super(EnderWatcherModule.enderWatcherTEType);
	}

	@Override
	public void tick() {
		BlockState state = getCachedState();
		boolean wasLooking = state.get(EnderWatcherBlock.WATCHED);
		int currWatch = state.get(EnderWatcherBlock.POWER);
		int range = 80;
		
		int newWatch = 0;
		List<PlayerEntity> players = world.getNonSpectatingEntities(PlayerEntity.class, new Box(pos.add(-range, -range, -range), pos.add(range, range, range)));
		
		boolean looking = false;
		for(PlayerEntity player : players) {
			ItemStack helm = player.getEquippedStack(EquipmentSlot.HEAD);
			if(!helm.isEmpty() && helm.getItem() == Items.PUMPKIN)
				continue;

			HitResult result = RayTraceHandler.rayTrace(player, world, player, ShapeType.OUTLINE, FluidHandling.NONE, 64);
			if(result != null && result instanceof BlockHitResult && ((BlockHitResult) result).getBlockPos().equals(pos)) {
				looking = true;
				
				Vec3d vec = result.getPos();
				Direction dir = ((BlockHitResult) result).getSide();
				double x = Math.abs(vec.x - pos.getX() - 0.5) * (1 - Math.abs(dir.getOffsetX()));
				double y = Math.abs(vec.y - pos.getY() - 0.5) * (1 - Math.abs(dir.getOffsetY()));
				double z = Math.abs(vec.z - pos.getZ() - 0.5) * (1 - Math.abs(dir.getOffsetZ()));
				
				// 0.7071067811865476 being the hypotenuse of an isosceles triangle with cathetus of length 0.5
				double fract = 1 - (Math.sqrt(x*x + y*y + z*z) / 0.7071067811865476);
				newWatch = Math.max(newWatch, (int) Math.ceil(fract * 15));
			}
		}
		
		if(!world.isClient && (looking != wasLooking || currWatch != newWatch))
			world.setBlockState(pos, world.getBlockState(pos).with(EnderWatcherBlock.WATCHED, looking).with(EnderWatcherBlock.POWER, newWatch), 1 | 2);
		
		if(looking) {
			double x = pos.getX() - 0.1 + Math.random() * 1.2;
			double y = pos.getY() - 0.1 + Math.random() * 1.2;
			double z = pos.getZ() - 0.1 + Math.random() * 1.2;

			world.addParticle(new DustParticleEffect(1.0F, 0.0F, 0.0F, 1.0F), x, y, z, 0.0D, 0.0D, 0.0D);
		}
	}

}
