package vazkii.quark.content.client.module;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiPredicate;
import java.util.regex.Pattern;
import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.potion.PotionUtil;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.client.event.GuiScreenEvent.KeyboardCharTypedEvent;
import net.minecraftforge.client.event.GuiScreenEvent.KeyboardKeyPressedEvent;
import net.minecraftforge.client.event.GuiScreenEvent.MouseClickedEvent;
import net.minecraftforge.client.event.RenderTooltipEvent;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.EmptyHandler;
import vazkii.arl.util.ClientTicker;
import vazkii.arl.util.ItemNBTHelper;
import vazkii.quark.api.IQuarkButtonIgnored;
import vazkii.quark.base.client.handler.InventoryButtonHandler;
import vazkii.quark.base.client.handler.InventoryButtonHandler.ButtonTargetType;
import vazkii.quark.base.handler.GeneralConfig;
import vazkii.quark.base.handler.InventoryTransferHandler;
import vazkii.quark.base.handler.MiscUtil;
import vazkii.quark.base.handler.SimilarBlockTypeHandler;
import vazkii.quark.base.module.LoadModule;
import vazkii.quark.base.module.QuarkModule;
import vazkii.quark.content.management.client.gui.MiniInventoryButton;
import vazkii.quark.base.module.ModuleCategory;

@LoadModule(category = ModuleCategory.CLIENT, hasSubscriptions = true, subscribeOn = Dist.CLIENT)
public class ChestSearchingModule extends QuarkModule {

	@Environment(EnvType.CLIENT) 
	private static TextFieldWidget searchBar;
	
	private static String text = "";
	private static boolean searchEnabled = false;
	private static long lastClick;
	private static int matched;

	@Override
	@Environment(EnvType.CLIENT)
	public void clientSetup() {
		InventoryButtonHandler.addButtonProvider(this, ButtonTargetType.CONTAINER_INVENTORY, 1, (parent, x, y) -> 
		new MiniInventoryButton(parent, 3, x, y, "quark.gui.button.filter", (b) -> {
			searchEnabled = !searchEnabled;
			updateSearchStatus();
		}).setTextureShift(() -> searchEnabled));
	}

	@SubscribeEvent
	@Environment(EnvType.CLIENT)
	public void initGui(GuiScreenEvent.InitGuiEvent.Post event) {
		Screen gui = event.getGui();
		if(gui instanceof HandledScreen && !(event.getGui() instanceof IQuarkButtonIgnored) && !GeneralConfig.ignoredScreens.contains(event.getGui().getClass().getName())) {
			MinecraftClient mc = gui.getMinecraft();
			HandledScreen<?> chest = (HandledScreen<?>) gui;
			if(InventoryTransferHandler.accepts(chest.getScreenHandler(), mc.player)) {
				searchBar = new TextFieldWidget(mc.textRenderer, chest.getGuiLeft() + 18, chest.getGuiTop() + 6, 117, 10, new LiteralText(text));

				searchBar.setText(text);
				searchBar.setMaxLength(50);
				searchBar.setHasBorder(false);
				updateSearchStatus();

				return;
			}
		} 

		searchBar = null;
	}

	private void updateSearchStatus() {
		searchBar.setEditable(searchEnabled);
		searchBar.setVisible(searchEnabled);
		searchBar.setSelected(searchEnabled);
	}

	@SubscribeEvent
	public void charTyped(KeyboardCharTypedEvent.Pre event) {
		if(searchBar != null && searchBar.isFocused() && searchEnabled) {
			searchBar.charTyped(event.getCodePoint(), event.getModifiers());
			text = searchBar.getText();

			event.setCanceled(true);
		}
	}

	@SubscribeEvent
	public void onKeypress(KeyboardKeyPressedEvent.Pre event) {
		if(searchBar != null && searchBar.isFocused() && searchEnabled) {
			searchBar.keyPressed(event.getKeyCode(), event.getScanCode(), event.getModifiers());
			text = searchBar.getText();

			event.setCanceled(event.getKeyCode() != 256); // 256 = escape
		}
	}

	@SubscribeEvent
	public void onClick(MouseClickedEvent.Pre event) {
		if(searchBar != null && searchEnabled) {
			searchBar.mouseClicked(event.getMouseX(), event.getMouseY(), event.getButton());

			long time = System.currentTimeMillis();
			long delta = time - lastClick;
			if(delta < 200 && searchBar.isFocused()) {
				searchBar.setText("");
				text = "";
			}

			lastClick = time;
		}
	}

	@SubscribeEvent
	public void onRender(GuiScreenEvent.DrawScreenEvent.Post event) {
		if(searchBar != null && searchEnabled)
			renderElements(event.getMatrixStack(), event.getGui());
	}

	private void renderElements(MatrixStack matrix, Screen gui) {
		RenderSystem.pushMatrix();
		drawBackground(matrix, gui, searchBar.x - 11, searchBar.y - 3);

		if(!text.isEmpty()) {
			if(gui instanceof HandledScreen) {
				HandledScreen<?> guiContainer = (HandledScreen<?>) gui;
				ScreenHandler container = guiContainer.getScreenHandler();

				int guiLeft = guiContainer.getGuiLeft();
				int guiTop = guiContainer.getGuiTop();

				matched = 0;
				for(Slot s : container.slots) {
					ItemStack stack = s.getStack();
					if(!namesMatch(stack, text)) {
						int x = guiLeft + s.x;
						int y = guiTop + s.y;

						Screen.fill(matrix, x, y, x + 16, y + 16, 0xAA000000);
					} else matched++;
				}
			}
		}

		if(matched == 0 && !text.isEmpty())
			searchBar.setEditableColor(0xFF5555);
		else searchBar.setEditableColor(0xFFFFFF);

		searchBar.render(matrix, 0, 0, 0);
		RenderSystem.popMatrix();
	}

	private void drawBackground(MatrixStack matrix, Screen gui, int x, int y) {
		if(gui == null)
			return;

		RenderSystem.color4f(1F, 1F, 1F, 1F);
		RenderSystem.disableLighting();
		MinecraftClient.getInstance().getTextureManager().bindTexture(MiscUtil.GENERAL_ICONS);
		Screen.drawTexture(matrix, x, y, 0, 0, 126, 13, 256, 256);
	}

	public static boolean namesMatch(ItemStack stack) {
		return !searchEnabled || namesMatch(stack, text);
	}

	public static boolean namesMatch(ItemStack stack, String search) {
		search = Formatting.strip(search.trim().toLowerCase(Locale.ROOT));
		if(search == null || search.isEmpty())
			return true;

		if(stack.isEmpty())
			return false;

		Item item = stack.getItem();
		Identifier res = item.getRegistryName();
		if(SimilarBlockTypeHandler.isShulkerBox(res)) {
			CompoundTag cmp = ItemNBTHelper.getCompound(stack, "BlockEntityTag", true);
			if (cmp != null) {
				if (!cmp.contains("id", Constants.NBT.TAG_STRING)) {
					cmp = cmp.copy();
					cmp.putString("id", "minecraft:shulker_box");
				}
				BlockEntity te = BlockEntity.createFromTag(((BlockItem) item).getBlock().getDefaultState(), cmp); 
				if (te != null) {
					LazyOptional<IItemHandler> handler = te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
					if (handler.isPresent()) {
						IItemHandler items = handler.orElseGet(EmptyHandler::new);

						for (int i = 0; i < items.getSlots(); i++)
							if (namesMatch(items.getStackInSlot(i), search))
								return true;
					}
				}
			}
		}

		String name = stack.getName().getString();
		name = Formatting.strip(name.trim().toLowerCase(Locale.ROOT));

		StringMatcher matcher = String::contains;

		if(search.length() >= 3 && search.startsWith("\"") && search.endsWith("\"")) {
			search = search.substring(1, search.length() - 1);
			matcher = String::equals;
		}

		if(search.length() >= 3 && search.startsWith("/") && search.endsWith("/")) {
			search = search.substring(1, search.length() - 1);
			matcher = (s1, s2) -> Pattern.compile(s2).matcher(s1).find();
		}

		if(stack.hasEnchantments()) {
			Map<Enchantment, Integer> enchants = EnchantmentHelper.get(stack);
			for(Enchantment e : enchants.keySet())
				if(e != null && matcher.test(e.getName(enchants.get(e)).toString().toLowerCase(Locale.ROOT), search))
					return true;
		}

		List<Text> potionNames = new ArrayList<>();
		PotionUtil.buildTooltip(stack, potionNames, 1F);
		for(Text s : potionNames) {
			if (matcher.test(Formatting.strip(s.toString().trim().toLowerCase(Locale.ROOT)), search))
				return true;
		}




		for(Map.Entry<Enchantment, Integer> entry : EnchantmentHelper.get(stack).entrySet()) {
			int lvl = entry.getValue();
			Enchantment e = entry.getKey();
			if(e != null && matcher.test(e.getName(lvl).toString().toLowerCase(Locale.ROOT), search))
				return true;
		}

		ItemGroup tab = item.getGroup();
		if(tab != null && matcher.test(tab.getTranslationKey().getString().toLowerCase(Locale.ROOT), search))
			return true;

		//		if(search.matches("favou?rites?") && FavoriteItems.isItemFavorited(stack))
		//			return true;

		Identifier itemName = item.getRegistryName();
		Optional<? extends ModContainer> mod = ModList.get().getModContainerById(itemName.getPath());
		if(mod.isPresent() && matcher.test(mod.orElse(null).getModInfo().getDisplayName().toLowerCase(Locale.ROOT), search))
			return true;

		if(matcher.test(name, search))
			return true;

		return false;
		//		return ISearchHandler.hasHandler(stack) && ISearchHandler.getHandler(stack).stackMatchesSearchQuery(search, matcher, ChestSearchBar::namesMatch);
	}

	private interface StringMatcher extends BiPredicate<String, String> { }

}
