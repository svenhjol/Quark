package vazkii.quark.content.experimental.module;

import java.util.List;

import com.mojang.text2speech.Narrator;

import net.minecraft.block.AbstractSignBlock;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.SignTileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
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

	@OnlyIn(Dist.CLIENT)
	private KeyBinding keybind;
	
	@OnlyIn(Dist.CLIENT)
	private KeyBinding keybindFull;
	
	float last;
	
	@Override
	@OnlyIn(Dist.CLIENT)
	public void clientSetup() {
		if(enabled) {
			keybind = ModKeybindHandler.init("narrator_readout", "n", ModKeybindHandler.ACCESSIBILITY_GROUP);
			keybindFull = ModKeybindHandler.init("narrator_full_readout", "m", ModKeybindHandler.ACCESSIBILITY_GROUP);
		}
	}

	@SubscribeEvent
	@OnlyIn(Dist.CLIENT)
	public void onMouseInput(InputEvent.MouseInputEvent event) {
		boolean down = isDown(event.getButton(), 0, true, keybind);
		boolean full = isDown(event.getButton(), 0, true, keybindFull);
		
		acceptInput(down || full, down);
	}

	@SubscribeEvent
	@OnlyIn(Dist.CLIENT)
	public void onKeyInput(InputEvent.KeyInputEvent event) {
		boolean down = isDown(event.getKey(), event.getScanCode(), false, keybind);
		boolean full = isDown(event.getKey(), event.getScanCode(), false, keybindFull);
		
		acceptInput(down || full, down);
	}
	
	@OnlyIn(Dist.CLIENT)
	private boolean isDown(int key, int scancode, boolean mouse, KeyBinding keybind) {
		Minecraft mc = Minecraft.getInstance();
		if(mc.currentScreen != null) {
			if(mouse)
				return (keybind.matchesMouseKey(key) &&
						(keybind.getKeyModifier() == KeyModifier.NONE || keybind.getKeyModifier().isActive(KeyConflictContext.GUI)));
			
			else return (keybind.matchesKey(key, scancode) &&
					(keybind.getKeyModifier() == KeyModifier.NONE || keybind.getKeyModifier().isActive(KeyConflictContext.GUI)));
		} 
		else return keybind.isKeyDown();
	}
	
	@OnlyIn(Dist.CLIENT)
	private void acceptInput(boolean down, boolean full) {
		Minecraft mc = Minecraft.getInstance();
		
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
	
	@OnlyIn(Dist.CLIENT)
	private String getReadout(Minecraft mc, boolean full) {
		PlayerEntity player = mc.player;
		if(player == null)
			return I18n.format("quark.readout.not_ingame");

		StringBuilder sb = new StringBuilder();
		
		if(mc.currentScreen == null) {
			RayTraceResult ray = mc.objectMouseOver;
			if(ray != null && ray instanceof BlockRayTraceResult) {
				BlockPos pos = ((BlockRayTraceResult) ray).getPos();
				BlockState state = mc.world.getBlockState(pos);
				
				Item item = state.getBlock().asItem();
				if(item != null) {
					sb.append(I18n.format("quark.readout.looking", item.getDisplayName(new ItemStack(item)).getString()));
					
					if(full)
						sb.append(", ");
				}
				
				if(state.getBlock() instanceof AbstractSignBlock) {
					SignTileEntity tile = (SignTileEntity) mc.world.getTileEntity(pos);
					sb.append(I18n.format("quark.readout.sign_says"));
					for(ITextComponent cmp : tile.signText) {
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
				ItemStack stack = player.getHeldItemMainhand();
				ItemStack stack2 = player.getHeldItemOffhand();
				if(stack.isEmpty()) {
					stack = stack2;
					stack2 = ItemStack.EMPTY;
				}
				
				if(!stack.isEmpty()) {
					if(!stack2.isEmpty())
						sb.append(I18n.format("quark.readout.holding_with_off", stack.getCount(), stack.getDisplayName().getString(), stack2.getCount(), stack2.getDisplayName().getString()));
					else sb.append(I18n.format("quark.readout.holding", stack.getCount(), stack.getDisplayName().getString()));
					
					sb.append(", ");
				}
				
				sb.append(I18n.format("quark.readout.health", (int) mc.player.getHealth()));
				sb.append(", ");
				
				sb.append(I18n.format("quark.readout.food", mc.player.getFoodStats().getFoodLevel()));
			}
		}

		else {
			if(mc.currentScreen instanceof ContainerScreen) {
				ContainerScreen<?> cnt = (ContainerScreen<?>) mc.currentScreen;
				Slot slot = cnt.getSlotUnderMouse();
				ItemStack stack = (slot == null ? ItemStack.EMPTY : slot.getStack());
				if(stack.isEmpty())
					sb.append(I18n.format("quark.readout.no_item"));
				else {
					List<ITextComponent> tooltip = cnt.getTooltipFromItem(stack);
					
					for(ITextComponent t : tooltip) {
						ITextComponent print = t.deepCopy();
						List<ITextComponent> bros = print.getSiblings();
						
						for(ITextComponent sib : bros) {
							if(sib instanceof TranslationTextComponent) {
								TranslationTextComponent ttc = (TranslationTextComponent) sib;
								if(ttc.getKey().contains("enchantment.level.")) {
									bros.set(bros.indexOf(sib), new StringTextComponent(ttc.getKey().substring("enchantment.level.".length())));
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
