/**
 * This class was created by <WireSegal>. It's distributed as
 * part of the Quark Mod. Get the Source Code in github:
 * https://github.com/Vazkii/Quark
 * <p>
 * Quark is Open Source and distributed under the
 * CC-BY-NC-SA 3.0 License: https://creativecommons.org/licenses/by-nc-sa/3.0/deed.en_GB
 * <p>
 * File Created @ [Jul 13, 2019, 12:04 AM (EST)]
 */
package vazkii.quark.mobs.entity;

import net.minecraft.block.Blocks;
import net.minecraft.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.ai.pathing.PathNodeType;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.AbstractSkeletonEntity;
import net.minecraft.entity.mob.Monster;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.passive.RabbitEntity;
import net.minecraft.entity.passive.SheepEntity;
import net.minecraft.entity.passive.WolfEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Packet;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.*;
import net.minecraftforge.fml.network.NetworkHooks;
import vazkii.quark.base.handler.MiscUtil;
import vazkii.quark.mobs.ai.FindPlaceToSleepGoal;
import vazkii.quark.mobs.ai.SleepGoal;
import vazkii.quark.mobs.module.FoxhoundModule;
import vazkii.quark.tweaks.ai.WantLoveGoal;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Random;
import java.util.UUID;

public class FoxhoundEntity extends WolfEntity implements Monster {

	public static final Identifier FOXHOUND_LOOT_TABLE = new Identifier("quark", "entities/foxhound");

	private static final TrackedData<Boolean> TEMPTATION = DataTracker.registerData(FoxhoundEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
	private static final TrackedData<Boolean> SLEEPING = DataTracker.registerData(FoxhoundEntity.class, TrackedDataHandlerRegistry.BOOLEAN);

	private int timeUntilPotatoEmerges = 0;

	public FoxhoundEntity(EntityType<? extends FoxhoundEntity> type, World worldIn) {
		super(type, worldIn);
		this.setPathfindingPenalty(PathNodeType.WATER, -1.0F);
		this.setPathfindingPenalty(PathNodeType.LAVA, 1.0F);
		this.setPathfindingPenalty(PathNodeType.DANGER_FIRE, 1.0F);
		this.setPathfindingPenalty(PathNodeType.DAMAGE_FIRE, 1.0F);
	}

	@Override
	protected void initDataTracker() {
		super.initDataTracker();
		setCollarColor(DyeColor.ORANGE);
		dataTracker.startTracking(TEMPTATION, false);
		dataTracker.startTracking(SLEEPING, false);
	}

	@Override
	public int getLimitPerChunk() {
		return 4;
	}

	@Override
	public boolean isPersistent() {
		return super.isPersistent();
	}

	@Override
	public boolean cannotDespawn() {
		return isTamed();
	}

	@Override
	public boolean canImmediatelyDespawn(double distanceToClosestPlayer) {
		return !isTamed();
	}

	@Override
	public boolean isInsideWall() {
		return MiscUtil.isEntityInsideOpaqueBlock(this);
	}

	@Override
	public void tick() {
		super.tick();

		if (!world.isClient && world.getDifficulty() == Difficulty.PEACEFUL && !isTamed()) {
			remove();
			return;
		}

		//		if (!world.isRemote && TinyPotato.tiny_potato != null) {
		//			if (timeUntilPotatoEmerges == 1) {
		//				timeUntilPotatoEmerges = 0;
		//				ItemStack stack = new ItemStack(TinyPotato.tiny_potato);
		//				ItemNBTHelper.setBoolean(stack, "angery", true);
		//				entityDropItem(stack, 0f);
		//				playSound(SoundEvents.ENTITY_GENERIC_HURT, 1f, 1f);
		//			} else if (timeUntilPotatoEmerges > 1) {
		//				timeUntilPotatoEmerges--;
		//			}
		//		}

		if (WantLoveGoal.needsPets(this)) {
			Entity owner = getOwner();
			if (owner != null && owner.squaredDistanceTo(this) < 1 && !owner.isTouchingWater() && !owner.isFireImmune() && (!(owner instanceof PlayerEntity) || !((PlayerEntity) owner).isCreative()))
				owner.setOnFireFor(5);
		}

		Vec3d pos = getPos();
		if(this.world.isClient)
			this.world.addParticle(isSleeping() ? ParticleTypes.SMOKE : ParticleTypes.FLAME, pos.x + (this.random.nextDouble() - 0.5D) * this.getWidth(), pos.y + (this.random.nextDouble() - 0.5D) * this.getHeight(), pos.z + (this.random.nextDouble() - 0.5D) * this.getWidth(), 0.0D, 0.0D, 0.0D);

		if(isTamed()) {
			BlockPos below = getBlockPos().down(); // getPosition
			BlockEntity tile = world.getBlockEntity(below);
			if (tile instanceof AbstractFurnaceBlockEntity) {
				AbstractFurnaceBlockEntity furnace = (AbstractFurnaceBlockEntity) tile;
				int cookTime = furnace.cookTime;
				if (cookTime > 0 && cookTime % 3 == 0) {
					List<FoxhoundEntity> foxhounds = world.getEntities(FoxhoundEntity.class, new Box(getBlockPos()),
							(fox) -> fox != null && fox.isTamed());
					if(!foxhounds.isEmpty() && foxhounds.get(0) == this)
						furnace.cookTime = furnace.cookTime == 3 ? 5 :Math.min(furnace.cookTimeTotal - 1, cookTime + 1);
				}
			}
		}
	}

	@Override
	public boolean isTouchingWaterOrRain() {
		return false;
	}

	@Nonnull
	@Override
	protected Identifier getLootTableId() {
		return FOXHOUND_LOOT_TABLE;
	}

	protected SleepGoal sleepGoal;

	@Override
	protected void initGoals() {
		this.sleepGoal = new SleepGoal(this);
		this.goalSelector.add(1, new SwimGoal(this));
		this.goalSelector.add(2, this.sleepGoal);
		this.goalSelector.add(3, new SitGoal(this));
		this.goalSelector.add(4, new PounceAtTargetGoal(this, 0.4F));
		this.goalSelector.add(5, new MeleeAttackGoal(this, 1.0D, true));
		this.goalSelector.add(6, new FollowOwnerGoal(this, 1.0D, 10.0F, 2.0F, false));
		this.goalSelector.add(7, new AnimalMateGoal(this, 1.0D));
		this.goalSelector.add(8, new FindPlaceToSleepGoal(this, 0.8D, true));
		this.goalSelector.add(9, new FindPlaceToSleepGoal(this, 0.8D, false));
		this.goalSelector.add(10, new WanderAroundFarGoal(this, 1.0D));
		this.goalSelector.add(11, new WolfBegGoal(this, 8.0F));
		this.goalSelector.add(12, new LookAtEntityGoal(this, PlayerEntity.class, 8.0F));
		this.goalSelector.add(12, new LookAroundGoal(this));
		this.targetSelector.add(1, new RevengeGoal(this));
		this.targetSelector.add(2, new AttackWithOwnerGoal(this));
		this.targetSelector.add(3, new RevengeGoal(this).setGroupRevenge());
		this.targetSelector.add(4, new FollowTargetIfTamedGoal<>(this, AnimalEntity.class, false,
				target -> target instanceof SheepEntity || target instanceof RabbitEntity));
		this.targetSelector.add(4, new FollowTargetIfTamedGoal<>(this, PlayerEntity.class, false,
				target -> !isTamed()));
		this.targetSelector.add(5, new FollowTargetGoal<>(this, AbstractSkeletonEntity.class, false));
	}

	@Override
	public int getAngerTime() {
		if (!isTamed() && world.getDifficulty() != Difficulty.PEACEFUL)
			return 0;
		return super.getAngerTime();
	}

	@Override
	public boolean tryAttack(Entity entityIn) {
		if (entityIn.getType().isFireImmune()) {
			if (entityIn instanceof PlayerEntity)
				return false;
			return super.tryAttack(entityIn);
		}

		boolean flag = entityIn.damage(DamageSource.mob(this).setFire(),
				((int)this.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE)));

		if (flag) {
			entityIn.setOnFireFor(5);
			this.dealDamage(this, entityIn);
		}

		return flag;
	}

	@Override
	public boolean damage(@Nonnull DamageSource source, float amount) {
		setWoke();
		return super.damage(source, amount);
	}

	@Nonnull
	@Override
	public ActionResult interactMob(PlayerEntity player, @Nonnull Hand hand) {
		ItemStack itemstack = player.getStackInHand(hand);

		if(itemstack.getItem() == Items.BONE && !isTamed())
			return ActionResult.PASS;

		if (!this.isTamed() && !itemstack.isEmpty()) {
			if (itemstack.getItem() == Items.COAL && (world.getDifficulty() == Difficulty.PEACEFUL || player.isCreative() || player.getStatusEffect(StatusEffects.FIRE_RESISTANCE) != null) && !world.isClient) {
				if (random.nextDouble() < FoxhoundModule.tameChance) {
					this.setOwner(player);
					this.navigation.stop();
					this.setTarget(null);
					this.setSitting(true);
					this.setHealth(20.0F);
					this.world.sendEntityStatus(this, (byte)7);
				} else {
					this.world.sendEntityStatus(this, (byte)6);
				}

				if (!player.isCreative())
					itemstack.decrement(1);
				return ActionResult.SUCCESS;
			}
		}

		//		if (itemstack.getItem() == Item.getItemFromBlock(TinyPotato.tiny_potato)) {
		//			this.playSound(SoundEvents.ENTITY_GENERIC_EAT, 1F, 0.5F + (float) Math.random() * 0.5F);
		//			if (!player.isCreative())
		//				itemstack.shrink(1);
		//
		//			this.timeUntilPotatoEmerges = 1201;
		//
		//			return true;
		//		}

		if (!world.isClient) {
			setWoke();
		}

		return super.interactMob(player, hand);
	}

	@Override
	public boolean canBreedWith(AnimalEntity otherAnimal) {
		return super.canBreedWith(otherAnimal) && otherAnimal instanceof FoxhoundEntity;
	}

	@Override
	public WolfEntity createChild(PassiveEntity otherParent) {
		WolfEntity entitywolf = new FoxhoundEntity(FoxhoundModule.foxhoundType, this.world);
		UUID uuid = this.getOwnerUuid();

		if (uuid != null) {
			entitywolf.setOwnerUuid(uuid);
			entitywolf.setTamed(true);
		}

		return entitywolf;
	}

	@Override
	public void writeCustomDataToTag(CompoundTag compound) {
		super.writeCustomDataToTag(compound);
		compound.putInt("OhLawdHeComin", timeUntilPotatoEmerges);
	}

	@Override
	public void readCustomDataFromTag(CompoundTag compound) {
		super.readCustomDataFromTag(compound);
		timeUntilPotatoEmerges = compound.getInt("OhLawdHeComin");
	}

	@Override
	protected SoundEvent getAmbientSound() {
		return isSleeping() ? null : super.getAmbientSound();
	}

	@Override
	public boolean isSleeping() {
		return dataTracker.get(SLEEPING);
	}

	public void setSleeping(boolean sleeping) {
		dataTracker.set(SLEEPING, sleeping);
	}


	public static boolean canSpawnHere(WorldAccess world, BlockPos pos, Random rand) {
		if (world.getLightLevel(LightType.SKY, pos) > rand.nextInt(32)) {
			return false;
		} else {
			int light = world.getWorld().isThundering() ? world.getLightLevel(pos, 10) : world.getLightLevel(pos);
			return light <= rand.nextInt(8);
		}
	}

	@Override
	public float getPathfindingFavor(BlockPos pos, WorldView worldIn) {
		return worldIn.getBlockState(pos.down()).getBlock() == Blocks.NETHERRACK ? 10.0F : worldIn.getBrightness(pos) - 0.5F;
	}

	public static boolean spawnPredicate(EntityType<? extends FoxhoundEntity> type, WorldAccess world, SpawnReason reason, BlockPos pos, Random rand) {
		return world.getDifficulty() != Difficulty.PEACEFUL && canSpawnHere(world, pos, rand);
	}

	public SleepGoal getSleepGoal() {
		return sleepGoal;
	}

	private void setWoke() {
		SleepGoal sleep = getSleepGoal();
		if(sleep != null) {
			setSleeping(false);
			sleep.setSleeping(false);
		}
	}

	@Nonnull
	@Override
	public Packet<?> createSpawnPacket() {
		return NetworkHooks.getEntitySpawningPacket(this);
	}
}
