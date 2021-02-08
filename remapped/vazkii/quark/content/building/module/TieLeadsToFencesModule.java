package vazkii.quark.content.building.module;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.attribute.DefaultAttributeRegistry;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.tag.BlockTags;
import net.minecraft.tag.Tag;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
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
    
	public static Tag<Block> leadConnectableTag;

	@Override
	public void construct() {
		leashKnot2Entity = EntityType.Builder.<LeashKnot2Entity>create(LeashKnot2Entity::new, SpawnGroup.MISC)
                .setDimensions(6F / 16F, 0.5F)
                .setTrackingRange(10)
                .setUpdateInterval(Integer.MAX_VALUE)
                .setShouldReceiveVelocityUpdates(false)
                .setCustomClientFactory((spawnEntity, world) -> new LeashKnot2Entity(leashKnot2Entity, world))
                .build("leash_knot_fake");
        RegistryHelper.register(leashKnot2Entity, "leash_knot_fake");
	}
	
	@Override
	public void setup() {
		DefaultAttributeRegistry.put(leashKnot2Entity, MobEntity.createMobAttributes().build());
		
		leadConnectableTag = BlockTags.createOptional(new Identifier(Quark.MOD_ID, "lead_connectable"));
	}
	
    @Override
    @Environment(EnvType.CLIENT)
    public void clientSetup() {
        RenderingRegistry.registerEntityRenderingHandler(leashKnot2Entity, (manager) -> new LeashKnot2Renderer(manager));
    }
    
	@SubscribeEvent
	public void onRightClick(RightClickBlock event) {
		World world = event.getWorld();
		if(world.isClient || event.getHand() != Hand.MAIN_HAND)
			return;
		
		PlayerEntity player = event.getPlayer();
		ItemStack stack = player.getStackInHand(event.getHand());
		BlockPos pos = event.getPos();
		BlockState state = world.getBlockState(pos);
		
		if(stack.getItem() == Items.LEAD && state.getBlock().isIn(leadConnectableTag)) {
			for(MobEntity mob : world.getNonSpectatingEntities(MobEntity.class, new Box(player.getX() - 7, player.getY() - 7, player.getZ() - 7, player.getX() + 7, player.getY() + 7, player.getZ() + 7))) {
				if(mob.getHoldingEntity() == player)
					return;
			}

			LeashKnot2Entity knot = new LeashKnot2Entity(leashKnot2Entity, world);
			knot.updatePosition(pos.getX() + 0.5, pos.getY() + 0.5 - 1F / 8F, pos.getZ() + 0.5);
			world.spawnEntity(knot);
			knot.attachLeash(player, true);

			if(!player.isCreative())
				stack.decrement(1);
			world.playSound(null, pos, SoundEvents.ENTITY_LEASH_KNOT_PLACE, SoundCategory.BLOCKS, 1F, 1F);
			event.setCanceled(true);
		}
	}
	
}
