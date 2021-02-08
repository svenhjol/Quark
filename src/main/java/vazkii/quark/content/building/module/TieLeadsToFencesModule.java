package vazkii.quark.content.building.module;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.attributes.GlobalEntityTypeAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ITag;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickBlock;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import vazkii.arl.util.RegistryHelper;
import vazkii.quark.base.Quark;
import vazkii.quark.base.module.LoadModule;
import vazkii.quark.base.module.QuarkModule;
import vazkii.quark.content.building.client.render.LeashKnot2Renderer;
import vazkii.quark.content.building.entity.LeashKnot2Entity;
import vazkii.quark.base.module.ModuleCategory;

@LoadModule(category = ModuleCategory.BUILDING, hasSubscriptions = true)
public class TieLeadsToFencesModule extends QuarkModule {

    public static EntityType<LeashKnot2Entity> leashKnot2Entity;
    
	public static ITag<Block> leadConnectableTag;

	@Override
	public void construct() {
		leashKnot2Entity = EntityType.Builder.<LeashKnot2Entity>create(LeashKnot2Entity::new, EntityClassification.MISC)
                .size(6F / 16F, 0.5F)
                .setTrackingRange(10)
                .setUpdateInterval(Integer.MAX_VALUE)
                .setShouldReceiveVelocityUpdates(false)
                .setCustomClientFactory((spawnEntity, world) -> new LeashKnot2Entity(leashKnot2Entity, world))
                .build("leash_knot_fake");
        RegistryHelper.register(leashKnot2Entity, "leash_knot_fake");
	}
	
	@Override
	public void setup() {
		GlobalEntityTypeAttributes.put(leashKnot2Entity, MobEntity.func_233666_p_().create());
		
		leadConnectableTag = BlockTags.createOptional(new ResourceLocation(Quark.MOD_ID, "lead_connectable"));
	}
	
    @Override
    @OnlyIn(Dist.CLIENT)
    public void clientSetup() {
        RenderingRegistry.registerEntityRenderingHandler(leashKnot2Entity, (manager) -> new LeashKnot2Renderer(manager));
    }
    
	@SubscribeEvent
	public void onRightClick(RightClickBlock event) {
		World world = event.getWorld();
		if(world.isRemote || event.getHand() != Hand.MAIN_HAND)
			return;
		
		PlayerEntity player = event.getPlayer();
		ItemStack stack = player.getHeldItem(event.getHand());
		BlockPos pos = event.getPos();
		BlockState state = world.getBlockState(pos);
		
		if(stack.getItem() == Items.LEAD && state.getBlock().isIn(leadConnectableTag)) {
			for(MobEntity mob : world.getEntitiesWithinAABB(MobEntity.class, new AxisAlignedBB(player.getPosX() - 7, player.getPosY() - 7, player.getPosZ() - 7, player.getPosX() + 7, player.getPosY() + 7, player.getPosZ() + 7))) {
				if(mob.getLeashHolder() == player)
					return;
			}

			LeashKnot2Entity knot = new LeashKnot2Entity(leashKnot2Entity, world);
			knot.setPosition(pos.getX() + 0.5, pos.getY() + 0.5 - 1F / 8F, pos.getZ() + 0.5);
			world.addEntity(knot);
			knot.setLeashHolder(player, true);

			if(!player.isCreative())
				stack.shrink(1);
			world.playSound(null, pos, SoundEvents.ENTITY_LEASH_KNOT_PLACE, SoundCategory.BLOCKS, 1F, 1F);
			event.setCanceled(true);
		}
	}
	
}
