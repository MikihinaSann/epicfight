package yesman.epicfight.api.client.event.instances;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import yesman.epicfight.api.client.camera.EpicFightCameraAPI;
import yesman.epicfight.api.event.EventInstance;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;

public class ItemUsedInDecoupledCamera extends EventInstance {
    private EpicFightCameraAPI cameraApi;
    private Player player;
    private PlayerPatch<?> playerpatch;
    private ItemStack itemstack;
    private InteractionHand hand;

    public ItemUsedInDecoupledCamera(EpicFightCameraAPI cameraApi, Player player, PlayerPatch<?> playerpatch, ItemStack itemstack, InteractionHand hand) {
        this.cameraApi = cameraApi;
        this.player = player;
        this.playerpatch = playerpatch;
        this.itemstack = itemstack;
        this.hand = hand;
    }

    public EpicFightCameraAPI getCameraAPI() {
        return this.cameraApi;
    }

    public Player getPlayer() {
        return this.player;
    }

    public PlayerPatch<?> getPlayerPatch() {
        return this.playerpatch;
    }

    public ItemStack getItemStack() {
        return this.itemstack;
    }

    public InteractionHand getInteractionHand() {
        return this.hand;
    }
}
