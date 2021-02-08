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
package vazkii.quark.content.mobs.entity;

import java.util.List;
import java.util.Random;
import java.util.UUID;

import javax.annotation.Nonnull;

import net.minecraft.block.Blocks;
import net.minecraft.entity.AgeableEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ILivingEntityData;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.goal.BegGoal;
import net.minecraft.entity.ai.goal.BreedGoal;
import net.minecraft.entity.ai.goal.FollowOwnerGoal;
import net.minecraft.entity.ai.goal.HurtByTargetGoal;
import net.minecraft.entity.ai.goal.LeapAtTargetGoal;
import net.minecraft.entity.ai.goal.LookAtGoal;
import net.minecraft.entity.ai.goal.LookRandomlyGoal;
import net.minecraft.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.entity.ai.goal.NearestAttackableTargetGoal;
import net.minecraft.entity.ai.goal.NonTamedTargetGoal;
import net.minecraft.entity.ai.goal.OwnerHurtTargetGoal;
import net.minecraft.entity.ai.goal.SitGoal;
import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.entity.ai.goal.WaterAvoidingRandomWalkingGoal;
import net.minecraft.entity.monster.AbstractSkeletonEntity;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.RabbitEntity;
import net.minecraft.entity.passive.SheepEntity;
import net.minecraft.entity.passive.WolfEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.DyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.particles.BasicParticleType;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.potion.Effects;
import net.minecraft.tileentity.AbstractFurnaceTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.registry.DynamicRegistries;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.WorldGenRegistries;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.IServerWorld;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.LightType;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeRegistry;
import net.minecraft.world.biome.Biomes;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fml.network.NetworkHooks;
import vazkii.quark.base.handler.MiscUtil;
import vazkii.quark.content.mobs.ai.FindPlaceToSleepGoal;
import vazkii.quark.content.mobs.ai.SleepGoal;
import vazkii.quark.content.mobs.module.FoxhoundModule;
import vazkii.quark.content.tweaks.ai.WantLoveGoal;

public class FoxhoundEntity extends WolfEntity implements IMob {

	public static final ResourceLocation FOXHOUND_LOOT_TABLE = new ResourceLocation("quark", "entities/foxhound");

	private static final DataParameter<Boolean> TEMPTATION = EntityDataManager.createKey(FoxhoundEntity.class, DataSerializers.BOOLEAN);
	private static final DataParameter<Boolean> SLEEPING = EntityDataManager.createKey(FoxhoundEntity.class, DataSerializers.BOOLEAN);
	private static final DataParameter<Boolean> IS_BLUE = EntityDataManager.createKey(FoxhoundEntity.class, DataSerializers.BOOLEAN);

	private int timeUntilPotatoEmerges = 0;

	public FoxhoundEntity(EntityType<? extends FoxhoundEntity> type, World worldIn) {
		super(type, worldIn);
		this.setPathPriority(PathNodeType.WATER, -1.0F);
		this.setPathPriority(PathNodeType.LAVA, 1.0F);
		this.setPathPriority(PathNodeType.DANGER_FIRE, 1.0F);
		this.setPathPriority(PathNodeType.DAMAGE_FIRE, 1.0F);
	}

	@Override
	protected void registerData() {
		super.registerData();
		setCollarColor(DyeColor.ORANGE);
		dataManager.register(TEMPTATION, false);
		dataManager.register(SLEEPING, false);
		dataManager.register(IS_BLUE, false);
	}

	@Override
	public int getMaxSpawnedInChunk() {
		return 4;
	}

	@Override
	public boolean isNoDespawnRequired() {
		return super.isNoDespawnRequired();
	}

	@Override
	public boolean preventDespawn() {
		return isTamed();
	}

	@Override
	public boolean canDespawn(double distanceToClosestPlayer) {
		return !isTamed();
	}

//	@Override
//	public boolean isEntityInsideOpaqueBlock() {
//		return MiscUtil.isEntityInsideOpaqueBlock(this);
//	}
	
	@Override
	public ILivingEntityData onInitialSpawn(IServerWorld worldIn, DifficultyInstance difficultyIn, SpawnReason reason, ILivingEntityData spawnDataIn, CompoundNBT dataTag) {
		Biome biome = worldIn.getBiome(new BlockPos(getPositionVec()));
		ResourceLocation res = worldIn.getWorld().func_241828_r().getRegistry(Registry.BIOME_KEY).getKey(biome);
		
		if(res.equals(Biomes.SOUL_SAND_VALLEY.getLocation()))
			setBlue(true);
		
		return super.onInitialSpawn(worldIn, difficultyIn, reason, spawnDataIn, dataTag);
	}

	@Override
	public void tick() {
		super.tick();

		if (!world.isRemote && world.getDifficulty() == Difficulty.PEACEFUL && !isTamed()) {
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
			if (owner != null && owner.getDistanceSq(this) < 1 && !owner.isInWater() && !owner.isImmuneToFire() && (!(owner instanceof PlayerEntity) || !((PlayerEntity) owner).isCreative()))
				owner.setFire(5);
		}

		Vector3d pos = getPositionVec();
		if(this.world.isRemote) {
			BasicParticleType particle = ParticleTypes.FLAME;
			if(isSleeping())
				particle = ParticleTypes.SMOKE;
			else if(isBlue())
				particle = ParticleTypes.SOUL_FIRE_FLAME;
			
			this.world.addParticle(particle, pos.x + (this.rand.nextDouble() - 0.5D) * this.getWidth(), pos.y + (this.rand.nextDouble() - 0.5D) * this.getHeight(), pos.z + (this.rand.nextDouble() - 0.5D) * this.getWidth(), 0.0D, 0.0D, 0.0D);
		}

		if(isTamed()) {
			BlockPos below = getPosition().down();
			TileEntity tile = world.getTileEntity(below);
			if (tile instanceof AbstractFurnaceTileEntity) {
				AbstractFurnaceTileEntity furnace = (AbstractFurnaceTileEntity) tile;
				int cookTime = furnace.cookTime;
				if (cookTime > 0 && cookTime % 3 == 0) {
					List<FoxhoundEntity> foxhounds = world.getEntitiesWithinAABB(FoxhoundEntity.class, new AxisAlignedBB(getPosition()),
							(fox) -> fox != null && fox.isTamed());
					if(!foxhounds.isEmpty() && foxhounds.get(0) == this)
						furnace.cookTime = furnace.cookTime == 3 ? 5 :Math.min(furnace.cookTimeTotal - 1, cookTime + 1);
				}
			}
		}
	}

	@Override
	public boolean isWet() {
		return false;
	}

	@Nonnull
	@Override
	protected ResourceLocation getLootTable() {
		return FOXHOUND_LOOT_TABLE;
	}

	protected SleepGoal sleepGoal;

	@Override
	protected void registerGoals() {
		this.sleepGoal = new SleepGoal(this);
		this.goalSelector.addGoal(1, new SwimGoal(this));
		this.goalSelector.addGoal(2, this.sleepGoal);
		this.goalSelector.addGoal(3, new SitGoal(this));
		this.goalSelector.addGoal(4, new LeapAtTargetGoal(this, 0.4F));
		this.goalSelector.addGoal(5, new MeleeAttackGoal(this, 1.0D, true));
		this.goalSelector.addGoal(6, new FollowOwnerGoal(this, 1.0D, 10.0F, 2.0F, false));
		this.goalSelector.addGoal(7, new BreedGoal(this, 1.0D));
		this.goalSelector.addGoal(8, new FindPlaceToSleepGoal(this, 0.8D, true));
		this.goalSelector.addGoal(9, new FindPlaceToSleepGoal(this, 0.8D, false));
		this.goalSelector.addGoal(10, new WaterAvoidingRandomWalkingGoal(this, 1.0D));
		this.goalSelector.addGoal(11, new BegGoal(this, 8.0F));
		this.goalSelector.addGoal(12, new LookAtGoal(this, PlayerEntity.class, 8.0F));
		this.goalSelector.addGoal(12, new LookRandomlyGoal(this));
		this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
		this.targetSelector.addGoal(2, new OwnerHurtTargetGoal(this));
		this.targetSelector.addGoal(3, new HurtByTargetGoal(this).setCallsForHelp());
		this.targetSelector.addGoal(4, new NonTamedTargetGoal<>(this, AnimalEntity.class, false,
				target -> target instanceof SheepEntity || target instanceof RabbitEntity));
		this.targetSelector.addGoal(4, new NonTamedTargetGoal<>(this, PlayerEntity.class, false,
				target -> !isTamed()));
//		this.targetSelector.addGoal(5, new NearestAttackableTargetGoal<>(this, AbstractSkeletonEntity.class, false));
	}

	public int getAngerTime() {
		if (!isTamed() && world.getDifficulty() != Difficulty.PEACEFUL)
			return 0;
		return super.getAngerTime();
	}
	
	@Override
	public boolean attackEntityAsMob(Entity entityIn) {
		if (entityIn.getType().isImmuneToFire()) {
			if (entityIn instanceof PlayerEntity)
				return false;
			return super.attackEntityAsMob(entityIn);
		}

		boolean flag = entityIn.attackEntityFrom(DamageSource.causeMobDamage(this).setFireDamage(),
				((int)this.getAttributeValue(Attributes.ATTACK_DAMAGE)));

		if (flag) {
			entityIn.setFire(5);
			this.applyEnchantments(this, entityIn);
		}

		return flag;
	}

	@Override
	public boolean attackEntityFrom(@Nonnull DamageSource source, float amount) {
		setWoke();
		return super.attackEntityFrom(source, amount);
	}

	@Nonnull
	@Override
	public ActionResultType func_230254_b_(PlayerEntity player, @Nonnull Hand hand) {
		ItemStack itemstack = player.getHeldItem(hand);

		if(itemstack.getItem() == Items.BONE && !isTamed())
			return ActionResultType.PASS;

		if (!this.isTamed() && !itemstack.isEmpty()) {
			if (itemstack.getItem() == Items.COAL && (world.getDifficulty() == Difficulty.PEACEFUL || player.isCreative() || player.getActivePotionEffect(Effects.FIRE_RESISTANCE) != null) && !world.isRemote) {
				if (rand.nextDouble() < FoxhoundModule.tameChance) {
					this.setTamedBy(player);
					this.navigator.clearPath();
					this.setAttackTarget(null);
					this.func_233687_w_(true);
					this.setHealth(20.0F);
					this.world.setEntityState(this, (byte)7);
				} else {
					this.world.setEntityState(this, (byte)6);
				}

				if (!player.isCreative())
					itemstack.shrink(1);
				return ActionResultType.SUCCESS;
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

		if (!world.isRemote) {
			setWoke();
		}

		return super.func_230254_b_(player, hand);
	}

	@Override
	public boolean canMateWith(AnimalEntity otherAnimal) {
		return super.canMateWith(otherAnimal) && otherAnimal instanceof FoxhoundEntity;
	}

	@Override // createChild
	public WolfEntity func_241840_a(ServerWorld sworld, AgeableEntity otherParent) {
		FoxhoundEntity kid = new FoxhoundEntity(FoxhoundModule.foxhoundType, this.world);
		UUID uuid = this.getOwnerId();

		if (uuid != null) {
			kid.setOwnerId(uuid);
			kid.setTamed(true);
		}
		
		if(isBlue())
			kid.setBlue(true);

		return kid;
	}

	@Override
	public void writeAdditional(CompoundNBT compound) {
		super.writeAdditional(compound);
		
		compound.putInt("OhLawdHeComin", timeUntilPotatoEmerges);
		compound.putBoolean("IsSlep", isSleeping());
		compound.putBoolean("IsBlue", isBlue());
	}

	@Override
	public void readAdditional(CompoundNBT compound) {
		super.readAdditional(compound);
		
		timeUntilPotatoEmerges = compound.getInt("OhLawdHeComin");
		setSleeping(compound.getBoolean("IsSlep"));
		setBlue(compound.getBoolean("IsBlue"));
	}

	@Override
	protected SoundEvent getAmbientSound() {
		return isSleeping() ? null : super.getAmbientSound();
	}

	@Override
	public boolean isSleeping() {
		return dataManager.get(SLEEPING);
	}
	
	public boolean isBlue() {
		return dataManager.get(IS_BLUE);
	}

	public void setSleeping(boolean sleeping) {
		dataManager.set(SLEEPING, sleeping);
	}
	
	public void setBlue(boolean blue) {
		dataManager.set(IS_BLUE, blue);
	}

//	public static boolean canSpawnHere(IServerWorld world, BlockPos pos, Random rand) {
//		if (world.getLightFor(LightType.SKY, pos) > rand.nextInt(32)) {
//			return false;
//		} else {
//			int light = world.getWorld().isThundering() ? world.getNeighborAwareLightSubtracted(pos, 10) : world.getLight(pos);
//			return light <= rand.nextInt(8);
//		}
//	}

	@Override
	public float getBlockPathWeight(BlockPos pos, IWorldReader worldIn) {
		return worldIn.getBlockState(pos.down()).getBlock().isIn(FoxhoundModule.foxhoundSpawnableTag) ? 10.0F : worldIn.getBrightness(pos) - 0.5F;
	}
	
	public static boolean spawnPredicate(EntityType<? extends FoxhoundEntity> type, IServerWorld world, SpawnReason reason, BlockPos pos, Random rand) {
		return world.getDifficulty() != Difficulty.PEACEFUL && world.getBlockState(pos.down()).isIn(FoxhoundModule.foxhoundSpawnableTag);
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
	public IPacket<?> createSpawnPacket() {
		return NetworkHooks.getEntitySpawningPacket(this);
	}
}
