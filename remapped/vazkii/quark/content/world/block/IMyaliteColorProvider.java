package vazkii.quark.content.world.block;

import java.awt.Color;
import java.util.stream.IntStream;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.color.block.BlockColorProvider;
import net.minecraft.client.color.item.ItemColorProvider;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.noise.OctaveSimplexNoiseSampler;
import net.minecraft.world.gen.ChunkRandom;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import vazkii.arl.interf.IBlockColorProvider;

public interface IMyaliteColorProvider extends IBlockColorProvider {
	
	static final OctaveSimplexNoiseSampler NOISE = new OctaveSimplexNoiseSampler(new ChunkRandom(4543543), IntStream.rangeClosed(-4, 4));
	
	@Override
    @Environment(EnvType.CLIENT)
	public default BlockColorProvider getBlockColor() {
		return (state, world, pos, tintIndex) -> getColor(pos, myaliteS(), myaliteB());
	}
	
	@Override
    @Environment(EnvType.CLIENT)
	public default ItemColorProvider getItemColor() {
		return (stack, tintIndex) -> {
			MinecraftClient mc = MinecraftClient.getInstance();
			if(mc.player == null)
				return getColor(BlockPos.ORIGIN, myaliteS(), myaliteB());
			
			BlockPos pos = mc.player.getBlockPos();
			HitResult res = mc.crosshairTarget;
			if(res != null && res instanceof BlockHitResult)
				pos = ((BlockHitResult) res).getBlockPos();
			
			return getColor(pos, myaliteS(), myaliteB());
		};
	}
	
	default float myaliteS() { return 0.7F; }
	default float myaliteB() { return 0.8F; }
	
	public static int getColor(BlockPos pos, float s, float b) {
		final double sp = 0.15;
    	final double range = 0.3;
    	final double shift = 0.05;
	
		double x = pos.getX() * sp;
		double y = pos.getY() * sp;
		double z = pos.getZ() * sp;
		
		double xv = x + Math.sin(z) * 2;
		double zv = z + Math.cos(x) * 2;
		double yv = y + Math.sin(y + Math.PI / 4) * 2;
		
		double noise = NOISE.sample(xv + yv, zv + (yv * 2), false);
		
    	double h = noise * (range / 2) - range + shift;

		return Color.HSBtoRGB((float) h, s, b);
    }
	
}