package yesman.epicfight.client.renderer.shader.compute.backend.program;

import org.joml.Matrix4f;
import org.lwjgl.opengl.GL46C;
import org.lwjgl.system.MemoryStack;

public class Uniform {
    private final int programHandle;
    private final int uniformLocation;
    
    public Uniform(int programHandle, int uniformLocation) {
		this.programHandle = programHandle;
		this.uniformLocation = uniformLocation;
    }

    public void uploadMatrix4f(Matrix4f matrix) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
			GL46C.glProgramUniformMatrix4fv(programHandle, uniformLocation, false, matrix.get(stack.callocFloat(16)));
        }
    }

    public void uploadUnsignedInt(int value) {
		GL46C.glProgramUniform1ui(programHandle, uniformLocation, value);
    }

    public void uploadVec4(float a, float b, float c, float d){
		GL46C.glProgramUniform4f(programHandle, uniformLocation, a, b, c, d);
    }
}