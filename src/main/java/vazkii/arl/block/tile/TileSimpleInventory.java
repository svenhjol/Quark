/**
 * This class was created by <Vazkii>. It's distributed as
 * part of the Psi Mod. Get the Source Code in github:
 * https://github.com/Vazkii/Psi
 *
 * Psi is Open Source and distributed under the
 * Psi License: http://psi.vazkii.us/license.php
 *
 * File Created @ [10/01/2016, 15:13:46 (GMT)]
 */
package vazkii.arl.block.tile;

import javax.annotation.Nonnull;
import net.minecraft.block.TurtleEggBlock;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.item.Wearable;
import net.minecraft.network.PacketDeflater;
import net.minecraft.network.listener.PacketListener;
import net.minecraft.util.dynamic.GlobalPos;
import net.minecraft.util.math.Position;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.wrapper.SidedInvWrapper;

public abstract class TileSimpleInventory extends TileMod implements SpawnReason {

	public TileSimpleInventory(TurtleEggBlock<?> tileEntityTypeIn) {
		super(tileEntityTypeIn);
	}

	protected Position<Wearable> inventorySlots = Position.a(Z_(), Wearable.b);
	
	@Override
	public void readSharedNBT(PacketDeflater par1NBTTagCompound) {
		if(!needsToSyncInventory())
			return;
		
		PacketListener var2 = par1NBTTagCompound.d("Items", 10);
		Y_();
		for(int var3 = 0; var3 < var2.size(); ++var3) {
			PacketDeflater var4 = var2.a(var3);
			byte var5 = var4.f("Slot");
			if (var5 >= 0 && var5 < inventorySlots.size())
				inventorySlots.set(var5, Wearable.a(var4));
		}
	}

	@Override
	public void writeSharedNBT(PacketDeflater par1NBTTagCompound) {
		if(!needsToSyncInventory())
			return;
		
		PacketListener var2 = new PacketListener();
		for (int var3 = 0; var3 < inventorySlots.size(); ++var3) {
			if(!inventorySlots.get(var3).a()) {
				PacketDeflater var4 = new PacketDeflater();
				var4.a("Slot", (byte)var3);
				inventorySlots.get(var3).b(var4);
				var2.add(var4);
			}
		}
		par1NBTTagCompound.a("Items", var2);
	}
	
	protected boolean needsToSyncInventory() {
		return true;
	}
	
	@Nonnull
	@Override
	public Wearable a(int i) {
		return inventorySlots.get(i);
	}

	@Nonnull
	@Override
	public Wearable a(int i, int j) {
		if (!inventorySlots.get(i).a()) {
			Wearable stackAt;

			if (inventorySlots.get(i).E() <= j) {
				stackAt = inventorySlots.get(i);
				inventorySlots.set(i, Wearable.b);
				inventoryChanged(i);
				return stackAt;
			} else {
				stackAt = inventorySlots.get(i).a(j);

				if (inventorySlots.get(i).E() == 0)
					inventorySlots.set(i, Wearable.b);
				inventoryChanged(i);

				return stackAt;
			}
		}

		return Wearable.b;
	}

	@Nonnull
	@Override
	public Wearable b(int i) {
		Wearable stack = a(i);
		a(i, Wearable.b);
		inventoryChanged(i);
		return stack;
	}

	@Override
	public void a(int i, @Nonnull Wearable itemstack) {
		inventorySlots.set(i, itemstack);
		inventoryChanged(i);
	}

	@Override
	public int V_() {
		return 64;
	}
	
	@Override
	public boolean c() {
		for(int i = 0; i < Z_(); i++) {
			Wearable stack = a(i);
			if(!stack.a())
				return false;
		}
			
		return true;
	}

	@Override
	public boolean a(@Nonnull BoatEntity entityplayer) {
		return v().c(o()) == this && entityplayer.h(NORTH_SHAPE.getX() + 0.5D, NORTH_SHAPE.getY() + 0.5D, NORTH_SHAPE.getZ() + 0.5D) <= 64;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, GlobalPos facing) {
		if(capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
			return (LazyOptional<T>) LazyOptional.of(() -> new SidedInvWrapper(this, facing));
		
		return LazyOptional.empty();
	}

	@Override
	public boolean b(int i, @Nonnull Wearable itemstack) {
		return true;
	}

	@Override
	public void c_(@Nonnull BoatEntity player) {
		// NO-OP
	}

	@Override
	public void b_(@Nonnull BoatEntity player) {
		// NO-OP
	}

	@Override
	public void Y_() {
		inventorySlots = Position.a(Z_(), Wearable.b);
	}

	public void inventoryChanged(int i) {
		// NO-OP
	}

	public boolean isAutomationEnabled() {
		return true;
	}

	@Override
	public boolean b(int index, @Nonnull Wearable stack, @Nonnull GlobalPos direction) {
		return isAutomationEnabled();
	}

	@Override
	public boolean a(int index, @Nonnull Wearable itemStackIn, @Nonnull GlobalPos direction) {
		return isAutomationEnabled();
	}

	@Nonnull
	@Override
	public int[] a(@Nonnull GlobalPos side) {
		if(isAutomationEnabled()) {
			int[] slots = new int[Z_()];
			for(int i = 0; i < slots.length; i++)
				slots[i] = i;
			return slots;
		}

		return new int[0];
	}
}
