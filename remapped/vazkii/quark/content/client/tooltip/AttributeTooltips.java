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
import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityGroup;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.PotionItem;
import net.minecraft.item.TippedArrowItem;
import net.minecraft.potion.PotionUtil;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
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

	private static final EntityAttribute MAX_HEALTH = EntityAttributes.GENERIC_MAX_HEALTH;
	private static final EntityAttribute KNOCKBACK_RESISTANCE = EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE;
	private static final EntityAttribute MOVEMENT_SPEED = EntityAttributes.GENERIC_MOVEMENT_SPEED;
	private static final EntityAttribute ATTACK_DAMAGE = EntityAttributes.GENERIC_ATTACK_DAMAGE;
	private static final EntityAttribute ATTACK_SPEED = EntityAttributes.GENERIC_ATTACK_SPEED;
	private static final EntityAttribute ARMOR = EntityAttributes.GENERIC_ARMOR;
	private static final EntityAttribute ARMOR_TOUGHNESS = EntityAttributes.GENERIC_ARMOR_TOUGHNESS;
	private static final EntityAttribute LUCK = EntityAttributes.GENERIC_LUCK;
	private static final EntityAttribute REACH_DISTANCE = ForgeMod.REACH_DISTANCE.get();

	public static final ImmutableSet<EntityAttribute> VALID_ATTRIBUTES = ImmutableSet.of(
			ATTACK_DAMAGE,
			ATTACK_SPEED,
			REACH_DISTANCE,
			ARMOR,
			ARMOR_TOUGHNESS,
			KNOCKBACK_RESISTANCE,
			MAX_HEALTH,
			MOVEMENT_SPEED,
			LUCK);

	private static final ImmutableSet<EntityAttribute> MULTIPLIER_ATTRIBUTES = ImmutableSet.of(
			MOVEMENT_SPEED);

	private static final ImmutableSet<EntityAttribute> POTION_MULTIPLIER_ATTRIBUTES = ImmutableSet.of(
			ATTACK_SPEED);

	private static final ImmutableSet<EntityAttribute> PERCENT_ATTRIBUTES = ImmutableSet.of(
			KNOCKBACK_RESISTANCE,
			LUCK);

	private static final ImmutableSet<EntityAttribute> DIFFERENCE_ATTRIBUTES = ImmutableSet.of(
			MAX_HEALTH,
			REACH_DISTANCE);

	private static final ImmutableSet<EntityAttribute> NONMAIN_DIFFERENCE_ATTRIBUTES = ImmutableSet.of(
			ATTACK_DAMAGE,
			ATTACK_SPEED);

	private static String format(EntityAttribute attribute, double value, EquipmentSlot slot) {
		if (PERCENT_ATTRIBUTES.contains(attribute))
			return (value > 0 ? "+" : "") + ItemStack.MODIFIER_FORMAT.format(value * 100) + "%";
		else if (MULTIPLIER_ATTRIBUTES.contains(attribute) || (slot == null && POTION_MULTIPLIER_ATTRIBUTES.contains(attribute)))
			return ItemStack.MODIFIER_FORMAT.format(value / baseValue(attribute)) + "x";
		else if (DIFFERENCE_ATTRIBUTES.contains(attribute) || (slot != EquipmentSlot.MAINHAND && NONMAIN_DIFFERENCE_ATTRIBUTES.contains(attribute)))
			return (value > 0 ? "+" : "") + ItemStack.MODIFIER_FORMAT.format(value);
		else
			return ItemStack.MODIFIER_FORMAT.format(value);
	}

	private static double baseValue(EntityAttribute attribute) {
		if(attribute == MOVEMENT_SPEED)
			return 0.1;
		else if(attribute == ATTACK_SPEED)
			return 4;
		else if(attribute == MAX_HEALTH)
			return 20;
		return 1;
	}

	private static int renderPosition(EntityAttribute attribute) {
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

	@Environment(EnvType.CLIENT)
	public static void makeTooltip(ItemTooltipEvent event) {
		MinecraftClient mc = MinecraftClient.getInstance();
		ItemStack stack = event.getItemStack();

		if(!Screen.hasShiftDown()) {
			List<Text> tooltipRaw = event.getToolTip();
			Map<EquipmentSlot, StringBuilder> attributeTooltips = Maps.newHashMap();

			boolean onlyInvalid = true;
			Multimap<EntityAttribute, EntityAttributeModifier> baseCheck = null;
			boolean allAreSame = true;

			EquipmentSlot[] slots = EquipmentSlot.values();
			slots = Arrays.copyOf(slots, slots.length + 1);
			
			for(EquipmentSlot slot : slots) {
				if (canStripAttributes(stack, slot)) {
					Multimap<EntityAttribute, EntityAttributeModifier> slotAttributes = getModifiers(stack, slot);

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
							Text component = tooltipRaw.get(i);
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

			EquipmentSlot primarySlot = MobEntity.getPreferredEquipmentSlot(stack);
			boolean showSlots = !allAreSame && (onlyInvalid ||
					(attributeTooltips.size() == 1 && attributeTooltips.containsKey(primarySlot)));

			for (int i = 0; i < slots.length; i++) {
				EquipmentSlot slot = slots[slots.length - (i + 1)];
				if (attributeTooltips.containsKey(slot)) {
					int len = mc.textRenderer.getWidth(attributeTooltips.get(slot).toString()) + 16;
					if (showSlots)
						len += 20;

					String spaceStr = "";
					while(mc.textRenderer.getWidth(spaceStr) < len)
						spaceStr += " ";

					tooltipRaw.add(1, new LiteralText(spaceStr));
					if (allAreSame)
						break;
				}
			}
		}
	}

	private static final UUID DUMMY_UUID = new UUID(0, 0);
	private static final EntityAttributeModifier DUMMY_MODIFIER = new EntityAttributeModifier(DUMMY_UUID, "NO-OP", 0.0, EntityAttributeModifier.Operation.ADDITION);

	public static Multimap<EntityAttribute, EntityAttributeModifier> getModifiers(ItemStack stack, EquipmentSlot slot) {
		if (slot == null) {
			List<StatusEffectInstance> potions = PotionUtil.getPotionEffects(stack);
			Multimap<EntityAttribute, EntityAttributeModifier> out = HashMultimap.create();

			for (StatusEffectInstance potioneffect : potions) {
				StatusEffect potion = potioneffect.getEffectType();
				Map<EntityAttribute, EntityAttributeModifier> map = potion.getAttributeModifiers();

				for (EntityAttribute attribute : map.keySet()) {
					EntityAttributeModifier baseModifier = map.get(attribute);
					EntityAttributeModifier amplified = new EntityAttributeModifier(baseModifier.getName(), potion.adjustModifierAmount(potioneffect.getAmplifier(), baseModifier), baseModifier.getOperation());
					out.put(attribute, amplified);
				}
			}

			return out;
		}

		Multimap<EntityAttribute, EntityAttributeModifier> out = stack.getAttributeModifiers(slot);
		if(out.isEmpty())
			out = HashMultimap.create();
		else out = HashMultimap.create(out); // convert to our own map

		if (slot == EquipmentSlot.MAINHAND) {
			if (EnchantmentHelper.getAttackDamage(stack, EntityGroup.DEFAULT) > 0)
				out.put(ATTACK_DAMAGE, DUMMY_MODIFIER);

			if (out.containsKey(ATTACK_DAMAGE) && !out.containsKey(ATTACK_SPEED))
				out.put(ATTACK_SPEED, DUMMY_MODIFIER);
			else if (out.containsKey(ATTACK_SPEED) && !out.containsKey(ATTACK_DAMAGE))
				out.put(ATTACK_DAMAGE, DUMMY_MODIFIER);
		}

		return out;
	}

	public static boolean extractAttributeValues(ItemTooltipEvent event, ItemStack stack, List<Text> tooltip, Map<EquipmentSlot, StringBuilder> attributeTooltips, boolean onlyInvalid, EquipmentSlot slot, Multimap<EntityAttribute, EntityAttributeModifier> slotAttributes) {
		boolean anyInvalid = false;
		for(EntityAttribute attr : slotAttributes.keys()) {
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

	private static TranslatableText getMatchingOrSibling(Text component, String key) {
		if (component instanceof TranslatableText)
			return key.equals(((TranslatableText) component).getKey()) ?
					(TranslatableText) component : null;

					for (Text sibling : component.getSiblings()) {
						if (sibling instanceof TranslatableText)
							return getMatchingOrSibling(sibling, key);
					}

					return null;
	}

	private static boolean equalsOrSibling(Text component, String key) {
		return getMatchingOrSibling(component, key) != null;
	}

	private static final ImmutableSet<String> ATTRIBUTE_FORMATS = ImmutableSet.of("plus", "take", "equals");

	@Environment(EnvType.CLIENT)
	private static boolean isAttributeLine(Text lineRaw, EntityAttribute attr) {
		String attNamePattern = attr.getTranslationKey();

		for (String att : ATTRIBUTE_FORMATS) {
			for (int mod = 0; mod < 3; mod++) {
				String pattern = "attribute.modifier." + att + "." + mod;
				TranslatableText line = getMatchingOrSibling(lineRaw, pattern);
				if (line != null) {
					Object[] formatArgs = line.getArgs();
					if (formatArgs.length > 1) {
						Object formatArg = formatArgs[1];
						if (formatArg instanceof Text &&
								equalsOrSibling((Text) formatArg, attNamePattern))
							return true;
					}
				}
			}
		}

		return false;
	}

	@Environment(EnvType.CLIENT)
	private static int renderAttribute(MatrixStack matrix, EntityAttribute attribute, EquipmentSlot slot, int x, int y, ItemStack stack, Multimap<EntityAttribute, EntityAttributeModifier> slotAttributes, MinecraftClient mc) {
		double value = getAttribute(mc.player, slot, stack, slotAttributes, attribute);
		if (value != 0) {
			RenderSystem.color3f(1F, 1F, 1F);
			mc.getTextureManager().bindTexture(MiscUtil.GENERAL_ICONS);
			DrawableHelper.drawTexture(matrix, x, y, renderPosition(attribute), 0, 9, 9, 256, 256);

			String valueStr = format(attribute, value, slot);

			int color = value < 0 || (valueStr.endsWith("x") && value / baseValue(attribute) < 1) ? 0xFF5555 : 0xFFFFFF;

			mc.textRenderer.drawWithShadow(matrix, valueStr, x + 12, y + 1, color);
			x += mc.textRenderer.getWidth(valueStr) + 20;
		}

		return x;
	}

	private static EquipmentSlot getPrimarySlot(ItemStack stack) {
		if (stack.getItem() instanceof PotionItem || stack.getItem() instanceof TippedArrowItem)
			return null;
		return MobEntity.getPreferredEquipmentSlot(stack);
	}

	@Environment(EnvType.CLIENT)
	public static void renderTooltip(RenderTooltipEvent.PostText event) {
		ItemStack stack = event.getStack();	
		MatrixStack matrix = event.getMatrixStack();
		if(!Screen.hasShiftDown()) {
			matrix.push();
			matrix.translate(0, 0, 500);
			RenderSystem.color3f(1F, 1F, 1F);
			MinecraftClient mc = MinecraftClient.getInstance();
			matrix.translate(0F, 0F, mc.getItemRenderer().zOffset);

			int baseX = event.getX();
			int y = TooltipUtils.shiftTextByLines(event.getLines(), event.getY() + 10);

			EquipmentSlot primarySlot = getPrimarySlot(stack);
			boolean onlyInvalid = true;
			boolean showSlots = false;
			int attributeHash = 0;

			boolean allAreSame = true;


			EquipmentSlot[] slots = EquipmentSlot.values();
			slots = Arrays.copyOf(slots, slots.length + 1);

			shouldShow: for (EquipmentSlot slot : slots) {
				if (canStripAttributes(stack, slot)) {
					Multimap<EntityAttribute, EntityAttributeModifier> slotAttributes = getModifiers(stack, slot);
					if (slot == EquipmentSlot.MAINHAND)
						attributeHash = slotAttributes.hashCode();
					else if (allAreSame && attributeHash != slotAttributes.hashCode())
						allAreSame = false;

					for (EntityAttribute attr : slotAttributes.keys()) {
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


			for (EquipmentSlot slot : slots) {
				if (canStripAttributes(stack, slot)) {
					int x = baseX;

					Multimap<EntityAttribute, EntityAttributeModifier> slotAttributes = getModifiers(stack, slot);

					boolean anyToRender = false;
					for (EntityAttribute attr : slotAttributes.keys()) {
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
						DrawableHelper.drawTexture(matrix, x, y, 202 + (slot == null ? -1 : slot.ordinal()) * 9, 35, 9, 9, 256, 256);
						x += 20;
					}

					for (EntityAttribute key : VALID_ATTRIBUTES)
						x = renderAttribute(matrix, key, slot, x, y, stack, slotAttributes, mc);

					for (EntityAttribute key : slotAttributes.keys()) {
						if (!VALID_ATTRIBUTES.contains(key)) {
							mc.textRenderer.drawWithShadow(matrix, "[+]", x + 1, y + 1, 0xFFFF55);
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

	private static boolean canStripAttributes(ItemStack stack, @Nullable EquipmentSlot slot) {
		if (stack.isEmpty())
			return false;

		if (slot == null)
			return (ItemNBTHelper.getInt(stack, "HideFlags", 0) & 32) == 0;

		return (ItemNBTHelper.getInt(stack, "HideFlags", 0) & 2) == 0;
	}

	private static double getAttribute(PlayerEntity player, EquipmentSlot slot, ItemStack stack, Multimap<EntityAttribute, EntityAttributeModifier> map, EntityAttribute key) {
		if(player == null) // apparently this can happen
			return 0;

		Collection<EntityAttributeModifier> collection = map.get(key);
		if(collection.isEmpty())
			return 0;

		double value = 0;

		if (!PERCENT_ATTRIBUTES.contains(key)) {
			if (slot != null || !key.equals(ATTACK_DAMAGE)) { // ATTACK_DAMAGE
				EntityAttributeInstance attribute = player.getAttributeInstance(key);
				if (attribute != null)
					value = attribute.getBaseValue();
			}
		}

		for (EntityAttributeModifier modifier : collection) {
			if (modifier.getOperation() == EntityAttributeModifier.Operation.ADDITION)
				value += modifier.getValue();
		}

		double rawValue = value;

		for (EntityAttributeModifier modifier : collection) {
			if (modifier.getOperation() == EntityAttributeModifier.Operation.MULTIPLY_BASE)
				value += rawValue * modifier.getValue();
		}

		for (EntityAttributeModifier modifier : collection) {
			if (modifier.getOperation() == EntityAttributeModifier.Operation.MULTIPLY_TOTAL)
				value += value * modifier.getValue();
		}


		if (key.equals(ATTACK_DAMAGE) && slot == EquipmentSlot.MAINHAND)
			value += EnchantmentHelper.getAttackDamage(stack, EntityGroup.DEFAULT);

		if (DIFFERENCE_ATTRIBUTES.contains(key) || (slot != EquipmentSlot.MAINHAND && NONMAIN_DIFFERENCE_ATTRIBUTES.contains(key))) {
			if (slot != null || !key.equals(ATTACK_DAMAGE)) {
				EntityAttributeInstance attribute = player.getAttributeInstance(key);
				if (attribute != null)
					value -= attribute.getBaseValue();
			}
		}

		return value;
	}
}
