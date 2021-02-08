/**
 * This class was created by <WireSegal>. It's distributed as
 * part of the Quark Mod. Get the Source Code in github:
 * https://github.com/Vazkii/Quark
 * <p>
 * Quark is Open Source and distributed under the
 * CC-BY-NC-SA 3.0 License: https://creativecommons.org/licenses/by-nc-sa/3.0/deed.en_GB
 * <p>
 * File Created @ [Jul 13, 2019, 19:51 AM (EST)]
 */
package vazkii.quark.content.mobs.entity;

import java.util.Random;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.collect.Lists;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityGroup;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LightningEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.goal.AnimalMateGoal;
import net.minecraft.entity.ai.goal.EscapeDangerGoal;
import net.minecraft.entity.ai.goal.FollowParentGoal;
import net.minecraft.entity.ai.goal.LookAroundGoal;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.ai.goal.TemptGoal;
import net.minecraft.entity.ai.goal.WanderAroundFarGoal;
import net.minecraft.entity.ai.pathing.PathNodeType;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributeModifier.Operation;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.Ingredient;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.tag.ItemTags;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Difficulty;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;
import net.minecraftforge.fml.network.NetworkHooks;
import vazkii.quark.base.handler.MiscUtil;
import vazkii.quark.base.handler.QuarkSounds;
import vazkii.quark.content.mobs.ai.RaveGoal;
import vazkii.quark.content.mobs.module.CrabsModule;

public class CrabEntity extends AnimalEntity implements IEntityAdditionalSpawnData {

	public static final Identifier CRAB_LOOT_TABLE = new Identifier("quark", "entities/crab");

	private static final TrackedData<Float> SIZE_MODIFIER = DataTracker.registerData(CrabEntity.class, TrackedDataHandlerRegistry.FLOAT);
	private static final TrackedData<Integer> VARIANT = DataTracker.registerData(CrabEntity.class, TrackedDataHandlerRegistry.INTEGER);

	private static int lightningCooldown;
	private Ingredient temptationItems;

	private boolean crabRave;
	private BlockPos jukeboxPosition;

	public CrabEntity(EntityType<? extends CrabEntity> type, World worldIn) {
		this(type, worldIn, 1);
	}

	public CrabEntity(EntityType<? extends CrabEntity> type, World worldIn, float sizeModifier) {
		super(type, worldIn);
		this.setPathfindingPenalty(PathNodeType.LAVA, -1.0F);
		if (sizeModifier != 1)
			dataTracker.set(SIZE_MODIFIER, sizeModifier);
	}

	public static boolean spawnPredicate(EntityType<? extends AnimalEntity> type, WorldAccess world, SpawnReason reason, BlockPos pos, Random random) {
		return world.getBlockState(pos.down()).getMaterial() == Material.AGGREGATE && world.getLightLevel(pos) > 8;
	}

	public static void rave(WorldAccess world, BlockPos pos, boolean raving) {
		for(CrabEntity crab : world.getNonSpectatingEntities(CrabEntity.class, (new Box(pos)).expand(3.0D)))
			crab.party(pos, raving);
	}

	@Override
	public float getPathfindingFavor(BlockPos pos, WorldView world) {
		return world.getBlockState(pos.down()).getBlock() == Blocks.SAND ? 10.0F : world.getBrightness(pos) - 0.5F;
	}

	@Override
	public boolean canBreatheInWater() {
		return true;
	}

	@Nonnull
	@Override
	public EntityGroup getGroup() {
		return EntityGroup.ARTHROPOD;
	}

	@Override
	protected void initDataTracker() {
		super.initDataTracker();

		dataTracker.startTracking(SIZE_MODIFIER, 1f);
		dataTracker.startTracking(VARIANT, -1);
	}

	@Nullable
	@Override
	protected SoundEvent getAmbientSound() {
		return QuarkSounds.ENTITY_CRAB_IDLE;
	}

	@Nullable
	@Override
	protected SoundEvent getDeathSound() {
		return QuarkSounds.ENTITY_CRAB_DIE;
	}

	@Nullable
	@Override
	protected SoundEvent getHurtSound(DamageSource source) {
		return QuarkSounds.ENTITY_CRAB_HURT;
	}

	@Override
	protected float getActiveEyeHeight(EntityPose pose, EntityDimensions size) {
		return 0.2f * size.height;
	}

	public float getSizeModifier() {
		return dataTracker.get(SIZE_MODIFIER);
	}

	@Override
	protected void initGoals() {
		this.goalSelector.add(1, new EscapeDangerGoal(this, 1.25D));
		this.goalSelector.add(2, new RaveGoal(this));
		this.goalSelector.add(3, new AnimalMateGoal(this, 1.0D));
		this.goalSelector.add(4, new TemptGoal(this, 1.2D, false, getTemptationItems()));
		this.goalSelector.add(5, new FollowParentGoal(this, 1.1D));
		this.goalSelector.add(6, new WanderAroundFarGoal(this, 1.0D));
		this.goalSelector.add(7, new LookAtEntityGoal(this, PlayerEntity.class, 6.0F));
		this.goalSelector.add(8, new LookAroundGoal(this));
	}

	public static DefaultAttributeContainer.Builder prepareAttributes() {
		return MobEntity.createMobAttributes()
				.add(EntityAttributes.GENERIC_MAX_HEALTH, 20.0D)
				.add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.25D)
				.add(EntityAttributes.GENERIC_ARMOR, 3.0D)
				.add(EntityAttributes.GENERIC_ARMOR_TOUGHNESS, 2.0D)
				.add(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE, 0.5D);
	}

//	@Override
//	public boolean isEntityInsideOpaqueBlock() {
//		return MiscUtil.isEntityInsideOpaqueBlock(this);
//	}

	@Override
	public void tick() {
		super.tick();

		if(!world.isClient && dataTracker.get(VARIANT) == -1) {
			int variant = 0;
			if(random.nextBoolean()) {
				variant += random.nextInt(2) + 1;
			}

			dataTracker.set(VARIANT, variant);
		}

		if (touchingWater)
			stepHeight = 1F;
		else
			stepHeight = 0.6F;

		if (lightningCooldown > 0) {
			lightningCooldown--;
			extinguish();
		}

		Vec3d pos = getPos();
		if(isRaving() && (jukeboxPosition == null || jukeboxPosition.getSquaredDistance(pos.x, pos.y, pos.z, true) > 24.0D || world.getBlockState(jukeboxPosition).getBlock() != Blocks.JUKEBOX))
			party(null, false);

		if(isRaving() && world.isClient && age % 10 == 0) {
			BlockPos below = getBlockPos().down();
			BlockState belowState = world.getBlockState(below);
			if(belowState.getMaterial() == Material.AGGREGATE)
				world.syncWorldEvent(2001, below, Block.getRawIdFromState(belowState));
		}
	}

	@Nonnull
	@Override
	public EntityDimensions getDimensions(EntityPose poseIn) {
		return super.getDimensions(poseIn).scaled(this.getSizeModifier());
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
	public boolean isInvulnerableTo(@Nonnull DamageSource source) {
		return super.isInvulnerableTo(source) ||
				source == DamageSource.LIGHTNING_BOLT ||
				getSizeModifier() > 1 && source.isFire();
	}
	
	@Override
	public void onStruckByLightning(ServerWorld sworld, LightningEntity lightningBolt) { // onStruckByLightning
		if (lightningCooldown > 0 || world.isClient)
			return;

		float sizeMod = getSizeModifier();
		if (sizeMod <= 15) {

			this.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH).addPersistentModifier(new EntityAttributeModifier("Lightning Bonus", 0.5, Operation.ADDITION));
			this.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED).addPersistentModifier(new EntityAttributeModifier("Lightning Debuff", -0.05, Operation.ADDITION));
			this.getAttributeInstance(EntityAttributes.GENERIC_ARMOR).addPersistentModifier(new EntityAttributeModifier("Lightning Bonus", 0.125, Operation.ADDITION));

			float sizeModifier = Math.min(sizeMod + 1, 16);
			this.dataTracker.set(SIZE_MODIFIER, sizeModifier);
			calculateDimensions();

			lightningCooldown = 150;
		}
	}

	@Override
	public void pushAwayFrom(@Nonnull Entity entityIn) {
		if (getSizeModifier() <= 1)
			super.pushAwayFrom(entityIn);
	}

	@Override
	protected void pushAway(Entity entityIn) {
		super.pushAway(entityIn);
		if (world.getDifficulty() != Difficulty.PEACEFUL) {
			if (entityIn instanceof LivingEntity && !(entityIn instanceof CrabEntity))
				entityIn.damage(DamageSource.CACTUS, 1f);
		}
	}

	@Override
	public boolean isBreedingItem(ItemStack stack) {
		return !stack.isEmpty() && getTemptationItems().test(stack);
	}

	private Ingredient getTemptationItems() {
		if(temptationItems == null)
			temptationItems =  Ingredient.merge(Lists.newArrayList(
					Ingredient.ofItems(Items.WHEAT, Items.CHICKEN),
					Ingredient.fromTag(ItemTags.FISHES)
					));
		
		return temptationItems;
	}

	@Nullable
	@Override // createChild
	public PassiveEntity createChild(ServerWorld sworld, @Nonnull PassiveEntity other) {
		return new CrabEntity(CrabsModule.crabType, world);
	}
	
	@Nonnull
	@Override
	protected Identifier getLootTableId() {
		return CRAB_LOOT_TABLE;
	}

	public int getVariant() {
		return Math.max(0, dataTracker.get(VARIANT));
	}

	public void party(BlockPos pos, boolean isPartying) {
		// A separate method, due to setPartying being side-only.
		jukeboxPosition = pos;
		crabRave = isPartying;
	}

	@Override
	@Environment(EnvType.CLIENT)
	public void setNearbySongPlaying(BlockPos pos, boolean isPartying) {
		party(pos, isPartying);
	}

	public boolean isRaving() {
		return crabRave;
	}

	@Override
	public void onTrackedDataSet(@Nonnull TrackedData<?> parameter) {
		if (parameter.equals(SIZE_MODIFIER))
			calculateDimensions();

		super.onTrackedDataSet(parameter);
	}

	@Nonnull
	@Override
	public Packet<?> createSpawnPacket() {
		return NetworkHooks.getEntitySpawningPacket(this);
	}

	@Override
	public void writeSpawnData(PacketByteBuf buffer) {
		buffer.writeFloat(getSizeModifier());
	}

	@Override
	public void readSpawnData(PacketByteBuf buffer) {
		dataTracker.set(SIZE_MODIFIER, buffer.readFloat());
	}

	@Override
	public void readCustomDataFromTag(CompoundTag compound) {
		super.readCustomDataFromTag(compound);

		lightningCooldown = compound.getInt("LightningCooldown");

		if (compound.contains("EnemyCrabRating")) {
			float sizeModifier = compound.getFloat("EnemyCrabRating");
			dataTracker.set(SIZE_MODIFIER, sizeModifier);
		}

		if(compound.contains("Variant"))
			dataTracker.set(VARIANT, compound.getInt("Variant"));
	}

	@Override
	public void writeCustomDataToTag(CompoundTag compound) {
		super.writeCustomDataToTag(compound);
		compound.putFloat("EnemyCrabRating", getSizeModifier());
		compound.putInt("LightningCooldown", lightningCooldown);
		compound.putInt("Variant", dataTracker.get(VARIANT));
	}


}
