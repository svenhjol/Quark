package vazkii.quark.addons.oddities.module;

import java.util.*;

import com.google.common.collect.Lists;

import net.minecraft.block.Block;
import net.minecraft.client.gui.ScreenManager;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.DyeColor;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.extensions.IForgeContainerType;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import vazkii.arl.util.ItemNBTHelper;
import vazkii.arl.util.RegistryHelper;
import vazkii.quark.addons.oddities.block.MatrixEnchantingTableBlock;
import vazkii.quark.addons.oddities.client.render.MatrixEnchantingTableTileEntityRenderer;
import vazkii.quark.addons.oddities.client.screen.MatrixEnchantingScreen;
import vazkii.quark.addons.oddities.container.MatrixEnchantingContainer;
import vazkii.quark.addons.oddities.tile.MatrixEnchantingTableTileEntity;
import vazkii.quark.base.Quark;
import vazkii.quark.base.handler.OverrideRegistryHandler;
import vazkii.quark.base.module.LoadModule;
import vazkii.quark.base.module.QuarkModule;
import vazkii.quark.base.module.ModuleCategory;
import vazkii.quark.base.module.config.Config;

@LoadModule(category = ModuleCategory.ODDITIES, requiredMod = Quark.ODDITIES_ID, hasSubscriptions = true, subscribeOn = Dist.CLIENT)
public class MatrixEnchantingModule extends QuarkModule {

	public static TileEntityType<MatrixEnchantingTableTileEntity> tileEntityType;
	public static ContainerType<MatrixEnchantingContainer> containerType;

	@Config(description = "The maximum enchanting power the matrix enchanter can accept")
	public static int maxBookshelves = 15;

	@Config(description = "Should this be X, the price of a piece increase by 1 every X pieces you generate")
	public static int piecePriceScale = 9;

	@Config(description = "The higher this is, the better enchantments you'll get on books")
	public static int bookEnchantability = 12;

	@Config(description = "How many pieces you can generate without any bookshelves")
	public static int baseMaxPieceCount = 3;

	@Config(description = "How many pieces you can generate without any bookshelves (for Books)")
	public static int baseMaxPieceCountBook = 1;

	@Config(description = "At which piece count the calculation for the min level should default to increasing one per piece rather than using the scale factor")
	public static int minLevelCutoff = 10;

	@Config(description = "How many pieces a single Lapis can generate")
	public static int chargePerLapis = 4;

	@Config(description = "How much the min level requirement for adding a new piece should increase for each piece added (up until the value of Min Level Cutoff)")
	public static double minLevelScaleFactor = 1.5;

	@Config(description = "How much the min level requirement for adding a new piece to a book should increase per each bookshelf being used")
	public static double minLevelScaleFactorBook = 2.0;

	@Config(description = "How much to multiply the frequency of pieces where at least one of the same type has been generated")
	public static double dupeMultiplier = 1.4;

	@Config(description = "How much to multiply the frequency of pieces where incompatible pieces have been generated")
	public static double incompatibleMultiplier = 0.0;

	@Config(description = "Set to false to disable the ability to create Enchanted Books")
	public static boolean allowBooks = true;

	@Config(description = "Set this to true to allow treasure enchantments to be rolled as pieces")
	public static boolean allowTreasures = false;

	@Config(description = "Set to false to disable the tooltip for items with pending enchantments")
	public static boolean showTooltip = true;

	@Config(description = "By default, enchantment rarities are fuzzed a bit to feel better with the new system. Set this to false to override this behaviour.")
	public static boolean normalizeRarity = true;
	
	@Config(description = "Matrix Enchanting can be done with water instead of air around the enchanting table. Set this to false to disable this behaviour.")
	public static boolean allowUnderwaterEnchanting = true;

	@Config(description = "A list of enchantment IDs you don't want the enchantment table to be able to create")
	public static List<String> disallowedEnchantments = Lists.newArrayList();

	@Config(description = "An array of influences each candle should apply. This list must be 16 elements long, and is in order of wool colors.")
	private static List<String> influencesList = Lists.newArrayList(
			"minecraft:unbreaking", // White
			"minecraft:fire_protection", // Orange
			"minecraft:knockback,minecraft:punch", // Magenta
			"minecraft:feather_falling", // Light Blue
			"minecraft:looting,minecraft:fortune,minecraft:luck_of_the_sea", // Yellow 
			"minecraft:blast_protection", // Lime
			"minecraft:silk_touch,minecraft:channeling", // Pink 
			"minecraft:bane_of_arthropods", // Gray
			"minecraft:protection", // Light Gray
			"minecraft:respiration,minecraft:loyalty,minecraft:infinity", // Cyan
			"minecraft:sweeping,minecraft:multishot", // Purple
			"minecraft:efficiency,minecraft:sharpness,minecraft:lure,minecraft:power,minecraft:impaling,minecraft:quick_charge", // Blue
			"minecraft:aqua_affinity,minecraft:depth_strider,minecraft:riptide", //Brown
			"minecraft:thorns,minecraft:piercing", // Green
			"minecraft:fire_aspect,minecraft:flame",  // Red
			"minecraft:smite,minecraft:projectile_protection" // Black
			);

	@Config(description = "Set to false to disable the ability to influence enchantment outcomes with candles (requires the Tallow and Candles module enabled)")
	public static boolean allowInfluencing = true;

	@Config(description = "The max amount of candles that can influence a single enchantment")
	public static int influenceMax = 4;

	@Config(description = "How much each candle influences an enchantment. This works as a multiplier to its weight")
	public static double influencePower = 0.125;

	public static Map<DyeColor, List<Enchantment>> candleInfluences;

	@Override
	public void construct() {
		Block matrixEnchanter = new MatrixEnchantingTableBlock();
		OverrideRegistryHandler.registerBlock(matrixEnchanter, "enchanting_table", ItemGroup.DECORATIONS);

		containerType = IForgeContainerType.create(MatrixEnchantingContainer::fromNetwork);
		RegistryHelper.register(containerType, "matrix_enchanting");

		tileEntityType = TileEntityType.Builder.create(MatrixEnchantingTableTileEntity::new, matrixEnchanter).build(null);
		RegistryHelper.register(tileEntityType, "matrix_enchanting");
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void clientSetup() {
		ScreenManager.registerFactory(containerType, MatrixEnchantingScreen::new);
		ClientRegistry.bindTileEntityRenderer(tileEntityType, MatrixEnchantingTableTileEntityRenderer::new);	
	}
	

	@SubscribeEvent
	@OnlyIn(Dist.CLIENT)
	public void onTooltip(ItemTooltipEvent event) {
		ItemStack stack = event.getItemStack();
		if(showTooltip && ItemNBTHelper.verifyExistence(stack, MatrixEnchantingTableTileEntity.TAG_STACK_MATRIX))
			event.getToolTip().add(new TranslationTextComponent("quark.gui.enchanting.pending").mergeStyle(TextFormatting.AQUA));
	}

	@Override
	public void configChanged() {
		parseInfluences();
	}

	private void parseInfluences() {
		candleInfluences = new HashMap<>();

		if(influencesList.size() != 16) {
			(new IllegalArgumentException("Matrix Enchanting Influences must be of size 16, please fix this in the config.")).printStackTrace();
			allowInfluencing = false;
			return;
		}

		for (int i = 0; i < 16; i++) {
			List<Enchantment> list = new LinkedList<>();
			candleInfluences.put(DyeColor.values()[i], list);

			String s = influencesList.get(i);
			String[] tokens = s.split(",");

			for (String enchStr : tokens) {
				enchStr = enchStr.trim();
				
				Optional<Enchantment> ench = Registry.ENCHANTMENT.getOptional(new ResourceLocation(enchStr));
				if (ench.isPresent()) {
					list.add(ench.get());
				}
				else {
					Quark.LOG.error("Matrix Enchanting Influencing: Enchantment " + enchStr + " does not exist!");
				}
			}
		}
	}

}
