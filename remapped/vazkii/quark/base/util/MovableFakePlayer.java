package vazkii.quark.base.util;

import com.mojang.authlib.GameProfile;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.common.util.FakePlayer;

public class MovableFakePlayer extends FakePlayer {

	public MovableFakePlayer(ServerWorld world, GameProfile name) {
		super(world, name);
	}

	@Override
	public Vec3d getPos() {
		return new Vec3d(getX(), getY(), getZ());
	}
	
	@Override
	public BlockPos getBlockPos() {
		return new BlockPos((int) getX(), (int) getY(), (int) getZ());
	}
	
}
