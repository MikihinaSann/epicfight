package mod.azure.azurelibarmor.common.render.armor;

import com.mojang.blaze3d.vertex.PoseStack;
import mod.azure.azurelibarmor.common.model.AzBakedModel;
import mod.azure.azurelibarmor.common.model.AzBone;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

/// Stub for AzureLibArmor's AzArmorRenderer.
public class AzArmorRenderer {
    public void prepForRender(LivingEntity entity, ItemStack stack, EquipmentSlot slot, HumanoidModel<?> model) {}
    public void prepMatrixForBone(PoseStack poseStack, AzBone bone) {}
    public AzBakedModel getBakedModel() { return null; }
    public AzBone getRightArmBone(AzBakedModel model) { return null; }
    public AzBone getLeftArmBone(AzBakedModel model) { return null; }
    public AzBone getRightLegBone(AzBakedModel model) { return null; }
    public AzBone getLeftLegBone(AzBakedModel model) { return null; }
    public AzBone getRightBootBone(AzBakedModel model) { return null; }
    public AzBone getLeftBootBone(AzBakedModel model) { return null; }
    public AzBone getBodyBone(AzBakedModel model) { return null; }
    public Object provider() { return null; }
    public AzBakedModel provideBakedModel(LivingEntity entity, ItemStack stack) { return null; }
    public Object rendererPipeline() { return new Object(); }
}
