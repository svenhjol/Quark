package vazkii.quark.content.automation.ai;

import java.util.EnumSet;
import java.util.Random;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.goal.RandomWalkingGoal;
import net.minecraft.entity.monster.EndermiteEntity;
import net.minecraft.entity.monster.ShulkerEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import vazkii.quark.content.automation.module.EndermitesFormShulkersModule;

public class FormShulkerGoal extends RandomWalkingGoal {
	
	private final EndermiteEntity endermite;
	private Direction facing;
	private boolean doMerge;

	public FormShulkerGoal(EndermiteEntity endermite) {
		super(endermite, 1.0D, 10);
		this.endermite = endermite;
		setMutexFlags(EnumSet.of(Flag.TARGET));
	}
	
	@Override
	public boolean shouldExecute() {
		if(endermite.getAttackTarget() != null)
			return false;
		else if(!endermite.getNavigator().noPath())
			return false;
		else {
			Random random = endermite.getRNG();

			if(random.nextDouble() < EndermitesFormShulkersModule.chance) {
				facing = Direction.getRandomDirection(random);
				Vector3d pos = endermite.getPositionVec();
				BlockPos blockpos = (new BlockPos(pos.x, pos.y + 0.5D, pos.z)).offset(facing);
				BlockState iblockstate = endermite.getEntityWorld().getBlockState(blockpos);

				if(iblockstate.getBlock() == Blocks.PURPUR_BLOCK) {
					doMerge = true;
					return true;
				}
			}

			doMerge = false;
			return super.shouldExecute();
		}
	}

	@Override
	public boolean shouldContinueExecuting() {
		return !doMerge && super.shouldContinueExecuting();
	}
	
	@Override
	public void startExecuting() {
		if(!doMerge)
			super.startExecuting();
		else {
			World world = endermite.getEntityWorld();
			Vector3d pos = endermite.getPositionVec();
			BlockPos blockpos = (new BlockPos(pos.x, pos.y + 0.5D, pos.z)).offset(facing);
			BlockState iblockstate = world.getBlockState(blockpos);

			if(iblockstate.getBlock() == Blocks.PURPUR_BLOCK) {
				world.removeBlock(blockpos, false);

				ShulkerEntity shulker = new ShulkerEntity(EntityType.SHULKER, world);
				shulker.setAttachmentPos(blockpos);
				shulker.setPosition(blockpos.getX() + 0.5, blockpos.getY() + 0.5, blockpos.getZ() + 0.5);
				world.addEntity(shulker);
				
				if(endermite.hasCustomName())
					shulker.setCustomName(endermite.getCustomName());
				endermite.spawnExplosionParticle();
				endermite.remove();
			}
		}
	}
	
}
