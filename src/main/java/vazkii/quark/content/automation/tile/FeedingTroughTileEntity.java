package vazkii.quark.content.automation.tile;

import java.util.List;
import java.util.Random;
import java.util.UUID;

import javax.annotation.Nonnull;

import com.mojang.authlib.GameProfile;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.goal.TemptGoal;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.DispenserContainer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.particles.ItemParticleData;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.LockableLootTileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.vector.Vector2f;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.util.FakePlayer;
import vazkii.quark.base.handler.MiscUtil;
import vazkii.quark.base.util.MovableFakePlayer;
import vazkii.quark.content.automation.block.FeedingTroughBlock;
import vazkii.quark.content.automation.module.FeedingTroughModule;

/**
 * @author WireSegal
 * Created at 9:39 AM on 9/20/19.
 */
public class FeedingTroughTileEntity extends LockableLootTileEntity implements ITickableTileEntity {

    private static final GameProfile DUMMY_PROFILE = new GameProfile(UUID.randomUUID(), "[FeedingTrough]");

    private NonNullList<ItemStack> stacks;

    private FakePlayer foodHolder = null;

    private int cooldown = 0;
    private long internalRng = 0;

    protected FeedingTroughTileEntity(TileEntityType<? extends FeedingTroughTileEntity> type) {
        super(type);
        this.stacks = NonNullList.withSize(9, ItemStack.EMPTY);
    }

    public FeedingTroughTileEntity() {
        this(FeedingTroughModule.tileEntityType);
    }

    public FakePlayer getFoodHolder(TemptGoal goal) {
        if (foodHolder == null && world instanceof ServerWorld)
            foodHolder = new MovableFakePlayer((ServerWorld) world, DUMMY_PROFILE);

        AnimalEntity entity = (AnimalEntity) goal.creature;

        if (foodHolder != null) {
            for (int i = 0; i < getSizeInventory(); i++) {
                ItemStack stack = getStackInSlot(i);
                if (goal.isTempting(stack) && entity.isBreedingItem(stack)) {
                    foodHolder.inventory.mainInventory.set(foodHolder.inventory.currentItem, stack);
                    Vector3d position = new Vector3d(pos.getX(), pos.getY(), pos.getZ()).add(0.5, -1, 0.5);
                    Vector3d direction = goal.creature.getPositionVec().subtract(position).normalize();
                    Vector2f angles = MiscUtil.getMinecraftAngles(direction);

                    Vector3d shift = direction.scale(-0.5 / Math.max(
                            Math.abs(direction.x), Math.max(
                                    Math.abs(direction.y),
                                    Math.abs(direction.z))));

                    Vector3d truePos = position.add(shift);

                    foodHolder.setLocationAndAngles(truePos.x, truePos.y, truePos.z, angles.x, angles.y);
                    return foodHolder;
                }
            }
        }

        return null;
    }

    @Override
    public void tick() {
        if (world != null && !world.isRemote) {
            if (cooldown > 0)
                cooldown--;
            else {
            	cooldown = FeedingTroughModule.cooldown; // minimize aabb calls
            	List<AnimalEntity> animals = world.getEntitiesWithinAABB(AnimalEntity.class, new AxisAlignedBB(pos).grow(1.5, 0, 1.5).contract(0, 0.75, 0));
            	
                for (AnimalEntity creature : animals) {
                    if (creature.canFallInLove() && creature.getGrowingAge() == 0) {
                        for (int i = 0; i < getSizeInventory(); i++) {
                            ItemStack stack = getStackInSlot(i);
                            if (creature.isBreedingItem(stack)) {
                                creature.playSound(creature.getEatSound(stack), 0.5F + 0.5F * world.rand.nextInt(2), (world.rand.nextFloat() - world.rand.nextFloat()) * 0.2F + 1.0F);
                                addItemParticles(creature, stack, 16);
                                
                                if(getSpecialRand().nextDouble() < FeedingTroughModule.loveChance) {
                                	List<AnimalEntity> animalsAround = world.getEntitiesWithinAABB(AnimalEntity.class, new AxisAlignedBB(pos).grow(FeedingTroughModule.range));
                                	if(animalsAround.size() <= FeedingTroughModule.maxAnimals)
                                		creature.setInLove(null);
                                }

                                stack.shrink(1);
                                markDirty();
                                
                                return;
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public void markDirty() {
        super.markDirty();
        BlockState state = getBlockState();
        if (world != null && state.getBlock() instanceof FeedingTroughBlock) {
            boolean full = state.get(FeedingTroughBlock.FULL);
            boolean shouldBeFull = !isEmpty();

            if (full != shouldBeFull)
                world.setBlockState(pos, state.with(FeedingTroughBlock.FULL, shouldBeFull), 2);
        }
    }

    private void addItemParticles(Entity entity, ItemStack stack, int count) {
        for(int i = 0; i < count; ++i) {
            Vector3d direction = new Vector3d((entity.world.rand.nextFloat() - 0.5D) * 0.1D, Math.random() * 0.1D + 0.1D, 0.0D);
            direction = direction.rotatePitch(-entity.rotationPitch * ((float)Math.PI / 180F));
            direction = direction.rotateYaw(-entity.rotationYaw * ((float)Math.PI / 180F));
            double yVelocity = (-entity.world.rand.nextFloat()) * 0.6D - 0.3D;
            Vector3d position = new Vector3d((entity.world.rand.nextFloat() - 0.5D) * 0.3D, yVelocity, 0.6D);
            Vector3d entityPos = entity.getPositionVec();
            position = position.rotatePitch(-entity.rotationPitch * ((float)Math.PI / 180F));
            position = position.rotateYaw(-entity.rotationYaw * ((float)Math.PI / 180F));
            position = position.add(entityPos.x, entityPos.y + entity.getEyeHeight(), entityPos.z);
            if (this.world instanceof ServerWorld)
                ((ServerWorld)this.world).spawnParticle(new ItemParticleData(ParticleTypes.ITEM, stack), position.x, position.y, position.z, 1, direction.x, direction.y + 0.05D, direction.z, 0.0D);
            else if (this.world != null)
                this.world.addParticle(new ItemParticleData(ParticleTypes.ITEM, stack), position.x, position.y, position.z, direction.x, direction.y + 0.05D, direction.z);
        }
    }
    
    private Random getSpecialRand() {
        Random specialRand = new Random(internalRng);
        internalRng = specialRand.nextLong();
        return specialRand;
    }

    @Override
    public int getSizeInventory() {
        return 9;
    }

    @Override
    public boolean isEmpty() {
        for (int i = 0; i < getSizeInventory(); i++) {
            ItemStack stack = getStackInSlot(i);
            if (!stack.isEmpty())
                return false;
        }

        return true;
    }

    @Override
    @Nonnull
    protected ITextComponent getDefaultName() {
        return new TranslationTextComponent("quark.container.feeding_trough");
    }

    @Override
    public void read(BlockState state, CompoundNBT nbt) {
    	super.read(state, nbt);
    	
        this.cooldown = nbt.getInt("Cooldown");
        this.internalRng = nbt.getLong("rng");
        this.stacks = NonNullList.withSize(this.getSizeInventory(), ItemStack.EMPTY);
        if (!this.checkLootAndRead(nbt))
            ItemStackHelper.loadAllItems(nbt, this.stacks);

    }

    @Override
    @Nonnull
    public CompoundNBT write(CompoundNBT nbt) {
        super.write(nbt);
        nbt.putInt("Cooldown", cooldown);
        nbt.putLong("rng", internalRng);
        if (!this.checkLootAndWrite(nbt))
            ItemStackHelper.saveAllItems(nbt, this.stacks);

        return nbt;
    }

    @Override
    @Nonnull
    protected NonNullList<ItemStack> getItems() {
        return this.stacks;
    }

    @Override
    protected void setItems(@Nonnull NonNullList<ItemStack> items) {
        this.stacks = items;
    }

    @Override
    @Nonnull
    protected Container createMenu(int id, @Nonnull PlayerInventory playerInventory) {
        return new DispenserContainer(id, playerInventory, this);
    }
}
