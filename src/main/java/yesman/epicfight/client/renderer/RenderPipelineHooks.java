package yesman.epicfight.client.renderer;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * Indirection points around Epic Fight's pre-transformed mesh draws so optional
 * render-acceleration mods (currently AcceleratedRendering) can exclude those
 * draws from their own GPU vertex-transform pipeline. Epic Fight's skinned
 * vertices are already fully posed when they reach the VertexConsumer - letting
 * an acceleration mod transform them again corrupts the geometry.
 *
 * The hooks stay no-ops unless a compat module rebinds them, keeping this class
 * free of any hard reference to optional mods.
 */
@OnlyIn(Dist.CLIENT)
public class RenderPipelineHooks {
	private static Runnable preSkinnedMeshDraw = () -> {};
	private static Runnable postSkinnedMeshDraw = () -> {};

	private static Runnable preEntityRender = () -> {};
	private static Runnable postEntityRender = () -> {};

	public static void setSkinnedMeshDrawHooks(Runnable pre, Runnable post) {
		preSkinnedMeshDraw = pre;
		postSkinnedMeshDraw = post;
	}

	/** Escape hatch: wraps the entire patched-entity render instead of only the mesh draws. */
	public static void setEntityRenderHooks(Runnable pre, Runnable post) {
		preEntityRender = pre;
		postEntityRender = post;
	}

	public static void preEntityRender() {
		preEntityRender.run();
	}

	public static void postEntityRender() {
		postEntityRender.run();
	}

	public static void preSkinnedMeshDraw() {
		preSkinnedMeshDraw.run();
	}

	public static void postSkinnedMeshDraw() {
		postSkinnedMeshDraw.run();
	}
}
