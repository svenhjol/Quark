package vazkii.arl.util;

import net.minecraft.util.BlockMirror;
import net.minecraft.util.dynamic.GlobalPos;

public final class RotationHandler {

	private static final BlockMirror[] FACING_TO_ROTATION = new BlockMirror[] {
			BlockMirror.NONE,
			BlockMirror.NONE,
			BlockMirror.NONE,
			BlockMirror.FRONT_BACK,
			BlockMirror.directionTransformation,
			BlockMirror.LEFT_RIGHT
	};

	public static GlobalPos rotateFacing(GlobalPos facing, BlockMirror rot) {
		return rot.a(facing);
	}

	public static GlobalPos rotateFacing(GlobalPos facing, GlobalPos rot) {
		return rotateFacing(facing, getRotationFromFacing(rot));
	}

	public static BlockMirror getRotationFromFacing(GlobalPos facing) {
		return FACING_TO_ROTATION[facing.ordinal()];
	}

	public static int[] applyRotation(BlockMirror rot, int x, int z) {
		switch(rot) {
			case FRONT_BACK: return new int[] { -x, -z }; 
			case LEFT_RIGHT: return new int[] { z, -x };
			case directionTransformation: return new int[] { -z, x };
			default: return new int[] { x, z };
		}
	}

}
