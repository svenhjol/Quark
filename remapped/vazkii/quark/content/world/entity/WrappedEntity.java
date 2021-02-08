package vazkii.quark.content.world.entity;

import javax.annotation.Nonnull;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class WrappedEntity extends ZombieEntity {

	public static final Identifier WRAPPED_LOOT_TABLE = new Identifier("quark", "entities/wrapped");
	
	public WrappedEntity(EntityType<? extends WrappedEntity> type, World worldIn) {
		super(type, worldIn);
	}

	@Override
	public boolean tryAttack(Entity entityIn) {
		boolean flag = super.tryAttack(entityIn);
		if (flag && this.getMainHandStack().isEmpty() && entityIn instanceof LivingEntity) {
			float f = this.world.getLocalDifficulty(new BlockPos(getX(), getY(), getY())).getLocalDifficulty();
			((LivingEntity)entityIn).addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 140 * (int)f));
		}

		return flag;
	}
	
	@Nonnull
	@Override
	protected Identifier getLootTableId() {
		return WRAPPED_LOOT_TABLE;
	}

}
