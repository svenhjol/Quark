/**
 * This class was created by <Vazkii>. It's distributed as
 * part of the Quark Mod. Get the Source Code in github:
 * https://github.com/Vazkii/Quark
 *
 * Quark is Open Source and distributed under the
 * CC-BY-NC-SA 3.0 License: https://creativecommons.org/licenses/by-nc-sa/3.0/deed.en_GB
 *
 * File Created @ [02/04/2016, 17:44:30 (GMT)]
 */
package vazkii.quark.base.network.message;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.network.NetworkEvent;
import vazkii.arl.network.IMessage;
import vazkii.quark.base.network.QuarkNetwork;

public class SpamlessChatMessage implements IMessage {

	private static final long serialVersionUID = -4716987873031723456L;

	public Text message;
	public int id;

	public SpamlessChatMessage() { }

	public SpamlessChatMessage(Text message, int id) {
		this.message = message;
		this.id = id;
	}

	@Override
	@Environment(EnvType.CLIENT)
	public boolean receive(NetworkEvent.Context context) {
		context.enqueueWork(() -> {
			InGameHud gui = MinecraftClient.getInstance().inGameHud;
			gui.getChatHud().addMessage(message, id, gui.getTicks(), false); // print message and delete if same ID, called by printChatMessageWithOptionalDeletion
		});
		
		return true;
	}

	public static void sendToPlayer(PlayerEntity player, int id, Text component) {
		if (player instanceof ServerPlayerEntity)
			QuarkNetwork.sendToPlayer(new SpamlessChatMessage(component, id), (ServerPlayerEntity) player);
	}

}
