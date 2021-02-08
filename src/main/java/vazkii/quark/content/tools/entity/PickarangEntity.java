package vazkii.quark.content.tools.entity;

import java.util.List;
import java.util.UUID;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.collect.Multimap;

import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.Attribute;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.AttributeModifierManager;
import net.minecraft.entity.ai.attributes.AttributeModifierMap;
import net.minecraft.entity.ai.attributes.AttributeModifierMap.MutableAttribute;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.item.ExperienceOrbEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.entity.projectile.ProjectileHelper;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.network.IPacket;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.Hand;
import net.minecraft.util.IndirectEntityDamageSource;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.RayTraceResult.Type;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.network.NetworkHooks;
import vazkii.quark.base.handler.QuarkSounds;
import vazkii.quark.content.mobs.entity.ToretoiseEntity;
import vazkii.quark.content.tools.module.PickarangModule;

public class PickarangEntity extends ProjectileEntity {

	private static final DataParameter<ItemStack> STACK = EntityDataManager.createKey(PickarangEntity.class, DataSerializers.ITEMSTACK);
	private static final DataParameter<Boolean> RETURNING = EntityDataManager.createKey(PickarangEntity.class, DataSerializers.BOOLEAN);
	private static final DataParameter<Boolean> NETHERITE_SYNCED = EntityDataManager.createKey(PickarangEntity.class, DataSerializers.BOOLEAN);

	protected LivingEntity owner;
	private UUID ownerId;

	private int liveTime;
	private int slot;
	private int blockHitCount;
	public boolean netherite;

	private IntOpenHashSet entitiesHit;

	private static final String TAG_RETURNING = "returning";
	private static final String TAG_LIVE_TIME = "liveTime";
	private static final String TAG_BLOCKS_BROKEN = "hitCount";
	private static final String TAG_RETURN_SLOT = "returnSlot";
	private static final String TAG_ITEM_STACK = "itemStack";
	private static final String TAG_NETHERITE = "netherite";

	public PickarangEntity(EntityType<? extends PickarangEntity> type, World worldIn) {
		super(type, worldIn);
	}

	public PickarangEntity(World worldIn, LivingEntity throwerIn) {
		super(PickarangModule.pickarangType, worldIn);
		Vector3d pos = throwerIn.getPositionVec();
		this.setPosition(pos.x, pos.y + throwerIn.getEyeHeight(), pos.z);
		ownerId = throwerIn.getUniqueID();
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public boolean isInRangeToRenderDist(double distance) {
		double d0 = this.getBoundingBox().getAverageEdgeLength() * 4.0D;
		if (Double.isNaN(d0)) d0 = 4.0D;

		d0 = d0 * 64.0D;
		return distance < d0 * d0;
	}

	public void shoot(Entity entityThrower, float rotationPitchIn, float rotationYawIn, float pitchOffset, float velocity, float inaccuracy) {
		float f = -MathHelper.sin(rotationYawIn * ((float)Math.PI / 180F)) * MathHelper.cos(rotationPitchIn * ((float)Math.PI / 180F));
		float f1 = -MathHelper.sin((rotationPitchIn + pitchOffset) * ((float)Math.PI / 180F));
		float f2 = MathHelper.cos(rotationYawIn * ((float)Math.PI / 180F)) * MathHelper.cos(rotationPitchIn * ((float)Math.PI / 180F));
		this.shoot(f, f1, f2, velocity, inaccuracy);
		Vector3d Vector3d = entityThrower.getMotion();
		this.setMotion(this.getMotion().add(Vector3d.x, entityThrower.onGround ? 0.0D : Vector3d.y, Vector3d.z));
	}


	@Override
	public void shoot(double x, double y, double z, float velocity, float inaccuracy) {
		Vector3d Vector3d = (new Vector3d(x, y, z)).normalize().add(this.rand.nextGaussian() * 0.0075F * inaccuracy, this.rand.nextGaussian() * 0.0075F * inaccuracy, this.rand.nextGaussian() * 0.0075F * inaccuracy).scale(velocity);
		this.setMotion(Vector3d);
		float f = MathHelper.sqrt(horizontalMag(Vector3d));
		this.rotationYaw = (float)(MathHelper.atan2(Vector3d.x, Vector3d.z) * (180F / (float)Math.PI));
		this.rotationPitch = (float)(MathHelper.atan2(Vector3d.y, f) * (180F / (float)Math.PI));
		this.prevRotationYaw = this.rotationYaw;
		this.prevRotationPitch = this.rotationPitch;
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void setVelocity(double x, double y, double z) {
		this.setMotion(x, y, z);
		if (this.prevRotationPitch == 0.0F && this.prevRotationYaw == 0.0F) {
			float f = MathHelper.sqrt(x * x + z * z);
			this.rotationYaw = (float)(MathHelper.atan2(x, z) * (180F / (float)Math.PI));
			this.rotationPitch = (float)(MathHelper.atan2(y, f) * (180F / (float)Math.PI));
			this.prevRotationYaw = this.rotationYaw;
			this.prevRotationPitch = this.rotationPitch;
		}

	}

	public void setThrowData(int slot, ItemStack stack, boolean netherite) {
		this.slot = slot;
		setStack(stack.copy());
		this.netherite = netherite;
	}

	@Override
	protected void registerData() {
		dataManager.register(STACK, new ItemStack(PickarangModule.pickarang));
		dataManager.register(RETURNING, false);
		dataManager.register(NETHERITE_SYNCED, false);
	}

	protected void checkImpact() {
		if(world.isRemote)
			return;

		Vector3d motion = getMotion();
		Vector3d position = getPositionVec();
		Vector3d rayEnd = position.add(motion);

		boolean doEntities = true;
		int tries = 100;

		while(isAlive() && !dataManager.get(RETURNING)) {
			if(doEntities) {
				EntityRayTraceResult result = raycastEntities(position, rayEnd);
				if(result != null)
					onImpact(result);
				else doEntities = false;
			} else {
				RayTraceResult result = world.rayTraceBlocks(new RayTraceContext(position, rayEnd, RayTraceContext.BlockMode.COLLIDER, RayTraceContext.FluidMode.NONE, this));
				if(result.getType() == Type.MISS)
					return;
				else onImpact(result);
			}

			if(tries-- <= 0) {
				(new RuntimeException("Pickarang hit way too much, this shouldn't happen")).printStackTrace();
				return;
			}
		}
	}

	@Nullable
	protected EntityRayTraceResult raycastEntities(Vector3d from, Vector3d to) {
		return ProjectileHelper.rayTraceEntities(world, this, from, to, getBoundingBox().expand(getMotion()).grow(1.0D), (entity) -> 
		!entity.isSpectator() 
		&& entity.isAlive() 
		&& (entity.canBeCollidedWith() || entity instanceof PickarangEntity) 
		&& entity != getThrower() 
		&& (entitiesHit == null || !entitiesHit.contains(entity.getEntityId())));
	}

	@Override
	protected void onImpact(@Nonnull RayTraceResult result) {
		LivingEntity owner = getThrower();

		if(result.getType() == Type.BLOCK && result instanceof BlockRayTraceResult) {
			BlockPos hit = ((BlockRayTraceResult) result).getPos();
			BlockState state = world.getBlockState(hit);

			if(getPiercingModifier() == 0 || state.getMaterial().isOpaque())
				addHit();

			if(!(owner instanceof ServerPlayerEntity))
				return;

			ServerPlayerEntity player = (ServerPlayerEntity) owner;

			float hardness = state.getBlockHardness(world, hit);
			if (hardness <= PickarangModule.maxHardness && hardness >= 0) {
				ItemStack prev = player.getHeldItemMainhand();
				player.setHeldItem(Hand.MAIN_HAND, getStack());

				if (player.interactionManager.tryHarvestBlock(hit))
					world.playEvent(null, 2001, hit, Block.getStateId(state));
				else
					clank();

				setStack(player.getHeldItemMainhand());

				player.setHeldItem(Hand.MAIN_HAND, prev);
			} else
				clank();

		} else if(result.getType() == Type.ENTITY && result instanceof EntityRayTraceResult) {
			Entity hit = ((EntityRayTraceResult) result).getEntity();

			if(hit != owner) {
				addHit(hit);
				if (hit instanceof PickarangEntity) {
					((PickarangEntity) hit).setReturning();
					clank();
				} else {
					ItemStack pickarang = getStack();
					Multimap<Attribute, AttributeModifier> modifiers = pickarang.getAttributeModifiers(EquipmentSlotType.MAINHAND);

					if (owner != null) {
						ItemStack prev = owner.getHeldItemMainhand();
						owner.setHeldItem(Hand.MAIN_HAND, pickarang);
						owner.getAttributeManager().reapplyModifiers(modifiers); 

						int ticksSinceLastSwing = owner.ticksSinceLastSwing;
						owner.ticksSinceLastSwing = (int) (1.0 / owner.getAttributeValue(Attributes.ATTACK_SPEED) * 20.0) + 1;

						float prevHealth = hit instanceof LivingEntity ? ((LivingEntity) hit).getHealth() : 0;

						PickarangModule.setActivePickarang(this);

						hitEntity: {
							if(hit instanceof ToretoiseEntity) {
								ToretoiseEntity toretoise = (ToretoiseEntity) hit;
								int ore = toretoise.getOreType();
								
								if(ore != 0) {
									addHit(toretoise);
									toretoise.dropOre(ore);
									break hitEntity;
								}
							}

							if (owner instanceof PlayerEntity)
								((PlayerEntity) owner).attackTargetEntityWithCurrentItem(hit);
							else
								owner.attackEntityAsMob(hit);

							if (hit instanceof LivingEntity && ((LivingEntity) hit).getHealth() == prevHealth)
								clank();
						}


						PickarangModule.setActivePickarang(null);

						owner.ticksSinceLastSwing = ticksSinceLastSwing;

						setStack(owner.getHeldItemMainhand());
						owner.setHeldItem(Hand.MAIN_HAND, prev);
						owner.getAttributeManager().reapplyModifiers(modifiers);
					} else {
						MutableAttribute mapBuilder = new MutableAttribute();
						mapBuilder.createMutableAttribute(Attributes.ATTACK_DAMAGE, 1); 
						AttributeModifierMap map = mapBuilder.create();
						AttributeModifierManager manager = new AttributeModifierManager(map);
						manager.reapplyModifiers(modifiers);
						
						ItemStack stack = getStack();
						stack.attemptDamageItem(1, world.rand, null);
						setStack(stack);
						hit.attackEntityFrom(new IndirectEntityDamageSource("player", this, this).setProjectile(),
								(float) manager.getAttributeValue(Attributes.ATTACK_DAMAGE));
					}
				}
			}
		}
	}

	public void spark() {
		playSound(QuarkSounds.ENTITY_PICKARANG_SPARK, 1, 1);
		setReturning();
	}

	public void clank() {
		playSound(QuarkSounds.ENTITY_PICKARANG_CLANK, 1, 1);
		setReturning();
	}

	public void addHit(Entity entity) {
		if (entitiesHit == null)
			entitiesHit = new IntOpenHashSet(5);
		entitiesHit.add(entity.getEntityId());
		postHit();
	}

	public void postHit() {
		if((entitiesHit == null ? 0 : entitiesHit.size()) + blockHitCount > getPiercingModifier())
			setReturning();
		else if (getPiercingModifier() > 0)
			setMotion(getMotion().scale(0.8));
	}

	public void addHit() {
		blockHitCount++;
		postHit();
	}

	protected void setReturning() {
		dataManager.set(RETURNING, true);
	}

	@Override
	public boolean isPushedByWater() {
		return false;
	}

	@Override
	public void tick() {
		Vector3d pos = getPositionVec();

		this.lastTickPosX = pos.x;
		this.lastTickPosY = pos.y;
		this.lastTickPosZ = pos.z;
		super.tick();

		if(!dataManager.get(RETURNING))
			checkImpact();

		Vector3d ourMotion = this.getMotion();
		setPosition(pos.x + ourMotion.x, pos.y + ourMotion.y, pos.z + ourMotion.z);

		float f = MathHelper.sqrt(horizontalMag(ourMotion));
		this.rotationYaw = (float)(MathHelper.atan2(ourMotion.x, ourMotion.z) * (180F / (float)Math.PI));

		this.rotationPitch = (float)(MathHelper.atan2(ourMotion.y, f) * (180F / (float)Math.PI));
		while (this.rotationPitch - this.prevRotationPitch < -180.0F) this.prevRotationPitch -= 360.0F;

		while(this.rotationPitch - this.prevRotationPitch >= 180.0F) this.prevRotationPitch += 360.0F;

		while(this.rotationYaw - this.prevRotationYaw < -180.0F) this.prevRotationYaw -= 360.0F;

		while(this.rotationYaw - this.prevRotationYaw >= 180.0F) this.prevRotationYaw += 360.0F;

		this.rotationPitch = MathHelper.lerp(0.2F, this.prevRotationPitch, this.rotationPitch);
		this.rotationYaw = MathHelper.lerp(0.2F, this.prevRotationYaw, this.rotationYaw);
		float drag;
		if (this.isInWater()) {
			for(int i = 0; i < 4; ++i) {
				this.world.addParticle(ParticleTypes.BUBBLE, pos.x - ourMotion.x * 0.25D, pos.y - ourMotion.y * 0.25D, pos.z - ourMotion.z * 0.25D, ourMotion.x, ourMotion.y, ourMotion.z);
			}

			drag = 0.8F;
		} else drag = 0.99F;

		this.setMotion(ourMotion.scale(drag));

		pos = getPositionVec();
		this.setPosition(pos.x, pos.y, pos.z);

		if(!isAlive())
			return;

		ItemStack stack = getStack();
		
		if(dataManager.get(NETHERITE_SYNCED)) {
			if(Math.random() < 0.4)
				this.world.addParticle(ParticleTypes.FLAME, 
						pos.x - ourMotion.x * 0.25D + (Math.random() - 0.5) * 0.4, 
						pos.y - ourMotion.y * 0.25D + (Math.random() - 0.5) * 0.4, 
						pos.z - ourMotion.z * 0.25D + (Math.random() - 0.5) * 0.4, 
						(Math.random() - 0.5) * 0.1, 
						(Math.random() - 0.5) * 0.1, 
						(Math.random() - 0.5) * 0.1);
		} else if(!world.isRemote && netherite)
			dataManager.set(NETHERITE_SYNCED, true);
		
		boolean returning = dataManager.get(RETURNING);
		liveTime++;

		LivingEntity owner = getThrower();
		if(owner == null || !owner.isAlive() || !(owner instanceof PlayerEntity)) {
			if(!world.isRemote) {
				while(isEntityInsideOpaqueBlock())
					setPosition(getPosX(), getPosY() + 1, getPosZ());
					
				entityDropItem(stack, 0);
				remove();
			}

			return;
		}
		
		if(!returning) {
			if(liveTime > PickarangModule.timeout)
				setReturning();
			if (!world.getWorldBorder().contains(getBoundingBox()))
				spark();
		} else {
			noClip = true;

			int eff = getEfficiencyModifier();

			List<ItemEntity> items = world.getEntitiesWithinAABB(ItemEntity.class, getBoundingBox().grow(2));
			List<ExperienceOrbEntity> xp = world.getEntitiesWithinAABB(ExperienceOrbEntity.class, getBoundingBox().grow(2));

			Vector3d ourPos = getPositionVec();
			for(ItemEntity item : items) {
				if (item.isPassenger())
					continue;
				item.startRiding(this);

				item.setPickupDelay(2);
			}

			for(ExperienceOrbEntity xpOrb : xp) {
				if (xpOrb.isPassenger())
					continue;
				xpOrb.startRiding(this);

				xpOrb.delayBeforeCanPickup = 2;
			}

			Vector3d ownerPos = owner.getPositionVec().add(0, 1, 0);
			Vector3d motion = ownerPos.subtract(ourPos);
			double motionMag = 3.25 + eff * 0.25;

			if(motion.lengthSquared() < motionMag) {
				PlayerEntity player = (PlayerEntity) owner;
				ItemStack stackInSlot = player.inventory.getStackInSlot(slot);

				if(!world.isRemote) {
					playSound(QuarkSounds.ENTITY_PICKARANG_PICKUP, 1, 1);

					if(!stack.isEmpty()) if (player.isAlive() && stackInSlot.isEmpty())
						player.inventory.setInventorySlotContents(slot, stack);
					else if (!player.isAlive() || !player.inventory.addItemStackToInventory(stack))
						player.dropItem(stack, false);

					if (player.isAlive()) {
						for (ItemEntity item : items)
							if(item.isAlive()) {
								ItemStack drop = item.getItem();
								if (!player.addItemStackToInventory(drop))
									player.dropItem(drop, false);
								item.remove();
							}

						for (ExperienceOrbEntity xpOrb : xp) 
							if(xpOrb.isAlive())
								xpOrb.onCollideWithPlayer(player);

						for (Entity riding : getPassengers()) {
							if (!riding.isAlive())
								continue;

							if (riding instanceof ItemEntity) {
								ItemStack drop = ((ItemEntity) riding).getItem();
								if (!player.addItemStackToInventory(drop))
									player.dropItem(drop, false);
								riding.remove();
							} else if (riding instanceof ExperienceOrbEntity)
								riding.onCollideWithPlayer(player);
						}
					}

					remove();
				}
			} else
				setMotion(motion.normalize().scale(0.7 + eff * 0.325F));
		}
	}

	@Nullable
	public LivingEntity getThrower() {
		if (this.owner == null && this.ownerId != null && this.world instanceof ServerWorld) {
			Entity entity = ((ServerWorld)this.world).getEntityByUuid(this.ownerId);
			if (entity instanceof LivingEntity) {
				this.owner = (LivingEntity)entity;
			} else {
				this.ownerId = null;
			}
		}

		return this.owner;
	}

	@Override
	protected boolean canFitPassenger(Entity passenger) {
		return super.canFitPassenger(passenger) || passenger instanceof ItemEntity || passenger instanceof ExperienceOrbEntity;
	}

	@Override
	public double getMountedYOffset() {
		return 0;
	}

	@Nonnull
	@Override
	public SoundCategory getSoundCategory() {
		return SoundCategory.PLAYERS;
	}

	public int getEfficiencyModifier() {
		return EnchantmentHelper.getEnchantmentLevel(Enchantments.EFFICIENCY, getStack());
	}

	public int getPiercingModifier() {
		return EnchantmentHelper.getEnchantmentLevel(Enchantments.PIERCING, getStack());
	}

	public ItemStack getStack() {
		return dataManager.get(STACK);
	}

	public void setStack(ItemStack stack) {
		dataManager.set(STACK, stack);
	}

	@Override
	public void readAdditional(@Nonnull CompoundNBT compound) {
		dataManager.set(RETURNING, compound.getBoolean(TAG_RETURNING));
		liveTime = compound.getInt(TAG_LIVE_TIME);
		blockHitCount = compound.getInt(TAG_BLOCKS_BROKEN);
		slot = compound.getInt(TAG_RETURN_SLOT);

		if (compound.contains(TAG_ITEM_STACK))
			setStack(ItemStack.read(compound.getCompound(TAG_ITEM_STACK)));
		else
			setStack(new ItemStack(PickarangModule.pickarang));

		if (compound.contains("owner", 10)) {
			INBT owner = compound.get("owner");
			if (owner != null)
				this.ownerId = NBTUtil.readUniqueId(owner);
		}
		
		netherite = compound.getBoolean(TAG_NETHERITE);
	}

	@Override
	public void writeAdditional(@Nonnull CompoundNBT compound) {
		compound.putBoolean(TAG_RETURNING, dataManager.get(RETURNING));
		compound.putInt(TAG_LIVE_TIME, liveTime);
		compound.putInt(TAG_BLOCKS_BROKEN, blockHitCount);
		compound.putInt(TAG_RETURN_SLOT, slot);

		compound.put(TAG_ITEM_STACK, getStack().serializeNBT());
		if (this.ownerId != null)
			compound.put("owner", NBTUtil.func_240626_a_(this.ownerId));
		
		compound.putBoolean(TAG_NETHERITE, netherite);
	}

	@Nonnull
	@Override
	public IPacket<?> createSpawnPacket() {
		return NetworkHooks.getEntitySpawningPacket(this);
	}

}
