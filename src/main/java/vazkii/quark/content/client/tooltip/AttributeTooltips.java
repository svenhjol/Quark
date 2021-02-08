package vazkii.quark.content.client.tooltip;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.annotation.Nullable;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.CreatureAttribute;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.attributes.Attribute;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.attributes.ModifiableAttributeInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.item.PotionItem;
import net.minecraft.item.TippedArrowItem;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.PotionUtils;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderTooltipEvent;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import vazkii.arl.util.ItemNBTHelper;
import vazkii.quark.base.handler.MiscUtil;

/**
 * @author WireSegal
 * Created at 10:34 AM on 9/1/19.
 */
public class AttributeTooltips {

	private static final Attribute MAX_HEALTH = Attributes.MAX_HEALTH;
	private static final Attribute KNOCKBACK_RESISTANCE = Attributes.KNOCKBACK_RESISTANCE;
	private static final Attribute MOVEMENT_SPEED = Attributes.MOVEMENT_SPEED;
	private static final Attribute ATTACK_DAMAGE = Attributes.ATTACK_DAMAGE;
	private static final Attribute ATTACK_SPEED = Attributes.ATTACK_SPEED;
	private static final Attribute ARMOR = Attributes.ARMOR;
	private static final Attribute ARMOR_TOUGHNESS = Attributes.ARMOR_TOUGHNESS;
	private static final Attribute LUCK = Attributes.LUCK;
	private static final Attribute REACH_DISTANCE = ForgeMod.REACH_DISTANCE.get();

	public static final ImmutableSet<Attribute> VALID_ATTRIBUTES = ImmutableSet.of(
			ATTACK_DAMAGE,
			ATTACK_SPEED,
			REACH_DISTANCE,
			ARMOR,
			ARMOR_TOUGHNESS,
			KNOCKBACK_RESISTANCE,
			MAX_HEALTH,
			MOVEMENT_SPEED,
			LUCK);

	private static final ImmutableSet<Attribute> MULTIPLIER_ATTRIBUTES = ImmutableSet.of(
			MOVEMENT_SPEED);

	private static final ImmutableSet<Attribute> POTION_MULTIPLIER_ATTRIBUTES = ImmutableSet.of(
			ATTACK_SPEED);

	private static final ImmutableSet<Attribute> PERCENT_ATTRIBUTES = ImmutableSet.of(
			KNOCKBACK_RESISTANCE,
			LUCK);

	private static final ImmutableSet<Attribute> DIFFERENCE_ATTRIBUTES = ImmutableSet.of(
			MAX_HEALTH,
			REACH_DISTANCE);

	private static final ImmutableSet<Attribute> NONMAIN_DIFFERENCE_ATTRIBUTES = ImmutableSet.of(
			ATTACK_DAMAGE,
			ATTACK_SPEED);

	private static String format(Attribute attribute, double value, EquipmentSlotType slot) {
		if (PERCENT_ATTRIBUTES.contains(attribute))
			return (value > 0 ? "+" : "") + ItemStack.DECIMALFORMAT.format(value * 100) + "%";
		else if (MULTIPLIER_ATTRIBUTES.contains(attribute) || (slot == null && POTION_MULTIPLIER_ATTRIBUTES.contains(attribute)))
			return ItemStack.DECIMALFORMAT.format(value / baseValue(attribute)) + "x";
		else if (DIFFERENCE_ATTRIBUTES.contains(attribute) || (slot != EquipmentSlotType.MAINHAND && NONMAIN_DIFFERENCE_ATTRIBUTES.contains(attribute)))
			return (value > 0 ? "+" : "") + ItemStack.DECIMALFORMAT.format(value);
		else
			return ItemStack.DECIMALFORMAT.format(value);
	}

	private static double baseValue(Attribute attribute) {
		if(attribute == MOVEMENT_SPEED)
			return 0.1;
		else if(attribute == ATTACK_SPEED)
			return 4;
		else if(attribute == MAX_HEALTH)
			return 20;
		return 1;
	}

	private static int renderPosition(Attribute attribute) {
		if(attribute == ATTACK_DAMAGE)
			return 238;
		else if(attribute == ATTACK_SPEED)
			return 247;
		else if(attribute == REACH_DISTANCE)
			return 193;
		else if(attribute == ARMOR)
			return 229;
		else if(attribute == ARMOR_TOUGHNESS)
			return 220;
		else if(attribute == KNOCKBACK_RESISTANCE)
			return 175;
		else if(attribute == MOVEMENT_SPEED)
			return 184;
		else if(attribute == LUCK)
			return 202;
		return 211;
	}

	@OnlyIn(Dist.CLIENT)
	public static void makeTooltip(ItemTooltipEvent event) {
		Minecraft mc = Minecraft.getInstance();
		ItemStack stack = event.getItemStack();

		if(!Screen.hasShiftDown()) {
			List<ITextComponent> tooltipRaw = event.getToolTip();
			Map<EquipmentSlotType, StringBuilder> attributeTooltips = Maps.newHashMap();

			boolean onlyInvalid = true;
			Multimap<Attribute, AttributeModifier> baseCheck = null;
			boolean allAreSame = true;

			EquipmentSlotType[] slots = EquipmentSlotType.values();
			slots = Arrays.copyOf(slots, slots.length + 1);
			
			for(EquipmentSlotType slot : slots) {
				if (canStripAttributes(stack, slot)) {
					Multimap<Attribute, AttributeModifier> slotAttributes = getModifiers(stack, slot);

					if (baseCheck == null)
						baseCheck = slotAttributes;
					else if (slot != null && allAreSame && !slotAttributes.equals(baseCheck))
						allAreSame = false;
					
					if (!slotAttributes.isEmpty()) {
						if (slot == null)
							allAreSame = false;

						String slotDesc = slot == null ? "potion.whenDrank" : "item.modifiers." + slot.getName();

						int index = -1;
						for (int i = 0; i < tooltipRaw.size(); i++) {
							ITextComponent component = tooltipRaw.get(i);
							if (equalsOrSibling(component, slotDesc)) {
								index = i;
								break;
							}
						}

						if (index < 0)
							continue;

						tooltipRaw.remove(index - 1); // Remove blank space
						tooltipRaw.remove(index - 1); // Remove actual line
					}

					onlyInvalid = extractAttributeValues(event, stack, tooltipRaw, attributeTooltips, onlyInvalid, slot, slotAttributes);
				}
			}

			EquipmentSlotType primarySlot = MobEntity.getSlotForItemStack(stack);
			boolean showSlots = !allAreSame && (onlyInvalid ||
					(attributeTooltips.size() == 1 && attributeTooltips.containsKey(primarySlot)));

			for (int i = 0; i < slots.length; i++) {
				EquipmentSlotType slot = slots[slots.length - (i + 1)];
				if (attributeTooltips.containsKey(slot)) {
					int len = mc.fontRenderer.getStringWidth(attributeTooltips.get(slot).toString()) + 16;
					if (showSlots)
						len += 20;

					String spaceStr = "";
					while(mc.fontRenderer.getStringWidth(spaceStr) < len)
						spaceStr += " ";

					tooltipRaw.add(1, new StringTextComponent(spaceStr));
					if (allAreSame)
						break;
				}
			}
		}
	}

	private static final UUID DUMMY_UUID = new UUID(0, 0);
	private static final AttributeModifier DUMMY_MODIFIER = new AttributeModifier(DUMMY_UUID, "NO-OP", 0.0, AttributeModifier.Operation.ADDITION);

	public static Multimap<Attribute, AttributeModifier> getModifiers(ItemStack stack, EquipmentSlotType slot) {
		if (slot == null) {
			List<EffectInstance> potions = PotionUtils.getEffectsFromStack(stack);
			Multimap<Attribute, AttributeModifier> out = HashMultimap.create();

			for (EffectInstance potioneffect : potions) {
				Effect potion = potioneffect.getPotion();
				Map<Attribute, AttributeModifier> map = potion.getAttributeModifierMap();

				for (Attribute attribute : map.keySet()) {
					AttributeModifier baseModifier = map.get(attribute);
					AttributeModifier amplified = new AttributeModifier(baseModifier.getName(), potion.getAttributeModifierAmount(potioneffect.getAmplifier(), baseModifier), baseModifier.getOperation());
					out.put(attribute, amplified);
				}
			}

			return out;
		}

		Multimap<Attribute, AttributeModifier> out = stack.getAttributeModifiers(slot);
		if(out.isEmpty())
			out = HashMultimap.create();
		else out = HashMultimap.create(out); // convert to our own map

		if (slot == EquipmentSlotType.MAINHAND) {
			if (EnchantmentHelper.getModifierForCreature(stack, CreatureAttribute.UNDEFINED) > 0)
				out.put(ATTACK_DAMAGE, DUMMY_MODIFIER);

			if (out.containsKey(ATTACK_DAMAGE) && !out.containsKey(ATTACK_SPEED))
				out.put(ATTACK_SPEED, DUMMY_MODIFIER);
			else if (out.containsKey(ATTACK_SPEED) && !out.containsKey(ATTACK_DAMAGE))
				out.put(ATTACK_DAMAGE, DUMMY_MODIFIER);
		}

		return out;
	}

	public static boolean extractAttributeValues(ItemTooltipEvent event, ItemStack stack, List<ITextComponent> tooltip, Map<EquipmentSlotType, StringBuilder> attributeTooltips, boolean onlyInvalid, EquipmentSlotType slot, Multimap<Attribute, AttributeModifier> slotAttributes) {
		boolean anyInvalid = false;
		for(Attribute attr : slotAttributes.keys()) {
			if(VALID_ATTRIBUTES.contains(attr)) {
				onlyInvalid = false;
				double attributeValue = getAttribute(event.getPlayer(), slot, stack, slotAttributes, attr);
				if (attributeValue != 0) {
					if (!attributeTooltips.containsKey(slot))
						attributeTooltips.put(slot, new StringBuilder());
					attributeTooltips.get(slot).append(format(attr, attributeValue, slot));
				}
			} else if (!anyInvalid) {
				anyInvalid = true;
				if (!attributeTooltips.containsKey(slot))
					attributeTooltips.put(slot, new StringBuilder());
				attributeTooltips.get(slot).append("[+]");
			}

			for (int i = 1; i < tooltip.size(); i++) {
				if (isAttributeLine(tooltip.get(i), attr)) {
					tooltip.remove(i);
					break;
				}
			}
		}
		return onlyInvalid;
	}

	private static TranslationTextComponent getMatchingOrSibling(ITextComponent component, String key) {
		if (component instanceof TranslationTextComponent)
			return key.equals(((TranslationTextComponent) component).getKey()) ?
					(TranslationTextComponent) component : null;

					for (ITextComponent sibling : component.getSiblings()) {
						if (sibling instanceof TranslationTextComponent)
							return getMatchingOrSibling(sibling, key);
					}

					return null;
	}

	private static boolean equalsOrSibling(ITextComponent component, String key) {
		return getMatchingOrSibling(component, key) != null;
	}

	private static final ImmutableSet<String> ATTRIBUTE_FORMATS = ImmutableSet.of("plus", "take", "equals");

	@OnlyIn(Dist.CLIENT)
	private static boolean isAttributeLine(ITextComponent lineRaw, Attribute attr) {
		String attNamePattern = attr.getAttributeName();

		for (String att : ATTRIBUTE_FORMATS) {
			for (int mod = 0; mod < 3; mod++) {
				String pattern = "attribute.modifier." + att + "." + mod;
				TranslationTextComponent line = getMatchingOrSibling(lineRaw, pattern);
				if (line != null) {
					Object[] formatArgs = line.getFormatArgs();
					if (formatArgs.length > 1) {
						Object formatArg = formatArgs[1];
						if (formatArg instanceof ITextComponent &&
								equalsOrSibling((ITextComponent) formatArg, attNamePattern))
							return true;
					}
				}
			}
		}

		return false;
	}

	@OnlyIn(Dist.CLIENT)
	private static int renderAttribute(MatrixStack matrix, Attribute attribute, EquipmentSlotType slot, int x, int y, ItemStack stack, Multimap<Attribute, AttributeModifier> slotAttributes, Minecraft mc) {
		double value = getAttribute(mc.player, slot, stack, slotAttributes, attribute);
		if (value != 0) {
			RenderSystem.color3f(1F, 1F, 1F);
			mc.getTextureManager().bindTexture(MiscUtil.GENERAL_ICONS);
			AbstractGui.blit(matrix, x, y, renderPosition(attribute), 0, 9, 9, 256, 256);

			String valueStr = format(attribute, value, slot);

			int color = value < 0 || (valueStr.endsWith("x") && value / baseValue(attribute) < 1) ? 0xFF5555 : 0xFFFFFF;

			mc.fontRenderer.drawStringWithShadow(matrix, valueStr, x + 12, y + 1, color);
			x += mc.fontRenderer.getStringWidth(valueStr) + 20;
		}

		return x;
	}

	private static EquipmentSlotType getPrimarySlot(ItemStack stack) {
		if (stack.getItem() instanceof PotionItem || stack.getItem() instanceof TippedArrowItem)
			return null;
		return MobEntity.getSlotForItemStack(stack);
	}

	@OnlyIn(Dist.CLIENT)
	public static void renderTooltip(RenderTooltipEvent.PostText event) {
		ItemStack stack = event.getStack();	
		MatrixStack matrix = event.getMatrixStack();
		if(!Screen.hasShiftDown()) {
			matrix.push();
			matrix.translate(0, 0, 500);
			RenderSystem.color3f(1F, 1F, 1F);
			Minecraft mc = Minecraft.getInstance();
			matrix.translate(0F, 0F, mc.getItemRenderer().zLevel);

			int baseX = event.getX();
			int y = TooltipUtils.shiftTextByLines(event.getLines(), event.getY() + 10);

			EquipmentSlotType primarySlot = getPrimarySlot(stack);
			boolean onlyInvalid = true;
			boolean showSlots = false;
			int attributeHash = 0;

			boolean allAreSame = true;


			EquipmentSlotType[] slots = EquipmentSlotType.values();
			slots = Arrays.copyOf(slots, slots.length + 1);

			shouldShow: for (EquipmentSlotType slot : slots) {
				if (canStripAttributes(stack, slot)) {
					Multimap<Attribute, AttributeModifier> slotAttributes = getModifiers(stack, slot);
					if (slot == EquipmentSlotType.MAINHAND)
						attributeHash = slotAttributes.hashCode();
					else if (allAreSame && attributeHash != slotAttributes.hashCode())
						allAreSame = false;

					for (Attribute attr : slotAttributes.keys()) {
						if (VALID_ATTRIBUTES.contains(attr)) {
							onlyInvalid = false;
							if (slot != primarySlot) {
								showSlots = true;
								break shouldShow;
							}
						}
					}
				}
			}

			if (allAreSame)
				showSlots = false;
			else if (onlyInvalid)
				showSlots = true;


			for (EquipmentSlotType slot : slots) {
				if (canStripAttributes(stack, slot)) {
					int x = baseX;

					Multimap<Attribute, AttributeModifier> slotAttributes = getModifiers(stack, slot);

					boolean anyToRender = false;
					for (Attribute attr : slotAttributes.keys()) {
						double value = getAttribute(mc.player, slot, stack, slotAttributes, attr);
						if (value != 0) {
							anyToRender = true;
							break;
						}
					}

					if (!anyToRender)
						continue;

					if (showSlots) {
						RenderSystem.color3f(1F, 1F, 1F);
						mc.getTextureManager().bindTexture(MiscUtil.GENERAL_ICONS);
						AbstractGui.blit(matrix, x, y, 202 + (slot == null ? -1 : slot.ordinal()) * 9, 35, 9, 9, 256, 256);
						x += 20;
					}

					for (Attribute key : VALID_ATTRIBUTES)
						x = renderAttribute(matrix, key, slot, x, y, stack, slotAttributes, mc);

					for (Attribute key : slotAttributes.keys()) {
						if (!VALID_ATTRIBUTES.contains(key)) {
							mc.fontRenderer.drawStringWithShadow(matrix, "[+]", x + 1, y + 1, 0xFFFF55);
							break;
						}
					}


					y += 10;

					if (allAreSame)
						break;
				}
			}

			matrix.pop();
		}
	}

	private static boolean canStripAttributes(ItemStack stack, @Nullable EquipmentSlotType slot) {
		if (stack.isEmpty())
			return false;

		if (slot == null)
			return (ItemNBTHelper.getInt(stack, "HideFlags", 0) & 32) == 0;

		return (ItemNBTHelper.getInt(stack, "HideFlags", 0) & 2) == 0;
	}

	private static double getAttribute(PlayerEntity player, EquipmentSlotType slot, ItemStack stack, Multimap<Attribute, AttributeModifier> map, Attribute key) {
		if(player == null) // apparently this can happen
			return 0;

		Collection<AttributeModifier> collection = map.get(key);
		if(collection.isEmpty())
			return 0;

		double value = 0;

		if (!PERCENT_ATTRIBUTES.contains(key)) {
			if (slot != null || !key.equals(ATTACK_DAMAGE)) { // ATTACK_DAMAGE
				ModifiableAttributeInstance attribute = player.getAttribute(key);
				if (attribute != null)
					value = attribute.getBaseValue();
			}
		}

		for (AttributeModifier modifier : collection) {
			if (modifier.getOperation() == AttributeModifier.Operation.ADDITION)
				value += modifier.getAmount();
		}

		double rawValue = value;

		for (AttributeModifier modifier : collection) {
			if (modifier.getOperation() == AttributeModifier.Operation.MULTIPLY_BASE)
				value += rawValue * modifier.getAmount();
		}

		for (AttributeModifier modifier : collection) {
			if (modifier.getOperation() == AttributeModifier.Operation.MULTIPLY_TOTAL)
				value += value * modifier.getAmount();
		}


		if (key.equals(ATTACK_DAMAGE) && slot == EquipmentSlotType.MAINHAND)
			value += EnchantmentHelper.getModifierForCreature(stack, CreatureAttribute.UNDEFINED);

		if (DIFFERENCE_ATTRIBUTES.contains(key) || (slot != EquipmentSlotType.MAINHAND && NONMAIN_DIFFERENCE_ATTRIBUTES.contains(key))) {
			if (slot != null || !key.equals(ATTACK_DAMAGE)) {
				ModifiableAttributeInstance attribute = player.getAttribute(key);
				if (attribute != null)
					value -= attribute.getBaseValue();
			}
		}

		return value;
	}
}
