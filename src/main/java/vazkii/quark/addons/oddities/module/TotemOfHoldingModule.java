package vazkii.quark.addons.oddities.module;

import java.util.Collection;
import java.util.Objects;

import com.mojang.datafixers.util.Pair;

import net.minecraft.client.renderer.model.ModelResourceLocation;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemModelsProperties;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import vazkii.arl.util.RegistryHelper;
import vazkii.quark.addons.oddities.entity.TotemOfHoldingEntity;
import vazkii.quark.addons.oddities.item.SoulCompassItem;
import vazkii.quark.addons.oddities.render.TotemOfHoldingRenderer;
import vazkii.quark.base.Quark;
import vazkii.quark.base.client.handler.RequiredModTooltipHandler;
import vazkii.quark.base.module.LoadModule;
import vazkii.quark.base.module.QuarkModule;
import vazkii.quark.base.module.ModuleCategory;
import vazkii.quark.base.module.config.Config;

/**
 * @author WireSegal
 * Created at 1:21 PM on 3/30/20.
 */
@LoadModule(category = ModuleCategory.ODDITIES, requiredMod = Quark.ODDITIES_ID, hasSubscriptions = true)
public class TotemOfHoldingModule extends QuarkModule {
    private static final String TAG_LAST_TOTEM = "quark:lastTotemOfHolding";

    private static final String TAG_DEATH_X = "quark:deathX";
    private static final String TAG_DEATH_Z = "quark:deathZ";
    private static final String TAG_DEATH_DIM = "quark:deathDim";

    public static EntityType<TotemOfHoldingEntity> totemType;

    public static SoulCompassItem soulCompass;

    public static final ModelResourceLocation MODEL_LOC = new ModelResourceLocation(new ResourceLocation(Quark.MOD_ID, "totem_of_holding"), "inventory");

    @Config(description = "Set this to false to remove the behaviour where totems destroy themselves if the player dies again.")
    public static boolean darkSoulsMode = true;

    @Config(name = "Spawn Totem on PVP Kill")
    public static boolean enableOnPK = false;

    @Config(description = "Set this to true to make it so that if a totem is destroyed, the items it holds are destroyed alongside it rather than dropped")
    public static boolean destroyLostItems = false;

    @Config(description = "Set this to false to only allow the owner of a totem to collect its items rather than any player")
    public static boolean allowAnyoneToCollect = true;

    @Config(flag = "soul_compass")
    public static boolean enableSoulCompass = true;

    @Override
    public void construct() {
        soulCompass = new SoulCompassItem(this);
        soulCompass.setCondition(() -> enableSoulCompass);

        totemType = EntityType.Builder.create(TotemOfHoldingEntity::new, EntityClassification.MISC)
                .size(0.5F, 1F)
                .setTrackingRange(64)
                .setUpdateInterval(128)
                .immuneToFire()
                .setShouldReceiveVelocityUpdates(false)
                .setCustomClientFactory((spawnEntity, world) -> new TotemOfHoldingEntity(totemType, world))
                .build("totem");
        RegistryHelper.register(totemType, "totem");
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void clientSetup() {
        RenderingRegistry.registerEntityRenderingHandler(totemType, TotemOfHoldingRenderer::new);
        
		ItemModelsProperties.registerProperty(soulCompass, new ResourceLocation("angle"), SoulCompassItem::angle);
		
		RequiredModTooltipHandler.map(soulCompass, Quark.ODDITIES_ID);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void modelRegistry() {
        ModelLoader.addSpecialModel(MODEL_LOC);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onPlayerDrops(LivingDropsEvent event) {
        LivingEntity entity = event.getEntityLiving();
        if (!(entity instanceof PlayerEntity))
            return;

        Collection<ItemEntity> drops = event.getDrops();

        if(!event.isCanceled() && (enableOnPK || !(event.getSource().getTrueSource() instanceof PlayerEntity))) {
            PlayerEntity player = (PlayerEntity) entity;
            CompoundNBT data = player.getPersistentData();
            CompoundNBT persistent = data.getCompound(PlayerEntity.PERSISTED_NBT_TAG);

            if(!drops.isEmpty()) {
                TotemOfHoldingEntity totem = new TotemOfHoldingEntity(totemType, player.world);
                totem.setPosition(player.getPosX(), Math.max(3, player.getPosY() + 1), player.getPosZ());
                totem.setOwner(player);
                totem.setCustomName(player.getDisplayName());
                drops.stream()
                        .filter(Objects::nonNull)
                        .map(ItemEntity::getItem)
                        .filter(stack -> !stack.isEmpty())
                        .forEach(totem::addItem);
                if (!player.world.isRemote)
                    player.world.addEntity(totem);

                persistent.putString(TAG_LAST_TOTEM, totem.getUniqueID().toString());

                event.setCanceled(true);
            } else persistent.putString(TAG_LAST_TOTEM, "");

            BlockPos pos = player.getPosition(); // getPosition
            persistent.putInt(TAG_DEATH_X, pos.getX());
            persistent.putInt(TAG_DEATH_Z, pos.getZ());
            persistent.putString(TAG_DEATH_DIM, player.world.getDimensionKey().getLocation().toString());

            if(!data.contains(PlayerEntity.PERSISTED_NBT_TAG))
                data.put(PlayerEntity.PERSISTED_NBT_TAG, persistent);
        }
    }

    public static String getTotemUUID(PlayerEntity player) {
        CompoundNBT cmp = player.getPersistentData().getCompound(PlayerEntity.PERSISTED_NBT_TAG);
        if(cmp.contains(TAG_LAST_TOTEM))
            return cmp.getString(TAG_LAST_TOTEM);

        return "";
    }

    public static Pair<BlockPos, String> getPlayerDeathPosition(Entity e) {
        if(e instanceof PlayerEntity) {
            CompoundNBT cmp = e.getPersistentData().getCompound(PlayerEntity.PERSISTED_NBT_TAG);
            if(cmp.contains(TAG_LAST_TOTEM)) {
                int x = cmp.getInt(TAG_DEATH_X);
                int z = cmp.getInt(TAG_DEATH_Z);
                String dim = cmp.getString(TAG_DEATH_DIM);
                return Pair.of(new BlockPos(x, -1, z), dim);
            }
        }

        return null;
    }
}
