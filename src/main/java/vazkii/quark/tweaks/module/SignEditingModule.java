package vazkii.quark.tweaks.module;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.DyeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.Event.Result;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import vazkii.quark.base.module.LoadModule;
import vazkii.quark.base.module.Module;
import vazkii.quark.base.module.ModuleCategory;
import vazkii.quark.base.module.ModuleLoader;
import vazkii.quark.base.module.config.Config;
import vazkii.quark.base.network.QuarkNetwork;
import vazkii.quark.base.network.message.EditSignMessage;

@LoadModule(category = ModuleCategory.TWEAKS, hasSubscriptions = true)
public class SignEditingModule extends Module {

	@Config public static boolean requiresEmptyHand = false;

	@Environment(EnvType.CLIENT)
	public static void openSignGuiClient(BlockPos pos) {
		if(!ModuleLoader.INSTANCE.isModuleEnabled(SignEditingModule.class))
			return;

		MinecraftClient mc = MinecraftClient.getInstance();
		BlockEntity tile = mc.world.getBlockEntity(pos);

		if(tile instanceof SignBlockEntity)
			mc.player.openEditSignScreen((SignBlockEntity) tile);
	}

	@SubscribeEvent
	public void onInteract(PlayerInteractEvent.RightClickBlock event) {
		if(event.getUseBlock() == Result.DENY)
			return;	
		
		BlockEntity tile = event.getWorld().getBlockEntity(event.getPos());
		PlayerEntity player = event.getPlayer();
		ItemStack stack = player.getMainHandStack();

		if(player instanceof ServerPlayerEntity 
				&& tile instanceof SignBlockEntity 
				&& !doesSignHaveCommand((SignBlockEntity) tile)
				&& (!requiresEmptyHand || stack.isEmpty()) 
				&& !(stack.getItem() instanceof DyeItem)
				&& !tile.getCachedState().getBlock().getRegistryName().getNamespace().equals("signbutton")
				&& player.canPlaceOn(event.getPos(), event.getFace(), event.getItemStack()) 
				&& !event.getEntity().isSneaky()) {

			SignBlockEntity sign = (SignBlockEntity) tile;
			sign.setEditor(player);
			sign.editable = true;

			QuarkNetwork.sendToPlayer(new EditSignMessage(event.getPos()), (ServerPlayerEntity) player);
			
			event.setCanceled(true);
			event.setCancellationResult(ActionResult.SUCCESS);
		}
	}

	private boolean doesSignHaveCommand(SignBlockEntity sign) {
		for(Text itextcomponent : sign.text) { 
			Style style = itextcomponent == null ? null : itextcomponent.getStyle();
			if (style != null && style.getClickEvent() != null) {
				ClickEvent clickevent = style.getClickEvent();
				if (clickevent.getAction() == ClickEvent.Action.RUN_COMMAND) {
					return true;
				}
			}
		}

		return false;
	}

}
