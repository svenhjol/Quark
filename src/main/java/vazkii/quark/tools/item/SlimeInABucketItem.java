package vazkii.quark.tools.item;

import javax.annotation.Nonnull;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.SlimeEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.gen.ChunkRandom;
import vazkii.arl.util.ItemNBTHelper;
import vazkii.quark.base.item.QuarkItem;
import vazkii.quark.base.module.Module;

public class SlimeInABucketItem extends QuarkItem {

	public static final String TAG_ENTITY_DATA = "slime_nbt";
	public static final String TAG_EXCITED = "excited";

	public SlimeInABucketItem(Module module) {
		super("slime_in_a_bucket", module, 
				new Item.Settings()
				.maxCount(1)
				.group(ItemGroup.MISC)
				.recipeRemainder(Items.BUCKET));
	}
	
	@Override
	public void inventoryTick(ItemStack stack, World world, Entity entity, int itemSlot, boolean isSelected) {
		if(world instanceof ServerWorld) {
			Vec3d pos = entity.getPos();
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
	public ActionResult useOnBlock(ItemUsageContext context) {
		BlockPos pos = context.getBlockPos();
		Direction facing = context.getSide();
		World worldIn = context.getWorld();
		PlayerEntity playerIn = context.getPlayer();
		Hand hand = context.getHand();
		
		double x = pos.getX() + 0.5 + facing.getOffsetX();
		double y = pos.getY() + 0.5 + facing.getOffsetY();
		double z = pos.getZ() + 0.5 + facing.getOffsetZ();

		if(!worldIn.isClient) {
			SlimeEntity slime = new SlimeEntity(EntityType.SLIME, worldIn);
			
			CompoundTag data = ItemNBTHelper.getCompound(playerIn.getStackInHand(hand), TAG_ENTITY_DATA, true);
			if(data != null)
				slime.fromTag(data);
			else {
				slime.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH).setBaseValue(1.0); // MAX_HEALTH
				slime.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED).setBaseValue(0.3); // MOVEMENT_SPEED
				slime.setHealth(slime.getMaxHealth());
			}
				
			slime.updatePosition(x, y, z);

			worldIn.spawnEntity(slime);
			playerIn.swingHand(hand);
		}

		if(!playerIn.isCreative())
			playerIn.setStackInHand(hand, new ItemStack(Items.BUCKET));
		
		return ActionResult.SUCCESS;
	}

	@Nonnull
	@Override
	public Text getName(@Nonnull ItemStack stack) {
		if(stack.hasTag()) {
			CompoundTag cmp = ItemNBTHelper.getCompound(stack, TAG_ENTITY_DATA, false);
			if(cmp != null && cmp.contains("CustomName")) {
				Text custom = Text.Serializer.fromJson(cmp.getString("CustomName"));
				return new TranslatableText("item.quark.slime_in_a_bucket.named", custom);
			}
		}
		
		return super.getName(stack);
	}

	public static boolean isSlimeChunk(ServerWorld world, int x, int z) {
		ChunkPos chunkpos = new ChunkPos(new BlockPos(x, 0, z));
		return ChunkRandom.getSlimeRandom(chunkpos.x, chunkpos.z, world.getSeed(), 987234911L).nextInt(10) == 0;
	}
	
}
