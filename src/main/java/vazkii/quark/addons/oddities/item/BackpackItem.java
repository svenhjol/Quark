package vazkii.quark.addons.oddities.item;

import java.util.Map;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

import javax.annotation.Nonnull;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;

import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.Attribute;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.ArmorMaterial;
import net.minecraft.item.DyeableArmorItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Rarity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
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

public class BackpackItem extends DyeableArmorItem implements IQuarkItem, IItemColorProvider, INamedContainerProvider {

	private static final String WORN_TEXTURE = Quark.MOD_ID + ":textures/misc/backpack_worn.png";
	private static final String WORN_OVERLAY_TEXTURE = Quark.MOD_ID + ":textures/misc/backpack_worn_overlay.png";

	private final QuarkModule module;

	@OnlyIn(Dist.CLIENT)
	@SuppressWarnings("rawtypes")
	private BipedModel model;

	public BackpackItem(QuarkModule module) {
		super(ArmorMaterial.LEATHER, EquipmentSlotType.CHEST, 
				new Item.Properties()
				.maxStackSize(1)
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
		if(worldIn.isRemote)
			return;

		boolean hasItems = !BackpackModule.superOpMode && doesBackpackHaveItems(stack);

		Map<Enchantment, Integer> enchants = EnchantmentHelper.getEnchantments(stack);
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
				entityIn.entityDropItem(copy, 0);
			}
		} else if(isCursed) {
			enchants.remove(Enchantments.BINDING_CURSE);
			changedEnchants = true;
		}

		if(changedEnchants)
			EnchantmentHelper.setEnchantments(enchants, stack);
	}

	@Override
	public boolean onEntityItemUpdate(ItemStack stack, ItemEntity entityItem) {
		if(BackpackModule.superOpMode || entityItem.world.isRemote)
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
				InventoryHelper.spawnItemStack(entityItem.world, entityItem.getPosX(), entityItem.getPosY(), entityItem.getPosZ(), copy);
			}
		}

		CompoundNBT comp = ItemNBTHelper.getNBT(stack);
		comp.remove("Inventory");
		if (comp.size() == 0)
			stack.setTag(null);

		return false;
	}

	@Nonnull
	@Override
	public ICapabilityProvider initCapabilities(ItemStack stack, CompoundNBT oldCapNbt) {
		ProxiedItemStackHandler handler = new ProxiedItemStackHandler(stack, 27);

		if (oldCapNbt != null && oldCapNbt.contains("Parent")) {
			CompoundNBT itemData = oldCapNbt.getCompound("Parent");
			ItemStackHandler stacks = new ItemStackHandler();
			stacks.deserializeNBT(itemData);

			for (int i = 0; i < stacks.getSlots(); i++)
				handler.setStackInSlot(i, stacks.getStackInSlot(i));

			oldCapNbt.remove("Parent");
		}	

		return handler;
	}

	@Override
	public Multimap<Attribute, AttributeModifier> getAttributeModifiers(EquipmentSlotType slot, ItemStack stack) {
		return ImmutableMultimap.of();
	}

	@Override
	public String getArmorTexture(ItemStack stack, Entity entity, EquipmentSlotType slot, String type) {
		return type != null && type.equals("overlay") ? WORN_OVERLAY_TEXTURE : WORN_TEXTURE;
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	@SuppressWarnings("unchecked")
	public <A extends BipedModel<?>> A getArmorModel(LivingEntity entityLiving, ItemStack itemStack, EquipmentSlotType armorSlot, A _default) {
		if(model == null)
			model = new BackpackModel();

		return (A) model;
	}

	@Override
	public boolean hasEffect(ItemStack stack) {
		return false;
	}

	@Override
	public boolean isEnchantable(@Nonnull ItemStack stack) {
		return false;
	}

	@Override
	public void fillItemGroup(@Nonnull ItemGroup group, @Nonnull NonNullList<ItemStack> items) {
		if(isEnabled() || group == ItemGroup.SEARCH)
			super.fillItemGroup(group, items);
	}

	public boolean isEnabled() {
		return module != null && module.enabled;
	}

	@Override
	public IItemColor getItemColor() {
		return (stack, i) -> i > 0 ? -1 : ((DyeableArmorItem) stack.getItem()).getColor(stack);
	}

	@Override
	public Container createMenu(int id, PlayerInventory inv, PlayerEntity player) {
		return new BackpackContainer(id, player);
	}

	@Override
	public ITextComponent getDisplayName() {
		return new TranslationTextComponent(getTranslationKey());
	}


}
