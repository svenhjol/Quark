package vazkii.quark.content.building.entity;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.block.BlockState;
import net.minecraft.block.FenceBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.item.LeashKnotEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import vazkii.quark.content.building.module.TieLeadsToFencesModule;

public class LeashKnot2Entity extends MobEntity {

	public LeashKnot2Entity(EntityType<? extends LeashKnot2Entity> type,World worldIn) {
		super(type, worldIn);
		
		setNoAI(true);
	}

	@Override
	public boolean attackEntityFrom(@Nonnull DamageSource source, float amount) {
		dismantle(!source.isCreativePlayer());
		return true;
	}

	@Override
	public void tick() {
		super.tick();
		
		Vector3d pos = getPositionVec();
		double decimal = pos.y - (int) pos.y;
		double target = 0.375;
		if(decimal != target) {
			double diff = target - decimal;
			setPosition(pos.x, pos.y + diff, pos.z);
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
		return getLeashHolder();
	}
	
	@Override
	public ActionResultType applyPlayerInteraction(PlayerEntity player, Vector3d vec, Hand hand) {
		if(!world.isRemote) {
			Entity holder = getHolder();
			holder.remove();
			dismantle(!player.isCreative());
		}
		
		return ActionResultType.SUCCESS;
	}
	
	private void dismantle(boolean drop) {
		world.playSound(null, new BlockPos(getPositionVec()), SoundEvents.ENTITY_LEASH_KNOT_BREAK, SoundCategory.BLOCKS, 1F, 1F);
		if(isAlive() && getHolder() != null && drop && !world.isRemote)
			entityDropItem(Items.LEAD, 1);
		remove();
		
		Entity holder = getHolder();
		if (holder instanceof LeashKnotEntity)
			holder.remove();
	}
	
}
