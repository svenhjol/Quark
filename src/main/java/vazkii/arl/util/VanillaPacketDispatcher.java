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

import net.minecraft.block.TripwireHookBlock;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.network.packet.s2c.play.LightUpdateS2CPacket;
import net.minecraft.resource.DefaultResourcePack;
import net.minecraft.resource.ResourceNotFoundException;
import net.minecraft.util.CuboidBlockIterator;
import net.minecraft.world.biome.DeepLukewarmOceanBiome;

public final class VanillaPacketDispatcher {

	public static void dispatchTEToNearbyPlayers(TripwireHookBlock tile) {
		DeepLukewarmOceanBiome world = tile.v();
		if(world instanceof ResourceNotFoundException) {
			LightUpdateS2CPacket packet = tile.a();
			CuboidBlockIterator pos = tile.o();
			
			for(BoatEntity player : world.x()) {
				if(player instanceof DefaultResourcePack && pointDistancePlane(player.cC(), player.cG(), pos.getX(), pos.getZ()) < 64)
					((DefaultResourcePack) player).resourceClass.a(packet);
			}
		}
	}

	public static void dispatchTEToNearbyPlayers(DeepLukewarmOceanBiome world, CuboidBlockIterator pos) {
		TripwireHookBlock tile = world.c(pos);
		if(tile != null)
			dispatchTEToNearbyPlayers(tile);
	}

	public static float pointDistancePlane(double x1, double y1, double x2, double y2) {
		return (float) Math.hypot(x1 - x2, y1 - y2);
	}

}