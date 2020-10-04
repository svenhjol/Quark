package vazkii.quark.management.client.render;

import javax.annotation.Nonnull;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.model.json.ModelTransformation.Mode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.util.math.Vector3f;
import net.minecraft.entity.Entity;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import vazkii.quark.management.entity.ChestPassengerEntity;

/**
 * @author WireSegal
 * Created at 2:02 PM on 9/3/19.
 */
public class ChestPassengerRenderer extends EntityRenderer<ChestPassengerEntity> {

    public ChestPassengerRenderer(EntityRenderDispatcher renderManager) {
        super(renderManager);
    }
    
    @Override
    	public void render(ChestPassengerEntity entity, float yaw, float partialTicks, MatrixStack matrix, VertexConsumerProvider buffer, int light) {
        if(!entity.hasVehicle())
            return;

        Entity riding = entity.getVehicle();
        if (riding == null)
            return;

        BoatEntity boat = (BoatEntity) riding;
        super.render(entity, yaw, partialTicks, matrix, buffer, light);
        
        float rot = 180F - yaw;

        ItemStack stack = entity.getChestType();

        matrix.push();
        matrix.translate(0, 0.375, 0);
        matrix.multiply(Vector3f.POSITIVE_Y.getDegreesQuaternion(rot));
        float timeSinceHit = boat.getDamageWobbleTicks() - partialTicks;
        float damageTaken = boat.getDamageWobbleStrength() - partialTicks;

        if (damageTaken < 0.0F)
            damageTaken = 0.0F;

        if (timeSinceHit > 0.0F) {
        	double angle = MathHelper.sin(timeSinceHit) * timeSinceHit * damageTaken / 10.0F * boat.getDamageWobbleSide();
            matrix.multiply(Vector3f.POSITIVE_X.getDegreesQuaternion((float) angle));
        }

        float rock = boat.interpolateBubbleWobble(partialTicks);
        if (!MathHelper.approximatelyEquals(rock, 0.0F)) {
        	 matrix.multiply(Vector3f.POSITIVE_X.getDegreesQuaternion(rock));
        }

        if (riding.getPassengerList().size() > 1)
        	matrix.translate(0F, 0F, -0.6F);
        else
        	matrix.translate(0F, 0F, -0.45F);

        matrix.translate(0F, 0.7F - 0.375F, 0.6F - 0.15F);

        matrix.scale(1.75F, 1.75F, 1.75F);

        MinecraftClient.getInstance().getItemRenderer().renderItem(stack, Mode.FIXED, light, OverlayTexture.DEFAULT_UV, matrix, buffer);
        matrix.pop();
    }

    @Override
    public Identifier getEntityTexture(@Nonnull ChestPassengerEntity entity) {
        return null;
    }

}
