package vazkii.quark.content.tweaks.module;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Objects;
import java.util.UUID;

import org.lwjgl.opengl.GL11;

import com.google.common.collect.ImmutableMap;
import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.StairsBlock;
import net.minecraft.block.enums.BlockHalf;
import net.minecraft.block.enums.SlabType;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.client.util.Window;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.state.property.Properties;
import net.minecraft.state.property.Property;
import net.minecraft.text.KeybindText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.hit.HitResult.Type;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import vazkii.arl.network.MessageSerializer;
import vazkii.quark.api.IRotationLockable;
import vazkii.quark.base.client.handler.ModKeybindHandler;
import vazkii.quark.base.handler.MiscUtil;
import vazkii.quark.base.module.LoadModule;
import vazkii.quark.base.module.QuarkModule;
import vazkii.quark.base.module.ModuleCategory;
import vazkii.quark.base.module.ModuleLoader;
import vazkii.quark.base.network.QuarkNetwork;
import vazkii.quark.base.network.message.SetLockProfileMessage;
import vazkii.quark.content.building.block.VerticalSlabBlock;
import vazkii.quark.content.building.block.VerticalSlabBlock.VerticalSlabType;

@LoadModule(category = ModuleCategory.TWEAKS, hasSubscriptions = true)
public class LockRotationModule extends QuarkModule {

	private static final String TAG_LOCKED_ONCE = "quark:locked_once";

	private static final HashMap<UUID, LockProfile> lockProfiles = new HashMap<>();
	
	@Environment(EnvType.CLIENT)
	private LockProfile clientProfile;

	@Environment(EnvType.CLIENT)
	private KeyBinding keybind;

	@Override
	public void configChanged() {
		lockProfiles.clear();
	}
	
	@Override
	public void setup() {
		MessageSerializer.mapHandler(LockProfile.class, LockProfile::readProfile, LockProfile::writeProfile);
	}

	@Override
	@Environment(EnvType.CLIENT)
	public void clientSetup() {
		keybind = ModKeybindHandler.init("lock_rotation", "k", ModKeybindHandler.MISC_GROUP);
	}

	public static BlockState fixBlockRotation(BlockState state, ItemPlacementContext ctx) {
		if (state == null || ctx.getPlayer() == null || !ModuleLoader.INSTANCE.isModuleEnabled(LockRotationModule.class))
			return state;

		UUID uuid = ctx.getPlayer().getUuid();
		if(lockProfiles.containsKey(uuid)) {
			LockProfile profile = lockProfiles.get(uuid);
			BlockState transformed = getRotatedState(ctx.getWorld(), ctx.getBlockPos(), state, profile.facing.getOpposite(), profile.half);
			
			if(!transformed.equals(state))
				return Block.postProcessState(transformed, ctx.getWorld(), ctx.getBlockPos());
		}

		return state;
	}

	public static BlockState getRotatedState(World world, BlockPos pos, BlockState state, Direction face, int half) {
		BlockState setState = state;
		ImmutableMap<Property<?>, Comparable<?>> props = state.getEntries();
		Block block = state.getBlock();


		if(block instanceof IRotationLockable)
			setState = ((IRotationLockable) block).applyRotationLock(world, pos, state, face, half);
		
		// General Facing
		else if(props.containsKey(Properties.FACING))
			setState = state.with(Properties.FACING, face);

		// Vertical Slabs
		else if (props.containsKey(VerticalSlabBlock.TYPE) && props.get(VerticalSlabBlock.TYPE) != VerticalSlabType.DOUBLE && face.getAxis() != Axis.Y)
			setState = state.with(VerticalSlabBlock.TYPE, Objects.requireNonNull(VerticalSlabType.fromDirection(face)));

		// Horizontal Facing
		else if(props.containsKey(Properties.HORIZONTAL_FACING) && face.getAxis() != Axis.Y) {
			if(block instanceof StairsBlock)
				setState = state.with(Properties.HORIZONTAL_FACING, face.getOpposite());
			else setState = state.with(Properties.HORIZONTAL_FACING, face);
		} 

		// Pillar Axis
		else if(props.containsKey(Properties.AXIS))
			setState = state.with(Properties.AXIS, face.getAxis());

		// Hopper Facing
		else if(props.containsKey(Properties.HOPPER_FACING))
			setState = state.with(Properties.HOPPER_FACING, face == Direction.DOWN ? face : face.getOpposite());

		// Half
		if(half != -1) {
			// Slab type
			if(props.containsKey(Properties.SLAB_TYPE) && props.get(Properties.SLAB_TYPE) != SlabType.DOUBLE)
				setState = setState.with(Properties.SLAB_TYPE, half == 1 ? SlabType.TOP : SlabType.BOTTOM);
			
			// Half (stairs)
			else if(props.containsKey(Properties.BLOCK_HALF))
				setState = setState.with(Properties.BLOCK_HALF, half == 1 ? BlockHalf.TOP : BlockHalf.BOTTOM);
		}
		
		return setState;
	}

	@SubscribeEvent
	public void onPlayerLogoff(PlayerLoggedOutEvent event) {
		lockProfiles.remove(event.getPlayer().getUuid());
	}

	@SubscribeEvent
	@Environment(EnvType.CLIENT)
	public void onMouseInput(InputEvent.MouseInputEvent event) {
		acceptInput();
	}

	@SubscribeEvent
	@Environment(EnvType.CLIENT)
	public void onKeyInput(InputEvent.KeyInputEvent event) {
		acceptInput();
	}

	private void acceptInput() {
		MinecraftClient mc = MinecraftClient.getInstance();
		boolean down = keybind.isPressed();
		if(mc.isWindowFocused() && down) {
			LockProfile newProfile;
			HitResult result = mc.crosshairTarget;

			if(result instanceof BlockHitResult && result.getType() == Type.BLOCK) {
				BlockHitResult bresult = (BlockHitResult) result;
				Vec3d hitVec = bresult.getPos();
				Direction face = bresult.getSide();

				int half = (int) ((hitVec.y - (int) hitVec.y) * 2);
				if(face.getAxis() == Axis.Y)
					half = -1;

				newProfile = new LockProfile(face.getOpposite(), half);

			} else {
				Vec3d look = mc.player.getRotationVector();
				newProfile = new LockProfile(Direction.getFacing((float) look.x, (float) look.y, (float) look.z), -1);
			}

			if(clientProfile != null && clientProfile.equals(newProfile))
				clientProfile = null;
			else clientProfile = newProfile;
			QuarkNetwork.sendToServer(new SetLockProfileMessage(clientProfile));
		}
	}

	@SubscribeEvent
	@Environment(EnvType.CLIENT)
	public void onHUDRender(RenderGameOverlayEvent.Post event) {
		if(event.getType() == ElementType.ALL && clientProfile != null) {
			MinecraftClient mc = MinecraftClient.getInstance();
			MatrixStack matrix = event.getMatrixStack();
			
			RenderSystem.enableBlend();
			RenderSystem.enableAlphaTest();
			RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
			RenderSystem.color4f(1F, 1F, 1F, 0.5F);

			mc.textureManager.bindTexture(MiscUtil.GENERAL_ICONS);

			Window window = event.getWindow();
			int x = window.getScaledWidth() / 2 + 20;
			int y = window.getScaledHeight() / 2 - 8;
			Screen.drawTexture(matrix, x, y, clientProfile.facing.ordinal() * 16, 65, 16, 16, 256, 256);

			if(clientProfile.half > -1)
				Screen.drawTexture(matrix, x + 16, y, clientProfile.half * 16, 81, 16, 16, 256, 256);
		}
	}

	public static void setProfile(PlayerEntity player, LockProfile profile) {
		UUID uuid = player.getUuid();
		
		if(profile == null)
			lockProfiles.remove(uuid);
		else {
			boolean locked = player.getPersistentData().getBoolean(TAG_LOCKED_ONCE);
			if(!locked) {
				Text keybind = new KeybindText("quark.keybind.lock_rotation").formatted(Formatting.AQUA);
				Text text = new TranslatableText("quark.misc.rotation_lock", keybind);
				player.sendSystemMessage(text, UUID.randomUUID());

				player.getPersistentData().putBoolean(TAG_LOCKED_ONCE, true);
			}

			lockProfiles.put(uuid, profile);
		}
	}

	public static class LockProfile {

		public final Direction facing;
		public final int half;

		public LockProfile(Direction facing, int half) {
			this.facing = facing;
			this.half = half;
		}

		public static LockProfile readProfile(PacketByteBuf buf, Field field) {
			boolean valid = buf.readBoolean();
			if(!valid)
				return null;

			int face = buf.readInt();
			int half = buf.readInt();
			return new LockProfile(Direction.byId(face), half);
		}

		public static void writeProfile(PacketByteBuf buf, Field field, LockProfile p) {
			if(p == null)
				buf.writeBoolean(false);
			else {
				buf.writeBoolean(true);
				buf.writeInt(p.facing.getId());
				buf.writeInt(p.half);
			}
		}

		@Override
		public boolean equals(Object other) {
			if(other == this)
				return true;
			if(!(other instanceof LockProfile))
				return false;

			LockProfile otherProfile = (LockProfile) other;
			return otherProfile.facing == facing && otherProfile.half == half;
		}

		@Override
		public int hashCode() {
			return facing.hashCode() * 31 + half;
		}
	}

}
