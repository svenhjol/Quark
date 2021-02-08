package vazkii.quark.content.world.gen.structure.processor;

import java.util.Random;

import net.minecraft.block.Blocks;
import net.minecraft.block.ChestBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.ChestTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.gen.feature.template.IStructureProcessorType;
import net.minecraft.world.gen.feature.template.PlacementSettings;
import net.minecraft.world.gen.feature.template.StructureProcessor;
import net.minecraft.world.gen.feature.template.Template;
import net.minecraft.world.gen.feature.template.Template.BlockInfo;
import vazkii.quark.content.world.gen.structure.BigDungeonStructure;
import vazkii.quark.content.world.module.BigDungeonModule;

public class BigDungeonChestProcessor extends StructureProcessor {
	
    public BigDungeonChestProcessor() { 
    	// NO-OP
    }
    
    @Override
    public BlockInfo process(IWorldReader worldReaderIn, BlockPos pos, BlockPos otherposidk, BlockInfo p_215194_3_, BlockInfo blockInfo, PlacementSettings placementSettingsIn, Template template) {
    	if(blockInfo.state.getBlock() instanceof ChestBlock) {
    		Random rand = placementSettingsIn.getRandom(blockInfo.pos);
    		if(rand.nextDouble() > BigDungeonModule.chestChance)
	            return new BlockInfo(blockInfo.pos, Blocks.CAVE_AIR.getDefaultState(), new CompoundNBT());
    		if (blockInfo.nbt.getString("id").equals("minecraft:chest")) {
    			blockInfo.nbt.putString("LootTable", BigDungeonModule.lootTable);
    			blockInfo.nbt.putLong("LootTableSeed", rand.nextLong());
    			return new BlockInfo(blockInfo.pos, blockInfo.state, blockInfo.nbt);
    		}
    	}
    	
    	return blockInfo;
    }
    
	@Override
	protected IStructureProcessorType<?> getType() {
		return BigDungeonStructure.CHEST_PROCESSOR_TYPE;
	}

}
