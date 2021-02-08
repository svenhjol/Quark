package vazkii.quark.content.world.module;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.GenerationStage.Decoration;
import net.minecraftforge.common.ToolType;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import vazkii.quark.base.block.QuarkBlock;
import vazkii.quark.base.block.QuarkPillarBlock;
import vazkii.quark.base.handler.VariantHandler;
import vazkii.quark.base.item.QuarkItem;
import vazkii.quark.base.module.LoadModule;
import vazkii.quark.base.module.QuarkModule;
import vazkii.quark.base.module.ModuleCategory;
import vazkii.quark.base.module.config.Config;
import vazkii.quark.base.world.WorldGenHandler;
import vazkii.quark.base.world.WorldGenWeights;
import vazkii.quark.base.world.config.DimensionConfig;
import vazkii.quark.base.world.config.OrePocketConfig;
import vazkii.quark.base.world.generator.OreGenerator;
import vazkii.quark.content.world.block.BiotiteOreBlock;

@LoadModule(category = ModuleCategory.WORLD, hasSubscriptions = true)
public class BiotiteModule extends QuarkModule {

	@Config public boolean generateNaturally = false;
	@Config public boolean generateOnDragonDeath = true;
	@Config public int clustersPerDragonTick = 16;
	@Config public int dragonTicksPerCluster = 1;
	
	@Config public static DimensionConfig dimensions = DimensionConfig.end(false);
	@Config public static OrePocketConfig oreSettings = new OrePocketConfig(1, 64, 14, 16);

	private OreGenerator oregen;
	
	@Override
	public void construct() {
		new QuarkItem("biotite", this, new Item.Properties().group(ItemGroup.MATERIALS));
		
		Block biotite_ore = new BiotiteOreBlock(this);
		oregen = new OreGenerator(dimensions, oreSettings, biotite_ore.getDefaultState(), OreGenerator.ENDSTONE_MATCHER, () -> generateNaturally);
		WorldGenHandler.addGenerator(this, oregen, Decoration.UNDERGROUND_ORES, WorldGenWeights.BIOTITE);
		
		Block.Properties props = Block.Properties.create(Material.ROCK, MaterialColor.BLACK)
				.setRequiresTool() // needs tool
        		.harvestTool(ToolType.PICKAXE)
        		.hardnessAndResistance(0.8F);
		
		VariantHandler.addSlabAndStairs(new QuarkBlock("biotite_block", this, ItemGroup.BUILDING_BLOCKS, props));
		VariantHandler.addSlabAndStairs(new QuarkBlock("smooth_biotite", this, ItemGroup.BUILDING_BLOCKS, props));
		new QuarkBlock("chiseled_biotite_block", this, ItemGroup.BUILDING_BLOCKS, props);
		new QuarkPillarBlock("biotite_pillar", this, ItemGroup.BUILDING_BLOCKS, props);
		new QuarkBlock("biotite_bricks", this, ItemGroup.BUILDING_BLOCKS, props);
	}
	
	@SubscribeEvent
	public void onEntityTick(LivingUpdateEvent event) {
		if(generateOnDragonDeath && event.getEntityLiving() instanceof EnderDragonEntity && !event.getEntity().getEntityWorld().isRemote) {
			EnderDragonEntity dragon = (EnderDragonEntity) event.getEntity();
			World world = dragon.getEntityWorld();
			
			if(dragon.deathTicks > 0 && dragon.deathTicks % dragonTicksPerCluster == 0) {
				Random rand = world.rand;
				BlockPos basePos = dragon.getPosition();
				basePos = new BlockPos(basePos.getX() - 128, 0, basePos.getZ() -128);

				for(int i = 0; i < clustersPerDragonTick; i++) {
					BlockPos pos = basePos.add(rand.nextInt(256), rand.nextInt(64), rand.nextInt(256));
					oregen.place(world, rand, pos);
				}
			}
		}
	}
	
}
