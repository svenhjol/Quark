package vazkii.quark.addons.oddities.item;

import java.util.Map;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

import javax.annotation.Nonnull;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.color.item.ItemColorProvider;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ArmorMaterials;
import net.minecraft.item.DyeableArmorItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.Rarity;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import vazkii.arl.interf.IItemColorProvider;
import vazkii.arl.util.ItemNBTHelper;
import vazkii.arl.util.RegistryHelper;
import vazkii.quark.addons.oddities.client.model.BackpackModel;
import vazkii.quark.addons.oddities.container.BackpackContainer;
import vazkii.quark.addons.oddities.module.BackpackModule;
import vazkii.quark.base.Quark;
import vazkii.quark.base.handler.ProxiedItemStackHandler;
import vazkii.quark.base.item.IQuarkItem;
import vazkii.quark.base.module.QuarkModule;

public class BackpackItem extends DyeableArmorItem implements IQuarkItem, IItemColorProvider, NamedScreenHandlerFactory {

	private static final String WORN_TEXTURE = Quark.MOD_ID + ":textures/misc/backpack_worn.png";
	private static final String WORN_OVERLAY_TEXTURE = Quark.MOD_ID + ":textures/misc/backpack_worn_overlay.png";

	private final QuarkModule module;

	@Environment(EnvType.CLIENT)
	@SuppressWarnings("rawtypes")
	private BipedEntityModel model;

	public BackpackItem(QuarkModule module) {
		super(ArmorMaterials.LEATHER, EquipmentSlot.CHEST, 
				new Item.Settings()
				.maxCount(1)
				.maxDamage(0)
				.group(ItemGroup.TOOLS)
				.rarity(Rarity.RARE));

		RegistryHelper.registerItem(this, "backpack");
		this.module = module;
	}

	@Override
	public QuarkModule getModule() {
		return module;
	}

	public static boolean doesBackpackHaveItems(ItemStack stack) {
		LazyOptional<IItemHandler> handlerOpt  = stack.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);

		if (!handlerOpt.isPresent())
			return false;

		IItemHandler handler = handlerOpt.orElse(null); 
		for(int i = 0; i < handler.getSlots(); i++)
			if(!handler.getStackInSlot(i).isEmpty())
				return true;

		return false;
	}
	
	@Override
	public boolean isDamageable() {
		return false;
	}
	
	@Override
	public <T extends LivingEntity> int damageItem(ItemStack stack, int amount, T entity, Consumer<T> onBroken) {
		return 0;
	}

	@Override
	public void inventoryTick(ItemStack stack, World worldIn, Entity entityIn, int itemSlot, boolean isSelected) {
		if(worldIn.isClient)
			return;

		boolean hasItems = !BackpackModule.superOpMode && doesBackpackHaveItems(stack);

		Map<Enchantment, Integer> enchants = EnchantmentHelper.get(stack);
		boolean isCursed = enchants.containsKey(Enchantments.BINDING_CURSE);
		boolean changedEnchants = false;

		if(hasItems) {
			if(BackpackModule.isEntityWearingBackpack(entityIn, stack)) {
				if(!isCursed) {
					enchants.put(Enchantments.BINDING_CURSE, 1);
					changedEnchants = true;
				}
			} else {
				ItemStack copy = stack.copy();
				stack.setCount(0);
				entityIn.dropStack(copy, 0);
			}
		} else if(isCursed) {
			enchants.remove(Enchantments.BINDING_CURSE);
			changedEnchants = true;
		}

		if(changedEnchants)
			EnchantmentHelper.set(enchants, stack);
	}

	@Override
	public boolean onEntityItemUpdate(ItemStack stack, ItemEntity entityItem) {
		if(BackpackModule.superOpMode || entityItem.world.isClient)
			return false;

		if (!ItemNBTHelper.detectNBT(stack))
			return false;

		LazyOptional<IItemHandler> handlerOpt  = stack.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);

		if(!handlerOpt.isPresent())
			return false;

		IItemHandler handler = handlerOpt.orElse(null); 

		for(int i = 0; i < handler.getSlots(); i++) {
			ItemStack stackAt = handler.getStackInSlot(i);
			if(!stackAt.isEmpty()) {
				ItemStack copy = stackAt.copy();
				ItemScatterer.spawn(entityItem.world, entityItem.getX(), entityItem.getY(), entityItem.getZ(), copy);
			}
		}

		CompoundTag comp = ItemNBTHelper.getNBT(stack);
		comp.remove("Inventory");
		if (comp.getSize() == 0)
			stack.setTag(null);

		return false;
	}

	@Nonnull
	@Override
	public ICapabilityProvider initCapabilities(ItemStack stack, CompoundTag oldCapNbt) {
		ProxiedItemStackHandler handler = new ProxiedItemStackHandler(stack, 27);

		if (oldCapNbt != null && oldCapNbt.contains("Parent")) {
			CompoundTag itemData = oldCapNbt.getCompound("Parent");
			ItemStackHandler stacks = new ItemStackHandler();
			stacks.deserializeNBT(itemData);

			for (int i = 0; i < stacks.getSlots(); i++)
				handler.setStackInSlot(i, stacks.getStackInSlot(i));

			oldCapNbt.remove("Parent");
		}	

		return handler;
	}

	@Override
	public Multimap<EntityAttribute, EntityAttributeModifier> getAttributeModifiers(EquipmentSlot slot, ItemStack stack) {
		return ImmutableMultimap.of();
	}

	@Override
	public String getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot, String type) {
		return type != null && type.equals("overlay") ? WORN_OVERLAY_TEXTURE : WORN_TEXTURE;
	}

	@Override
	@Environment(EnvType.CLIENT)
	@SuppressWarnings("unchecked")
	public <A extends BipedEntityModel<?>> A getArmorModel(LivingEntity entityLiving, ItemStack itemStack, EquipmentSlot armorSlot, A _default) {
		if(model == null)
			model = new BackpackModel();

		return (A) model;
	}

	@Override
	public boolean hasGlint(ItemStack stack) {
		return false;
	}

	@Override
	public boolean isEnchantable(@Nonnull ItemStack stack) {
		return false;
	}

	@Override
	public void appendStacks(@Nonnull ItemGroup group, @Nonnull DefaultedList<ItemStack> items) {
		if(isEnabled() || group == ItemGroup.SEARCH)
			super.appendStacks(group, items);
	}

	public boolean isEnabled() {
		return module != null && module.enabled;
	}

	@Override
	public ItemColorProvider getItemColor() {
		return (stack, i) -> i > 0 ? -1 : ((DyeableArmorItem) stack.getItem()).getColor(stack);
	}

	@Override
	public ScreenHandler createMenu(int id, PlayerInventory inv, PlayerEntity player) {
		return new BackpackContainer(id, player);
	}

	@Override
	public Text getDisplayName() {
		return new TranslatableText(getTranslationKey());
	}


}
