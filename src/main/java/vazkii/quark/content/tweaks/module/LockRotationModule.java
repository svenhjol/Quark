package vazkii.quark.content.tweaks.module;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Objects;
import java.util.UUID;

import org.lwjgl.opengl.GL11;

import com.google.common.collect.ImmutableMap;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.StairsBlock;
import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.network.PacketBuffer;
import net.minecraft.state.Property;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.state.properties.Half;
import net.minecraft.state.properties.SlabType;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.RayTraceResult.Type;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.KeybindTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
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
	
	@OnlyIn(Dist.CLIENT)
	private LockProfile clientProfile;

	@OnlyIn(Dist.CLIENT)
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
	@OnlyIn(Dist.CLIENT)
	public void clientSetup() {
		keybind = ModKeybindHandler.init("lock_rotation", "k", ModKeybindHandler.MISC_GROUP);
	}

	public static BlockState fixBlockRotation(BlockState state, BlockItemUseContext ctx) {
		if (state == null || ctx.getPlayer() == null || !ModuleLoader.INSTANCE.isModuleEnabled(LockRotationModule.class))
			return state;

		UUID uuid = ctx.getPlayer().getUniqueID();
		if(lockProfiles.containsKey(uuid)) {
			LockProfile profile = lockProfiles.get(uuid);
			BlockState transformed = getRotatedState(ctx.getWorld(), ctx.getPos(), state, profile.facing.getOpposite(), profile.half);
			
			if(!transformed.equals(state))
				return Block.getValidBlockForPosition(transformed, ctx.getWorld(), ctx.getPos());
		}

		return state;
	}

	public static BlockState getRotatedState(World world, BlockPos pos, BlockState state, Direction face, int half) {
		BlockState setState = state;
		ImmutableMap<Property<?>, Comparable<?>> props = state.getValues();
		Block block = state.getBlock();


		if(block instanceof IRotationLockable)
			setState = ((IRotationLockable) block).applyRotationLock(world, pos, state, face, half);
		
		// General Facing
		else if(props.containsKey(BlockStateProperties.FACING))
			setState = state.with(BlockStateProperties.FACING, face);

		// Vertical Slabs
		else if (props.containsKey(VerticalSlabBlock.TYPE) && props.get(VerticalSlabBlock.TYPE) != VerticalSlabType.DOUBLE && face.getAxis() != Axis.Y)
			setState = state.with(VerticalSlabBlock.TYPE, Objects.requireNonNull(VerticalSlabType.fromDirection(face)));

		// Horizontal Facing
		else if(props.containsKey(BlockStateProperties.HORIZONTAL_FACING) && face.getAxis() != Axis.Y) {
			if(block instanceof StairsBlock)
				setState = state.with(BlockStateProperties.HORIZONTAL_FACING, face.getOpposite());
			else setState = state.with(BlockStateProperties.HORIZONTAL_FACING, face);
		} 

		// Pillar Axis
		else if(props.containsKey(BlockStateProperties.AXIS))
			setState = state.with(BlockStateProperties.AXIS, face.getAxis());

		// Hopper Facing
		else if(props.containsKey(BlockStateProperties.FACING_EXCEPT_UP))
			setState = state.with(BlockStateProperties.FACING_EXCEPT_UP, face == Direction.DOWN ? face : face.getOpposite());

		// Half
		if(half != -1) {
			// Slab type
			if(props.containsKey(BlockStateProperties.SLAB_TYPE) && props.get(BlockStateProperties.SLAB_TYPE) != SlabType.DOUBLE)
				setState = setState.with(BlockStateProperties.SLAB_TYPE, half == 1 ? SlabType.TOP : SlabType.BOTTOM);
			
			// Half (stairs)
			else if(props.containsKey(BlockStateProperties.HALF))
				setState = setState.with(BlockStateProperties.HALF, half == 1 ? Half.TOP : Half.BOTTOM);
		}
		
		return setState;
	}

	@SubscribeEvent
	public void onPlayerLogoff(PlayerLoggedOutEvent event) {
		lockProfiles.remove(event.getPlayer().getUniqueID());
	}

	@SubscribeEvent
	@OnlyIn(Dist.CLIENT)
	public void onMouseInput(InputEvent.MouseInputEvent event) {
		acceptInput();
	}

	@SubscribeEvent
	@OnlyIn(Dist.CLIENT)
	public void onKeyInput(InputEvent.KeyInputEvent event) {
		acceptInput();
	}

	private void acceptInput() {
		Minecraft mc = Minecraft.getInstance();
		boolean down = keybind.isKeyDown();
		if(mc.isGameFocused() && down) {
			LockProfile newProfile;
			RayTraceResult result = mc.objectMouseOver;

			if(result instanceof BlockRayTraceResult && result.getType() == Type.BLOCK) {
				BlockRayTraceResult bresult = (BlockRayTraceResult) result;
				Vector3d hitVec = bresult.getHitVec();
				Direction face = bresult.getFace();

				int half = (int) ((hitVec.y - (int) hitVec.y) * 2);
				if(face.getAxis() == Axis.Y)
					half = -1;

				newProfile = new LockProfile(face.getOpposite(), half);

			} else {
				Vector3d look = mc.player.getLookVec();
				newProfile = new LockProfile(Direction.getFacingFromVector((float) look.x, (float) look.y, (float) look.z), -1);
			}

			if(clientProfile != null && clientProfile.equals(newProfile))
				clientProfile = null;
			else clientProfile = newProfile;
			QuarkNetwork.sendToServer(new SetLockProfileMessage(clientProfile));
		}
	}

	@SubscribeEvent
	@OnlyIn(Dist.CLIENT)
	public void onHUDRender(RenderGameOverlayEvent.Post event) {
		if(event.getType() == ElementType.ALL && clientProfile != null) {
			Minecraft mc = Minecraft.getInstance();
			MatrixStack matrix = event.getMatrixStack();
			
			RenderSystem.enableBlend();
			RenderSystem.enableAlphaTest();
			RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
			RenderSystem.color4f(1F, 1F, 1F, 0.5F);

			mc.textureManager.bindTexture(MiscUtil.GENERAL_ICONS);

			MainWindow window = event.getWindow();
			int x = window.getScaledWidth() / 2 + 20;
			int y = window.getScaledHeight() / 2 - 8;
			Screen.blit(matrix, x, y, clientProfile.facing.ordinal() * 16, 65, 16, 16, 256, 256);

			if(clientProfile.half > -1)
				Screen.blit(matrix, x + 16, y, clientProfile.half * 16, 81, 16, 16, 256, 256);
		}
	}

	public static void setProfile(PlayerEntity player, LockProfile profile) {
		UUID uuid = player.getUniqueID();
		
		if(profile == null)
			lockProfiles.remove(uuid);
		else {
			boolean locked = player.getPersistentData().getBoolean(TAG_LOCKED_ONCE);
			if(!locked) {
				ITextComponent keybind = new KeybindTextComponent("quark.keybind.lock_rotation").mergeStyle(TextFormatting.AQUA);
				ITextComponent text = new TranslationTextComponent("quark.misc.rotation_lock", keybind);
				player.sendMessage(text, UUID.randomUUID());

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

		public static LockProfile readProfile(PacketBuffer buf, Field field) {
			boolean valid = buf.readBoolean();
			if(!valid)
				return null;

			int face = buf.readInt();
			int half = buf.readInt();
			return new LockProfile(Direction.byIndex(face), half);
		}

		public static void writeProfile(PacketBuffer buf, Field field, LockProfile p) {
			if(p == null)
				buf.writeBoolean(false);
			else {
				buf.writeBoolean(true);
				buf.writeInt(p.facing.getIndex());
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
