package net.neoforged.neoforge.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.block.model.BakedModel;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.InteractionHand;

/// Stub for NeoForge's ClientHooks.
public class ClientHooks {
    public static Object onClickInput(int button, int key, InteractionHand hand) {
        return null;
    }

    public static float getGuiFarPlane() {
        return 11000.0F;
    }

    public static BakedModel handleCameraTransforms(PoseStack poseStack, BakedModel model, ItemDisplayContext displayContext, boolean applyLeftHandTransform) {
        return model;
    }
}
