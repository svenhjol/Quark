package vazkii.quark.content.building.module;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.google.common.base.Supplier;
import com.google.common.collect.ImmutableSet;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ChestBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.passive.horse.AbstractChestedHorseEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.gen.feature.jigsaw.JigsawPiece;
import net.minecraft.world.gen.feature.jigsaw.LegacySingleJigsawPiece;
import net.minecraft.world.gen.feature.structure.AbstractVillagePiece;
import net.minecraft.world.gen.feature.structure.Structure;
import net.minecraft.world.gen.feature.structure.StructurePiece;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.common.Tags;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.registries.ForgeRegistries;
import vazkii.arl.util.RegistryHelper;
import vazkii.quark.base.handler.MiscUtil;
import vazkii.quark.base.module.LoadModule;
import vazkii.quark.base.module.ModuleCategory;
import vazkii.quark.base.module.QuarkModule;
import vazkii.quark.base.module.config.Config;
import vazkii.quark.content.building.block.VariantChestBlock;
import vazkii.quark.content.building.block.VariantTrappedChestBlock;
import vazkii.quark.content.building.client.render.VariantChestTileEntityRenderer;
import vazkii.quark.content.building.recipe.MixedChestRecipe;
import vazkii.quark.content.building.tile.VariantChestTileEntity;
import vazkii.quark.content.building.tile.VariantTrappedChestTileEntity;

@LoadModule(category = ModuleCategory.BUILDING, hasSubscriptions = true)
public class VariantChestsModule extends QuarkModule {

	private static final Pattern VILLAGE_PIECE_PATTERN = Pattern.compile("\\w+\\[\\w+\\[([a-z_]+)\\:village\\/(.+?)\\/.+\\]\\]");
	
	private static final String DONK_CHEST = "Quark:DonkChest";

	private static final ImmutableSet<String> OVERWORLD_WOODS = ImmutableSet.copyOf(MiscUtil.OVERWORLD_WOOD_TYPES);
	private static final ImmutableSet<String> NETHER_WOODS = ImmutableSet.copyOf(MiscUtil.NETHER_WOOD_TYPES);
	
	private static final ImmutableSet<String> MOD_WOODS = ImmutableSet.of();

	public static TileEntityType<VariantChestTileEntity> chestTEType;
	public static TileEntityType<VariantTrappedChestTileEntity> trappedChestTEType;

	private static List<Supplier<Block>> chestTypes = new LinkedList<>();
	private static List<Supplier<Block>> trappedChestTypes = new LinkedList<>();
	
	private static List<Block> allChests = new LinkedList<>();
	private static Map<String, Block> chestMappings = new HashMap<>();

	private static Structure<?> currentStructure;
	private static List<StructurePiece> currentComponents;
	
	@Config
	private static boolean replaceWorldgenChests = true;
	
	private static boolean staticEnabled = false;
	
	@Config(description =  "Chests to put in each structure. The format per entry is \"structure=chest\", where \"structure\" is a structure ID, and \"chest\" is a block ID, which must correspond to a standard chest block.")
	public static List<String> structureChests = Arrays.asList(
			 "minecraft:village_plains=quark:oak_chest",
			 "minecraft:igloo=quark:spruce_chest",
			 "minecraft:village_snowy=quark:spruce_chest",
			 "minecraft:village_taiga=quark:spruce_chest",
			 "minecraft:desert_pyramid=quark:birch_chest",
			 "minecraft:jungle_pyramid=quark:jungle_chest",
			 "minecraft:village_desert=quark:jungle_chest",
			 "minecraft:village_savanna=quark:acacia_chest",
			 "minecraft:mansion=quark:dark_oak_chest",
			 "minecraft:pillager_outpost=quark:dark_oak_chest",
			 "minecraft:ruined_portal=quark:crimson_chest",
			 "minecraft:bastion_remnant=quark:crimson_chest",
			 "minecraft:fortress=quark:nether_brick_chest",
			 "minecraft:endcity=quark:purpur_chest",
			 "bettermineshafts:mineshaft=quark:oak_chest",
			 "cobbler:shulker_factory=quark:purpur_chest",
			 "conjurer_illager:theatre=quark:dark_oak_chest",
			 "dungeoncrawl:dungeon=quark:oak_chest",
			 "dungeons_plus:bigger_dungeon=quark:oak_chest",
			 "dungeons_plus:end_ruins=quark:purpur_chest",
			 "dungeons_plus:leviathan=quark:jungle_chest",
			 "dungeons_plus:snowy_temple=quark:spruce_chest",
			 "dungeons_plus:soul_prison=quark:warped_chest",
			 "dungeons_plus:tower=quark:oak_chest",
			 "dungeons_plus:warped_garden=quark:warped__chest",
			 "hunterillager:hunterhouse=quark:oak_chest",
			 "iceandfire:gorgon_temple=quark:jungle_chest",
			 "pandoras_creatures:end_prison=quark:purpur_chest", 
			 "repurposed_structures:fortress_jungle=quark:jungle_chest",
			 "repurposed_structures:igloo_grassy=quark:oak_chest",
			 "repurposed_structures:igloo_stone=quark:spruce_chest",
			 "repurposed_structures:mineshaft_birch=quark:birch_chest",
			 "repurposed_structures:mineshaft_desert=quark:jungle_chest",
			 "repurposed_structures:mineshaft_end=quark:purpur_chest",
		 	 "repurposed_structures:mineshaft_icy=quark:spruce_chest",
			 "repurposed_structures:mineshaft_jungle=quark:jungle_chest",
			 "repurposed_structures:mineshaft_nether=quark:nether_brick_chest",
			 "repurposed_structures:mineshaft_ocean=quark:prismarine_chest",
			 "repurposed_structures:mineshaft_savanna=quark:acacia_chest",
			 "repurposed_structures:mineshaft_stone=quark:spruce_chest",
			 "repurposed_structures:mineshaft_swamp_or_dark_forest=quark:dark_oak_chest",
			 "repurposed_structures:mineshaft_taiga=quark:spruce_chest",
			 "repurposed_structures:outpost_badlands=quark:dark_oak_chest",
			 "repurposed_structures:outpost_birch=quark:birch_chest",
			 "repurposed_structures:outpost_crimson=quark:crimson_chest",
			 "repurposed_structures:outpost_desert=quark:jungle_chest",
			 "repurposed_structures:outpost_giant_tree_taiga=quark:spruce_chest",
			 "repurposed_structures:outpost_icy=quark:spruce_chest",
			 "repurposed_structures:outpost_jungle=quark:jungle_chest",
			 "repurposed_structures:outpost_nether_brick=quark:nether_brick_chest",
			 "repurposed_structures:outpost_snowy=quark:spruce_chest",
 			 "repurposed_structures:outpost_warped=quark:warped_chest",
			 "repurposed_structures:pyramid_badlands=quark:dark_oak_chest",
			 "repurposed_structures:pyramid_nether=quark:nether_brick_chest",
			 "repurposed_structures:ruined_portal_end=quark:purpur_chest",
			 "repurposed_structures:shipwreck_crimson=quark:crimson_chest",
			 "repurposed_structures:shipwreck_end=quark:purpur_chest",
 			 "repurposed_structures:shipwreck_nether_bricks=quark:nether_brick_chest",
  		 	 "repurposed_structures:shipwreck_warped=quark:warped_chest",
   		 	 "repurposed_structures:stronghold_nether=quark:nether_brick_chest",
			 "repurposed_structures:stronghold_stonebrick=quark:oak_chest",
			 "repurposed_structures:temple_nether_basalt=quark:nether_brick_chest",
 			 "repurposed_structures:temple_nether_crimson=quark:crimson_chest",
 			 "repurposed_structures:temple_nether_soul=quark:warped_chest",
  		 	 "repurposed_structures:temple_nether_warped=quark:warped_chest",
			 "repurposed_structures:temple_nether_wasteland=quark:nether_brick_chest",
			 "repurposed_structures:village_badlands=quark:dark_oak_chest",
			 "repurposed_structures:village_birch=quark:birch_chest",
			 "repurposed_structures:village_crimson=quark:crimson_chest",
			 "repurposed_structures:village_dark_oak=quark:dark_oak_chest",
 			 "repurposed_structures:village_giant_taiga=quark:spruce_chest",
			 "repurposed_structures:village_jungle=quark:jungle_chest",
			 "repurposed_structures:village_mountains=quark:spruce_chest",
			 "repurposed_structures:village_oak=quark:oak_chest",
 			 "repurposed_structures:village_swamp=quark:oak_chest",
			 "repurposed_structures:village_warped=quark:warped_chest",
			 "valhelsia_structures:castle=quark:spruce_chest",
			 "valhelsia_structures:castle_ruin=quark:oak_chest",
			 "valhelsia_structures:desert_house=quark:spruce_chest",
			 "valhelsia_structures:forge=quark:spruce_chest",
			 "valhelsia_structures:player_house=quark:oak_chest",
			 "valhelsia_structures:small_castle=quark:oak_chest",
			 "valhelsia_structures:small_dungeon=quark:oak_chest",
			 "valhelsia_structures:tower_ruin=quark:spruce_chest");
	
	@Override
	public void construct() {
		ForgeRegistries.RECIPE_SERIALIZERS.register(MixedChestRecipe.SERIALIZER);
		
		OVERWORLD_WOODS.forEach(s -> addChest(s, Blocks.CHEST));
		NETHER_WOODS.forEach(s -> addChest(s, Blocks.CHEST));
		MOD_WOODS.forEach(s -> addModChest(s, Blocks.CHEST));

		addChest("nether_brick", Blocks.NETHER_BRICKS);
		addChest("purpur", Blocks.PURPUR_BLOCK);
		addChest("prismarine", Blocks.PRISMARINE);
		addChest("mushroom", Blocks.RED_MUSHROOM_BLOCK);

		chestTEType = registerChests(VariantChestTileEntity::new, chestTypes);
		trappedChestTEType = registerChests(VariantTrappedChestTileEntity::new, trappedChestTypes);

		RegistryHelper.register(chestTEType, "variant_chest");
		RegistryHelper.register(trappedChestTEType, "variant_trapped_chest");
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void clientSetup() {
		ClientRegistry.bindTileEntityRenderer(chestTEType, VariantChestTileEntityRenderer::new);
		ClientRegistry.bindTileEntityRenderer(trappedChestTEType, VariantChestTileEntityRenderer::new);
	}
	
	@Override
	public void configChanged() {
		super.configChanged();
		
		staticEnabled = enabled;
		
		chestMappings.clear();
		for(String s : structureChests) {
			String[] toks = s.split("=");
			if(toks.length == 2) {
				String left = toks[0];
				String right = toks[1];
				
				Registry.BLOCK.getOptional(new ResourceLocation(right)).ifPresent(block -> {
					if (block != Blocks.AIR) {
						chestMappings.put(left, block);
					}
				});
			}
		}
	}
	
	public static void setActiveStructure(Structure<?> structure, List<StructurePiece> components) {
		currentStructure = structure;
		currentComponents = components;
	}
	
	public static BlockState getGenerationChestBlockState(BlockState current) {
		if(replaceWorldgenChests && current.getBlock() == Blocks.CHEST && currentStructure != null && staticEnabled) {
			ResourceLocation res = currentStructure.getRegistryName();
			if(res == null)
				return current;
			String name = res.toString();
			
			if("minecraft:village".equals(name)) {
				if(currentComponents != null && currentComponents.size() > 0) {
					StructurePiece first = currentComponents.get(0);
					if(first instanceof AbstractVillagePiece) {
						AbstractVillagePiece avp = (AbstractVillagePiece) first;
						JigsawPiece jigsaw = avp.getJigsawPiece();
						if(jigsaw instanceof LegacySingleJigsawPiece) {
							LegacySingleJigsawPiece legacyJigsaw = (LegacySingleJigsawPiece) jigsaw;
							String type = legacyJigsaw.toString();
							Matcher match = VILLAGE_PIECE_PATTERN.matcher(type);
							if(match.matches()) {
								String namespace = match.group(1);
								String villageType = match.group(2);
								
								name += "_" + villageType;
								if(!namespace.equals("minecraft"))
									name = name.replace("minecraft\\:", namespace);
							}
							
						}
					}
				}
			}
			
			if(chestMappings.containsKey(name)) {
				Block block = chestMappings.get(name);
				BlockState placeState = block.getDefaultState();
				if(placeState.getProperties().contains(ChestBlock.TYPE)) {
					placeState = placeState.with(ChestBlock.FACING, current.get(ChestBlock.FACING)).with(ChestBlock.TYPE, current.get(ChestBlock.TYPE)).with(ChestBlock.WATERLOGGED, current.get(ChestBlock.WATERLOGGED));
					return placeState;
				}
			}
		}
		
		return current;
	}

	private void addChest(String name, Block from) {
		addChest(name, Block.Properties.from(from));
	}

	private void addChest(String name, Block.Properties props) {
		chestTypes.add(() -> new VariantChestBlock(name, this, () -> chestTEType, props));
		trappedChestTypes.add(() -> new VariantTrappedChestBlock(name, this, () -> trappedChestTEType, props));
	}

	private void addModChest(String nameRaw, Block from) {
		String[] toks = nameRaw.split(":");
		String name = toks[1];
		String mod = toks[0];
		addModChest(name, mod, Block.Properties.from(from));
	}

	private void addModChest(String name, String mod, Block.Properties props) {
		chestTypes.add(() -> new VariantChestBlock.Compat(name, mod, this, () -> chestTEType, props));
		trappedChestTypes.add(() -> new VariantTrappedChestBlock.Compat(name, mod, this, () -> trappedChestTEType, props));
	}

	public static <T extends TileEntity> TileEntityType<T> registerChests(Supplier<? extends T> factory, List<Supplier<Block>> list) {
		List<Block> blockTypes = list.stream().map(Supplier::get).collect(Collectors.toList());
		allChests.addAll(blockTypes);
		return TileEntityType.Builder.<T>create(factory, blockTypes.toArray(new Block[blockTypes.size()])).build(null);
	}
	
	@Override
	public void textureStitch(TextureStitchEvent.Pre event) {
		if(event.getMap().getTextureLocation().toString().equals("minecraft:textures/atlas/chest.png")) {
			for(Block b : allChests)
				VariantChestTileEntityRenderer.accept(event, b);
		}
	}
	
	@SubscribeEvent
	public void onClickEntity(PlayerInteractEvent.EntityInteractSpecific event) {
		Entity target = event.getTarget();
		PlayerEntity player = event.getPlayer();
		ItemStack held = player.getHeldItem(event.getHand());

		if (!held.isEmpty() && target instanceof AbstractChestedHorseEntity) {
			AbstractChestedHorseEntity horse = (AbstractChestedHorseEntity) target;

			if (!horse.hasChest() && held.getItem() != Items.CHEST) {
				if (held.getItem().isIn(Tags.Items.CHESTS_WOODEN)) {
					event.setCanceled(true);
					event.setCancellationResult(ActionResultType.SUCCESS);

					if (!target.world.isRemote) {
						ItemStack copy = held.copy();
						copy.setCount(1);
						held.shrink(1);

						horse.getPersistentData().put(DONK_CHEST, copy.serializeNBT());

						horse.setChested(true);
						horse.initHorseChest();
						horse.playChestEquipSound();
					}
				}
			}
		}
	}

	private static final ThreadLocal<ItemStack> WAIT_TO_REPLACE_CHEST = new ThreadLocal<>();

	@SubscribeEvent
	public void onDeath(LivingDeathEvent event) {
		Entity target = event.getEntityLiving();
		if (target instanceof AbstractChestedHorseEntity) {
			AbstractChestedHorseEntity horse = (AbstractChestedHorseEntity) target;
			ItemStack chest = ItemStack.read(horse.getPersistentData().getCompound(DONK_CHEST));
			if (!chest.isEmpty() && horse.hasChest())
				WAIT_TO_REPLACE_CHEST.set(chest);
		}
	}

	@SubscribeEvent
	public void onEntityJoinWorld(EntityJoinWorldEvent event) {
		Entity target = event.getEntity();
		if (target instanceof ItemEntity && ((ItemEntity) target).getItem().getItem() == Items.CHEST) {
			ItemStack local = WAIT_TO_REPLACE_CHEST.get();
			if (local != null && !local.isEmpty())
				((ItemEntity) target).setItem(local);
			WAIT_TO_REPLACE_CHEST.remove();
		}
	}
	
	public static interface IChestTextureProvider {
		String getChestTexturePath();
		boolean isTrap();
	}

}
