package vazkii.quark.content.mobs.entity;

import java.util.List;
import java.util.Random;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.collect.Sets;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityData;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.goal.PrioritizedGoal;
import net.minecraft.entity.ai.goal.TemptGoal;
import net.minecraft.entity.ai.goal.WanderAroundFarGoal;
import net.minecraft.entity.ai.pathing.PathNodeType;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Packet;
import net.minecraft.particle.BlockStateParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.recipe.Ingredient;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.Difficulty;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.extensions.IForgeWorldServer;
import net.minecraftforge.fml.network.NetworkHooks;
import vazkii.quark.base.handler.MiscUtil;
import vazkii.quark.base.handler.QuarkSounds;
import vazkii.quark.base.module.ModuleLoader;
import vazkii.quark.base.util.IfFlagGoal;
import vazkii.quark.content.mobs.ai.ActWaryGoal;
import vazkii.quark.content.mobs.ai.FavorBlockGoal;
import vazkii.quark.content.mobs.ai.RunAndPoofGoal;
import vazkii.quark.content.mobs.module.FrogsModule;
import vazkii.quark.content.mobs.module.StonelingsModule;

public class StonelingEntity extends PathAwareEntity {

	public static final Identifier CARRY_LOOT_TABLE = new Identifier("quark", "entities/stoneling_carry");

	private static final TrackedData<ItemStack> CARRYING_ITEM = DataTracker.registerData(StonelingEntity.class, TrackedDataHandlerRegistry.ITEM_STACK);
	private static final TrackedData<Byte> VARIANT = DataTracker.registerData(StonelingEntity.class, TrackedDataHandlerRegistry.BYTE);
	private static final TrackedData<Float> HOLD_ANGLE = DataTracker.registerData(StonelingEntity.class, TrackedDataHandlerRegistry.FLOAT);

	private static final String TAG_CARRYING_ITEM = "carryingItem";
	private static final String TAG_VARIANT = "variant";
	private static final String TAG_HOLD_ANGLE = "itemAngle";
	private static final String TAG_PLAYER_MADE = "playerMade";

	private ActWaryGoal waryGoal;

	private boolean isTame;

	public StonelingEntity(EntityType<? extends StonelingEntity> type, World worldIn) {
		super(type, worldIn);
		this.setPathfindingPenalty(PathNodeType.DAMAGE_CACTUS, 1.0F);
		this.setPathfindingPenalty(PathNodeType.DANGER_CACTUS, 1.0F);
	}

	@Override
	protected void initDataTracker() {
		super.initDataTracker();

		dataTracker.startTracking(CARRYING_ITEM, ItemStack.EMPTY);
		dataTracker.startTracking(VARIANT, (byte) 0);
		dataTracker.startTracking(HOLD_ANGLE, 0F);
	}

	@Override
	protected void initGoals() {
		goalSelector.add(5, new WanderAroundFarGoal(this, 0.2, 0.98F));
		goalSelector.add(4, new FavorBlockGoal(this, 0.2, s -> s.getBlock().isIn(Tags.Blocks.ORES_DIAMOND)));
		goalSelector.add(3, new IfFlagGoal(new TemptGoal(this, 0.6, Ingredient.fromTag(Tags.Items.GEMS_DIAMOND), false), () -> StonelingsModule.enableDiamondHeart && !StonelingsModule.tamableStonelings));
		goalSelector.add(2, new RunAndPoofGoal<>(this, PlayerEntity.class, 4, 0.5, 0.5));
		goalSelector.add(1, waryGoal = new ActWaryGoal(this, 0.1, 6, () -> StonelingsModule.cautiousStonelings));
		goalSelector.add(0, new IfFlagGoal(new TemptGoal(this, 0.6, Ingredient.fromTag(Tags.Items.GEMS_DIAMOND), false), () -> StonelingsModule.tamableStonelings));

	}

	public static DefaultAttributeContainer.Builder prepareAttributes() {
        return MobEntity.createMobAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 8.0D)
                .add(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE, 1D);
    }

	@Override
	public void tick() {
		super.tick();

		if (touchingWater)
			stepHeight = 1F;
		else
			stepHeight = 0.6F;

		if (!world.isClient && world.getDifficulty() == Difficulty.PEACEFUL && !isTame) {
			remove();
			for (Entity passenger : getPassengersDeep())
				if (!(passenger instanceof PlayerEntity))
					passenger.remove();
		}

		this.prevBodyYaw = this.prevYaw;
		this.bodyYaw = this.yaw;
	}

	@Override
	public SpawnGroup getClassification(boolean forSpawnCount) {
		if (isTame)
			return SpawnGroup.CREATURE;
		return SpawnGroup.MONSTER;
	}

	@Override
	public boolean canImmediatelyDespawn(double distanceToClosestPlayer) {
		return !isTame;
	}
	
	@Override
	public void checkDespawn() {
		boolean wasAlive = isAlive();
		super.checkDespawn();
		if (!isAlive() && wasAlive)
			for (Entity passenger : getPassengersDeep())
				if (!(passenger instanceof PlayerEntity))
					passenger.remove();
	}

	@Override // processInteract
	public ActionResult interactMob(PlayerEntity player, @Nonnull Hand hand) {
		ItemStack stack = player.getStackInHand(hand);

		if(!stack.isEmpty() && stack.getItem() == Items.NAME_TAG)
			return stack.getItem().useOnEntity(stack, player, this, hand);
		else
			return super.interactMob(player, hand);
	}

	@Nonnull
	@Override
	public ActionResult interactAt(PlayerEntity player, Vec3d vec, Hand hand) {
		if(hand == Hand.MAIN_HAND && isAlive()) {
			ItemStack playerItem = player.getStackInHand(hand);
			Vec3d pos = getPos();

			if(!world.isClient) {
				if (isPlayerMade()) {
					if (!player.isSneaky() && !playerItem.isEmpty()) {

						EnumStonelingVariant currentVariant = getVariant();
						EnumStonelingVariant targetVariant = null;
						Block targetBlock = null;
						mainLoop: for (EnumStonelingVariant variant : EnumStonelingVariant.values()) {
							for (Block block : variant.getBlocks()) {
								if (block.asItem() == playerItem.getItem()) {
									targetVariant = variant;
									targetBlock = block;
									break mainLoop;
								}
							}
						}

						if (targetVariant != null) {
							if (world instanceof ServerWorld) {
								((ServerWorld) world).spawnParticles(ParticleTypes.HEART, pos.x, pos.y + getHeight(), pos.z, 1, 0.1, 0.1, 0.1, 0.1);
								if (targetVariant != currentVariant)
									((ServerWorld) world).spawnParticles(new BlockStateParticleEffect(ParticleTypes.BLOCK, targetBlock.getDefaultState()), pos.x, pos.y + getHeight() / 2, pos.z, 16, 0.1, 0.1, 0.1, 0.25);
							}

							if (targetVariant != currentVariant) {
								playSound(QuarkSounds.ENTITY_STONELING_EAT, 1F, 1F);
								dataTracker.set(VARIANT, targetVariant.getIndex());
							}

							playSound(QuarkSounds.ENTITY_STONELING_PURR, 1F, 1F + world.random.nextFloat() * 1F);

							heal(1);

							if (!player.abilities.creativeMode)
								playerItem.decrement(1);

							return ActionResult.SUCCESS;
						}

						return ActionResult.PASS;
					}

					ItemStack stonelingItem = dataTracker.get(CARRYING_ITEM);

					if (!stonelingItem.isEmpty() || !playerItem.isEmpty()) {
						player.setStackInHand(hand, stonelingItem.copy());
						dataTracker.set(CARRYING_ITEM, playerItem.copy());

						if (playerItem.isEmpty())
							playSound(QuarkSounds.ENTITY_STONELING_GIVE, 1F, 1F);
						else playSound(QuarkSounds.ENTITY_STONELING_TAKE, 1F, 1F);
					}
				} else if (StonelingsModule.tamableStonelings && playerItem.getItem().isIn(Tags.Items.GEMS_DIAMOND)) {
					heal(8);

					setPlayerMade(true);

					playSound(QuarkSounds.ENTITY_STONELING_PURR, 1F, 1F + world.random.nextFloat() * 1F);

					if (!player.abilities.creativeMode)
						playerItem.decrement(1);

					if (world instanceof ServerWorld)
						((ServerWorld) world).spawnParticles(ParticleTypes.HEART, pos.x, pos.y + getHeight(), pos.z, 4, 0.1, 0.1, 0.1, 0.1);

					return ActionResult.SUCCESS;
				}
			}
		}

		return ActionResult.PASS;
	}

	@Nullable
	@Override
	public EntityData initialize(ServerWorldAccess world, LocalDifficulty difficulty, SpawnReason spawnReason, @Nullable EntityData data, @Nullable CompoundTag compound) {
		byte variant;
		if (data instanceof EnumStonelingVariant)
			variant = ((EnumStonelingVariant) data).getIndex();
		else
			variant = (byte) world.getRandom().nextInt(EnumStonelingVariant.values().length);

		dataTracker.set(VARIANT, variant);
		dataTracker.set(HOLD_ANGLE, world.getRandom().nextFloat() * 90 - 45);

		if(!isTame && !world.isClient() && world instanceof IForgeWorldServer) {
			if (ModuleLoader.INSTANCE.isModuleEnabled(FrogsModule.class) && random.nextDouble() < 0.01) {
				FrogEntity frog = new FrogEntity(FrogsModule.frogType, world.toServerWorld(), 0.25f);
				Vec3d pos = getPos();

				frog.updatePosition(pos.x, pos.y, pos.z);
				world.spawnEntity(frog);
				frog.startRiding(this);
			} else {
				List<ItemStack> items = ((IForgeWorldServer) world).getWorldServer().getServer().getLootManager()
						.getTable(CARRY_LOOT_TABLE).generateLoot(new LootContext.Builder((ServerWorld) world).build(LootContextTypes.EMPTY));
				if (!items.isEmpty())
					dataTracker.set(CARRYING_ITEM, items.get(0));
			}
		}

		return super.initialize(world, difficulty, spawnReason, data, compound);
	}


	@Override
	public boolean isInvulnerableTo(@Nonnull DamageSource source) {
		return source == DamageSource.CACTUS || source.isProjectile() || super.isInvulnerableTo(source);
	}

	@Override
	public boolean canBreatheInWater() {
		return true;
	}


	@Override
	public boolean canSpawn(WorldView worldReader) {
		return worldReader.intersectsEntities(this, VoxelShapes.cuboid(getBoundingBox()));
	}

	@Override
	public double getMountedHeightOffset() {
		return this.getHeight();
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
	public boolean handleFallDamage(float distance, float damageMultiplier) {
		return false;
	}

	@Override
	protected void applyDamage(@Nonnull DamageSource damageSrc, float damageAmount) {
		super.applyDamage(damageSrc, damageAmount);

		if(!isPlayerMade() && damageSrc.getAttacker() instanceof PlayerEntity) {
			startle();
			for (Entity entity : world.getOtherEntities(this,
					getBoundingBox().expand(16))) {
				if (entity instanceof StonelingEntity) {
					StonelingEntity stoneling = (StonelingEntity) entity;
					if (!stoneling.isPlayerMade() && stoneling.getVisibilityCache().canSee(this)) {
						startle();
					}
				}
			}
		}
	}

	public boolean isStartled() {
		return waryGoal.isStartled();
	}

	public void startle() {
		waryGoal.startle();
		Set<PrioritizedGoal> entries = Sets.newHashSet(goalSelector.goals);

		for (PrioritizedGoal task : entries)
			if (task.getGoal() instanceof TemptGoal)
				goalSelector.remove(task.getGoal());
	}

	@Override
	protected void dropEquipment(DamageSource damage, int looting, boolean wasRecentlyHit) {
		super.dropEquipment(damage, looting, wasRecentlyHit);

		ItemStack stack = getCarryingItem();
		if(!stack.isEmpty())
			dropStack(stack, 0F);
	}

	public void setPlayerMade(boolean value) {
		isTame = value;
	}

	public ItemStack getCarryingItem() {
		return dataTracker.get(CARRYING_ITEM);
	}

	public EnumStonelingVariant getVariant() {
		return EnumStonelingVariant.byIndex(dataTracker.get(VARIANT));
	}

	public float getItemAngle() {
		return dataTracker.get(HOLD_ANGLE);
	}

	public boolean isPlayerMade() {
		return isTame;
	}

	@Override
	public void readCustomDataFromTag(CompoundTag compound) {
		super.readCustomDataFromTag(compound);

		if(compound.contains(TAG_CARRYING_ITEM, 10)) {
			CompoundTag itemCmp = compound.getCompound(TAG_CARRYING_ITEM);
			ItemStack stack = ItemStack.fromTag(itemCmp);
			dataTracker.set(CARRYING_ITEM, stack);
		}

		dataTracker.set(VARIANT, compound.getByte(TAG_VARIANT));
		dataTracker.set(HOLD_ANGLE, compound.getFloat(TAG_HOLD_ANGLE));
		setPlayerMade(compound.getBoolean(TAG_PLAYER_MADE));
	}

	@Override
	public boolean canSee(Entity entityIn) {
		Vec3d pos = getPos();
		Vec3d epos = entityIn.getPos();
		
		Vec3d origin = new Vec3d(pos.x, pos.y + getStandingEyeHeight(), pos.z);
		float otherEyes = entityIn.getStandingEyeHeight();
		for (float height = 0; height <= otherEyes; height += otherEyes / 8) {
			if (this.world.raycast(new RaycastContext(origin, epos.add(0, height, 0), RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, this)).getType() == HitResult.Type.MISS)
				return true;
		}

		return false;
	}

	@Override
	public void writeCustomDataToTag(CompoundTag compound) {
		super.writeCustomDataToTag(compound);

		compound.put(TAG_CARRYING_ITEM, getCarryingItem().serializeNBT());

		compound.putByte(TAG_VARIANT, getVariant().getIndex());
		compound.putFloat(TAG_HOLD_ANGLE, getItemAngle());
		compound.putBoolean(TAG_PLAYER_MADE, isPlayerMade());
	}

	public static boolean spawnPredicate(EntityType<? extends StonelingEntity> type, ServerWorldAccess world, SpawnReason reason, BlockPos pos, Random rand) {
		return pos.getY() <= StonelingsModule.maxYLevel && MiscUtil.validSpawnLight(world, pos, rand) && MiscUtil.validSpawnLocation(type, world, reason, pos);
	}

	@Override
	public boolean canSpawn(@Nonnull WorldAccess world, SpawnReason reason) {
		BlockState state = world.getBlockState(new BlockPos(getPos()).down());
		if (state.getMaterial() != Material.STONE)
			return false;
		
		return StonelingsModule.dimensions.canSpawnHere(world) && super.canSpawn(world, reason);
	}

	@Nullable
	@Override
	protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
		return QuarkSounds.ENTITY_STONELING_CRY;
	}

	@Nullable
	@Override
	protected SoundEvent getDeathSound() {
		return QuarkSounds.ENTITY_STONELING_DIE;
	}

	@Override
	public int getMinAmbientSoundDelay() {
		return 1200;
	}

	@Override
	public void playAmbientSound() {
		SoundEvent sound = this.getAmbientSound();

		if (sound != null) this.playSound(sound, this.getSoundVolume(), 1f);
	}

	@Nullable
	@Override
	protected SoundEvent getAmbientSound() {
		if (hasCustomName()) {
			String customName = getName().getString();
			if (customName.equalsIgnoreCase("michael stevens") || customName.equalsIgnoreCase("vsauce"))
				return QuarkSounds.ENTITY_STONELING_MICHAEL;
		}

		return null;
	}

	@Nonnull
	@Override
	public Packet<?> createSpawnPacket() {
		return NetworkHooks.getEntitySpawningPacket(this);
	}

	@Override
	public float getPathfindingFavor(BlockPos pos, WorldView world) {
		return 0.5F - world.getBrightness(pos);
	}
}
