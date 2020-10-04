package vazkii.quark.automation.ai;

import java.util.EnumSet;
import java.util.Random;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.goal.WanderAroundGoal;
import net.minecraft.entity.mob.EndermiteEntity;
import net.minecraft.entity.mob.ShulkerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import vazkii.quark.automation.module.EndermitesFormShulkersModule;

public class FormShulkerGoal extends WanderAroundGoal {
	
	private final EndermiteEntity endermite;
	private Direction facing;
	private boolean doMerge;

	public FormShulkerGoal(EndermiteEntity endermite) {
		super(endermite, 1.0D, 10);
		this.endermite = endermite;
		setControls(EnumSet.of(Control.TARGET));
	}
	
	@Override
	public boolean canStart() {
		if(endermite.getTarget() != null)
			return false;
		else if(!endermite.getNavigation().isIdle())
			return false;
		else {
			Random random = endermite.getRandom();

			if(random.nextDouble() < EndermitesFormShulkersModule.chance) {
				facing = Direction.random(random); // random
				Vec3d pos = endermite.getPos();
				BlockPos blockpos = (new BlockPos(pos.x, pos.y + 0.5D, pos.z)).offset(facing);
				BlockState iblockstate = endermite.getEntityWorld().getBlockState(blockpos);

				if(iblockstate.getBlock() == Blocks.PURPUR_BLOCK) {
					doMerge = true;
					return true;
				}
			}

			doMerge = false;
			return super.canStart();
		}
	}

	@Override
	public boolean shouldContinue() {
		return !doMerge && super.shouldContinue();
	}
	
	@Override
	public void start() {
		if(!doMerge)
			super.start();
		else {
			World world = endermite.getEntityWorld();
			Vec3d pos = endermite.getPos();
			BlockPos blockpos = (new BlockPos(pos.x, pos.y + 0.5D, pos.z)).offset(facing);
			BlockState iblockstate = world.getBlockState(blockpos);

			if(iblockstate.getBlock() == Blocks.PURPUR_BLOCK) {
				world.removeBlock(blockpos, false);

				ShulkerEntity shulker = new ShulkerEntity(EntityType.SHULKER, world);
				shulker.setAttachedBlock(blockpos);
				shulker.updatePosition(blockpos.getX() + 0.5, blockpos.getY() + 0.5, blockpos.getZ() + 0.5);
				world.spawnEntity(shulker);
				
				if(endermite.hasCustomName())
					shulker.setCustomName(endermite.getCustomName());
				endermite.playSpawnEffects();
				endermite.remove();
			}
		}
	}
	
}
