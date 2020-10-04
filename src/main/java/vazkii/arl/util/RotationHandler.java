package vazkii.arl.util;

import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.Direction;

public final class RotationHandler {

	private static final BlockRotation[] FACING_TO_ROTATION = new BlockRotation[] {
			BlockRotation.NONE,
			BlockRotation.NONE,
			BlockRotation.NONE,
			BlockRotation.CLOCKWISE_180,
			BlockRotation.COUNTERCLOCKWISE_90,
			BlockRotation.CLOCKWISE_90
	};

	public static Direction rotateFacing(Direction facing, BlockRotation rot) {
		return rot.rotate(facing);
	}

	public static Direction rotateFacing(Direction facing, Direction rot) {
		return rotateFacing(facing, getRotationFromFacing(rot));
	}

	public static BlockRotation getRotationFromFacing(Direction facing) {
		return FACING_TO_ROTATION[facing.ordinal()];
	}

	public static int[] applyRotation(BlockRotation rot, int x, int z) {
		switch(rot) {
			case CLOCKWISE_180: return new int[] { -x, -z }; 
			case CLOCKWISE_90: return new int[] { z, -x };
			case COUNTERCLOCKWISE_90: return new int[] { -z, x };
			default: return new int[] { x, z };
		}
	}

}
