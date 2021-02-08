package vazkii.quark.addons.oddities.module;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScreenManager;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.inventory.InventoryScreen;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.DyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemModelsProperties;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Rarity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.common.extensions.IForgeContainerType;
import net.minecraftforge.event.TickEvent.ClientTickEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import vazkii.arl.util.RegistryHelper;
import vazkii.quark.addons.oddities.client.screen.BackpackInventoryScreen;
import vazkii.quark.addons.oddities.container.BackpackContainer;
import vazkii.quark.addons.oddities.item.BackpackItem;
import vazkii.quark.base.Quark;
import vazkii.quark.base.block.QuarkBlock;
import vazkii.quark.base.client.handler.RequiredModTooltipHandler;
import vazkii.quark.base.item.QuarkItem;
import vazkii.quark.base.module.LoadModule;
import vazkii.quark.base.module.QuarkModule;
import vazkii.quark.base.module.ModuleCategory;
import vazkii.quark.base.module.config.Config;
import vazkii.quark.base.network.QuarkNetwork;
import vazkii.quark.base.network.message.HandleBackpackMessage;

@LoadModule(category = ModuleCategory.ODDITIES, hasSubscriptions = true, requiredMod = Quark.ODDITIES_ID)
public class BackpackModule extends QuarkModule {

	@Config(description =  "Set this to true to allow the backpacks to be unequipped even with items in them") 
	public static boolean superOpMode = false;
	
	@Config(flag = "ravager_hide")
	public static boolean enableRavagerHide = true;
	
	@Config public static int baseRavagerHideDrop = 1;
	@Config public static double extraChancePerLooting = 0.5;

	public static Item backpack;
	public static Item ravager_hide;
	
	public static Block bonded_ravager_hide;
	
    public static ContainerType<BackpackContainer> container;
    private static ItemStack heldStack = null;

	@OnlyIn(Dist.CLIENT)
	private static boolean backpackRequested;

	@Override
	public void construct() {
		backpack = new BackpackItem(this);
		ravager_hide = new QuarkItem("ravager_hide", this, new Item.Properties().rarity(Rarity.RARE).group(ItemGroup.MATERIALS)).setCondition(() -> enableRavagerHide);
		
		container = IForgeContainerType.create(BackpackContainer::fromNetwork);
		RegistryHelper.register(container, "backpack");
		
		bonded_ravager_hide = new QuarkBlock("bonded_ravager_hide", this, ItemGroup.BUILDING_BLOCKS, Block.Properties.create(Material.WOOL, DyeColor.BLACK)
				.hardnessAndResistance(1F)
				.sound(SoundType.CLOTH))
		.setCondition(() -> enableRavagerHide);
	}
	
	@Override
	@OnlyIn(Dist.CLIENT)
	public void clientSetup() {
		ScreenManager.registerFactory(container, BackpackInventoryScreen::new);
		
		ItemModelsProperties.registerProperty(backpack, new ResourceLocation("has_items"), 
				(stack, world, entity) -> (!BackpackModule.superOpMode && BackpackItem.doesBackpackHaveItems(stack)) ? 1 : 0);
		
		RequiredModTooltipHandler.map(backpack, Quark.ODDITIES_ID);
		RequiredModTooltipHandler.map(ravager_hide, Quark.ODDITIES_ID);
		RequiredModTooltipHandler.map(bonded_ravager_hide, Quark.ODDITIES_ID);
	}
	
	@SubscribeEvent
	public void onDrops(LivingDropsEvent event) {
		LivingEntity entity = event.getEntityLiving();
		if(enableRavagerHide && entity.getType() == EntityType.RAVAGER) {
			int amount = baseRavagerHideDrop;
			double chance = (double) event.getLootingLevel() * extraChancePerLooting;
			while(chance > baseRavagerHideDrop) {
				chance--;
				amount++;
			}
			if(chance > 0 && entity.world.rand.nextDouble() < chance)
				amount++;
			
			event.getDrops().add(new ItemEntity(entity.world, entity.getPosX(), entity.getPosY(), entity.getPosZ(), new ItemStack(ravager_hide, amount)));
		}
	}

	@SubscribeEvent
	@OnlyIn(Dist.CLIENT)
	public void onOpenGUI(GuiOpenEvent event) {
		PlayerEntity player = Minecraft.getInstance().player;
		if(player != null && isInventoryGUI(event.getGui()) && !player.isCreative() && isEntityWearingBackpack(player)) {
			requestBackpack();
			event.setCanceled(true);
		}
	}
	
	@SubscribeEvent
	@OnlyIn(Dist.CLIENT)
	public void clientTick(ClientTickEvent event) {
		Minecraft mc = Minecraft.getInstance();
		if(isInventoryGUI(mc.currentScreen) && !backpackRequested && isEntityWearingBackpack(mc.player)) {
			requestBackpack();
			backpackRequested = true;
		} else if(mc.currentScreen instanceof BackpackInventoryScreen) {
			if(heldStack != null) {
				mc.player.inventory.setItemStack(heldStack);
				heldStack = null;
			}
			
			backpackRequested = false;
		}
	}

	private void requestBackpack() {
		heldStack = Minecraft.getInstance().player.inventory.getItemStack();
		QuarkNetwork.sendToServer(new HandleBackpackMessage(true));
	}

	@SubscribeEvent
	@OnlyIn(Dist.CLIENT)
	public void removeCurseTooltip(ItemTooltipEvent event) {
		if(!superOpMode && event.getItemStack().getItem() instanceof BackpackItem)
			for(ITextComponent s : event.getToolTip())
				if(s.getString().equals(Enchantments.BINDING_CURSE.getDisplayName(1).getString())) {
					event.getToolTip().remove(s);
					return;
				}
	}

	@OnlyIn(Dist.CLIENT)
	private static boolean isInventoryGUI(Screen gui) {
		return gui != null && gui.getClass() == InventoryScreen.class;
	}
	
	public static boolean isEntityWearingBackpack(Entity e) {
		if(e instanceof LivingEntity) {
			LivingEntity living = (LivingEntity) e;
			ItemStack chestArmor = living.getItemStackFromSlot(EquipmentSlotType.CHEST);
			return chestArmor.getItem() instanceof BackpackItem;
		}

		return false;
	}

	public static boolean isEntityWearingBackpack(Entity e, ItemStack stack) {
		if(e instanceof LivingEntity) {
			LivingEntity living = (LivingEntity) e;
			ItemStack chestArmor = living.getItemStackFromSlot(EquipmentSlotType.CHEST);
			return chestArmor == stack;
		}

		return false;
	}

}
