package vazkii.quark.addons.oddities.tile;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.Tickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import vazkii.quark.addons.oddities.block.MagnetBlock;
import vazkii.quark.addons.oddities.magnetsystem.MagnetSystem;
import vazkii.quark.addons.oddities.module.MagnetsModule;

public class MagnetTileEntity extends BlockEntity implements Tickable {

	public MagnetTileEntity() {
		super(MagnetsModule.magnetType);
	}

	@Override
	public void tick() {
		BlockState state = getCachedState();
		boolean powered = state.get(MagnetBlock.POWERED);

		if(powered) {
			Direction dir = state.get(MagnetBlock.FACING);
			int power = getPower(dir);
			magnetize(dir, dir, power);
			magnetize(dir.getOpposite(), dir, power);
		}
	}

	private void magnetize(Direction dir, Direction moveDir, int power) {
		if (world == null)
			return;

		double magnitude = (dir == moveDir ? 1 : -1);

		double particleMotion = 0.05 * magnitude;
		double particleChance = 0.2;
		double xOff = dir.getOffsetX() * particleMotion;
		double yOff = dir.getOffsetY() * particleMotion;
		double zOff = dir.getOffsetZ() * particleMotion;

		for(int i = 1; i <= power; i++) {
			BlockPos targetPos = pos.offset(dir, i);
			BlockState targetState = world.getBlockState(targetPos);

			if (targetState.getBlock() == MagnetsModule.magnetized_block)
				break;

			if(!world.isClient && targetState.getBlock() != Blocks.MOVING_PISTON && targetState.getBlock() != MagnetsModule.magnetized_block) {
				PistonBehavior reaction = MagnetSystem.getPushAction(this, targetPos, targetState, moveDir);
				if (reaction == PistonBehavior.IGNORE || reaction == PistonBehavior.DESTROY) {
					BlockPos frontPos = targetPos.offset(moveDir);
					BlockState frontState = world.getBlockState(frontPos);
					if(frontState.isAir(world, frontPos))
						MagnetSystem.applyForce(world, targetPos, power - i + 1, dir == moveDir, moveDir, i, pos);
				}
			}

			if(!targetState.isAir(world, targetPos))
				break;

			if (world.isClient && Math.random() <= particleChance) {
				double x = targetPos.getX() + (xOff == 0 ? 0.5 : Math.random());
				double y = targetPos.getY() + (yOff == 0 ? 0.5 : Math.random());
				double z = targetPos.getZ() + (zOff == 0 ? 0.5 : Math.random());
				world.addParticle(ParticleTypes.SNEEZE, x, y, z, xOff, yOff, zOff);
			}
		}
	}

	private int getPower(Direction curr) {
		if (world == null)
			return 0;

		int power = 0;
		Direction opp = curr.getOpposite();
		
		for(Direction dir : Direction.values()) {
			if(dir != opp && dir != curr) {
				int offPower = world.getEmittedRedstonePower(pos.offset(dir), dir);
				power = Math.max(offPower, power);
			}
		}
		
		return power;
	}

}
