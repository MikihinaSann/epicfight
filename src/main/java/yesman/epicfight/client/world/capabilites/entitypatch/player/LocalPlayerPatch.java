package yesman.epicfight.client.world.capabilites.entitypatch.player;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.entity.PartEntity;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import yesman.epicfight.api.animation.JointTransform;
import yesman.epicfight.api.animation.Keyframe;
import yesman.epicfight.api.animation.Pose;
import yesman.epicfight.api.animation.TransformSheet;
import yesman.epicfight.api.animation.property.AnimationProperty.ActionAnimationProperty;
import yesman.epicfight.api.animation.types.ActionAnimation;
import yesman.epicfight.api.animation.types.AttackAnimation;
import yesman.epicfight.api.animation.types.DirectStaticAnimation;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.api.asset.AssetAccessor;
import yesman.epicfight.api.client.animation.AnimationSubFileReader;
import yesman.epicfight.api.client.animation.AnimationSubFileReader.PovSettings;
import yesman.epicfight.api.client.animation.AnimationSubFileReader.PovSettings.ViewLimit;
import yesman.epicfight.api.client.animation.Layer;
import yesman.epicfight.api.client.animation.property.ClientAnimationProperties;
import yesman.epicfight.api.client.input.action.EpicFightInputActions;
import yesman.epicfight.api.client.input.handlers.InputManager;
import yesman.epicfight.api.utils.math.MathUtils;
import yesman.epicfight.client.ClientEngine;
import yesman.epicfight.client.events.engine.RenderEngine;
import yesman.epicfight.client.gui.screen.SkillBookScreen;
import yesman.epicfight.client.input.EpicFightKeyMappings;
import yesman.epicfight.config.ClientConfig;
import yesman.epicfight.gameasset.Animations;
import yesman.epicfight.main.EpicFightSharedConstants;
import yesman.epicfight.network.EpicFightNetworkManager;
import yesman.epicfight.network.client.CPAnimatorControl;
import yesman.epicfight.network.client.CPChangePlayerMode;
import yesman.epicfight.network.client.CPModifyEntityModelYRot;
import yesman.epicfight.network.client.CPSetPlayerTarget;
import yesman.epicfight.network.client.CPSetStamina;
import yesman.epicfight.network.common.AnimatorControlPacket;
import yesman.epicfight.skill.modules.ChargeableSkill;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.EntityPatch;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;
import yesman.epicfight.world.capabilities.item.CapabilityItem;
import yesman.epicfight.world.capabilities.item.CapabilityItem.ZoomInType;
import yesman.epicfight.world.entity.eventlistener.PlayerEventListener.EventType;

@OnlyIn(Dist.CLIENT)
public class LocalPlayerPatch extends AbstractClientPlayerPatch<LocalPlayer> {
	private static final Set<UseAnim> SYNC_ITEM_ANIMATIONS = Set.of(UseAnim.BLOCK,UseAnim.BOW,UseAnim.SPEAR,UseAnim.CROSSBOW);
	private static final UUID ACTION_EVENT_UUID = UUID.fromString("d1a1e102-1621-11ed-861d-0242ac120002");
	private Minecraft minecraft;
	private LivingEntity rayTarget;
	
	private boolean lockingOnTarget;
	private float staminaO;
	private int prevChargingAmount;
	private float fpvXRotO;
	private float fpvXRot;
	private float fpvYRotO;
	private float fpvYRot;
	private int fpvLerpTick;
	
	private HitResult cameraBasedHitResult;
	private FirstPersonLayer firstPersonLayer = new FirstPersonLayer();
	private AnimationSubFileReader.PovSettings povSettings;
	
	@Override
	public void onConstructed(LocalPlayer entity) {
		super.onConstructed(entity);
		this.minecraft = Minecraft.getInstance();
	}
	
	@Override
	public void onJoinWorld(LocalPlayer player, EntityJoinLevelEvent event) {
		super.onJoinWorld(player, event);
		
		this.eventListeners.addEventListener(EventType.ACTION_EVENT_CLIENT, ACTION_EVENT_UUID, (playerEvent) -> {
			ClientEngine.getInstance().controlEngine.unlockHotkeys();
		});
	}
	
	public void onRespawnLocalPlayer(ClientPlayerNetworkEvent.Clone event) {
		this.onJoinWorld(event.getNewPlayer(), new EntityJoinLevelEvent(event.getNewPlayer(), event.getNewPlayer().level()));
	}
	
	@Override
	public void tick(LivingEvent.LivingTickEvent event) {
		this.staminaO = this.getStamina();
		
		if (this.isHoldingAny() && this.getHoldingSkill() instanceof ChargeableSkill) {
			this.prevChargingAmount = this.getChargingAmount();
		} else {
			this.prevChargingAmount = 0;
		}
		
		super.tick(event);
	}
	
	@Override
	public void clientTick(LivingEvent.LivingTickEvent event) {
		this.staminaO = this.getStamina();
		
		super.clientTick(event);
		
		// Calculate camera based ray trace result
		double pickRange = this.minecraft.options.renderDistance().get() * 16.0D;
		Vec3 cameraPos = this.minecraft.gameRenderer.getMainCamera().getPosition();
		Vec3 lookVec = new Vec3(this.minecraft.gameRenderer.getMainCamera().getLookVector());
		Vec3 rayEed = cameraPos.add(lookVec.x * pickRange, lookVec.y * pickRange, lookVec.z * pickRange);
		
		this.cameraBasedHitResult = this.original.level().clip(new ClipContext(cameraPos, rayEed, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, this.original));
		
		pickRange = this.cameraBasedHitResult.getLocation().distanceToSqr(cameraPos);
		AABB aabb = this.original.getBoundingBox().move(cameraPos.subtract(this.original.getEyePosition(1.0F))).expandTowards(lookVec.scale(pickRange)).inflate(1.0D, 1.0D, 1.0D);
        
		EntityHitResult entityHitResult = ProjectileUtil.getEntityHitResult(
			this.original,
			cameraPos,
			rayEed,
			aabb,
			entity -> {
				return !entity.isSpectator() && entity.isPickable() && !entity.is(this.original);
			},
			pickRange
		);
		
		if (entityHitResult != null) {
			this.cameraBasedHitResult = entityHitResult;
			
			if (!entityHitResult.getEntity().is(this.rayTarget)) {
				if (entityHitResult.getEntity() instanceof LivingEntity livingentity) {
					if (!(entityHitResult.getEntity() instanceof ArmorStand) && (!this.lockingOnTarget || EpicFightKeyMappings.SHIFT_TARGET.isDown())) {
						this.rayTarget = livingentity;
					}
				} else if (entityHitResult.getEntity() instanceof PartEntity<?> partEntity) {
					Entity parent = partEntity.getParent();
					
					if (parent instanceof LivingEntity parentLivingEntity && (!this.lockingOnTarget || EpicFightKeyMappings.SHIFT_TARGET.isDown())) {
						this.rayTarget = parentLivingEntity;
					}
				} else {
					this.setLockOn(false);
					this.rayTarget = null;
				}
				
				if (this.rayTarget != null) {
					EpicFightNetworkManager.sendToServer(new CPSetPlayerTarget(this.rayTarget.getId()));
				}
			}
		}
		
		RenderEngine renderEngine = ClientEngine.getInstance().renderEngine;
		boolean tpsMode = renderEngine.isTPSMode();
		
		if (tpsMode) {
			Vec3 view = new Vec3(this.minecraft.gameRenderer.getMainCamera().getLookVector());
			
			// If the hit result is in front of the player based on to camera, set missed.
			if (view.dot(this.cameraBasedHitResult.getLocation().subtract(this.original.getEyePosition()).normalize()) < -0.1D) {
				this.cameraBasedHitResult = BlockHitResult.miss(cameraPos.add(lookVec.x * 50.0D, lookVec.y * 50.0D, lookVec.z * 50.0D), Direction.UP, BlockPos.ZERO);
				
				if (!this.lockingOnTarget) {
					this.rayTarget = null;
				}
			}
			
			// If the ray target is in front of the player based on to camera, set missed.
			if (this.rayTarget != null) {
				double dot = view.dot(this.rayTarget.getEyePosition().subtract(this.original.getEyePosition()));
				
				if (dot < -0.1D) {
					if (!this.lockingOnTarget) {
						this.rayTarget = null;
					}
				}
			}
		}
		
		// Tick ray target
		if (this.rayTarget != null) {
			double distance = this.original.distanceTo(this.rayTarget);
			
			if (
				(entityHitResult == null || !entityHitResult.getEntity().is(this.rayTarget)) && // Prevent removing target that is set in this tick
				(
					// Target is dead
					this.rayTarget.isRemoved() ||
					// Target is invisible
					this.rayTarget.isInvisibleTo(this.original) ||
					// Distance too far
					distance > (this.original.getAttributeValue(ForgeMod.ENTITY_REACH.get()) + 3) * 5.0D || 
					// Angle between look vec and to target too wide
					!this.lockingOnTarget &&
						this.rayTarget.getBoundingBox().getCenter().subtract(this.minecraft.gameRenderer.getMainCamera().getPosition()).normalize()
							.dot(new Vec3(this.minecraft.gameRenderer.getMainCamera().getLookVector())) < Mth.clampedLerp(0.0D, 0.99D, Mth.inverseLerp(Mth.clamp(distance, 1.0D, 3.5D), 1.0D, 3.5D))
				)
			) {
				if (this.lockingOnTarget) {
					this.setLockOn(false);
				}
				
				this.rayTarget = null;
				EpicFightNetworkManager.sendToServer(new CPSetPlayerTarget(-1));
			}
		}
		
		float clamp = 30.0F;
		float desiredXRot = 0.0F;
		float desiredYRot = 0.0F;
		boolean shouldSyncYaw =
			Mth.abs(this.original.input.forwardImpulse) > 1.0E-5F || Mth.abs(this.original.input.leftImpulse) > 1.0E-5F || 	// When moving
			this.minecraft.options.keyAttack.isDown() ||																	// When pressing left button
			this.original.isUsingItem() && SYNC_ITEM_ANIMATIONS.contains(this.original.getUseItem().getUseAnimation()) ||	// When using an item
			renderEngine.isZooming() ||																						// when zooming
			this.isHoldingAny(); 																							// When holding a skill
		
		// Handle camera lock-on
		if (this.rayTarget != null && this.lockingOnTarget && !this.isLerpingFpv() && !EpicFightKeyMappings.SHIFT_TARGET.isDown()) {
			Vec3 povPos = tpsMode ? cameraPos : this.original.getEyePosition();
			Vec3 targetPos = tpsMode ? this.rayTarget.getBoundingBox().getCenter() : this.rayTarget.getEyePosition();
			Vec3 toTarget = targetPos.subtract(povPos);
			float xRot = (float)MathUtils.getXRotOfVector(toTarget);
			float yRot = (float)MathUtils.getYRotOfVector(toTarget);
			
			CameraType cameraType = this.minecraft.options.getCameraType();
			if (!cameraType.isFirstPerson()) xRot = Mth.clamp(xRot, -clamp, clamp);
			xRot += (cameraType.isFirstPerson() || tpsMode ? 0.0F : 30.0F + xRot * 0.5F);
			
			float xLerp = Mth.clamp(Mth.wrapDegrees(xRot - renderEngine.getCameraXRot()) * 0.4F, -clamp, clamp);
			float yLerp = Mth.clamp(Mth.wrapDegrees(yRot - renderEngine.getCameraYRot()) * 0.4F, -clamp, clamp);
			Vec3 playerToTarget = targetPos.subtract(this.original.getEyePosition());
			
			// Limit angle difference in cinemtaic camera
			if (tpsMode) {
				double angle = MathUtils.getAngleBetween(playerToTarget, toTarget);
				if (angle < 30.0D) renderEngine.setCameraRotations(renderEngine.getCameraXRot() + xLerp, renderEngine.getCameraYRot() + yLerp, false);
			} else {
				renderEngine.setCameraRotations(renderEngine.getCameraXRot() + xLerp, renderEngine.getCameraYRot() + yLerp, false);
			}
			
			desiredXRot = (float)MathUtils.getXRotOfVector(playerToTarget);
			desiredYRot = (float)MathUtils.getYRotOfVector(playerToTarget);
		} else if (this.lockingOnTarget && EpicFightKeyMappings.SHIFT_TARGET.isDown()) {
			desiredXRot = renderEngine.getCameraXRot();
			desiredYRot = renderEngine.getCameraYRot();
		} else if (tpsMode) { // Handle camera tps mode
			// Follows the camera angle when the degree difference between head and body is less than 50
			if (Mth.abs(Mth.wrapDegrees(renderEngine.getCameraYRot() - this.original.yBodyRot)) <= 51.0F || shouldSyncYaw) {
				Vec3 toHitResult;
				
				if (this.lockingOnTarget) toHitResult = this.rayTarget.getEyePosition();
				else if (this.cameraBasedHitResult.getType() == HitResult.Type.MISS) {
					// Determines lookscale based on x rotation for parabola-trajectory projectiles
					double delta = Mth.clamp(this.original.getXRot(), -30.0F, 0.0F) / -30.0F;
					double lookVecScale = Mth.clampedLerp(30.0D, 75.0D, delta);
					toHitResult = cameraPos.add(lookVec.scale(lookVecScale));
				}
				else toHitResult = this.cameraBasedHitResult.getLocation();
				toHitResult = toHitResult.subtract(this.original.getEyePosition());
				desiredXRot = (float)MathUtils.getXRotOfVector(toHitResult);
				desiredYRot = shouldSyncYaw ? (float)MathUtils.getYRotOfVector(toHitResult) : renderEngine.getCameraYRot();
			} else {
				desiredXRot = 0.0F;
				desiredYRot = this.original.yBodyRot;
				clamp = 15.0F;
			}
		}
		
		if (!this.state.inaction()) {
			if (tpsMode || this.minecraft.options.getCameraType() == CameraType.THIRD_PERSON_BACK && this.lockingOnTarget) {
				if (!this.getEntityState().turningLocked() || this.getEntityState().lockonRotate()) {
					float xDelta = Mth.clamp(Mth.wrapDegrees(desiredXRot - this.original.getXRot()), -clamp, clamp);
					float yDelta = Mth.clamp(Mth.wrapDegrees(desiredYRot - this.original.getYRot()), -clamp, clamp);
					this.original.setXRot(this.original.getXRot() + xDelta);
					this.original.setYRot(this.original.getYRot() + yDelta);
				}
			}
		}
		
		// Handle first person animation
		final AssetAccessor<? extends StaticAnimation> currentPlaying = this.firstPersonLayer.animationPlayer.getRealAnimation();
		
		boolean noPovAnimation = this.getClientAnimator().iterVisibleLayersUntilFalse(layer -> {
			if (layer.isOff()) {
				return true;
			}
			
			Optional<DirectStaticAnimation> optPovAnimation = layer.animationPlayer.getRealAnimation().get().getProperty(ClientAnimationProperties.POV_ANIMATION);
			Optional<PovSettings> optPovSettings = layer.animationPlayer.getRealAnimation().get().getProperty(ClientAnimationProperties.POV_SETTINGS);
			
			optPovAnimation.ifPresent(povAnimation -> {
				if (!povAnimation.equals(currentPlaying.get())) {
					this.firstPersonLayer.playAnimation(povAnimation, layer.animationPlayer.getRealAnimation(), this, 0.0F);
					this.povSettings = optPovSettings.get();
				}
			});
			
			return !optPovAnimation.isPresent();
		});
		
		if (noPovAnimation && !currentPlaying.equals(Animations.EMPTY_ANIMATION)) {
			this.firstPersonLayer.off();
		}
		
		this.firstPersonLayer.update(this);
		
		if (this.firstPersonLayer.animationPlayer.getAnimation().equals(Animations.EMPTY_ANIMATION)) {
			this.povSettings = null;
		}
		
		boolean isLerping = this.isLerpingFpv();
		
		if (isLerping) this.fpvLerpTick--;
		if (isLerping && !this.isLerpingFpv()) {
			this.original.setXRot(this.fpvXRot);
			this.original.setYRot(this.fpvYRot);
		}
	}
	
	@Override
	public boolean overrideRender() {
		// Disable rendering the player when animated first person model disabled
		if (this.original.is(this.minecraft.player)) {
			if (this.minecraft.options.getCameraType().isFirstPerson() && !ClientConfig.enableAnimatedFirstPersonModel) {
				return false;
			}
		}
		
		return super.overrideRender();
	}
	
	@Override
	public LivingEntity getTarget() {
		return this.rayTarget;
	}
	
	@Override
	public void toVanillaMode(boolean synchronize) {
		if (this.playerMode != PlayerMode.VANILLA) {
			ClientEngine.getInstance().renderEngine.downSlideSkillUI();
			
			if (ClientConfig.authSwitchCamera) {
				this.minecraft.options.setCameraType(CameraType.FIRST_PERSON);
			}
			
			if (synchronize) {
				EpicFightNetworkManager.sendToServer(new CPChangePlayerMode(PlayerMode.VANILLA));
			}
		}
		
		super.toVanillaMode(synchronize);
	}
	
	@Override
	public void toEpicFightMode(boolean synchronize) {
		if (this.playerMode != PlayerMode.EPICFIGHT) {
			ClientEngine.getInstance().renderEngine.upSlideSkillUI();
			
			if (ClientConfig.authSwitchCamera) {
				this.minecraft.options.setCameraType(CameraType.THIRD_PERSON_BACK);
			}
			
			if (synchronize) {
				EpicFightNetworkManager.sendToServer(new CPChangePlayerMode(PlayerMode.EPICFIGHT));
			}
		}
		
		super.toEpicFightMode(synchronize);
	}
	
	@Override
	public boolean isFirstPerson() {
		return this.minecraft.options.getCameraType() == CameraType.FIRST_PERSON;
	}
	
	@Override
	public boolean shouldBlockMoving() {
		return InputManager.isActionActive(EpicFightInputActions.MOVE_BACKWARD) || InputManager.isActionActive(EpicFightInputActions.SNEAK);
	}
	
	@Override
	public boolean shouldMoveOnCurrentSide(ActionAnimation actionAnimation) {
		if (!this.isLogicalClient()) {
			return false;
		}
		
		return actionAnimation.shouldPlayerMove(this);
	}
	
	public float getStaminaO() {
		return this.staminaO;
	}
	
	public int getPrevChargingAmount() {
		return this.prevChargingAmount;
	}

	public boolean isTargetLockedOn() {
		return this.lockingOnTarget;
	}
	
	public void setLockOn(boolean flag) {
		if (this.lockingOnTarget == flag) {
			return;
		}
		
		if (flag && this.rayTarget == null) {
			this.lockingOnTarget = false;
		} else {
			this.lockingOnTarget = flag;
			RenderEngine renderEngine = ClientEngine.getInstance().renderEngine;
			
			if (!renderEngine.isTPSMode()) {
				if (!this.lockingOnTarget) {
					this.original.setXRot(renderEngine.getCameraXRot());
				} else {
					renderEngine.setCameraRotations(this.original.getXRot(), this.original.getYRot(), true);
				}
			}
		}
	}
	
	public void toggleLockOn() {
		this.setLockOn(!this.lockingOnTarget);
	}
	
	public FirstPersonLayer getFirstPersonLayer() {
		return this.firstPersonLayer;
	}
	
	public AnimationSubFileReader.PovSettings getPovSettings() {
		return this.povSettings;
	}
	
	public boolean hasCameraAnimation() {
		return this.povSettings != null && this.povSettings.cameraTransform() != null;
	}
	
	@Override
	public void setStamina(float value) {
		EpicFightNetworkManager.sendToServer(new CPSetStamina(value, true));
	}
	
	@Override
	public void setModelYRot(float amount, boolean sendPacket) {
		super.setModelYRot(amount, sendPacket);
		
		if (sendPacket) {
			EpicFightNetworkManager.sendToServer(new CPModifyEntityModelYRot(amount));
		}
	}
	
	public float getModelYRot() {
		return this.modelYRot;
	}
	
	public void setModelYRotInGui(float rotDeg) {
		this.useModelYRot = true;
		this.modelYRot = rotDeg;
	}
	
	public void disableModelYRotInGui(float originalDeg) {
		this.useModelYRot = false;
		this.modelYRot = originalDeg;
	}
	
	public void fixFpvRotation(float xRot, float yRot) {
		this.fpvXRot = Mth.wrapDegrees(xRot);
		this.fpvXRotO = Mth.wrapDegrees(this.original.getXRot());
		this.fpvYRot = Mth.wrapDegrees(yRot);
		this.fpvYRotO = Mth.wrapDegrees(this.original.getYRot());
		this.fpvLerpTick = 5;
	}
	
	public float getLerpedFpvXRot(float partialTicks) {
		float delta = ((this.fpvLerpTick) / 5.0F + (1.0F - partialTicks) * (1.0F / 5.0F));
		return this.isLerpingFpv() ? Mth.rotLerp(delta, this.fpvXRot, this.fpvXRotO) : this.original.getXRot();
	}
	
	public float getLerpedFpvYRot(float partialTicks) {
		float delta = ((this.fpvLerpTick) / 5.0F + (1.0F - partialTicks) * (1.0F / 5.0F));
		return this.isLerpingFpv() ? Mth.rotLerp(delta, this.fpvYRot, this.fpvYRotO) : this.original.getYRot();
	}
	
	public boolean isLerpingFpv() {
		return this.fpvLerpTick > -1;
	}
	
	public Vec3 getRelativeMoveVector(Vec3 relative, float magnitude) {
		return Entity.getInputVector(relative, magnitude, ClientEngine.getInstance().renderEngine.isTPSMode() && !this.lockingOnTarget ? ClientEngine.getInstance().renderEngine.getCameraYRot() : this.minecraft.player.getYRot());
	}
	
	public HitResult getCameraBasedHitResult() {
		return this.cameraBasedHitResult;
	}
	
	@Override
	public void disableModelYRot(boolean sendPacket) {
		super.disableModelYRot(sendPacket);
		
		if (sendPacket) {
			EpicFightNetworkManager.sendToServer(new CPModifyEntityModelYRot());
		}
	}
	
	@Override
	public double checkXTurn(double xRot) {
		if (xRot == 0.0D) {
			return xRot;
		}
		
		if (ClientConfig.enablePovAction && this.minecraft.options.getCameraType().isFirstPerson() && this.isEpicFightMode() && !this.getFirstPersonLayer().isOff()) {
			ViewLimit viewLimit = this.getPovSettings().viewLimit();
			
			if (viewLimit != null) {
				float xRotDest = this.original.getXRot() + (float)xRot * 0.15F;
				
				if (xRotDest <= viewLimit.xRotMin() || xRotDest >= viewLimit.xRotMax()) {
					return 0.0D;
				}
			}
		}
		
		return xRot;
	}
	
	@Override
	public double checkYTurn(double yRot) {
		if (yRot == 0.0D) {
			return yRot;
		}
		
		if (ClientConfig.enablePovAction && this.minecraft.options.getCameraType().isFirstPerson() && this.isEpicFightMode() && !this.getFirstPersonLayer().isOff()) {
			ViewLimit viewLimit = this.getPovSettings().viewLimit();
			
			if (viewLimit != null) {
				float yCamera = Mth.wrapDegrees(this.original.getYRot());
				float yBody = MathUtils.findNearestRotation(yCamera, this.getYRot());
				float yRotDest = yCamera + (float)yRot * 0.15F;
				float yRotClamped = Mth.clamp(yRotDest, yBody + viewLimit.yRotMin(), yBody + viewLimit.yRotMax());
				
				if (yRotDest != yRotClamped) {
					return 0.0D;
				}
			}
		}
		
		return yRot;
	}
	
	@Override
	public void beginAction(ActionAnimation animation) {
		if (ClientEngine.getInstance().renderEngine.isTPSMode()) {
			if (this.rayTarget != null && animation instanceof AttackAnimation) {
				this.original.setYRot((float)MathUtils.getYRotOfVector(this.rayTarget.getEyePosition().subtract(this.original.getEyePosition())));
			} else {
				this.original.setYRot(ClientEngine.getInstance().renderEngine.getCameraYRot());
			}
		}
		
		if (!this.useModelYRot || animation.getProperty(ActionAnimationProperty.SYNC_CAMERA).orElse(false)) {
			this.modelYRot = this.original.getYRot();
		}
		
		if (this.rayTarget != null && this.lockingOnTarget && !this.rayTarget.isRemoved()) {
			Vec3 playerPosition = this.original.position();
			Vec3 targetPosition = this.rayTarget.position();
			Vec3 toTarget = targetPosition.subtract(playerPosition);
			this.original.setYRot((float)MathUtils.getYRotOfVector(toTarget));
		}
	}
	
	/**
	 * Play an animation after the current animation is finished
	 * @param animation
	 */
	@Override
	public void reserveAnimation(AssetAccessor<? extends StaticAnimation> animation) {
		this.animator.reserveAnimation(animation);
		EpicFightNetworkManager.sendToServer(new CPAnimatorControl(AnimatorControlPacket.Action.RESERVE, animation, 0.0F, false, false, false));
	}
	
	/**
	 * Play an animation without convert time
	 * @param animation
	 */
	@Override
	public void playAnimationInstantly(AssetAccessor<? extends StaticAnimation> animation) {
		this.animator.playAnimationInstantly(animation);
		EpicFightNetworkManager.sendToServer(new CPAnimatorControl(AnimatorControlPacket.Action.PLAY_INSTANTLY, animation, 0.0F, false, false, false));
	}
	
	/**
	 * Play a shooting animation to end aim pose
	 * This method doesn't send packet from client to server
	 */
	@Override
	public void playShootingAnimation() {
		this.animator.playShootingAnimation();
		EpicFightNetworkManager.sendToServer(new CPAnimatorControl(AnimatorControlPacket.Action.SHOT, -1, 0.0F, false, true, false));
	}
	
	/**
	 * Stop playing an animation
	 * @param animation
	 * @param transitionTimeModifier
	 */
	@Override
	public void stopPlaying(AssetAccessor<? extends StaticAnimation> animation) {
		this.animator.stopPlaying(animation);
		EpicFightNetworkManager.sendToServer(new CPAnimatorControl(AnimatorControlPacket.Action.STOP, animation, -1.0F, false, false, false));
	}
	
	/**
	 * Play an animation ensuring synchronization between client-server
	 * Plays animation when getting response from server if it called in client side.
	 * Do not call this in client side for non-player entities.
	 * 
	 * @param animation
	 * @param transitionTimeModifier
	 */
	@Override
	public void playAnimationSynchronized(AssetAccessor<? extends StaticAnimation> animation, float transitionTimeModifier) {
		EpicFightNetworkManager.sendToServer(new CPAnimatorControl(AnimatorControlPacket.Action.PLAY, animation, transitionTimeModifier, false, false, true));
	}
	
	/**
	 * Play an animation only in client side, including all clients tracking this entity
	 * @param animation
	 * @param convertTimeModifier
	 */
	@Override
	public void playAnimationInClientSide(AssetAccessor<? extends StaticAnimation> animation, float transitionTimeModifier) {
		this.animator.playAnimation(animation, transitionTimeModifier);
		EpicFightNetworkManager.sendToServer(new CPAnimatorControl(AnimatorControlPacket.Action.PLAY, animation, transitionTimeModifier, false, true, false));
	}
	
	/**
	 * Pause an animator until it receives a proper order
	 * @param action SOFT_PAUSE: resume when next animation plays
	 * 				 HARD_PAUSE: resume when hard pause is set false
	 * @param pause
	 **/
	@Override
	public void pauseAnimator(AnimatorControlPacket.Action action, boolean pause) {
		super.pauseAnimator(action, pause);
		EpicFightNetworkManager.sendToServer(new CPAnimatorControl(action, -1, 0.0F, pause, false, false));
	}
	
	@Override
	public void openSkillBook(ItemStack itemstack, InteractionHand hand) {
		if (itemstack.hasTag() && itemstack.getTag().contains("skill")) {
			Minecraft.getInstance().setScreen(new SkillBookScreen(this.original, itemstack, hand));
		}
	}
	
	@Override
	public void resetHolding() {
		if (this.holdingSkill != null) {
			ClientEngine.getInstance().controlEngine.releaseAllServedKeys();
		}
		
		super.resetHolding();
	}
	
	@Override
	public void updateHeldItem(CapabilityItem mainHandCap, CapabilityItem offHandCap) {
		super.updateHeldItem(mainHandCap, offHandCap);
		
		if (!ClientConfig.preferenceWork.checkHitResult()) {
			if (ClientConfig.combatPreferredItems.contains(this.original.getMainHandItem().getItem())) {
				this.toEpicFightMode(true); 
			} else if (ClientConfig.miningPreferredItems.contains(this.original.getMainHandItem().getItem())) {
				this.toVanillaMode(true);
			}
		}
	}
	
	/**
	 * Judge the next behavior depending on player's item preference and where he's looking at
	 * @return true if the next action is swing a weapon, false if the next action is breaking a block
	 */
	public boolean canPlayAttackAnimation() {
		if (this.getPlayerMode() == PlayerPatch.PlayerMode.VANILLA) {
			return false;
		}
		
		HitResult hitResult = 
			(ClientEngine.getInstance().renderEngine.isTPSMode() && this.cameraBasedHitResult != null && this.cameraBasedHitResult.getLocation().distanceToSqr(this.original.getEyePosition()) < this.original.getBlockReach() * this.original.getBlockReach())
				? this.cameraBasedHitResult : this.minecraft.hitResult;
		
		if (hitResult == null) {
			return true;
		}
		
		if (RenderEngine.hitResultEquals(this.minecraft.hitResult, HitResult.Type.ENTITY)) {
			Entity hitEntity = ((EntityHitResult)hitResult).getEntity();
			
			if (!(hitEntity instanceof LivingEntity) && !(hitEntity instanceof PartEntity)) {
				return false;
			}
		}
		
		if (this.lockingOnTarget) {
			return true;
		}
		
		if (ClientConfig.preferenceWork.checkHitResult()) {
			if (ClientConfig.combatPreferredItems.contains(this.original.getMainHandItem().getItem())) {
				if (RenderEngine.hitResultEquals(this.minecraft.hitResult, HitResult.Type.BLOCK) && this.minecraft.level != null) {
					BlockPos bp = ((BlockHitResult) this.minecraft.hitResult).getBlockPos();
					BlockState bs = this.minecraft.level.getBlockState(bp);
					return !this.original.getMainHandItem().getItem().canAttackBlock(bs, this.original.level(), bp, this.original) || !this.original.getMainHandItem().isCorrectToolForDrops(bs);
				}
			} else {
				return RenderEngine.hitResultNotEquals(this.minecraft.hitResult, HitResult.Type.BLOCK);
			}
			
			return true;
		} else {
			return this.getPlayerMode() == PlayerPatch.PlayerMode.EPICFIGHT;
		}
	}
	
	public boolean shouldHighlightTarget(Entity entity) {
		if (!ClientConfig.enableTargetEntityGuide) {
			return false;
		}
		
		if (entity == this.rayTarget) {
			if (!EpicFightCapabilities.getUnparameterizedEntityPatch(entity, EntityPatch.class).map(entitypatch -> entitypatch.isOutlineVisible(this)).orElse(false)) {
				return false;
			}
			
			if (ClientConfig.preferenceWork.checkHitResult()) {
				if (ClientConfig.combatPreferredItems.contains(this.original.getMainHandItem().getItem())) {
					if (RenderEngine.hitResultEquals(this.minecraft.hitResult, HitResult.Type.BLOCK) && this.minecraft.level != null) {
						BlockPos bp = ((BlockHitResult)this.minecraft.hitResult).getBlockPos();
						BlockState bs = this.minecraft.level.getBlockState(bp);
						return !this.original.getMainHandItem().getItem().canAttackBlock(bs, this.original.level(), bp, this.original) || !this.original.getMainHandItem().isCorrectToolForDrops(bs);
					}
					
					return true;
				} else {
					return this.minecraft.crosshairPickEntity == entity;
				}
			} else {
				return this.getPlayerMode() == PlayerPatch.PlayerMode.EPICFIGHT;
			}
		}
		
		return false;
	}
	
	@OnlyIn(Dist.CLIENT)
	public class FirstPersonLayer extends Layer {
		private TransformSheet linkCameraTransform = new TransformSheet(List.of(new Keyframe(0.0F, JointTransform.empty()), new Keyframe(Float.MAX_VALUE, JointTransform.empty())));
		
		public FirstPersonLayer() {
			super(null);
		}
		
		public void playAnimation(AssetAccessor<? extends StaticAnimation> nextFirstPersonAnimation, AssetAccessor<? extends StaticAnimation> originalAnimation, LivingEntityPatch<?> entitypatch, float transitionTimeModifier) {
			Optional<PovSettings> povSettings = originalAnimation.get().getProperty(ClientAnimationProperties.POV_SETTINGS);
			
			boolean hasPrevCameraAnimation = LocalPlayerPatch.this.povSettings != null && LocalPlayerPatch.this.povSettings.cameraTransform() != null;
			boolean hasNextCameraAnimation = povSettings.isPresent() && povSettings.get().cameraTransform() != null;
			
			// Activate pov animation
			if (hasPrevCameraAnimation || hasNextCameraAnimation) {
				if (hasPrevCameraAnimation) {
					this.linkCameraTransform.getKeyframes()[0].transform().copyFrom(LocalPlayerPatch.this.povSettings.cameraTransform().getInterpolatedTransform(this.animationPlayer.getElapsedTime()));
				} else {
					this.linkCameraTransform.getKeyframes()[0].transform().copyFrom(JointTransform.empty());
				}
				
				if (hasNextCameraAnimation) {
					this.linkCameraTransform.getKeyframes()[1].transform().copyFrom(povSettings.get().cameraTransform().getKeyframes()[0].transform());
				} else {
					this.linkCameraTransform.getKeyframes()[1].transform().clearTransform();
				}
				
				this.linkCameraTransform.getKeyframes()[1].setTime(nextFirstPersonAnimation.get().getTransitionTime());
			}
			
			super.playAnimation(nextFirstPersonAnimation, entitypatch, transitionTimeModifier);
		}
		
		public void off() {
			// Off camera animation
			if (LocalPlayerPatch.this.povSettings != null && LocalPlayerPatch.this.povSettings.cameraTransform() != null) {
				this.linkCameraTransform.getKeyframes()[0].transform().copyFrom(LocalPlayerPatch.this.povSettings.cameraTransform().getInterpolatedTransform(this.animationPlayer.getElapsedTime()));
				this.linkCameraTransform.getKeyframes()[1].transform().copyFrom(JointTransform.empty());
				this.linkCameraTransform.getKeyframes()[1].setTime(EpicFightSharedConstants.GENERAL_ANIMATION_TRANSITION_TIME);
			}
			
			super.off(LocalPlayerPatch.this);
		}
		
		@Override
		protected Pose getCurrentPose(LivingEntityPatch<?> entitypatch) {
			return this.animationPlayer.isEmpty() ? super.getCurrentPose(entitypatch) : this.animationPlayer.getCurrentPose(entitypatch, 0.0F);
		}
		
		public TransformSheet getLinkCameraTransform() {
			return this.linkCameraTransform;
		}
	}
}