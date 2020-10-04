package vazkii.quark.oddities.entity;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.Packet;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkHooks;
import vazkii.quark.oddities.module.TotemOfHoldingModule;

import javax.annotation.Nonnull;
import java.util.LinkedList;
import java.util.List;

/**
 * @author WireSegal
 * Created at 1:34 PM on 3/30/20.
 */
public class TotemOfHoldingEntity extends Entity {
    private static final String TAG_ITEMS = "storedItems";
    private static final String TAG_DYING = "dying";
    private static final String TAG_OWNER = "owner";

    private static final TrackedData<Boolean> DYING = DataTracker.registerData(TotemOfHoldingEntity.class, TrackedDataHandlerRegistry.BOOLEAN);

    public static final int DEATH_TIME = 40;

    private int deathTicks = 0;
    private String owner;
    private List<ItemStack> storedItems = new LinkedList<>();

    public TotemOfHoldingEntity(EntityType<? extends TotemOfHoldingEntity> entityType, World worldIn) {
        super(entityType, worldIn);
    }

    @Override
    protected void initDataTracker() {
        dataTracker.startTracking(DYING, false);
    }

    public void addItem(ItemStack stack) {
        storedItems.add(stack);
    }

    public void setOwner(PlayerEntity player) {
        owner = PlayerEntity.getUuidFromProfile(player.getGameProfile()).toString();
    }

    private PlayerEntity getOwnerEntity() {
        for(PlayerEntity player : world.getPlayers()) {
            String uuid = PlayerEntity.getUuidFromProfile(player.getGameProfile()).toString();
            if(uuid.equals(owner))
                return player;
        }

        return null;
    }

    @Override
    public boolean handleAttack(Entity e) {
        if(!world.isClient && e instanceof PlayerEntity) {
            PlayerEntity player = (PlayerEntity) e;

            if(!TotemOfHoldingModule.allowAnyoneToCollect && !player.isCreative()) {
                PlayerEntity owner = getOwnerEntity();
                if(e != owner)
                    return false;
            }

            int drops = Math.min(storedItems.size(), 3 + world.random.nextInt(4));

            for(int i = 0; i < drops; i++) {
                ItemStack stack = storedItems.remove(0);

                if(stack.getItem() instanceof ArmorItem) {
                    ArmorItem armor = (ArmorItem) stack.getItem();
                    EquipmentSlot slot = armor.getSlotType();
                    ItemStack curr = player.getEquippedStack(slot);

                    if(curr.isEmpty()) {
                        player.equipStack(slot, stack);
                        stack = null;
                    } else if(EnchantmentHelper.getLevel(Enchantments.BINDING_CURSE, curr) == 0) {
                        player.equipStack(slot, stack);
                        stack = curr;
                    }
                }

                if(stack != null)
                    if(!player.giveItemStack(stack))
                        dropStack(stack, 0);
            }

            if(world instanceof ServerWorld) {
                ((ServerWorld) world).spawnParticles(ParticleTypes.DAMAGE_INDICATOR, getX(), getY() + 0.5, getZ(), drops, 0.1, 0.5, 0.1, 0);
                ((ServerWorld) world).spawnParticles(ParticleTypes.ENCHANTED_HIT, getX(), getY() + 0.5, getZ(), drops, 0.4, 0.5, 0.4, 0);
            }
        }

        return false;
    }

    @Override
    public boolean collides() {
        return true;
    }

    @Override
    public void tick() {
        super.tick();

        if(!isAlive())
            return;

        if(TotemOfHoldingModule.darkSoulsMode) {
            PlayerEntity owner = getOwnerEntity();
            if(owner != null && !world.isClient) {
                String ownerTotem = TotemOfHoldingModule.getTotemUUID(owner);
                if(!getUuid().toString().equals(ownerTotem))
                    dropEverythingAndDie();
            }
        }

        if(storedItems.isEmpty() && !world.isClient)
            dataTracker.set(DYING, true);

        if(isDying()) {
            if(deathTicks > DEATH_TIME)
                remove();
            else deathTicks++;
        }

        else if(world.isClient)
            world.addParticle(ParticleTypes.PORTAL, getX(), getY() + (Math.random() - 0.5) * 0.2, getZ(), Math.random() - 0.5, Math.random() - 0.5, Math.random() - 0.5);
    }

    private void dropEverythingAndDie() {
        if(!TotemOfHoldingModule.destroyLostItems)
            for (ItemStack storedItem : storedItems)
                dropStack(storedItem, 0);

        storedItems.clear();

        remove();
    }

    public int getDeathTicks() {
        return deathTicks;
    }

    public boolean isDying() {
        return dataTracker.get(DYING);
    }

    @Override
    public void readCustomDataFromTag(@Nonnull CompoundTag compound) {
        ListTag list = compound.getList(TAG_ITEMS, 10);
        storedItems = new LinkedList<>();

        for(int i = 0; i < list.size(); i++) {
            CompoundTag cmp = list.getCompound(i);
            ItemStack stack = ItemStack.fromTag(cmp);
            storedItems.add(stack);
        }

        boolean dying = compound.getBoolean(TAG_DYING);
        dataTracker.set(DYING, dying);

        owner = compound.getString(TAG_OWNER);
    }

    @Override
    protected void writeCustomDataToTag(@Nonnull CompoundTag compound) {
        ListTag list = new ListTag();
        for(ItemStack stack : storedItems) {
            list.add(stack.serializeNBT());
        }

        compound.put(TAG_ITEMS, list);
        compound.putBoolean(TAG_DYING, isDying());
        if (owner != null)
            compound.putString(TAG_OWNER, owner);
    }

    @Nonnull
    @Override
    public Packet<?> createSpawnPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
}
