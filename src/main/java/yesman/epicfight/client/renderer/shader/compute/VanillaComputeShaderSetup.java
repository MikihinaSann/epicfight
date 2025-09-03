package yesman.epicfight.client.renderer.shader.compute;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import org.lwjgl.opengl.GL15C;
import org.lwjgl.opengl.GL46C;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormatElement;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import yesman.epicfight.api.client.model.SkinnedMesh;
import yesman.epicfight.api.client.model.SkinnedMesh.SkinnedMeshPart;
import yesman.epicfight.api.client.model.VertexBuilder;
import yesman.epicfight.api.model.Armature;
import yesman.epicfight.api.utils.GLConstants;
import yesman.epicfight.api.utils.math.OpenMatrix4f;
import yesman.epicfight.client.renderer.shader.compute.backend.buffers.DynamicSSBO;
import yesman.epicfight.client.renderer.shader.compute.backend.buffers.OutputSSBO;
import yesman.epicfight.client.renderer.shader.compute.backend.buffers.StaticSSBO;
import yesman.epicfight.client.renderer.shader.compute.backend.program.ComputeProgram;
import yesman.epicfight.client.renderer.shader.compute.loader.ComputeShaderProvider;

@OnlyIn(Dist.CLIENT)
public class VanillaComputeShaderSetup implements ComputeShaderSetup {
	private final StaticSSBO<Float> uvsBO;
	private final StaticSSBO<VertexObj> vObjBO;
	private final StaticSSBO<Integer> jointBO;
	private	final StaticSSBO<Float> weightBO;
	
	private final OutputSSBO outPos;
	private final OutputSSBO outNormal;
	private final OutputSSBO outColor;
	private final OutputSSBO outUv1;
	private final OutputSSBO outUv2;
	
	private final int arrayObjectId;
	private final int vcount;
	
	public VanillaComputeShaderSetup(SkinnedMesh skinnedMesh) {
		Map<VertexBuilder, Integer> vertexBuilderMap = new HashMap<> ();
		List<Float> uvList = new ArrayList<> ();
		
		this.arrayObjectId = GlStateManager._glGenVertexArrays();
		
		int currentBoundVao = GlStateManager._getInteger(GLConstants.GL_VERTEX_ARRAY_BINDING);
		int currentBoundVbo = GlStateManager._getInteger(GLConstants.GL_VERTEX_ARRAY_BUFFER_BINDING);
		GlStateManager._glBindVertexArray(this.arrayObjectId);
		
		skinnedMesh.getAllParts().forEach(skinnedMeshPart -> {
			skinnedMeshPart.initVBO(new VanillaPartBuffer(skinnedMeshPart.getVertices(), vertexBuilderMap, skinnedMesh.uvs(), uvList));
		});
		
		VertexObj[] vertexObjs = new VertexObj[vertexBuilderMap.size()];
		
		List<Integer> jointList = new ArrayList<> ();
		List<Float> weightList = new ArrayList<> ();
		
		vertexBuilderMap.forEach((vb, idx) -> {
			int startPos = jointList.size();
			
			for (int i = 0; i < skinnedMesh.affectingJointCounts()[vb.position]; i++) {
				int jointIndex = skinnedMesh.affectingJointIndices()[vb.position][i];
				int weightIndex = skinnedMesh.affectingWeightIndices()[vb.position][i];
				float weight = skinnedMesh.weights()[weightIndex];

				jointList.add(jointIndex);
				weightList.add(weight);
			}
			
			vertexObjs[idx] = new VertexObj(
				skinnedMesh.positions()[vb.position * 3],
				skinnedMesh.positions()[vb.position * 3 + 1],
				skinnedMesh.positions()[vb.position * 3 + 2],
				skinnedMesh.normals()[vb.normal * 3],
				skinnedMesh.normals()[vb.normal * 3 + 1],
				skinnedMesh.normals()[vb.normal * 3 + 2],
				startPos,
				startPos + skinnedMesh.affectingJointCounts()[vb.position]
			);
		});
		
		this.vcount = vertexObjs.length;
		
		this.uvsBO = new StaticSSBO<> (uvList, 1, (v, b) -> b.put(v));
		this.vObjBO = new StaticSSBO<> (List.of(vertexObjs), 8, VertexObj::store);
		this.jointBO = new StaticSSBO<> (jointList, 1, (v, b) -> b.put(Float.intBitsToFloat(v)));
		this.weightBO = new StaticSSBO<> (weightList, 1, (v, b) -> b.put(v));
		
		this.outPos = new OutputSSBO((short)3, vertexObjs.length, DynamicSSBO.DataMode.STREAM);
		this.outNormal = new OutputSSBO((short)3, vertexObjs.length, DynamicSSBO.DataMode.STREAM);
		this.outColor = new OutputSSBO((short)4, vertexObjs.length, DynamicSSBO.DataMode.STREAM);
		this.outUv1 = new OutputSSBO((short)1, vertexObjs.length, DynamicSSBO.DataMode.STREAM);
		this.outUv2 = new OutputSSBO((short)1, vertexObjs.length, DynamicSSBO.DataMode.STREAM);

		GlStateManager._glBindVertexArray(currentBoundVao);
		GlStateManager._glBindBuffer(GLConstants.GL_ARRAY_BUFFER, currentBoundVbo);
	}
	
	@Override
	public void bindBufferFormat(VertexFormat vertexFormat, int... buffers) {
		var elems = vertexFormat.getElements();
        
		for (int i = 0; i < elems.size(); ++i) {
			VertexFormatElement elem = elems.get(i);
			
			if (elem == DefaultVertexFormat.ELEMENT_POSITION) {
				ComputeShaderSetup.bindAttrPointer(buffers[0], 3, i, GL15C.GL_FLOAT);
			} else if (elem == DefaultVertexFormat.ELEMENT_UV) {
				ComputeShaderSetup.bindAttrPointer(buffers[3], 2, i, GL15C.GL_FLOAT);
			} else if (elem == DefaultVertexFormat.ELEMENT_COLOR) {
				GlStateManager._glBindBuffer(GL15C.GL_ARRAY_BUFFER, buffers[2]);
				GL46C.glVertexAttribPointer(i, 4, GL15C.GL_FLOAT, true, 0, 0);
				GL46C.glEnableVertexAttribArray(i);
			} else if (elem == DefaultVertexFormat.ELEMENT_NORMAL) {
				GlStateManager._glBindBuffer(GL15C.GL_ARRAY_BUFFER, buffers[1]);
				GL46C.glVertexAttribPointer(i, 3, GL15C.GL_FLOAT, true, 0, 0);
				GL46C.glEnableVertexAttribArray(i);
			} else if (elem == DefaultVertexFormat.ELEMENT_UV1) {
				ComputeShaderSetup.bindIntAttrPointer(buffers[4], 2, i, GL15C.GL_UNSIGNED_SHORT, 0);
			} else if (elem == DefaultVertexFormat.ELEMENT_UV2) {
				ComputeShaderSetup.bindIntAttrPointer(buffers[5], 2, i, GL15C.GL_UNSIGNED_SHORT, 0);
			}
		}
		
		GlStateManager._glBindBuffer(GL15C.GL_ARRAY_BUFFER, 0);
	}
	
	@Override
	public void applyComputeShader(PoseStack poseStack, OpenMatrix4f partTransform, float r, float g, float b, float a, int overlay, int light, int jointCount) {
		// compute shader setup
		ComputeProgram shader = ComputeShaderProvider.meshCompute;
		shader.useProgram();
		shader.getUniform("colorIn").uploadVec4(r, g, b, a);
		shader.getUniform("uv1In").uploadUnsignedInt(overlay);
		shader.getUniform("uv2In").uploadUnsignedInt(light);
		shader.getUniform("partTransform").uploadMatrix4f(OpenMatrix4f.exportToMojangMatrix(partTransform));
		shader.getUniform("normalTransform").uploadMatrix3f(poseStack.last().normal());
		
		ComputeShaderSetup.POSE_BO.bindBufferBase(0);
		
		this.vObjBO.bindBufferBase(1);
		this.jointBO.bindBufferBase(2);
		this.weightBO.bindBufferBase(3);
		
		this.outPos.bindBufferBase(4);
		this.outNormal.bindBufferBase(5);
		this.outColor.bindBufferBase(6);
		this.outUv1.bindBufferBase(7);

		this.outUv2.bindBufferBase(8);

		int workGroupSize = 128;
		int workGroupCount = (this.vcount + workGroupSize - 1) / workGroupSize;

		shader.dispatch(workGroupCount, 1, 1);
		shader.waitBarriers();

		ComputeShaderSetup.POSE_BO.unbind();
		
		this.vObjBO.unbind();
		this.jointBO.unbind();
		this.weightBO.unbind();
		
		this.outPos.unbind();
		this.outNormal.unbind();
		this.outColor.unbind();
		this.outUv1.unbind();
		this.outUv2.unbind();

		GL46C.glUseProgram(0);
	}
	
	@Override
	public void drawWithShader(SkinnedMesh skinnedMesh, PoseStack poseStack, RenderType renderType, int packedLight, float r, float g, float b, float a, int overlay, @Nullable Armature armature, OpenMatrix4f[] poses) {
		// pose setup and upload
		for (int i = 0; i < poses.length; i++) {
			TOTAL_POSES[i].load(poses[i]);
			
			if (armature != null) {
				TOTAL_POSES[i].mulBack(armature.searchJointById(i).getToOrigin());
			}
		}

		ComputeShaderSetup.POSE_BO.updateFromTo(0, poses.length);

		// state trace
		int currentBoundVao = GlStateManager._getInteger(GLConstants.GL_VERTEX_ARRAY_BINDING);
		int currentBoundVbo = GlStateManager._getInteger(GLConstants.GL_VERTEX_ARRAY_BUFFER_BINDING);

		// setup state
		GlStateManager._glBindVertexArray(this.arrayObjectId);
		renderType.setupRenderState();

		ShaderInstance shader = RenderSystem.getShader();
		this.bindBufferFormat(shader.getVertexFormat(), this.outPos.glSSBO, this.outNormal.glSSBO, this.outColor.glSSBO, this.uvsBO.glSSBO, this.outUv1.glSSBO, this.outUv2.glSSBO);
		
		ComputeShaderSetup.setShaderDefaultUniforms(shader, renderType.mode(), poseStack.last().pose(), RenderSystem.getProjectionMatrix(), Minecraft.getInstance().getWindow());
		shader.apply();
		
		// draw call
		for (SkinnedMeshPart part : skinnedMesh.getAllParts()) {
			if (part.isHidden()) {
				continue;
			}
			
			OpenMatrix4f transform = part.getVanillaPartTransform();
			
			// computeVertex
			if (transform == null) transform = OpenMatrix4f.IDENTITY;
			
			this.applyComputeShader(poseStack, transform, r, g, b, a, overlay, packedLight, poses.length);
			
			// draw call
			GL46C.glUseProgram(RenderSystem.getShader().getId());
			GL46C.glBindBuffer(GLConstants.GL_ELEMENT_ARRAY_BUFFER, part.getPartVBO().vboId());
			GL46C.glDrawElements(VertexFormat.Mode.TRIANGLES.asGLMode, part.getVertices().size(), VertexFormat.IndexType.INT.asGLType, 0);
		}
		
		// state restore
		RenderSystem.getShader().clear();
		renderType.clearRenderState();
		shader.getVertexFormat().clearBufferState();
		
		GlStateManager._glBindVertexArray(currentBoundVao);
		GlStateManager._glBindBuffer(GLConstants.GL_ARRAY_BUFFER, currentBoundVbo);
	}
	
	@Override
	public void destroyBuffers() {
		this.vObjBO.close();
		this.uvsBO.close();
		this.weightBO.close();
		this.jointBO.close();
		
		this.outNormal.close();
		this.outPos.close();
		
		this.outColor.close();
		this.outUv1.close();
		this.outUv2.close();
		
		RenderSystem.glDeleteVertexArrays(this.arrayObjectId);
	}
	
	@Override
	public int vaoId() {
		return this.arrayObjectId;
	}
	
	@Override
	public int vertexCount() {
		return this.vcount;
	}
	
	@OnlyIn(Dist.CLIENT)
	public static class VanillaPartBuffer implements MeshPartBuffer {
		private final int vboId;
		
		public VanillaPartBuffer(List<VertexBuilder> vertexBuilders, Map<VertexBuilder, Integer> vertexBuilderMap, float[] uvs, List<Float> uv) {
			ByteBuffer indicesBuffer = ByteBuffer.allocateDirect(vertexBuilders.size() * 4).order(ByteOrder.nativeOrder());
			
			for (VertexBuilder vb : vertexBuilders) {
				if (vertexBuilderMap.containsKey(vb)) {
					indicesBuffer.putInt(vertexBuilderMap.get(vb));
				} else {
					int next = vertexBuilderMap.size();
					indicesBuffer.putInt(next);
					vertexBuilderMap.put(vb, next);
					
					uv.add(uvs[vb.uv * 2]);
					uv.add(uvs[vb.uv * 2 + 1]);
				}
			}
			
			indicesBuffer.flip();
			
			this.vboId = GlStateManager._glGenBuffers();
			GlStateManager._glBindBuffer(GLConstants.GL_ELEMENT_ARRAY_BUFFER, this.vboId);
			GlStateManager._glBufferData(GLConstants.GL_ELEMENT_ARRAY_BUFFER, indicesBuffer, GLConstants.GL_STATIC_DRAW);
			GlStateManager._glBindBuffer(GLConstants.GL_ELEMENT_ARRAY_BUFFER, 0);
		}
		
		@Override
		public int vboId() {
			return this.vboId;
		}
		
		@Override
		public int partIdx() {
			return -1;
		}
	}
}
