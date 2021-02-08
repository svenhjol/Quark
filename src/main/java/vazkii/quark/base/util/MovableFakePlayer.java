package vazkii.quark.base.util;

import com.mojang.authlib.GameProfile;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.util.FakePlayer;

public class MovableFakePlayer extends FakePlayer {

	public MovableFakePlayer(ServerWorld world, GameProfile name) {
		super(world, name);
	}

	@Override
	public Vector3d getPositionVec() {
		return new Vector3d(getPosX(), getPosY(), getPosZ());
	}
	
	@Override
	public BlockPos getPosition() {
		return new BlockPos((int) getPosX(), (int) getPosY(), (int) getPosZ());
	}
	
}
