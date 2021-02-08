package vazkii.quark.content.tweaks.module;

import java.util.List;
import java.util.Objects;

import org.apache.commons.lang3.tuple.Pair;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.Window;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.sound.SoundCategory;
import net.minecraft.tag.ItemTags;
import net.minecraft.tag.Tag;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext.FluidHandling;
import net.minecraft.world.RaycastContext.ShapeType;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.event.TickEvent.ClientTickEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import vazkii.quark.base.Quark;
import vazkii.quark.base.handler.RayTraceHandler;
import vazkii.quark.base.module.LoadModule;
import vazkii.quark.base.module.QuarkModule;
import vazkii.quark.base.module.ModuleCategory;
import vazkii.quark.base.module.config.Config;

@LoadModule(category = ModuleCategory.TWEAKS, hasSubscriptions = true)
public class ReacharoundPlacingModule extends QuarkModule {

	@Config
	@Config.Min(0)
	@Config.Max(1)
	public double leniency = 0.5;

	@Config
	public List<String> whitelist = Lists.newArrayList();

	@Config
	public String display = "[  ]";

	@Config
	public String displayHorizontal = "<  >";

	private Pair<BlockPos, Direction> currentTarget;
	private int ticksDisplayed;

	public static Tag<Item> reacharoundTag;

	@Override
	public void setup() {
		reacharoundTag = ItemTags.createOptional(new Identifier(Quark.MOD_ID, "reacharound_able"));
	}

	@SubscribeEvent
	@Environment(EnvType.CLIENT)
	public void onRender(RenderGameOverlayEvent.Pre event) {
		if (event.getType() != RenderGameOverlayEvent.ElementType.CROSSHAIRS)
			return;

		MinecraftClient mc = MinecraftClient.getInstance();
		PlayerEntity player = mc.player;

		if (player != null && currentTarget != null) {
			Window res = event.getWindow();
			MatrixStack matrix = event.getMatrixStack();
			String text = (currentTarget.getRight().getAxis() == Axis.Y ? display : displayHorizontal);

			matrix.push();
			matrix.translate(res.getScaledWidth() / 2F, res.getScaledHeight() / 2f - 4, 0);

			float scale = Math.min(5, ticksDisplayed + event.getPartialTicks()) / 5F;
			scale *= scale;
			int opacity = ((int) (255 * scale)) << 24;

			matrix.scale(scale, 1F, 1F);
			matrix.translate(-mc.textRenderer.getWidth(text) / 2f, 0, 0);
			mc.textRenderer.draw(matrix, text, 0, 0, 0xFFFFFF | opacity);
			matrix.pop();
		}
	}

	@SubscribeEvent
	@Environment(EnvType.CLIENT)
	public void clientTick(ClientTickEvent event) {
		if(event.phase == Phase.END) {
			currentTarget = null;

			PlayerEntity player = MinecraftClient.getInstance().player;
			if(player != null)
				currentTarget = getPlayerReacharoundTarget(player);

			if(currentTarget != null) {
				if(ticksDisplayed < 5)
					ticksDisplayed++;
			} else ticksDisplayed = 0;
		}
	}

	@SubscribeEvent
	public void onRightClick(PlayerInteractEvent.RightClickItem event) {
		ItemStack stack = event.getItemStack();

		PlayerEntity player = event.getPlayer();
		Pair<BlockPos, Direction> pair = getPlayerReacharoundTarget(player);

		if(pair != null) {
			BlockPos pos = pair.getLeft();
			Direction dir = pair.getRight();

			if(!player.canPlaceOn(pos, dir, stack))
				return;
			
			int count = stack.getCount();
			Hand hand = event.getHand();

			ItemUsageContext context = new ItemUsageContext(player, hand, new BlockHitResult(new Vec3d(0.5F, 1F, 0.5F), dir, pos, false));
			boolean remote = player.world.isClient;
			Item item = stack.getItem();
			ActionResult res = remote ? ActionResult.SUCCESS : item.useOnBlock(context);

			if (res != ActionResult.PASS) {
				event.setCanceled(true);
				event.setCancellationResult(res);

				if(res == ActionResult.SUCCESS)
					player.swingHand(hand);
				else if(res == ActionResult.CONSUME) {
					BlockPos placedPos = pos;
					BlockState state = player.world.getBlockState(placedPos);
					BlockSoundGroup soundtype = state.getSoundType(player.world, placedPos, context.getPlayer());

					if(player.world instanceof ServerWorld)
						((ServerWorld) player.world).playSound(null, placedPos, soundtype.getPlaceSound(), SoundCategory.BLOCKS, (soundtype.getVolume() + 1.0F) / 2.0F, soundtype.getPitch() * 0.8F);

				}

				if(player.isCreative() && stack.getCount() < count && !remote)
					stack.setCount(count);
			}
		}
	}

	private Pair<BlockPos, Direction>  getPlayerReacharoundTarget(PlayerEntity player) {
		if(!(validateReacharoundStack(player.getMainHandStack()) || validateReacharoundStack(player.getOffHandStack())))
			return null;

		World world = player.world;

		Pair<Vec3d, Vec3d> params = RayTraceHandler.getEntityParams(player);
		double range = RayTraceHandler.getEntityRange(player);
		Vec3d rayPos = params.getLeft();
		Vec3d ray = params.getRight().multiply(range);

		HitResult normalRes = RayTraceHandler.rayTrace(player, world, rayPos, ray, ShapeType.OUTLINE, FluidHandling.NONE);

		if (normalRes.getType() == HitResult.Type.MISS) {
			Pair<BlockPos, Direction>  target = getPlayerVerticalReacharoundTarget(player, world, rayPos, ray);
			if(target != null)
				return target;

			target = getPlayerHorizontalReacharoundTarget(player, world, rayPos, ray);
			if(target != null)
				return target;
		}

		return null;
	}

	private Pair<BlockPos, Direction> getPlayerVerticalReacharoundTarget(PlayerEntity player, World world, Vec3d rayPos, Vec3d ray) {
		if(player.pitch < 0)
			return null;

		rayPos = rayPos.add(0, leniency, 0);
		HitResult take2Res = RayTraceHandler.rayTrace(player, world, rayPos, ray, ShapeType.OUTLINE, FluidHandling.NONE);

		if (take2Res.getType() == HitResult.Type.BLOCK && take2Res instanceof BlockHitResult) {
			BlockPos pos = ((BlockHitResult) take2Res).getBlockPos().down();
			BlockState state = world.getBlockState(pos);

			if (player.getPos().y - pos.getY() > 1 && (world.isAir(pos) || state.getMaterial().isReplaceable()))
				return Pair.of(pos, Direction.DOWN);
		}

		return null;
	}

	private Pair<BlockPos, Direction> getPlayerHorizontalReacharoundTarget(PlayerEntity player, World world, Vec3d rayPos, Vec3d ray) {
		Direction dir = Direction.fromRotation(player.yaw);
		rayPos = rayPos.subtract(leniency * dir.getOffsetX(), 0, leniency * dir.getOffsetZ());
		HitResult take2Res = RayTraceHandler.rayTrace(player, world, rayPos, ray, ShapeType.OUTLINE, FluidHandling.NONE);

		if (take2Res.getType() == HitResult.Type.BLOCK && take2Res instanceof BlockHitResult) {
			BlockPos pos = ((BlockHitResult) take2Res).getBlockPos().offset(dir);
			BlockState state = world.getBlockState(pos);

			if ((world.isAir(pos) || state.getMaterial().isReplaceable()))
				return Pair.of(pos, dir.getOpposite());
		}

		return null;
	}

	private boolean validateReacharoundStack(ItemStack stack) {
		Item item = stack.getItem();
		return item instanceof BlockItem || item.isIn(reacharoundTag) || whitelist.contains(Objects.toString(item.getRegistryName()));
	}

}
