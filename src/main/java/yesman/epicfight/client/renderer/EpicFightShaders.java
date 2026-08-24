package yesman.epicfight.client.renderer;
import yesman.epicfight.EpicFight;
import net.minecraft.client.Minecraft;

import java.io.IOException;

import javax.annotation.Nullable;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;

import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.fabricmc.api.EnvType;



import yesman.epicfight.main.EpicFightMod;


public class EpicFightShaders {
	public static ShaderInstance positionColorNormalShader;
	
	@Nullable
	public static ShaderInstance getPositionColorNormalShader() {
		return positionColorNormalShader;
	}
	
	
	public static void epicfight$registerShaders(net.neoforged.neoforge.client.event.RegisterShadersEvent event) throws IOException {
		event.registerShader(new ShaderInstance(event.getResourceProvider(), EpicFight.identifier("solid_model"), DefaultVertexFormat.POSITION_COLOR_NORMAL), reloadedShader -> {
			positionColorNormalShader = reloadedShader;
		});
	}
}
