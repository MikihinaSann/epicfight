package yesman.epicfight.client.renderer.patched.entity;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.function.Function;

/**
 * ponytail: defensive wrapper around Epic Fight's @Invoker interface mixins.
 *
 * The @Invoker interface mixins (MixinEntityRenderer, MixinLivingEntityRenderer) inject a synthetic method
 * into the base renderer class (EntityRenderer / LivingEntityRenderer) that subclasses inherit. On modpacks
 * that force a renderer subclass (e.g. PlayerRenderer) to class-finalize before the mixin lands
 * (Figura / epicarsenal / playerAnimator in the render chain), the synthetic method is missing on the
 * subclass and the invoker call throws AbstractMethodError at render time, crashing the client.
 *
 * This wrapper keeps the fast @Invoker path (zero overhead on healthy setups) and, on the first LinkageError
 * for a given renderer class, switches that class permanently to calling the protected vanilla method via
 * cached reflection — which has no inheritance/synthetic-method dependency. Reflection lookups are cached
 * per class; the broken-class set is weak so classloaders can GC.
 */
public final class InvokerCompat {
	// ponytail: shared erased param-type arrays for the wrapped @Invoker calls.
	public static final Class<?>[] SHOULD_SHOW_NAME_PARAMS = {net.minecraft.world.entity.Entity.class};
	public static final Class<?>[] RENDER_NAME_TAG_PARAMS = {net.minecraft.world.entity.Entity.class, net.minecraft.network.chat.Component.class, com.mojang.blaze3d.vertex.PoseStack.class, net.minecraft.client.renderer.MultiBufferSource.class, int.class};
	public static final Class<?>[] IS_BODY_VISIBLE_PARAMS = {net.minecraft.world.entity.LivingEntity.class};
	public static final Class<?>[] GET_RENDER_TYPE_PARAMS = {net.minecraft.world.entity.LivingEntity.class, boolean.class, boolean.class, boolean.class};
	public static final Class<?>[] GET_BOB_PARAMS = {net.minecraft.world.entity.LivingEntity.class, float.class};

	private static final Set<Class<?>> BROKEN = Collections.newSetFromMap(new WeakHashMap<>());
	private static final Map<MethodKey, Method> METHODS = new WeakHashMap<>();

	private record MethodKey(Class<?> clazz, String name, Class<?>[] params) {
	}

	private InvokerCompat() {
	}

	private static boolean isBroken(Object receiver) {
		Class<?> clazz = receiver.getClass();
		for (Class<?> c = clazz; c != null; c = c.getSuperclass()) {
			if (BROKEN.contains(c)) {
				return true;
			}
		}
		return false;
	}

	private static Method findMethod(Object receiver, String name, Class<?>[] params) {
		MethodKey key = new MethodKey(receiver.getClass(), name, params);
		return METHODS.computeIfAbsent(key, k -> {
			for (Class<?> c = k.clazz(); c != null; c = c.getSuperclass()) {
				try {
					Method m = c.getDeclaredMethod(k.name(), k.params());
					m.setAccessible(true);
					return m;
				} catch (NoSuchMethodException ignored) {
				}
			}
			return null;
		});
	}

	@SuppressWarnings("unchecked")
	private static <T> T fallback(Object receiver, String methodName, Class<?>[] paramTypes, Object[] args, T defaultValue) {
		Method m = findMethod(receiver, methodName, paramTypes);

		if (m == null) {
			return defaultValue;
		}

		try {
			return (T)m.invoke(receiver, args);
		} catch (Throwable throwable) {
			return defaultValue;
		}
	}

	/**
	 * Call a @Invoker method with a boolean return.
	 *
	 * @param receiver the renderer instance (the @Invoker target subclass)
	 * @param fastPath invoked on healthy setups (the real @Invoker cast + call)
	 * @param fallbackMethod the vanilla method name to reflect on LinkageError (e.g. "shouldShowName")
	 * @param paramTypes erased parameter types of the vanilla method
	 * @param args arguments to pass to the vanilla method
	 */
	public static boolean callBoolean(Object receiver, Function<Object, Boolean> fastPath, String fallbackMethod, Class<?>[] paramTypes, Object[] args) {
		if (isBroken(receiver)) {
			return fallback(receiver, fallbackMethod, paramTypes, args, Boolean.FALSE);
		}

		try {
			return fastPath.apply(receiver);
		} catch (LinkageError error) {
			BROKEN.add(receiver.getClass());
			return fallback(receiver, fallbackMethod, paramTypes, args, Boolean.FALSE);
		}
	}

	public static float callFloat(Object receiver, Function<Object, Float> fastPath, String fallbackMethod, Class<?>[] paramTypes, Object[] args) {
		if (isBroken(receiver)) {
			return fallback(receiver, fallbackMethod, paramTypes, args, 0.0F);
		}

		try {
			return fastPath.apply(receiver);
		} catch (LinkageError error) {
			BROKEN.add(receiver.getClass());
			return fallback(receiver, fallbackMethod, paramTypes, args, 0.0F);
		}
	}

	public static <R> R callObject(Object receiver, Function<Object, R> fastPath, String fallbackMethod, Class<?>[] paramTypes, Object[] args, R defaultValue) {
		if (isBroken(receiver)) {
			return fallback(receiver, fallbackMethod, paramTypes, args, defaultValue);
		}

		try {
			return fastPath.apply(receiver);
		} catch (LinkageError error) {
			BROKEN.add(receiver.getClass());
			return fallback(receiver, fallbackMethod, paramTypes, args, defaultValue);
		}
	}

	public static void callVoid(Object receiver, Function<Object, Void> fastPath, String fallbackMethod, Class<?>[] paramTypes, Object[] args) {
		if (isBroken(receiver)) {
			Method m = findMethod(receiver, fallbackMethod, paramTypes);

			if (m != null) {
				try {
					m.invoke(receiver, args);
				} catch (Throwable ignored) {
				}
			}
			return;
		}

		try {
			fastPath.apply(receiver);
		} catch (LinkageError error) {
			BROKEN.add(receiver.getClass());
			Method m = findMethod(receiver, fallbackMethod, paramTypes);

			if (m != null) {
				try {
					m.invoke(receiver, args);
				} catch (Throwable ignored) {
				}
			}
		}
	}
}
