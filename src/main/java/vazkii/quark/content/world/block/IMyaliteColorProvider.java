package vazkii.quark.content.world.block;

import java.awt.Color;
import java.util.stream.IntStream;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.color.IBlockColor;
import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.util.SharedSeedRandom;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.gen.PerlinNoiseGenerator;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import vazkii.arl.interf.IBlockColorProvider;

public interface IMyaliteColorProvider extends IBlockColorProvider {
	
	static final PerlinNoiseGenerator NOISE = new PerlinNoiseGenerator(new SharedSeedRandom(4543543), IntStream.rangeClosed(-4, 4));
	
	@Override
    @OnlyIn(Dist.CLIENT)
	public default IBlockColor getBlockColor() {
		return (state, world, pos, tintIndex) -> getColor(pos, myaliteS(), myaliteB());
	}
	
	@Override
    @OnlyIn(Dist.CLIENT)
	public default IItemColor getItemColor() {
		return (stack, tintIndex) -> {
			Minecraft mc = Minecraft.getInstance();
			if(mc.player == null)
				return getColor(BlockPos.ZERO, myaliteS(), myaliteB());
			
			BlockPos pos = mc.player.getPosition();
			RayTraceResult res = mc.objectMouseOver;
			if(res != null && res instanceof BlockRayTraceResult)
				pos = ((BlockRayTraceResult) res).getPos();
			
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
		
		double noise = NOISE.noiseAt(xv + yv, zv + (yv * 2), false);
		
    	double h = noise * (range / 2) - range + shift;

		return Color.HSBtoRGB((float) h, s, b);
    }
	
}