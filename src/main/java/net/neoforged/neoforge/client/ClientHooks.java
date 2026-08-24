package net.neoforged.neoforge.client;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.InteractionHand;
public class ClientHooks {
    public static Object onClickInput(int button, int key, InteractionHand hand) { return null; }
    public static float getGuiFarPlane() { return 11000.0F; }
    public static BakedModel handleCameraTransforms(PoseStack poseStack, BakedModel model, ItemDisplayContext displayContext, boolean applyLeftHandTransform) { return model; }
    public static net.minecraft.client.model.Model getArmorModel(Object entity, ItemStack stack, net.minecraft.world.entity.EquipmentSlot slot, Object model) { return null; }
    public static int getArmorLayerTintColor(ItemStack stack, Object entity, Object layer, int fallback, int slot) { return fallback; }
    public static net.minecraft.resources.ResourceLocation getArmorTexture(Object entity, ItemStack stack, Object layer, boolean inner, net.minecraft.world.entity.EquipmentSlot slot) { return null; }
}
