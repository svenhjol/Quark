package vazkii.quark.content.mobs.entity;

import com.google.common.collect.Lists;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Blocks;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.control.JumpControl;
import net.minecraft.entity.ai.control.MoveControl;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.ai.pathing.Path;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.passive.WolfEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.Ingredient;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.tag.ItemTags;
import net.minecraft.text.Text;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.common.IForgeShearable;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;
import net.minecraftforge.fml.network.NetworkHooks;
import vazkii.quark.base.handler.MiscUtil;
import vazkii.quark.base.handler.QuarkSounds;
import vazkii.quark.base.proxy.CommonProxy;
import vazkii.quark.content.mobs.ai.FavorBlockGoal;
import vazkii.quark.content.mobs.ai.PassivePassengerGoal;
import vazkii.quark.content.mobs.ai.TemptGoalButNice;
import vazkii.quark.content.mobs.module.FrogsModule;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Calendar;
import java.util.List;
import java.util.UUID;

@SuppressWarnings("deprecation")
public class FrogEntity extends AnimalEntity implements IEntityAdditionalSpawnData, IForgeShearable {

	public static final Identifier FROG_LOOT_TABLE = new Identifier("quark", "entities/frog");

	private static final TrackedData<Integer> TALK_TIME = DataTracker.registerData(FrogEntity.class, TrackedDataHandlerRegistry.INTEGER);
	private static final TrackedData<Float> SIZE_MODIFIER = DataTracker.registerData(FrogEntity.class, TrackedDataHandlerRegistry.FLOAT);
	private static final TrackedData<Boolean> HAS_SWEATER = DataTracker.registerData(FrogEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
	private static final TrackedData<Boolean> VOID = DataTracker.registerData(FrogEntity.class, TrackedDataHandlerRegistry.BOOLEAN);

	private static final UUID VOID_MODIFIER_UUID = UUID.fromString("212dbecc-7525-4137-a74b-361cc128d24f");

	public int spawnCd = -1;
	public int spawnChain = 30;

	public boolean isDuplicate = false;
	private boolean sweatered = false;

	private Ingredient[] temptationItems;

	public FrogEntity(EntityType<? extends FrogEntity> type, World worldIn) {
		this(type, worldIn, 1);
	}

	public FrogEntity(EntityType<? extends FrogEntity> type, World worldIn, float sizeModifier) {
		super(type, worldIn);
		if (sizeModifier != 1)
			dataTracker.set(SIZE_MODIFIER, sizeModifier);

		this.jumpControl = new FrogJumpController();
		this.moveControl = new FrogMoveController();
		this.setMovementSpeed(0.0D);
	}

	@Override
	protected void initDataTracker() {
		super.initDataTracker();

		dataTracker.startTracking(TALK_TIME, 0);
		dataTracker.startTracking(SIZE_MODIFIER, 1f);
		dataTracker.startTracking(HAS_SWEATER, false);
		dataTracker.startTracking(VOID, false);
	}

	@Override
	protected void initGoals() {
		goalSelector.add(0, new PassivePassengerGoal(this));
		goalSelector.add(1, new SwimGoal(this));
		goalSelector.add(2, new FrogPanicGoal(1.25));
		goalSelector.add(3, new AnimalMateGoal(this, 1.0));
		goalSelector.add(4, new TemptGoalButNice(this, 1.2, false, getTemptationItems(false), getTemptationItems(true)));
		goalSelector.add(5, new FollowParentGoal(this, 1.1));
		goalSelector.add(6, new FavorBlockGoal(this, 1, Blocks.LILY_PAD));
		goalSelector.add(7, new WanderAroundFarGoal(this, 1, 0.5F));
		goalSelector.add(8, new LookAtEntityGoal(this, PlayerEntity.class, 6));
		goalSelector.add(9, new LookAroundGoal(this));
	}

	public static DefaultAttributeContainer.Builder prepareAttributes() {
        return MobEntity.createMobAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 10.0D)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.25D)
				.add(ForgeMod.ENTITY_GRAVITY.get());
    }
	
	@Nonnull
	@Override
	public MoveControl getMoveControl() {
		return moveControl;
	}

	@Nonnull
	@Override
	public JumpControl getJumpControl() {
		return jumpControl;
	}

	@Override
	public boolean handleFallDamage(float distance, float damageMultiplier) {
		return false;
	}

	@Override
	protected float getActiveEyeHeight(@Nonnull EntityPose pose, EntityDimensions size) {
		return 0.2f * size.height;
	}
//
//	@Override
//	public boolean isEntityInsideOpaqueBlock() {
//		return MiscUtil.isEntityInsideOpaqueBlock(this);
//	}

	public int getTalkTime() {
		return dataTracker.get(TALK_TIME);
	}

	public float getSizeModifier() {
		return dataTracker.get(SIZE_MODIFIER);
	}
	
	public static boolean canBeSweatered() {
		Calendar calendar = Calendar.getInstance();
		return calendar.get(Calendar.MONTH) + 1 == 4 && calendar.get(Calendar.DAY_OF_MONTH) == 1;
	}



	@Override
	public void tick() {
		if(!world.isClient && !sweatered) {
			setSweater(CommonProxy.jingleTheBells && (getUuid().getLeastSignificantBits() % 10 == 0));
			sweatered = true;
		}
		
		if (this.jumpTicks != this.jumpDuration) ++this.jumpTicks;
		else if (this.jumpDuration != 0) {
			this.jumpTicks = 0;
			this.jumpDuration = 0;
			this.setJumping(false);
		}

		if (!isVoid() && hasCustomName() && getY() <= 0) {
			Text name = getCustomName();
			if (name != null && name.asString().equals("Jack")) {
				setVoid(true);
			}
		}

		super.tick();

		int talkTime = getTalkTime();
		if (talkTime > 0)
			dataTracker.set(TALK_TIME, talkTime - 1);

		if (FrogsModule.enableBigFunny && spawnCd > 0 && spawnChain > 0) {
			spawnCd--;
			if (spawnCd == 0 && !world.isClient) {
				float multiplier = 0.8F;
				FrogEntity newFrog = new FrogEntity(FrogsModule.frogType, world);
				Vec3d pos = getPos();
				newFrog.updatePosition(pos.x, pos.y, pos.z);
				newFrog.setVelocity((Math.random() - 0.5) * multiplier, (Math.random() - 0.5) * multiplier, (Math.random() - 0.5) * multiplier);
				newFrog.isDuplicate = true;
				newFrog.spawnCd = 2;
				newFrog.spawnChain = spawnChain - 1;
				world.spawnEntity(newFrog);
				spawnChain = 0;
			}
		}

		if (isVoid() && getY() > 256 + 64)
			destroy();

		this.prevYaw = this.prevHeadYaw;
		this.yaw = this.headYaw;
	}

	@Override
	protected boolean canDropLootAndXp() {
		return !isDuplicate && super.canDropLootAndXp();
	}

	@Nonnull
	@Override
	protected Identifier getLootTableId() {
		return FROG_LOOT_TABLE;
	}

	private int droppedLegs = -1;

	@Override
	protected void dropLoot(@Nonnull DamageSource source, boolean damagedByPlayer) {
		droppedLegs = 0;
		super.dropLoot(source, damagedByPlayer);
		droppedLegs = -1;
	}

	@Nullable
	@Override
	public ItemEntity dropStack(@Nonnull ItemStack stack, float offsetY) {
		if (droppedLegs >= 0) {
			int count = Math.max(4 - droppedLegs, 0);
			droppedLegs += stack.getCount();

			if (stack.getCount() > count) {
				ItemStack copy = stack.copy();
				copy.decrement(count);
				copy.getOrCreateSubTag("display")
						.putString("LocName", "item.quark.frog_maybe_leg.name");

				stack = stack.copy();
				stack.decrement(copy.getCount());

				super.dropStack(copy, offsetY);
			}
		}

		return super.dropStack(stack, offsetY);
	}

	@Nonnull
	@Override // processInteract
	public ActionResult interactMob(@Nonnull PlayerEntity player, @Nonnull Hand hand) {
		ActionResult parent = super.interactMob(player, hand);
		if(parent == ActionResult.SUCCESS)
			return parent;

		ItemStack stack = player.getStackInHand(hand);
		
		LocalDate date = LocalDate.now();
		if(DayOfWeek.from(date) == DayOfWeek.WEDNESDAY && stack.getItem() == Items.CLOCK) {
			if(!world.isClient && spawnChain > 0 && !isDuplicate) {
				if(FrogsModule.enableBigFunny) {
					spawnCd = 50;
					dataTracker.set(TALK_TIME, 80);
				}
					
				Vec3d pos = getPos();
				world.playSound(null, pos.x, pos.y, pos.z, QuarkSounds.ENTITY_FROG_WEDNESDAY, SoundCategory.NEUTRAL, 1F, 1F);
			}

			return ActionResult.SUCCESS;
		}
		
		if(stack.getItem().isIn(ItemTags.WOOL) && !hasSweater()) {
			if(!world.isClient) {
				setSweater(true);
				Vec3d pos = getPos();
				world.playSound(null, pos.x, pos.y, pos.z, BlockSoundGroup.WOOL.getPlaceSound(), SoundCategory.PLAYERS, 1F, 1F);
				stack.decrement(1);
			}
			
			player.swingHand(hand);
			return ActionResult.SUCCESS;
		}

		return ActionResult.PASS;
	}
	
	@Override
	public boolean isShearable(@Nonnull ItemStack item, World world, BlockPos pos) {
		return hasSweater();
	}
	
	@Nonnull
	@Override
	public List<ItemStack> onSheared(PlayerEntity player, @Nonnull ItemStack item, World iworld, BlockPos pos, int fortune) {
		setSweater(false);
		Vec3d epos = getPos();
		world.playSound(null, epos.x, epos.y, epos.z, SoundEvents.ENTITY_SHEEP_SHEAR, SoundCategory.PLAYERS, 1F, 1F);

		return Lists.newArrayList();
	}

	@Override // createChild
	public FrogEntity createChild(ServerWorld sworld, PassiveEntity otherParent) {
		if (isDuplicate)
			return null;

		float sizeMod = getSizeModifier();
		if (otherParent instanceof FrogEntity) {
			if (((FrogEntity) otherParent).isDuplicate)
				return null;

			sizeMod += ((FrogEntity) otherParent).getSizeModifier();
			sizeMod /= 2;
		}

		double regression = random.nextGaussian() / 20;
		regression *= Math.abs((sizeMod + regression) / sizeMod);

		return new FrogEntity(FrogsModule.frogType, world, MathHelper.clamp(sizeMod + (float) regression, 0.25f, 2.0f));
	}

	@Override
	public boolean isBreedingItem(ItemStack stack) {
		LocalDate date = LocalDate.now();
		return !stack.isEmpty() &&
				(FrogsModule.enableBigFunny && DayOfWeek.from(date) == DayOfWeek.WEDNESDAY ?
						getTemptationItems(true) : getTemptationItems(false)).test(stack);
	}
	
	private Ingredient getTemptationItems(boolean nice) {
		if(temptationItems == null)
			temptationItems =  new Ingredient[] {
					Ingredient.merge(Lists.newArrayList(
							Ingredient.ofItems(Items.SPIDER_EYE),
							Ingredient.fromTag(ItemTags.FISHES)
					)),
					Ingredient.merge(Lists.newArrayList(
							Ingredient.ofItems(Items.SPIDER_EYE, Items.CLOCK),
							Ingredient.fromTag(ItemTags.FISHES)
					))
			};
		
		return temptationItems[nice ? 1 : 0];
	}

	@Override
	public void readCustomDataFromTag(@Nonnull CompoundTag compound) {
		super.readCustomDataFromTag(compound);
		spawnCd = compound.getInt("Cooldown");
		if (compound.contains("Chain"))
			spawnChain = compound.getInt("Chain");
		dataTracker.set(TALK_TIME, compound.getInt("DudeAmount"));

		float sizeModifier = compound.contains("FrogAmount") ? compound.getFloat("FrogAmount") : 1f;
		dataTracker.set(SIZE_MODIFIER, sizeModifier);

		isDuplicate = compound.getBoolean("FakeFrog");
		
		sweatered = compound.getBoolean("SweaterComp");
		setSweater(compound.getBoolean("Sweater"));

		setVoid(compound.getBoolean("Jack"));
	}

	@Override
	public void writeCustomDataToTag(@Nonnull CompoundTag compound) {
		super.writeCustomDataToTag(compound);
		compound.putFloat("FrogAmount", getSizeModifier());
		compound.putInt("Cooldown", spawnCd);
		compound.putInt("Chain", spawnChain);
		compound.putInt("DudeAmount", getTalkTime());
		compound.putBoolean("FakeFrog", isDuplicate);
		compound.putBoolean("SweaterComp", sweatered);
		compound.putBoolean("Sweater", hasSweater());
		compound.putBoolean("Jack", isVoid());
	}

	@Override
	protected SoundEvent getAmbientSound() {
		return QuarkSounds.ENTITY_FROG_IDLE;
	}

	@Override
	protected SoundEvent getHurtSound(@Nonnull DamageSource damageSourceIn) {
		return QuarkSounds.ENTITY_FROG_HURT;
	}

	@Override
	protected SoundEvent getDeathSound() {
		return QuarkSounds.ENTITY_FROG_DIE;
	}

	protected SoundEvent getJumpSound() {
		return QuarkSounds.ENTITY_FROG_JUMP;
	}
	
	public boolean hasSweater() {
		return dataTracker.get(HAS_SWEATER);
	}
	
	public void setSweater(boolean sweater) {
		dataTracker.set(HAS_SWEATER, sweater);
	}

	public boolean isVoid() {
		return dataTracker.get(VOID);
	}

	@Override
	protected float getJumpVelocity() {
		float motion = super.getJumpVelocity();
		if (isVoid())
			return -motion;
		else
			return motion;
	}

	public void setVoid(boolean jack) {
		if (jack && this.getAttributeInstance(ForgeMod.ENTITY_GRAVITY.get()).getModifier(VOID_MODIFIER_UUID) == null)
			this.getAttributeInstance(ForgeMod.ENTITY_GRAVITY.get())
					.addPersistentModifier(new EntityAttributeModifier(VOID_MODIFIER_UUID, "Void gravity", -2, EntityAttributeModifier.Operation.MULTIPLY_BASE));
		else
			this.getAttributeInstance(ForgeMod.ENTITY_GRAVITY.get())
					.removeModifier(VOID_MODIFIER_UUID);

		dataTracker.set(VOID, jack);
	}

	// Begin copypasta from EntityRabbit

	private int jumpTicks;
	private int jumpDuration;
	private boolean wasOnGround;
	private int currentMoveTypeDuration;

	@Override
	public void mobTick() {
		if (this.currentMoveTypeDuration > 0) --this.currentMoveTypeDuration;

		if (this.onGround) {
			if (!this.wasOnGround) {
				this.setJumping(false);
				this.checkLandingDelay();
			}

			FrogJumpController jumpHelper = (FrogJumpController) this.jumpControl;

			if (!jumpHelper.getIsJumping()) {
				if (this.moveControl.isMoving() && this.currentMoveTypeDuration == 0) {
					Path path = this.navigation.getCurrentPath();
					Vec3d Vector3d = new Vec3d(this.moveControl.getTargetX(), this.moveControl.getTargetY(), this.moveControl.getTargetZ());

					if (path != null && path.getCurrentNodeIndex() < path.getLength())
						Vector3d = path.getNodePosition(this);

					this.calculateRotationYaw(Vector3d.x, Vector3d.z);
					this.startJumping();
				}
			} else if (!jumpHelper.canJump()) this.enableJumpControl();
		}

		this.wasOnGround = this.onGround;
	}

	@Override // spawnRunningParticles
	public boolean shouldSpawnSprintingParticles() {
		return false;
	}

	private void calculateRotationYaw(double x, double z) {
		Vec3d pos = getPos();
		this.yaw = (float) (MathHelper.atan2(z - pos.z, x - pos.x) * (180D / Math.PI)) - 90.0F;
	}

	private void enableJumpControl() {
		((FrogJumpController) this.jumpControl).setCanJump(true);
	}

	private void disableJumpControl() {
		((FrogJumpController) this.jumpControl).setCanJump(false);
	}

	private void updateMoveTypeDuration() {
		if (this.moveControl.getSpeed() < 2.2D)
			this.currentMoveTypeDuration = 10;
		else
			this.currentMoveTypeDuration = 1;
	}

	private void checkLandingDelay() {
		this.updateMoveTypeDuration();
		this.disableJumpControl();
	}

	@Override
	public void onTrackedDataSet(@Nonnull TrackedData<?> parameter) {
		if (parameter.equals(SIZE_MODIFIER))
			calculateDimensions();

		super.onTrackedDataSet(parameter);
	}

	@Override
	protected void jump() {
		super.jump();
		double d0 = this.moveControl.getSpeed();

		if (d0 > 0.0D) {
			Vec3d motion = getVelocity();
			double d1 = motion.x * motion.x + motion.z * motion.z;

			if (d1 < 0.01) {
				this.updateVelocity(0.1F, new Vec3d(0.0F, 0.0F, 1.0F));
			}
		}

		if (!this.world.isClient)
			this.world.sendEntityStatus(this, (byte) 1);
	}

	public void setMovementSpeed(double newSpeed) {
		this.getNavigation().setSpeed(newSpeed);
		this.moveControl.moveTo(this.moveControl.getTargetX(), this.moveControl.getTargetY(), this.moveControl.getTargetZ(), newSpeed);
	}

	@Override
	public void setJumping(boolean jumping) {
		super.setJumping(jumping);

		if (jumping)
			this.playSound(this.getJumpSound(), this.getSoundVolume(), ((this.random.nextFloat() - this.random.nextFloat()) * 0.2F + 1.0F) * 0.8F);
	}

	public void startJumping() {
		this.setJumping(true);
		this.jumpDuration = 10;
		this.jumpTicks = 0;
	}

	@Override
	@Environment(EnvType.CLIENT)
	public void handleStatus(byte id) {
		if (id == 1) {
//			this.createRunningParticles();
			this.jumpDuration = 10;
			this.jumpTicks = 0;
		} else
			super.handleStatus(id);
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
	
	public class FrogJumpController extends JumpControl {
		private boolean canJump;

		public FrogJumpController() {
			super(FrogEntity.this);
		}

		public boolean getIsJumping() {
			return this.active;
		}

		public boolean canJump() {
			return this.canJump;
		}

		public void setCanJump(boolean canJumpIn) {
			this.canJump = canJumpIn;
		}

		@Override
		public void tick() {
			if (this.active) {
				startJumping();
				this.active = false;
			}
		}
	}

	public class FrogMoveController extends MoveControl {
		private double nextJumpSpeed;

		public FrogMoveController() {
			super(FrogEntity.this);
		}

		@Override
		public void tick() {
			if (onGround && !jumping && !((FrogJumpController) jumpControl).getIsJumping())
				setMovementSpeed(0.0D);
			else if (this.isMoving()) setMovementSpeed(this.nextJumpSpeed);

			super.tick();
		}

		@Override
		public void moveTo(double x, double y, double z, double speedIn) {
			if (isTouchingWater()) speedIn = 1.5D;

			super.moveTo(x, y, z, speedIn);

			if (speedIn > 0.0D) this.nextJumpSpeed = speedIn;
		}
	}
	
	public class FrogPanicGoal extends EscapeDangerGoal {

		public FrogPanicGoal(double speedIn) {
			super(FrogEntity.this, speedIn);
		}

		@Override
		public void tick() {
			super.tick();
			setMovementSpeed(this.speed);
		}
	}

}

