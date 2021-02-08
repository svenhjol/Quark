package vazkii.quark.content.world.entity;

import javax.annotation.Nonnull;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.monster.ZombieEntity;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class WrappedEntity extends ZombieEntity {

	public static final ResourceLocation WRAPPED_LOOT_TABLE = new ResourceLocation("quark", "entities/wrapped");
	
	public WrappedEntity(EntityType<? extends WrappedEntity> type, World worldIn) {
		super(type, worldIn);
	}

	@Override
	public boolean attackEntityAsMob(Entity entityIn) {
		boolean flag = super.attackEntityAsMob(entityIn);
		if (flag && this.getHeldItemMainhand().isEmpty() && entityIn instanceof LivingEntity) {
			float f = this.world.getDifficultyForLocation(new BlockPos(getPosX(), getPosY(), getPosY())).getAdditionalDifficulty();
			((LivingEntity)entityIn).addPotionEffect(new EffectInstance(Effects.SLOWNESS, 140 * (int)f));
		}

		return flag;
	}
	
	@Nonnull
	@Override
	protected ResourceLocation getLootTable() {
		return WRAPPED_LOOT_TABLE;
	}

}
