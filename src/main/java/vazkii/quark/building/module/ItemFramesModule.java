package vazkii.quark.building.module;

import java.util.Map;

import com.google.common.collect.Maps;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.Items;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Identifier;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import vazkii.arl.util.RegistryHelper;
import vazkii.quark.base.Quark;
import vazkii.quark.base.module.LoadModule;
import vazkii.quark.base.module.Module;
import vazkii.quark.base.module.ModuleCategory;
import vazkii.quark.building.client.render.ColoredItemFrameRenderer;
import vazkii.quark.building.client.render.GlassItemFrameRenderer;
import vazkii.quark.building.entity.ColoredItemFrameEntity;
import vazkii.quark.building.entity.GlassItemFrameEntity;
import vazkii.quark.building.item.QuarkItemFrameItem;

/**
 * @author WireSegal
 * Created at 11:00 AM on 8/25/19.
 */
@LoadModule(category = ModuleCategory.BUILDING)
public class ItemFramesModule extends Module {
    public static Item glassFrame;
    private static Map<DyeColor, Item> coloredFrames = Maps.newEnumMap(DyeColor.class);

    public static EntityType<GlassItemFrameEntity> glassFrameEntity;
    public static EntityType<ColoredItemFrameEntity> coloredFrameEntity;

    public static Item getColoredFrame(DyeColor color) {
        return coloredFrames.getOrDefault(color, Items.ITEM_FRAME);
    }

    @Override
    public void construct() {
        glassFrameEntity = EntityType.Builder.<GlassItemFrameEntity>create(GlassItemFrameEntity::new, SpawnGroup.MISC)
                .setDimensions(0.5F, 0.5F)
                .setTrackingRange(10)
                .setUpdateInterval(Integer.MAX_VALUE)
                .setShouldReceiveVelocityUpdates(false)
                .setCustomClientFactory((spawnEntity, world) -> new GlassItemFrameEntity(glassFrameEntity, world))
                .build("glass_frame");
        RegistryHelper.register(glassFrameEntity, "glass_frame");

        coloredFrameEntity = EntityType.Builder.<ColoredItemFrameEntity>create(ColoredItemFrameEntity::new, SpawnGroup.MISC)
                .setDimensions(0.5F, 0.5F)
                .setTrackingRange(10)
                .setUpdateInterval(Integer.MAX_VALUE)
                .setCustomClientFactory((spawnEntity, world) -> new ColoredItemFrameEntity(coloredFrameEntity, world))
                .setShouldReceiveVelocityUpdates(false)
                .build("colored_frame");
        RegistryHelper.register(coloredFrameEntity, "colored_frame");

        glassFrame = new QuarkItemFrameItem("glass_item_frame", this, GlassItemFrameEntity::new,
                new Item.Settings().group(ItemGroup.DECORATIONS));

        for(DyeColor color : DyeColor.values())
            coloredFrames.put(color, new QuarkItemFrameItem(color.getName() + "_item_frame", this, // name
                    (world, pos, dir) -> new ColoredItemFrameEntity(world, pos, dir, color.getId()),
                    new Item.Settings().group(ItemGroup.DECORATIONS)));
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void clientSetup() {
        MinecraftClient mc = MinecraftClient.getInstance();

        RenderingRegistry.registerEntityRenderingHandler(glassFrameEntity, (manager) -> new GlassItemFrameRenderer(manager, mc.getItemRenderer()));
        RenderingRegistry.registerEntityRenderingHandler(coloredFrameEntity, (manager) -> new ColoredItemFrameRenderer(manager, mc.getItemRenderer()));
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void modelRegistry() {
        //reinstate when Forge fixes itself

//        StateContainer<Block, BlockState> dummyContainer = new StateContainer.Builder<Block, BlockState>(Blocks.AIR)
//                .add(BooleanProperty.create("map"))
//                .create(BlockState::new);
//        ModelLoader.addSpecialModel(new ModelResourceLocation(new ResourceLocation(Quark.MOD_ID, "glass_frame"), "normal"));
//        for (DyeColor color : DyeColor.values()) {
//            ResourceLocation coloredFrame = new ResourceLocation(Quark.MOD_ID, color.getName() + "_frame");
//            for (BlockState state : dummyContainer.getValidStates())
//                ModelLoader.addSpecialModel(BlockModelShapes.getModelLocation(coloredFrame, state));
//        }

    	// func_176610_l = name
        ModelLoader.addSpecialModel(new ModelIdentifier(new Identifier(Quark.MOD_ID, "glass_frame"), "inventory"));
        for (DyeColor color : DyeColor.values()) {
            ModelLoader.addSpecialModel(new ModelIdentifier(new Identifier(Quark.MOD_ID, color.asString() + "_frame_empty"), "inventory"));
            ModelLoader.addSpecialModel(new ModelIdentifier(new Identifier(Quark.MOD_ID, color.asString() + "_frame_map"), "inventory"));
        }
    }
}
