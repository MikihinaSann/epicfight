package yesman.epicfight.client.renderer.shader.compute.backend.buffers;

import java.io.Closeable;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.function.BiConsumer;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL15C;
import org.lwjgl.opengl.GL30C;
import org.lwjgl.opengl.GL43C;


public class DynamicSSBO<T> implements Closeable, IArrayBufferProxy {
    public final T[] src;
    public final short srcSize;
    public final int glSSBO;
    public final DataMode mode;
    public final BiConsumer<T, FloatBuffer> uploader;
    
    private final FloatBuffer buffer;
    private int lastBinding = -1;
    
    public DynamicSSBO(T[] src, short srcSize, DataMode DataMode, BiConsumer<T, FloatBuffer> uploader) {
        this.src = src;
        this.mode = DataMode;
        this.srcSize = srcSize;
        this.uploader = uploader;
        this.glSSBO = GL15C.glGenBuffers();
        
        GL15C.glBindBuffer(GL43C.GL_SHADER_STORAGE_BUFFER, this.glSSBO);
        GL15C.glBufferData(GL43C.GL_SHADER_STORAGE_BUFFER, (long) src.length * srcSize * 4, mode.glMode);
        GL15C.glBindBuffer(GL43C.GL_SHADER_STORAGE_BUFFER, 0);
        
        this.buffer = BufferUtils.createByteBuffer(src.length * srcSize * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
    }
    
    @Override
    public void updateAll() {
    	this.updateFromTo(0, this.src.length);
    }

    @Override
    public void updateFromTo(int from, int to) {
    	// Upload exactly the written range - flipping the staging buffer keeps a partial
    	// update from re-sending the whole (MAX_JOINTS-sized) buffer every frame
    	this.buffer.clear();

        for (int i = from; i < to; i++) {
        	this.uploader.accept(this.src[i], this.buffer);
        }

        this.buffer.flip();

        GL15C.glBindBuffer(GL43C.GL_SHADER_STORAGE_BUFFER, this.glSSBO);
		GL15C.glBufferSubData(GL43C.GL_SHADER_STORAGE_BUFFER, (long) srcSize * 4 * from, this.buffer);
        GL15C.glBindBuffer(GL43C.GL_SHADER_STORAGE_BUFFER, 0);
    }
    
    @Override
	public void bindBufferBase(int binding) {
    	this.unbind();
        this.lastBinding = binding;
        GL30C.glBindBufferBase(GL43C.GL_SHADER_STORAGE_BUFFER, binding, this.glSSBO);
    }
    
    @Override
	public void unbind() {
        if (this.lastBinding >= 0) {
        	GL30C.glBindBufferBase(GL43C.GL_SHADER_STORAGE_BUFFER, this.lastBinding, 0);
        }
    }
	
    @Override
    public void close() {
        if (this.glSSBO != 0) {
        	GL15C.glDeleteBuffers(this.glSSBO);
        }
    }
    
    public enum DataMode {
        STATIC(GL15C.GL_STATIC_DRAW), DYNAMIC(GL15C.GL_DYNAMIC_DRAW), STREAM(GL15C.GL_STREAM_DRAW);
    	
        public final int glMode;
        
        DataMode(int glMode) {
        	this.glMode = glMode;
        }
    }
}
