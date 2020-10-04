package vazkii.quark.building.module;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import com.google.common.base.Supplier;
import com.google.common.collect.ImmutableSet;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.passive.AbstractDonkeyEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.ActionResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.common.Tags;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import vazkii.arl.util.RegistryHelper;
import vazkii.quark.base.handler.MiscUtil;
import vazkii.quark.base.module.LoadModule;
import vazkii.quark.base.module.Module;
import vazkii.quark.base.module.ModuleCategory;
import vazkii.quark.building.block.VariantChestBlock;
import vazkii.quark.building.block.VariantTrappedChestBlock;
import vazkii.quark.building.client.render.VariantChestTileEntityRenderer;
import vazkii.quark.building.tile.VariantChestTileEntity;
import vazkii.quark.building.tile.VariantTrappedChestTileEntity;

@LoadModule(category = ModuleCategory.BUILDING, hasSubscriptions = true)
public class VariantChestsModule extends Module {

	private static final String DONK_CHEST = "Quark:DonkChest";

	private static final ImmutableSet<String> OVERWORLD_WOODS = ImmutableSet.copyOf(MiscUtil.OVERWORLD_WOOD_TYPES);
	private static final ImmutableSet<String> NETHER_WOODS = ImmutableSet.copyOf(MiscUtil.NETHER_WOOD_TYPES);
	
	private static final ImmutableSet<String> MOD_WOODS = ImmutableSet.of();

	public static BlockEntityType<VariantChestTileEntity> chestTEType;
	public static BlockEntityType<VariantTrappedChestTileEntity> trappedChestTEType;

	private static List<Supplier<Block>> chestTypes = new LinkedList<>();
	private static List<Supplier<Block>> trappedChestTypes = new LinkedList<>();
	
	private static List<Block> allChests = new LinkedList<>();

	@Override
	public void construct() {
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
	@Environment(EnvType.CLIENT)
	public void clientSetup() {
		ClientRegistry.bindTileEntityRenderer(chestTEType, VariantChestTileEntityRenderer::new);
		ClientRegistry.bindTileEntityRenderer(trappedChestTEType, VariantChestTileEntityRenderer::new);
	}

	private void addChest(String name, Block from) {
		addChest(name, Block.Properties.copy(from));
	}

	private void addChest(String name, Block.Properties props) {
		chestTypes.add(() -> new VariantChestBlock(name, this, () -> chestTEType, props));
		trappedChestTypes.add(() -> new VariantTrappedChestBlock(name, this, () -> trappedChestTEType, props));
	}

	private void addModChest(String nameRaw, Block from) {
		String[] toks = nameRaw.split(":");
		String name = toks[1];
		String mod = toks[0];
		addModChest(name, mod, Block.Properties.copy(from));
	}

	private void addModChest(String name, String mod, Block.Properties props) {
		chestTypes.add(() -> new VariantChestBlock.Compat(name, mod, this, () -> chestTEType, props));
		trappedChestTypes.add(() -> new VariantTrappedChestBlock.Compat(name, mod, this, () -> trappedChestTEType, props));
	}

	public static <T extends BlockEntity> BlockEntityType<T> registerChests(Supplier<? extends T> factory, List<Supplier<Block>> list) {
		List<Block> blockTypes = list.stream().map(Supplier::get).collect(Collectors.toList());
		allChests.addAll(blockTypes);
		return BlockEntityType.Builder.<T>create(factory, blockTypes.toArray(new Block[blockTypes.size()])).build(null);
	}
	
	@Override
	public void textureStitch(TextureStitchEvent.Pre event) {
		if(event.getMap().getId().toString().equals("minecraft:textures/atlas/chest.png")) {
			for(Block b : allChests)
				VariantChestTileEntityRenderer.accept(event, b);
		}
	}
	
	@SubscribeEvent
	public void onClickEntity(PlayerInteractEvent.EntityInteractSpecific event) {
		Entity target = event.getTarget();
		PlayerEntity player = event.getPlayer();
		ItemStack held = player.getStackInHand(event.getHand());

		if (!held.isEmpty() && target instanceof AbstractDonkeyEntity) {
			AbstractDonkeyEntity horse = (AbstractDonkeyEntity) target;

			if (!horse.hasChest() && held.getItem() != Items.CHEST) {
				if (held.getItem().isIn(Tags.Items.CHESTS_WOODEN)) {
					event.setCanceled(true);
					event.setCancellationResult(ActionResult.SUCCESS);

					if (!target.world.isClient) {
						ItemStack copy = held.copy();
						copy.setCount(1);
						held.decrement(1);

						horse.getPersistentData().put(DONK_CHEST, copy.serializeNBT());

						horse.setHasChest(true);
						horse.onChestedStatusChanged();
						horse.playAddChestSound();
					}
				}
			}
		}
	}

	private static final ThreadLocal<ItemStack> WAIT_TO_REPLACE_CHEST = new ThreadLocal<>();

	@SubscribeEvent
	public void onDeath(LivingDeathEvent event) {
		Entity target = event.getEntityLiving();
		if (target instanceof AbstractDonkeyEntity) {
			AbstractDonkeyEntity horse = (AbstractDonkeyEntity) target;
			ItemStack chest = ItemStack.fromTag(horse.getPersistentData().getCompound(DONK_CHEST));
			if (!chest.isEmpty() && horse.hasChest())
				WAIT_TO_REPLACE_CHEST.set(chest);
		}
	}

	@SubscribeEvent
	public void onEntityJoinWorld(EntityJoinWorldEvent event) {
		Entity target = event.getEntity();
		if (target instanceof ItemEntity && ((ItemEntity) target).getStack().getItem() == Items.CHEST) {
			ItemStack local = WAIT_TO_REPLACE_CHEST.get();
			if (local != null && !local.isEmpty())
				((ItemEntity) target).setStack(local);
			WAIT_TO_REPLACE_CHEST.remove();
		}
	}
	
	public static interface IChestTextureProvider {
		String getChestTexturePath();
		boolean isTrap();
	}

}
