package vazkii.quark.content.building.entity;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.block.BlockState;
import net.minecraft.block.FenceBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.decoration.LeashKnotEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import vazkii.quark.content.building.module.TieLeadsToFencesModule;

public class LeashKnot2Entity extends MobEntity {

	public LeashKnot2Entity(EntityType<? extends LeashKnot2Entity> type,World worldIn) {
		super(type, worldIn);
		
		setAiDisabled(true);
	}

	@Override
	public boolean damage(@Nonnull DamageSource source, float amount) {
		dismantle(!source.isSourceCreativePlayer());
		return true;
	}

	@Override
	public void tick() {
		super.tick();
		
		Vec3d pos = getPos();
		double decimal = pos.y - (int) pos.y;
		double target = 0.375;
		if(decimal != target) {
			double diff = target - decimal;
			updatePosition(pos.x, pos.y + diff, pos.z);
		}
		
		BlockState state = world.getBlockState(new BlockPos(pos));
		if(!state.getBlock().isIn(TieLeadsToFencesModule.leadConnectableTag)) {
			dismantle(true);
		} else {
			Entity holder = getHolder();
			if(holder == null || !holder.isAlive())
				dismantle(true);
		}
	}

	@Nullable
	private Entity getHolder() {
		return getHoldingEntity();
	}
	
	@Override
	public ActionResult interactAt(PlayerEntity player, Vec3d vec, Hand hand) {
		if(!world.isClient) {
			Entity holder = getHolder();
			holder.remove();
			dismantle(!player.isCreative());
		}
		
		return ActionResult.SUCCESS;
	}
	
	private void dismantle(boolean drop) {
		world.playSound(null, new BlockPos(getPos()), SoundEvents.ENTITY_LEASH_KNOT_BREAK, SoundCategory.BLOCKS, 1F, 1F);
		if(isAlive() && getHolder() != null && drop && !world.isClient)
			dropItem(Items.LEAD, 1);
		remove();
		
		Entity holder = getHolder();
		if (holder instanceof LeashKnotEntity)
			holder.remove();
	}
	
}
