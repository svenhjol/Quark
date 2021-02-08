package vazkii.quark.content.mobs.entity;

import java.util.List;
import java.util.Random;

import javax.annotation.Nonnull;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.Material;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.PistonBlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityData;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.goal.AnimalMateGoal;
import net.minecraft.entity.ai.goal.FollowParentGoal;
import net.minecraft.entity.ai.goal.LookAroundGoal;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.ai.goal.TemptGoal;
import net.minecraft.entity.ai.goal.WanderAroundGoal;
import net.minecraft.entity.ai.pathing.PathNodeType;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.recipe.Ingredient;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.Difficulty;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraftforge.common.ToolType;
import vazkii.quark.base.handler.MiscUtil;
import vazkii.quark.base.module.ModuleLoader;
import vazkii.quark.content.automation.module.IronRodModule;
import vazkii.quark.content.mobs.module.ToretoiseModule;
import vazkii.quark.content.world.module.CaveRootsModule;

public class ToretoiseEntity extends AnimalEntity {

	public static final int ORE_TYPES = 4; 
	private static final int DEFAULT_EAT_COOLDOWN = 20 * 60;
	public static final int ANGERY_TIME = 20; 

	private static final String TAG_TAMED = "tamed";
	private static final String TAG_ORE = "oreType";
	private static final String TAG_EAT_COOLDOWN = "eatCooldown";
	private static final String TAG_ANGERY_TICKS = "angeryTicks";

	public int rideTime;
	private boolean isTamed;
	private int eatCooldown;
	public int angeryTicks;

	private Ingredient goodFood;
	private LivingEntity lastAggressor;

	private static final TrackedData<Integer> ORE_TYPE = DataTracker.registerData(ToretoiseEntity.class, TrackedDataHandlerRegistry.INTEGER);

	public ToretoiseEntity(EntityType<? extends ToretoiseEntity> type, World world) {
		super(type, world);
		stepHeight = 1.0F;
		setPathfindingPenalty(PathNodeType.WATER, 1.0F);
	}

	@Override
	protected void initDataTracker() {
		super.initDataTracker();

		dataTracker.startTracking(ORE_TYPE, 0);
	}

	@Override
	protected void initGoals() {
		goalSelector.add(0, new AnimalMateGoal(this, 1.0));
		goalSelector.add(1, new TemptGoal(this, 1.25, getGoodFood(), false));
		goalSelector.add(2, new FollowParentGoal(this, 1.25));
		goalSelector.add(3, new WanderAroundGoal(this, 1.0D));
		goalSelector.add(4, new LookAtEntityGoal(this, PlayerEntity.class, 6.0F));
		goalSelector.add(5, new LookAroundGoal(this));
	}

	private Ingredient getGoodFood() {
		if(goodFood == null)
			goodFood = Ingredient.ofItems(ModuleLoader.INSTANCE.isModuleEnabled(CaveRootsModule.class) ? CaveRootsModule.rootItem : Items.CACTUS);

		return goodFood;
	}

	@Override
	public EntityData initialize(ServerWorldAccess p_213386_1_, LocalDifficulty p_213386_2_, SpawnReason p_213386_3_, EntityData p_213386_4_, CompoundTag p_213386_5_) {
		popOre(true);
		return p_213386_4_;
	}

	@Override
	public boolean canBreatheInWater() {
		return true;
	}

	@Override
	public boolean canFly() {
		return false;
	}

	@Override
	protected int getNextAirUnderwater(int air) {
		return air;
	}

	@Override
	public boolean isReadyToBreed() {
		return getOreType() == 0 && eatCooldown == 0;
	}

	@Override
	public SoundEvent getEatSound(ItemStack itemStackIn) {
		return null;
	}
//
//	@Override
//	public boolean isEntityInsideOpaqueBlock() {
//		return MiscUtil.isEntityInsideOpaqueBlock(this);
//	}

	@Override
	public void tick() {
		super.tick();

		Box aabb = getBoundingBox();
		double rheight = getOreType() == 0 ? 1 : 1.4;
		aabb = new Box(aabb.minX, aabb.minY, aabb.minZ, aabb.maxX, aabb.minY + rheight, aabb.maxZ);
		setBoundingBox(aabb);

		Entity riding = getVehicle();
		if(riding != null)
			rideTime++;
		else rideTime = 0;

		if(eatCooldown > 0)
			eatCooldown--;

		if(angeryTicks > 0 && isAlive()) {
			angeryTicks--;

			if(onGround) {
				int dangerRange = 3;
				double x = getX() + getWidth() / 2;
				double y = getY();
				double z = getZ() + getWidth() / 2;

				if(world instanceof ServerWorld) {
					if(angeryTicks == 3)
						playSound(SoundEvents.ENTITY_WITHER_BREAK_BLOCK, 1F, 0.2F);
					else if(angeryTicks == 0) {
						((ServerWorld) world).spawnParticles(ParticleTypes.CLOUD, x, y, z, 200, dangerRange, 0.5, dangerRange, 0);
					}
				}

				if(angeryTicks == 0) {
					Box hurtAabb = new Box(x - dangerRange, y - 1, z - dangerRange, x + dangerRange, y + 1, z + dangerRange);
					List<LivingEntity> hurtMeDaddy = world.getEntitiesByClass(LivingEntity.class, hurtAabb, e -> !(e instanceof ToretoiseEntity));

					LivingEntity aggressor = lastAggressor == null ? this : lastAggressor;
					DamageSource damageSource = DamageSource.mob(aggressor);
					for(LivingEntity e : hurtMeDaddy)
						e.damage(damageSource, 4 + world.getDifficulty().ordinal());
				}
			}
		}

		int ore = getOreType();
		if(ore != 0) breakOre: {
			Box ourBoundingBox = getBoundingBox();
			BlockPos min = new BlockPos(Math.round(ourBoundingBox.minX), Math.round(ourBoundingBox.minY), Math.round(ourBoundingBox.minZ));
			BlockPos max = new BlockPos(Math.round(ourBoundingBox.maxX), Math.round(ourBoundingBox.maxY), Math.round(ourBoundingBox.maxZ));

			for(int ix = min.getX(); ix <= max.getX(); ix++)
				for(int iy = min.getY(); iy <= max.getY(); iy++)
					for(int iz = min.getZ(); iz <= max.getZ(); iz++) {
						BlockPos test = new BlockPos(ix, iy, iz);
						BlockState state = world.getBlockState(test);
						if(state.getBlock() == Blocks.MOVING_PISTON) {
							BlockEntity tile = world.getBlockEntity(test);
							if(tile instanceof PistonBlockEntity) {
								PistonBlockEntity piston = (PistonBlockEntity) tile;
								BlockState pistonState = piston.getPushedBlock();
								if(pistonState.getBlock() == IronRodModule.iron_rod) {
									dropOre(ore);
									break breakOre;
								}
							}
						}
					}
		}
	}

	@Override
	public boolean damage(DamageSource source, float amount) {
		Entity e = source.getSource();
		int ore = getOreType();

		if(e instanceof LivingEntity) {
			LivingEntity living = (LivingEntity) e;
			ItemStack held = living.getMainHandStack();

			if(ore != 0 && held.getItem().getToolTypes(held).contains(ToolType.PICKAXE)) {
				if(!world.isClient) {
					if(held.isDamageable() && e instanceof PlayerEntity)
						MiscUtil.damageStack((PlayerEntity) e, Hand.MAIN_HAND, held, 1);

					dropOre(ore);
				}

				return false;
			}

			if(angeryTicks == 0) {
				angeryTicks = ANGERY_TIME;
				lastAggressor = living;
			}
		}

		return super.damage(source, amount);
	}

	public void dropOre(int ore) {
		playSound(SoundEvents.BLOCK_LANTERN_BREAK, 1F, 0.6F);

		Item drop = null;
		int countMult = 1;
		switch(ore) {
		case 1: 
			drop = Items.COAL;
			break;
		case 2:
			drop = Items.IRON_NUGGET;
			countMult *= 9;
			break;
		case 3:
			drop = Items.REDSTONE;
			countMult *= 3;
			break;
		case 4:
			drop = Items.LAPIS_LAZULI;
			countMult *= 2;
			break;
		}

		if(drop != null) {
			int count = 1;
			while(random.nextBoolean())
				count++;
			count *= countMult;

			dropStack(new ItemStack(drop, count), 1.2F);
		}

		dataTracker.set(ORE_TYPE, 0);
	}

	@Override
	public void lovePlayer(PlayerEntity player) {
		setLoveTicks(0);
	}

	@Override
	public void setLoveTicks(int ticks) {
		if(world.isClient)
			return;

		playSound(SoundEvents.ENTITY_GENERIC_EAT, 0.5F + 0.5F * world.random.nextInt(2), (world.random.nextFloat() - world.random.nextFloat()) * 0.2F + 1.0F);
		heal(8);

		if(!isTamed) {
			isTamed = true;

			if(world instanceof ServerWorld)
				((ServerWorld) world).spawnParticles(ParticleTypes.HEART, getX(), getY(), getZ(), 20, 0.5, 0.5, 0.5, 0);
		} else {
			popOre(false);
		}
	}

	private void popOre(boolean natural) {
		if(getOreType() == 0 && (natural || world.random.nextInt(3) == 0)) {
			int ore = random.nextInt(ORE_TYPES) + 1;
			dataTracker.set(ORE_TYPE, ore);

			if(!natural) {
				eatCooldown = DEFAULT_EAT_COOLDOWN;

				if(world instanceof ServerWorld) {
					((ServerWorld) world).spawnParticles(ParticleTypes.CLOUD, getX(), getY() + 0.5, getZ(), 100, 0.6, 0.6, 0.6, 0);
					playSound(SoundEvents.BLOCK_STONE_PLACE, 10F, 0.7F);
				}
			}
		}
	}

	@Override
	public boolean isBreedingItem(ItemStack stack) {
		return getGoodFood().test(stack);
	}

	@Override
	public boolean canImmediatelyDespawn(double distanceToClosestPlayer) {
		return !isTamed;
	}

	public static boolean spawnPredicate(EntityType<? extends ToretoiseEntity> type, ServerWorldAccess world, SpawnReason reason, BlockPos pos, Random rand) {
		return world.getDifficulty() != Difficulty.PEACEFUL && pos.getY() <= ToretoiseModule.maxYLevel && MiscUtil.validSpawnLight(world, pos, rand) && MiscUtil.validSpawnLocation(type, world, reason, pos);
	}

	@Override
	public boolean canSpawn(@Nonnull WorldAccess world, SpawnReason reason) {
		BlockState state = world.getBlockState((new BlockPos(getPos())).down());
		if (state.getMaterial() != Material.STONE)
			return false;

		return ToretoiseModule.dimensions.canSpawnHere(world);
	}

	@Override
	protected void jump() {
		// NO-OP
	}

	@Override
	public boolean handleFallDamage(float distance, float damageMultiplier) {
		return false;
	}

	@Override
	protected float getBaseMovementSpeedMultiplier() {
		return 0.9F;
	}

	@Override
	public boolean canBeLeashedBy(PlayerEntity player) {
		return false;
	}

	@Override
	protected float getSoundPitch() {
		return (random.nextFloat() - random.nextFloat()) * 0.2F + 0.6F;
	}

	@Override
	protected SoundEvent getAmbientSound() {
		return SoundEvents.ENTITY_TURTLE_AMBIENT_LAND;
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
		return SoundEvents.ENTITY_TURTLE_HURT;
	}

	@Override
	protected SoundEvent getDeathSound() {
		return SoundEvents.ENTITY_TURTLE_DEATH;
	}

	public int getOreType() {
		return dataTracker.get(ORE_TYPE);
	}

	@Override
	public void writeCustomDataToTag(CompoundTag compound) {
		super.writeCustomDataToTag(compound);
		compound.putBoolean(TAG_TAMED, isTamed);
		compound.putInt(TAG_ORE, getOreType());
		compound.putInt(TAG_EAT_COOLDOWN, eatCooldown);
		compound.putInt(TAG_ANGERY_TICKS, angeryTicks);
	}

	@Override
	public void readCustomDataFromTag(CompoundTag compound) {
		super.readCustomDataFromTag(compound);
		isTamed = compound.getBoolean(TAG_TAMED);
		dataTracker.set(ORE_TYPE, compound.getInt(TAG_ORE));
		eatCooldown = compound.getInt(TAG_EAT_COOLDOWN);
		angeryTicks = compound.getInt(TAG_ANGERY_TICKS);
	}

    public static DefaultAttributeContainer.Builder prepareAttributes() {
        return MobEntity.createMobAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 60.0D)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.08D) 
                .add(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE, 1.0D);
    }

	@Override // createChild
	public ToretoiseEntity createChild(ServerWorld sworld, PassiveEntity otherParent) {
		ToretoiseEntity e = new ToretoiseEntity(ToretoiseModule.toretoiseType, world);
		e.remove(); // kill the entity cuz toretoise doesn't make babies
		return e;
	}

}