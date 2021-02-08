package vazkii.quark.content.automation.tile;

import java.util.List;
import java.util.Random;
import java.util.UUID;

import javax.annotation.Nonnull;

import com.mojang.authlib.GameProfile;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.LootableContainerBlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.goal.TemptGoal;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.particle.ItemStackParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.screen.Generic3x3ContainerScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Tickable;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.common.util.FakePlayer;
import vazkii.quark.base.handler.MiscUtil;
import vazkii.quark.base.util.MovableFakePlayer;
import vazkii.quark.content.automation.block.FeedingTroughBlock;
import vazkii.quark.content.automation.module.FeedingTroughModule;

/**
 * @author WireSegal
 * Created at 9:39 AM on 9/20/19.
 */
public class FeedingTroughTileEntity extends LootableContainerBlockEntity implements Tickable {

    private static final GameProfile DUMMY_PROFILE = new GameProfile(UUID.randomUUID(), "[FeedingTrough]");

    private DefaultedList<ItemStack> stacks;

    private FakePlayer foodHolder = null;

    private int cooldown = 0;
    private long internalRng = 0;

    protected FeedingTroughTileEntity(BlockEntityType<? extends FeedingTroughTileEntity> type) {
        super(type);
        this.stacks = DefaultedList.ofSize(9, ItemStack.EMPTY);
    }

    public FeedingTroughTileEntity() {
        this(FeedingTroughModule.tileEntityType);
    }

    public FakePlayer getFoodHolder(TemptGoal goal) {
        if (foodHolder == null && world instanceof ServerWorld)
            foodHolder = new MovableFakePlayer((ServerWorld) world, DUMMY_PROFILE);

        AnimalEntity entity = (AnimalEntity) goal.mob;

        if (foodHolder != null) {
            for (int i = 0; i < size(); i++) {
                ItemStack stack = getStack(i);
                if (goal.isTemptedBy(stack) && entity.isBreedingItem(stack)) {
                    foodHolder.inventory.main.set(foodHolder.inventory.selectedSlot, stack);
                    Vec3d position = new Vec3d(pos.getX(), pos.getY(), pos.getZ()).add(0.5, -1, 0.5);
                    Vec3d direction = goal.mob.getPos().subtract(position).normalize();
                    Vec2f angles = MiscUtil.getMinecraftAngles(direction);

                    Vec3d shift = direction.multiply(-0.5 / Math.max(
                            Math.abs(direction.x), Math.max(
                                    Math.abs(direction.y),
                                    Math.abs(direction.z))));

                    Vec3d truePos = position.add(shift);

                    foodHolder.refreshPositionAndAngles(truePos.x, truePos.y, truePos.z, angles.x, angles.y);
                    return foodHolder;
                }
            }
        }

        return null;
    }

    @Override
    public void tick() {
        if (world != null && !world.isClient) {
            if (cooldown > 0)
                cooldown--;
            else {
            	cooldown = FeedingTroughModule.cooldown; // minimize aabb calls
            	List<AnimalEntity> animals = world.getNonSpectatingEntities(AnimalEntity.class, new Box(pos).expand(1.5, 0, 1.5).shrink(0, 0.75, 0));
            	
                for (AnimalEntity creature : animals) {
                    if (creature.canEat() && creature.getBreedingAge() == 0) {
                        for (int i = 0; i < size(); i++) {
                            ItemStack stack = getStack(i);
                            if (creature.isBreedingItem(stack)) {
                                creature.playSound(creature.getEatSound(stack), 0.5F + 0.5F * world.random.nextInt(2), (world.random.nextFloat() - world.random.nextFloat()) * 0.2F + 1.0F);
                                addItemParticles(creature, stack, 16);
                                
                                if(getSpecialRand().nextDouble() < FeedingTroughModule.loveChance) {
                                	List<AnimalEntity> animalsAround = world.getNonSpectatingEntities(AnimalEntity.class, new Box(pos).expand(FeedingTroughModule.range));
                                	if(animalsAround.size() <= FeedingTroughModule.maxAnimals)
                                		creature.lovePlayer(null);
                                }

                                stack.decrement(1);
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
        BlockState state = getCachedState();
        if (world != null && state.getBlock() instanceof FeedingTroughBlock) {
            boolean full = state.get(FeedingTroughBlock.FULL);
            boolean shouldBeFull = !isEmpty();

            if (full != shouldBeFull)
                world.setBlockState(pos, state.with(FeedingTroughBlock.FULL, shouldBeFull), 2);
        }
    }

    private void addItemParticles(Entity entity, ItemStack stack, int count) {
        for(int i = 0; i < count; ++i) {
            Vec3d direction = new Vec3d((entity.world.random.nextFloat() - 0.5D) * 0.1D, Math.random() * 0.1D + 0.1D, 0.0D);
            direction = direction.rotateX(-entity.pitch * ((float)Math.PI / 180F));
            direction = direction.rotateY(-entity.yaw * ((float)Math.PI / 180F));
            double yVelocity = (-entity.world.random.nextFloat()) * 0.6D - 0.3D;
            Vec3d position = new Vec3d((entity.world.random.nextFloat() - 0.5D) * 0.3D, yVelocity, 0.6D);
            Vec3d entityPos = entity.getPos();
            position = position.rotateX(-entity.pitch * ((float)Math.PI / 180F));
            position = position.rotateY(-entity.yaw * ((float)Math.PI / 180F));
            position = position.add(entityPos.x, entityPos.y + entity.getStandingEyeHeight(), entityPos.z);
            if (this.world instanceof ServerWorld)
                ((ServerWorld)this.world).spawnParticles(new ItemStackParticleEffect(ParticleTypes.ITEM, stack), position.x, position.y, position.z, 1, direction.x, direction.y + 0.05D, direction.z, 0.0D);
            else if (this.world != null)
                this.world.addParticle(new ItemStackParticleEffect(ParticleTypes.ITEM, stack), position.x, position.y, position.z, direction.x, direction.y + 0.05D, direction.z);
        }
    }
    
    private Random getSpecialRand() {
        Random specialRand = new Random(internalRng);
        internalRng = specialRand.nextLong();
        return specialRand;
    }

    @Override
    public int size() {
        return 9;
    }

    @Override
    public boolean isEmpty() {
        for (int i = 0; i < size(); i++) {
            ItemStack stack = getStack(i);
            if (!stack.isEmpty())
                return false;
        }

        return true;
    }

    @Override
    @Nonnull
    protected Text getContainerName() {
        return new TranslatableText("quark.container.feeding_trough");
    }

    @Override
    public void fromTag(BlockState state, CompoundTag nbt) {
    	super.fromTag(state, nbt);
    	
        this.cooldown = nbt.getInt("Cooldown");
        this.internalRng = nbt.getLong("rng");
        this.stacks = DefaultedList.ofSize(this.size(), ItemStack.EMPTY);
        if (!this.deserializeLootTable(nbt))
            Inventories.fromTag(nbt, this.stacks);

    }

    @Override
    @Nonnull
    public CompoundTag toTag(CompoundTag nbt) {
        super.toTag(nbt);
        nbt.putInt("Cooldown", cooldown);
        nbt.putLong("rng", internalRng);
        if (!this.serializeLootTable(nbt))
            Inventories.toTag(nbt, this.stacks);

        return nbt;
    }

    @Override
    @Nonnull
    protected DefaultedList<ItemStack> getInvStackList() {
        return this.stacks;
    }

    @Override
    protected void setInvStackList(@Nonnull DefaultedList<ItemStack> items) {
        this.stacks = items;
    }

    @Override
    @Nonnull
    protected ScreenHandler createScreenHandler(int id, @Nonnull PlayerInventory playerInventory) {
        return new Generic3x3ContainerScreenHandler(id, playerInventory, this);
    }
}
