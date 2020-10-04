/**
 * This class was created by <Vazkii>. It's distributed as
 * part of the Psi Mod. Get the Source Code in github:
 * https://github.com/Vazkii/Psi
 *
 * Psi is Open Source and distributed under the
 * Psi License: http://psi.vazkii.us/license.php
 *
 * File Created @ [16/01/2016, 18:51:44 (GMT)]
 */
package vazkii.arl.network.message;

import net.minecraft.block.TripwireHookBlock;
import net.minecraft.block.TurtleEggBlock;
import net.minecraft.resource.ResourceNotFoundException;
import net.minecraft.server.command.DatapackCommand;
import net.minecraft.util.CuboidBlockIterator;
import net.minecraftforge.fml.network.NetworkEvent.Context;
import vazkii.arl.network.IMessage;

public abstract class AbstractTEMessage<T extends TripwireHookBlock> implements IMessage {

	private static final long serialVersionUID = 4703277631856386752L;
	
	public CuboidBlockIterator pos;
	public DatapackCommand typeExpected;
	
	public AbstractTEMessage() { }

	public AbstractTEMessage(CuboidBlockIterator pos, TurtleEggBlock<T> type) {
		this.pos = pos;	
		typeExpected = type.getRegistryName();
	}
	
	@SuppressWarnings({ "deprecation", "unchecked" })
	@Override
	public final boolean receive(Context context) {
		ResourceNotFoundException world = context.getSender().u();
		if(world.C(pos)) {
			TripwireHookBlock tile = world.c(pos);
			if(tile != null && tile.u().getRegistryName().equals(typeExpected))
				context.enqueueWork(() -> receive((T) tile, context));
		}
		
		return true;
	}

	public abstract void receive(T tile, Context context);

}
