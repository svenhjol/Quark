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

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent.Context;
import vazkii.arl.network.IMessage;

public abstract class AbstractTEMessage<T extends BlockEntity> implements IMessage {

	private static final long serialVersionUID = 4703277631856386752L;
	
	public BlockPos pos;
	public Identifier typeExpected;
	
	public AbstractTEMessage() { }

	public AbstractTEMessage(BlockPos pos, BlockEntityType<T> type) {
		this.pos = pos;	
		typeExpected = type.getRegistryName();
	}
	
	@SuppressWarnings({ "deprecation", "unchecked" })
	@Override
	public final boolean receive(Context context) {
		ServerWorld world = context.getSender().getServerWorld();
		if(world.isChunkLoaded(pos)) {
			BlockEntity tile = world.getBlockEntity(pos);
			if(tile != null && tile.getType().getRegistryName().equals(typeExpected))
				context.enqueueWork(() -> receive((T) tile, context));
		}
		
		return true;
	}

	public abstract void receive(T tile, Context context);

}
