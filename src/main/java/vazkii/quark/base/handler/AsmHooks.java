package vazkii.quark.base.handler;

import java.util.List;
import java.util.Map;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.PistonBlockEntity;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.client.gui.screen.ingame.EnchantmentScreen;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.TemptGoal;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.text.MutableText;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import vazkii.quark.automation.client.render.ChainRenderer;
import vazkii.quark.automation.client.render.PistonTileEntityRenderer;
import vazkii.quark.automation.module.ChainLinkageModule;
import vazkii.quark.automation.module.FeedingTroughModule;
import vazkii.quark.automation.module.PistonsMoveTileEntitiesModule;
import vazkii.quark.building.module.VariantLaddersModule;
import vazkii.quark.client.tooltip.EnchantedBookTooltips;
import vazkii.quark.management.entity.ChestPassengerEntity;
import vazkii.quark.management.module.ItemSharingModule;
import vazkii.quark.mobs.entity.CrabEntity;
import vazkii.quark.tools.client.GlintRenderType;
import vazkii.quark.tools.item.PickarangItem;
import vazkii.quark.tools.module.AncientTomesModule;
import vazkii.quark.tools.module.ColorRunesModule;
import vazkii.quark.tools.module.PickarangModule;
import vazkii.quark.tweaks.client.emote.EmoteHandler;
import vazkii.quark.tweaks.module.HoeHarvestingModule;
import vazkii.quark.tweaks.module.ImprovedSleepingModule;
import vazkii.quark.tweaks.module.LockRotationModule;
import vazkii.quark.tweaks.module.SpringySlimeModule;

/**
 * @author WireSegal
 * Created at 10:10 AM on 8/15/19.
 */
@SuppressWarnings("unused")
public class AsmHooks {

	// ==========================================================================
	// Piston Logic Replacing
	// ==========================================================================

	public static PistonHandler transformStructureHelper(PistonHandler helper, World world, BlockPos sourcePos, Direction facing, boolean extending) {
		return new QuarkPistonStructureHelper(helper, world, sourcePos, facing, extending);
	}

	// ==========================================================================
	// Pistons Move TEs
	// ==========================================================================

	public static boolean setPistonBlock(World world, BlockPos pos, BlockState state, int flags) {
		return PistonsMoveTileEntitiesModule.setPistonBlock(world, pos, state, flags);
	}

	public static boolean shouldPistonMoveTE(boolean parent, BlockState state) {
		return PistonsMoveTileEntitiesModule.shouldMoveTE(parent, state);
	}

	public static void postPistonPush(PistonHandler helper, World world, Direction direction, boolean extending) {
		PistonsMoveTileEntitiesModule.detachTileEntities(world, helper, direction, extending);
	}

	@Environment(EnvType.CLIENT)
	public static boolean renderPistonBlock(PistonBlockEntity tileEntityIn, float partialTicks, MatrixStack matrixStackIn, VertexConsumerProvider bufferIn, int combinedLightIn, int combinedOverlayIn) {
		return PistonTileEntityRenderer.renderPistonBlock(tileEntityIn, partialTicks, matrixStackIn, bufferIn, combinedLightIn, combinedOverlayIn);
	}

	// ==========================================================================
	// Emotes
	// ==========================================================================

	public static void updateEmotes(LivingEntity entity) {
		EmoteHandler.updateEmotes(entity);
	}

	// ==========================================================================
	// Fortune on Hoes
	// ==========================================================================

	public static boolean canFortuneApply(Enchantment enchantment, ItemStack stack) {
		return HoeHarvestingModule.canFortuneApply(enchantment, stack);
	}

	// ==========================================================================
	// Improved Sleeping
	// ==========================================================================

	public static boolean isEveryoneAsleep(boolean parent) {
		return ImprovedSleepingModule.isEveryoneAsleep(parent);
	}

	// ==========================================================================
	// Items In Chat
	// ==========================================================================

	public static MutableText createStackComponent(MutableText parent, ItemStack stack) {
		return ItemSharingModule.createStackComponent(stack, parent);
	}

	public static int transformQuadRenderColor(int parent) {
		return ItemSharingModule.transformColor(parent);
	}

	// ==========================================================================
	// Springy Slime
	// ==========================================================================

	public static void applyCollisionLogic(Entity entity, Vec3d attempted, Vec3d actual) {
		SpringySlimeModule.onEntityCollision(entity, attempted, actual);
	}

	public static void recordMotion(Entity entity) {
		SpringySlimeModule.recordMotion(entity);
	}

	// ==========================================================================
	// Pickarang
	// ==========================================================================

	public static boolean canPiercingApply(Enchantment enchantment, ItemStack stack) {
		return enchantment == Enchantments.PIERCING && stack.getItem() instanceof PickarangItem;
	}

	public static boolean isNotEfficiency(Enchantment enchantment) {
		return enchantment != Enchantments.EFFICIENCY;
	}

	public static boolean canSharpnessApply(ItemStack stack) {
		return stack.getItem() instanceof PickarangItem;
	}

	public static DamageSource createPlayerDamage(PlayerEntity player) {
		return PickarangModule.createDamageSource(player);
	}

	// ==========================================================================
	// Chain Linkage
	// ==========================================================================

	public static void updateChain(Entity entity) {
		ChainLinkageModule.onEntityUpdate(entity);
	}

	@Environment(EnvType.CLIENT)
	public static void renderChain(EntityRenderer render, Entity entity, MatrixStack matrixStack, VertexConsumerProvider renderBuffer, float partTicks) {
		ChainRenderer.renderChain(render, entity, matrixStack, renderBuffer, partTicks);
	}

	public static void dropChain(Entity entity) {
		ChainLinkageModule.drop(entity);
	}

	// ==========================================================================
	// Feeding Troughs
	// ==========================================================================

	public static PlayerEntity findTroughs(PlayerEntity found, TemptGoal goal) {
		return FeedingTroughModule.temptWithTroughs(goal, found);
	}

	// ==========================================================================
	// Crabs
	// ==========================================================================

	public static void rave(WorldAccess world, BlockPos pos, int type, int record) {
		if (type == 1010)
			CrabEntity.rave(world, pos, record != 0);
	}

	// ==========================================================================
	// Chests in Boats
	// ==========================================================================

	public static Entity ensurePassengerIsNotChest(Entity passenger) {
		if (passenger instanceof ChestPassengerEntity)
			return null;
		return passenger;
	}

    // ==========================================================================
    // Rotation Lock
    // ==========================================================================

    public static BlockState alterPlacementState(BlockState state, ItemPlacementContext ctx) {
        return LockRotationModule.fixBlockRotation(state, ctx);
    }

	// ==========================================================================
	// Enchanted Book Tooltips
	// ==========================================================================

	@Environment(EnvType.CLIENT)
	public static List<String> captureEnchantingData(List<String> list, EnchantmentScreen screen, Enchantment enchantment, int level) {
		return EnchantedBookTooltips.captureEnchantingData(list, screen, enchantment, level);
	}

	public static Map<Enchantment, Integer> getAncientTomeEnchantments(ItemStack stack) {
		return AncientTomesModule.getTomeEnchantments(stack);
	}

	// ==========================================================================
	// Color Runes
	// ==========================================================================

	public static void setColorRuneTargetStack(LivingEntity living, EquipmentSlot slot) {
		setColorRuneTargetStack(living.getEquippedStack(slot));
	}

	public static void setColorRuneTargetStack(ItemStack stack) {
		ColorRunesModule.setTargetStack(stack);
	}

	public static RenderLayer getGlint() {
		return ColorRunesModule.getGlint();
	}

	public static RenderLayer getEntityGlint() {
		return ColorRunesModule.getEntityGlint();
	}

	public static RenderLayer getGlintDirect() {
		return ColorRunesModule.getGlintDirect();
	}

	public static RenderLayer getEntityGlintDirect() {
		return ColorRunesModule.getEntityGlint();
	}
	
	public static RenderLayer getArmorGlint() {
		return ColorRunesModule.getArmorGlint();
	}

	public static RenderLayer getArmorEntityGlint() {
		return ColorRunesModule.getArmorEntityGlint();
	}
	
	public static void addGlintTypes(Object2ObjectLinkedOpenHashMap<RenderLayer, BufferBuilder> map) {
		GlintRenderType.addGlintTypes(map);
	}
	
	// ==========================================================================
	// Flamerang
	// ==========================================================================
	
	public static boolean getIsFireResistant(boolean vanillaVal, Entity entity) {
		return PickarangModule.getIsFireResistant(vanillaVal, entity);
	}
	
	// ==========================================================================
	// Variant Ladders
	// ==========================================================================
	
	public static boolean isTrapdoorLadder(boolean defaultValue, WorldView world, BlockPos pos) {
		return VariantLaddersModule.isTrapdoorLadder(defaultValue, world, pos);
	}
	
}
