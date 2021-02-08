package vazkii.quark.content.automation.tile;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import vazkii.quark.content.automation.block.ChuteBlock;
import vazkii.quark.content.automation.module.ChuteModule;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author WireSegal
 * Created at 10:18 AM on 9/29/19.
 */
public class ChuteTileEntity extends TileEntity {
    public ChuteTileEntity() {
        super(ChuteModule.tileEntityType);
    }

    private boolean canDropItem() {
        if(world != null && world.getBlockState(pos).get(ChuteBlock.ENABLED)) {
            BlockPos below = pos.down();
            BlockState state = world.getBlockState(below);
            Block block = state.getBlock();
            return block.isAir(state, world, below) || state.getCollisionShape(world, below).isEmpty();
        }

        return false;
    }

    private final IItemHandler handler = new IItemHandler() {
        @Override
        public int getSlots() {
            return 1;
        }

        @Nonnull
        @Override
        public ItemStack getStackInSlot(int slot) {
            return ItemStack.EMPTY;
        }

        @Nonnull
        @Override
        public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
            if (!canDropItem())
                return stack;

            if(!simulate && world != null && !stack.isEmpty()) {
                ItemEntity entity = new ItemEntity(world, pos.getX() + 0.5, pos.getY() - 0.5, pos.getZ() + 0.5, stack.copy());
                entity.setMotion(0, 0, 0);
                world.addEntity(entity);
            }

            return ItemStack.EMPTY;
        }

        @Nonnull
        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            return ItemStack.EMPTY;
        }

        @Override
        public int getSlotLimit(int slot) {
            return 64;
        }

        @Override
        public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
            return true;
        }
    };

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if (side != Direction.DOWN && cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
            return LazyOptional.of(() -> handler).cast();
        return super.getCapability(cap, side);
    }
}
