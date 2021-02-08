package vazkii.quark.content.experimental.module;

import java.util.List;

import com.mojang.text2speech.Narrator;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.AbstractSignBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.client.settings.KeyModifier;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import vazkii.arl.util.ClientTicker;
import vazkii.quark.base.client.handler.ModKeybindHandler;
import vazkii.quark.base.module.LoadModule;
import vazkii.quark.base.module.ModuleCategory;
import vazkii.quark.base.module.QuarkModule;

@LoadModule(category = ModuleCategory.EXPERIMENTAL, enabledByDefault = false, hasSubscriptions = true, subscribeOn = Dist.CLIENT)
public class NarratorReadoutModule extends QuarkModule {

	@Environment(EnvType.CLIENT)
	private KeyBinding keybind;
	
	@Environment(EnvType.CLIENT)
	private KeyBinding keybindFull;
	
	float last;
	
	@Override
	@Environment(EnvType.CLIENT)
	public void clientSetup() {
		if(enabled) {
			keybind = ModKeybindHandler.init("narrator_readout", "n", ModKeybindHandler.ACCESSIBILITY_GROUP);
			keybindFull = ModKeybindHandler.init("narrator_full_readout", "m", ModKeybindHandler.ACCESSIBILITY_GROUP);
		}
	}

	@SubscribeEvent
	@Environment(EnvType.CLIENT)
	public void onMouseInput(InputEvent.MouseInputEvent event) {
		boolean down = isDown(event.getButton(), 0, true, keybind);
		boolean full = isDown(event.getButton(), 0, true, keybindFull);
		
		acceptInput(down || full, down);
	}

	@SubscribeEvent
	@Environment(EnvType.CLIENT)
	public void onKeyInput(InputEvent.KeyInputEvent event) {
		boolean down = isDown(event.getKey(), event.getScanCode(), false, keybind);
		boolean full = isDown(event.getKey(), event.getScanCode(), false, keybindFull);
		
		acceptInput(down || full, down);
	}
	
	@Environment(EnvType.CLIENT)
	private boolean isDown(int key, int scancode, boolean mouse, KeyBinding keybind) {
		MinecraftClient mc = MinecraftClient.getInstance();
		if(mc.currentScreen != null) {
			if(mouse)
				return (keybind.matchesMouse(key) &&
						(keybind.getKeyModifier() == KeyModifier.NONE || keybind.getKeyModifier().isActive(KeyConflictContext.GUI)));
			
			else return (keybind.matchesKey(key, scancode) &&
					(keybind.getKeyModifier() == KeyModifier.NONE || keybind.getKeyModifier().isActive(KeyConflictContext.GUI)));
		} 
		else return keybind.isPressed();
	}
	
	@Environment(EnvType.CLIENT)
	private void acceptInput(boolean down, boolean full) {
		MinecraftClient mc = MinecraftClient.getInstance();
		
		float curr = ClientTicker.total;
		if(down && (curr - last) > 10) {
			Narrator narrator = Narrator.getNarrator();
			String readout = getReadout(mc, full);
			if(readout != null) {
				narrator.say(readout, true);
				last = curr;
			}
		}
	}
	
	@Environment(EnvType.CLIENT)
	private String getReadout(MinecraftClient mc, boolean full) {
		PlayerEntity player = mc.player;
		if(player == null)
			return I18n.translate("quark.readout.not_ingame");

		StringBuilder sb = new StringBuilder();
		
		if(mc.currentScreen == null) {
			HitResult ray = mc.crosshairTarget;
			if(ray != null && ray instanceof BlockHitResult) {
				BlockPos pos = ((BlockHitResult) ray).getBlockPos();
				BlockState state = mc.world.getBlockState(pos);
				
				Item item = state.getBlock().asItem();
				if(item != null) {
					sb.append(I18n.translate("quark.readout.looking", item.getName(new ItemStack(item)).getString()));
					
					if(full)
						sb.append(", ");
				}
				
				if(state.getBlock() instanceof AbstractSignBlock) {
					SignBlockEntity tile = (SignBlockEntity) mc.world.getBlockEntity(pos);
					sb.append(I18n.translate("quark.readout.sign_says"));
					for(Text cmp : tile.text) {
						String msg = cmp.getString().trim();
						if(!msg.isEmpty()) {
							sb.append(cmp.getString());
							sb.append(" ");
						}
					}
					
					sb.append(". ");
				}
			}
			
			if(full) {
				ItemStack stack = player.getMainHandStack();
				ItemStack stack2 = player.getOffHandStack();
				if(stack.isEmpty()) {
					stack = stack2;
					stack2 = ItemStack.EMPTY;
				}
				
				if(!stack.isEmpty()) {
					if(!stack2.isEmpty())
						sb.append(I18n.translate("quark.readout.holding_with_off", stack.getCount(), stack.getName().getString(), stack2.getCount(), stack2.getName().getString()));
					else sb.append(I18n.translate("quark.readout.holding", stack.getCount(), stack.getName().getString()));
					
					sb.append(", ");
				}
				
				sb.append(I18n.translate("quark.readout.health", (int) mc.player.getHealth()));
				sb.append(", ");
				
				sb.append(I18n.translate("quark.readout.food", mc.player.getHungerManager().getFoodLevel()));
			}
		}

		else {
			if(mc.currentScreen instanceof HandledScreen) {
				HandledScreen<?> cnt = (HandledScreen<?>) mc.currentScreen;
				Slot slot = cnt.getSlotUnderMouse();
				ItemStack stack = (slot == null ? ItemStack.EMPTY : slot.getStack());
				if(stack.isEmpty())
					sb.append(I18n.translate("quark.readout.no_item"));
				else {
					List<Text> tooltip = cnt.getTooltipFromItem(stack);
					
					for(Text t : tooltip) {
						Text print = t.shallowCopy();
						List<Text> bros = print.getSiblings();
						
						for(Text sib : bros) {
							if(sib instanceof TranslatableText) {
								TranslatableText ttc = (TranslatableText) sib;
								if(ttc.getKey().contains("enchantment.level.")) {
									bros.set(bros.indexOf(sib), new LiteralText(ttc.getKey().substring("enchantment.level.".length())));
									break;
								}
							}
						}
						
						sb.append(print.getString());
						
						if(!full)
							break;
						
						sb.append(", ");
					}
				}
			}
			else sb.append(mc.currentScreen.getNarrationMessage());
		}
		

		return sb.toString();
	}
	
}
