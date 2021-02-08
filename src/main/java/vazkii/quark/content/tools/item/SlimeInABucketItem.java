package vazkii.quark.content.tools.item;

import javax.annotation.Nonnull;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.monster.SlimeEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SharedSeedRandom;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import vazkii.arl.util.ItemNBTHelper;
import vazkii.quark.base.item.QuarkItem;
import vazkii.quark.base.module.QuarkModule;

public class SlimeInABucketItem extends QuarkItem {

	public static final String TAG_ENTITY_DATA = "slime_nbt";
	public static final String TAG_EXCITED = "excited";

	public SlimeInABucketItem(QuarkModule module) {
		super("slime_in_a_bucket", module, 
				new Item.Properties()
				.maxStackSize(1)
				.group(ItemGroup.MISC)
				.containerItem(Items.BUCKET));
	}
	
	@Override
	public void inventoryTick(ItemStack stack, World world, Entity entity, int itemSlot, boolean isSelected) {
		if(world instanceof ServerWorld) {
			Vector3d pos = entity.getPositionVec();
			int x = MathHelper.floor(pos.x);
			int z = MathHelper.floor(pos.z);
			boolean slime = isSlimeChunk((ServerWorld) world, x, z);
			boolean excited = ItemNBTHelper.getBoolean(stack, TAG_EXCITED, false);
			if(excited != slime)
				ItemNBTHelper.setBoolean(stack, TAG_EXCITED, slime);
		}
	}
	
	@Nonnull
	@Override
	public ActionResultType onItemUse(ItemUseContext context) {
		BlockPos pos = context.getPos();
		Direction facing = context.getFace();
		World worldIn = context.getWorld();
		PlayerEntity playerIn = context.getPlayer();
		Hand hand = context.getHand();
		
		double x = pos.getX() + 0.5 + facing.getXOffset();
		double y = pos.getY() + 0.5 + facing.getYOffset();
		double z = pos.getZ() + 0.5 + facing.getZOffset();

		if(!worldIn.isRemote) {
			SlimeEntity slime = new SlimeEntity(EntityType.SLIME, worldIn);
			
			CompoundNBT data = ItemNBTHelper.getCompound(playerIn.getHeldItem(hand), TAG_ENTITY_DATA, true);
			if(data != null)
				slime.read(data);
			else {
				slime.getAttribute(Attributes.MAX_HEALTH).setBaseValue(1.0);
				slime.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0.3);
				slime.setHealth(slime.getMaxHealth());
			}
			
			slime.setPosition(x, y, z);

			worldIn.addEntity(slime);
			playerIn.swingArm(hand);
		}
		
		worldIn.playSound(playerIn, pos, SoundEvents.ITEM_BUCKET_EMPTY, SoundCategory.NEUTRAL, 1.0F, 1.0F);

		if(!playerIn.isCreative())
			playerIn.setHeldItem(hand, new ItemStack(Items.BUCKET));
		
		return ActionResultType.SUCCESS;
	}

	@Nonnull
	@Override
	public ITextComponent getDisplayName(@Nonnull ItemStack stack) {
		if(stack.hasTag()) {
			CompoundNBT cmp = ItemNBTHelper.getCompound(stack, TAG_ENTITY_DATA, false);
			if(cmp != null && cmp.contains("CustomName")) {
				ITextComponent custom = ITextComponent.Serializer.getComponentFromJson(cmp.getString("CustomName"));
				return new TranslationTextComponent("item.quark.slime_in_a_bucket.named", custom);
			}
		}
		
		return super.getDisplayName(stack);
	}

	public static boolean isSlimeChunk(ServerWorld world, int x, int z) {
		ChunkPos chunkpos = new ChunkPos(new BlockPos(x, 0, z));
		return SharedSeedRandom.seedSlimeChunk(chunkpos.x, chunkpos.z, world.getSeed(), 987234911L).nextInt(10) == 0;
	}
	
}
