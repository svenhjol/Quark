package vazkii.quark.world.gen.structure.processor;

import java.util.Random;

import net.minecraft.block.Blocks;
import net.minecraft.block.ChestBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.structure.Structure;
import net.minecraft.structure.Structure.StructureBlockInfo;
import net.minecraft.structure.StructurePlacementData;
import net.minecraft.structure.processor.StructureProcessor;
import net.minecraft.structure.processor.StructureProcessorType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldView;
import vazkii.quark.world.gen.structure.BigDungeonStructure;
import vazkii.quark.world.module.BigDungeonModule;

public class BigDungeonChestProcessor extends StructureProcessor {
	
    public BigDungeonChestProcessor() { 
    	// NO-OP
    }
    
    @Override
    public StructureBlockInfo process(WorldView worldReaderIn, BlockPos pos, BlockPos otherposidk, StructureBlockInfo p_215194_3_, StructureBlockInfo blockInfo, StructurePlacementData placementSettingsIn, Structure template) {
    	if(blockInfo.state.getBlock() instanceof ChestBlock) {
    		Random rand = placementSettingsIn.getRandom(blockInfo.pos);
    		if(rand.nextDouble() > BigDungeonModule.chestChance)
	            return new StructureBlockInfo(blockInfo.pos, Blocks.CAVE_AIR.getDefaultState(), new CompoundTag());
    		if (blockInfo.tag.getString("id").equals("minecraft:chest")) {
    			blockInfo.tag.putString("LootTable", BigDungeonModule.lootTable);
    			blockInfo.tag.putLong("LootTableSeed", rand.nextLong());
    			return new StructureBlockInfo(blockInfo.pos, blockInfo.state, blockInfo.tag);
    		}
    	}
    	
    	return blockInfo;
    }
    
	@Override
	protected StructureProcessorType<?> getType() {
		return BigDungeonStructure.CHEST_PROCESSOR_TYPE;
	}

}
