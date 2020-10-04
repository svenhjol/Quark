/**
 * This class was created by <WireSegal>. It's distributed as
 * part of the Quark Mod. Get the Source Code in github:
 * https://github.com/Vazkii/Quark
 * <p>
 * Quark is Open Source and distributed under the
 * CC-BY-NC-SA 3.0 License: https://creativecommons.org/licenses/by-nc-sa/3.0/deed.en_GB
 * <p>
 * File Created @ [Jul 05, 2019, 16:56 AM (EST)]
 */
package vazkii.quark.tweaks.module;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.CropBlock;
import net.minecraft.command.arguments.BlockArgumentParser;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.HoeItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.registries.IForgeRegistryEntry;
import vazkii.quark.base.module.LoadModule;
import vazkii.quark.base.module.Module;
import vazkii.quark.base.module.ModuleCategory;
import vazkii.quark.base.module.config.Config;
import vazkii.quark.base.network.QuarkNetwork;
import vazkii.quark.base.network.message.HarvestMessage;

@LoadModule(category = ModuleCategory.TWEAKS, hasSubscriptions = true)
public class SimpleHarvestModule extends Module {

	@Config(description = "Can players harvest crops with empty hand clicks?")
	public static boolean emptyHandHarvest = true;
	@Config(description = "Does harvesting crops with a hoe cost durability?")
	public static boolean harvestingCostsDurability = false;
	@Config(description = "Should Quark look for (nonvanilla) crops, and handle them?")
	public static boolean doHarvestingSearch = true;

	@Config(description = "Which crops can be harvested?\n" +
			"Format is: \"harvestState[,afterHarvest]\", i.e. \"minecraft:wheat[age=7]\" or \"minecraft:cocoa[age=2,facing=north],minecraft:cocoa[age=0,facing=north]\"")
	public static List<String> harvestableBlocks = Lists.newArrayList(
			"minecraft:wheat[age=7]",
			"minecraft:carrots[age=7]",
			"minecraft:potatoes[age=7]",
			"minecraft:beetroots[age=3]",
			"minecraft:nether_wart[age=3]",
			"minecraft:cocoa[age=2,facing=north],minecraft:cocoa[age=0,facing=north]",
			"minecraft:cocoa[age=2,facing=south],minecraft:cocoa[age=0,facing=south]",
			"minecraft:cocoa[age=2,facing=east],minecraft:cocoa[age=0,facing=east]",
			"minecraft:cocoa[age=2,facing=west],minecraft:cocoa[age=0,facing=west]");

	public static final Map<BlockState, BlockState> crops = Maps.newHashMap();


	@Override
	public void configChanged() {
		crops.clear();

		if (doHarvestingSearch) {
			GameRegistry.findRegistry(Block.class).getValues().stream()
					.filter(b -> !isVanilla(b) && b instanceof CropBlock)
					.map(b -> (CropBlock) b)
					.forEach(b -> crops.put(b.getDefaultState().with(b.getAgeProperty(), last(b.getAgeProperty().getValues())), b.getDefaultState()));
		}

		for (String harvestKey : harvestableBlocks) {
			BlockState initial, result;
			String[] split = tokenize(harvestKey);
			initial = fromString(split[0]);
			if (split.length > 1)
				result = fromString(split[1]);
			else
				result = initial.getBlock().getDefaultState();

			if (initial.getBlock() != Blocks.AIR)
				crops.put(initial, result);
		}
	}
	
	private int last(Collection<Integer> vals) {
		return vals.stream().max(Integer::compare).orElse(0);
	}

	private String[] tokenize(String harvestKey) {
		boolean inBracket = false;
		for (int i = 0; i < harvestKey.length(); i++) {
			char charAt = harvestKey.charAt(i);
			if (charAt == '[')
				inBracket = true;
			else if (charAt == ']')
				inBracket = false;
			else if (charAt == ',' && !inBracket)
				return new String[] { harvestKey.substring(0, i), harvestKey.substring(i + 1) };
		}
		return new String[] { harvestKey };
	}

	private boolean isVanilla(IForgeRegistryEntry<?> entry) {
		Identifier loc = entry.getRegistryName();
		if (loc == null)
			return true; // Just in case

		return loc.getNamespace().equals("minecraft");
	}

	private BlockState fromString(String key) {
		try {
			BlockArgumentParser parser = new BlockArgumentParser(new StringReader(key), false).parse(false);
			BlockState state = parser.getBlockState();
			return state == null ? Blocks.AIR.getDefaultState() : state;
		} catch (CommandSyntaxException e) {
			return Blocks.AIR.getDefaultState();
		}
	}

	private static void replant(World world, BlockPos pos, BlockState inWorld, PlayerEntity player) {
		ItemStack mainHand = player.getMainHandStack();
		boolean isHoe = !mainHand.isEmpty() && mainHand.getItem() instanceof HoeItem;

		BlockState newBlock = crops.get(inWorld);
		int fortune = HoeHarvestingModule.canFortuneApply(Enchantments.FORTUNE, mainHand) && isHoe ?
				EnchantmentHelper.getLevel(Enchantments.FORTUNE, mainHand) : 0;

		ItemStack copy = mainHand.copy();
		if (copy.isEmpty())
			copy = new ItemStack(Items.STICK);

		Map<Enchantment, Integer> enchMap = EnchantmentHelper.get(copy);
		enchMap.put(Enchantments.FORTUNE, fortune);
		EnchantmentHelper.set(enchMap, copy);

		if (world instanceof ServerWorld) {
			Item blockItem = inWorld.getBlock().asItem();
	        Block.getDroppedStacks(inWorld, (ServerWorld) world, pos, world.getBlockEntity(pos), player, copy).forEach((stack) -> {
	        	if(stack.getItem() == blockItem)
	        		stack.decrement(1);
	        	
	        	if(!stack.isEmpty())
	        		Block.dropStack(world, pos, stack);
	        });
	        inWorld.onStacksDropped(world, pos, copy);

			if (!world.isClient) {
				world.syncWorldEvent(2001, pos, Block.getRawIdFromState(newBlock));
				world.setBlockState(pos, newBlock);
			}
		}
	}

	@SubscribeEvent
	public void onClick(PlayerInteractEvent.RightClickBlock event) {
		if (click(event.getPlayer(), event.getPos())) {
			event.setCanceled(true);
			event.setCancellationResult(ActionResult.SUCCESS);
		}
	}

	public static boolean click(PlayerEntity player, BlockPos pos) {
		if (player == null)
			return false;

		ItemStack mainHand = player.getMainHandStack();
		boolean isHoe = !mainHand.isEmpty() && mainHand.getItem() instanceof HoeItem;

		if (!emptyHandHarvest && !isHoe)
			return false;

		int range = HoeHarvestingModule.getRange(mainHand);

		int harvests = 0;

		for(int x = 1 - range; x < range; x++) {
			for (int z = 1 - range; z < range; z++) {
				BlockPos shiftPos = pos.add(x, 0, z);

				BlockState worldBlock = player.world.getBlockState(shiftPos);
				if (crops.containsKey(worldBlock)) {
					replant(player.world, shiftPos, worldBlock, player);
					harvests++;
				}
			}
		}

		if (harvests > 0) {
			if (harvestingCostsDurability && isHoe && !player.world.isClient)
				mainHand.damage(1, player, (p) -> p.sendToolBreakStatus(Hand.MAIN_HAND));

			if (mainHand.isEmpty() && player.world.isClient)
				QuarkNetwork.sendToServer(new HarvestMessage(pos));
			return true;
		}

		return false;
	}
}
