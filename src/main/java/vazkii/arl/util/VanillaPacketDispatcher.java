/**
 * This class was created by <Vazkii>. It's distributed as
 * part of the Botania Mod. Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 *
 * File Created @ [Apr 9, 2015, 9:38:44 PM (GMT)]
 */
package vazkii.arl.util;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public final class VanillaPacketDispatcher {

	public static void dispatchTEToNearbyPlayers(BlockEntity tile) {
		World world = tile.getWorld();
		if(world instanceof ServerWorld) {
			BlockEntityUpdateS2CPacket packet = tile.toUpdatePacket();
			BlockPos pos = tile.getPos();
			
			for(PlayerEntity player : world.getPlayers()) {
				if(player instanceof ServerPlayerEntity && pointDistancePlane(player.getX(), player.getZ(), pos.getX(), pos.getZ()) < 64)
					((ServerPlayerEntity) player).networkHandler.sendPacket(packet);
			}
		}
	}

	public static void dispatchTEToNearbyPlayers(World world, BlockPos pos) {
		BlockEntity tile = world.getBlockEntity(pos);
		if(tile != null)
			dispatchTEToNearbyPlayers(tile);
	}

	public static float pointDistancePlane(double x1, double y1, double x2, double y2) {
		return (float) Math.hypot(x1 - x2, y1 - y2);
	}

}