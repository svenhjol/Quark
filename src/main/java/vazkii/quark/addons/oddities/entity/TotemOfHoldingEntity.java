package vazkii.quark.addons.oddities.entity;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.network.IPacket;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fml.network.NetworkHooks;
import vazkii.quark.addons.oddities.module.TotemOfHoldingModule;

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

    private static final DataParameter<Boolean> DYING = EntityDataManager.createKey(TotemOfHoldingEntity.class, DataSerializers.BOOLEAN);

    public static final int DEATH_TIME = 40;

    private int deathTicks = 0;
    private String owner;
    private List<ItemStack> storedItems = new LinkedList<>();

    public TotemOfHoldingEntity(EntityType<? extends TotemOfHoldingEntity> entityType, World worldIn) {
        super(entityType, worldIn);
    }

    @Override
    protected void registerData() {
        dataManager.register(DYING, false);
    }

    public void addItem(ItemStack stack) {
        storedItems.add(stack);
    }

    public void setOwner(PlayerEntity player) {
        owner = PlayerEntity.getUUID(player.getGameProfile()).toString();
    }

    private PlayerEntity getOwnerEntity() {
        for(PlayerEntity player : world.getPlayers()) {
            String uuid = PlayerEntity.getUUID(player.getGameProfile()).toString();
            if(uuid.equals(owner))
                return player;
        }

        return null;
    }

    @Override
    public boolean hitByEntity(Entity e) {
        if(!world.isRemote && e instanceof PlayerEntity) {
            PlayerEntity player = (PlayerEntity) e;

            if(!TotemOfHoldingModule.allowAnyoneToCollect && !player.isCreative()) {
                PlayerEntity owner = getOwnerEntity();
                if(e != owner)
                    return false;
            }

            int drops = Math.min(storedItems.size(), 3 + world.rand.nextInt(4));

            for(int i = 0; i < drops; i++) {
                ItemStack stack = storedItems.remove(0);

                if(stack.getItem() instanceof ArmorItem) {
                    ArmorItem armor = (ArmorItem) stack.getItem();
                    EquipmentSlotType slot = armor.getEquipmentSlot();
                    ItemStack curr = player.getItemStackFromSlot(slot);

                    if(curr.isEmpty()) {
                        player.setItemStackToSlot(slot, stack);
                        stack = null;
                    } else if(EnchantmentHelper.getEnchantmentLevel(Enchantments.BINDING_CURSE, curr) == 0) {
                        player.setItemStackToSlot(slot, stack);
                        stack = curr;
                    }
                }

                if(stack != null)
                    if(!player.addItemStackToInventory(stack))
                        entityDropItem(stack, 0);
            }

            if(world instanceof ServerWorld) {
                ((ServerWorld) world).spawnParticle(ParticleTypes.DAMAGE_INDICATOR, getPosX(), getPosY() + 0.5, getPosZ(), drops, 0.1, 0.5, 0.1, 0);
                ((ServerWorld) world).spawnParticle(ParticleTypes.ENCHANTED_HIT, getPosX(), getPosY() + 0.5, getPosZ(), drops, 0.4, 0.5, 0.4, 0);
            }
        }

        return false;
    }

    @Override
    public boolean canBeCollidedWith() {
        return true;
    }

    @Override
    public void tick() {
        super.tick();

        if(!isAlive())
            return;

        if(TotemOfHoldingModule.darkSoulsMode) {
            PlayerEntity owner = getOwnerEntity();
            if(owner != null && !world.isRemote) {
                String ownerTotem = TotemOfHoldingModule.getTotemUUID(owner);
                if(!getUniqueID().toString().equals(ownerTotem))
                    dropEverythingAndDie();
            }
        }

        if(storedItems.isEmpty() && !world.isRemote)
            dataManager.set(DYING, true);

        if(isDying()) {
            if(deathTicks > DEATH_TIME)
                remove();
            else deathTicks++;
        }

        else if(world.isRemote)
            world.addParticle(ParticleTypes.PORTAL, getPosX(), getPosY() + (Math.random() - 0.5) * 0.2, getPosZ(), Math.random() - 0.5, Math.random() - 0.5, Math.random() - 0.5);
    }

    private void dropEverythingAndDie() {
        if(!TotemOfHoldingModule.destroyLostItems)
            for (ItemStack storedItem : storedItems)
                entityDropItem(storedItem, 0);

        storedItems.clear();

        remove();
    }

    public int getDeathTicks() {
        return deathTicks;
    }

    public boolean isDying() {
        return dataManager.get(DYING);
    }

    @Override
    public void readAdditional(@Nonnull CompoundNBT compound) {
        ListNBT list = compound.getList(TAG_ITEMS, 10);
        storedItems = new LinkedList<>();

        for(int i = 0; i < list.size(); i++) {
            CompoundNBT cmp = list.getCompound(i);
            ItemStack stack = ItemStack.read(cmp);
            storedItems.add(stack);
        }

        boolean dying = compound.getBoolean(TAG_DYING);
        dataManager.set(DYING, dying);

        owner = compound.getString(TAG_OWNER);
    }

    @Override
    protected void writeAdditional(@Nonnull CompoundNBT compound) {
        ListNBT list = new ListNBT();
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
    public IPacket<?> createSpawnPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
}
