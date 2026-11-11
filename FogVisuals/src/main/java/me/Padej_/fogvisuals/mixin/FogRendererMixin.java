package me.Padej_.fogvisuals.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import me.Padej_.fogvisuals.fog.FogManager;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.fog.FogData;
import net.minecraft.client.renderer.fog.FogRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Runs after the vanilla fog has been computed for the frame and lets FogVisuals
 * rewrite the final {@link FogData} before it is uploaded to the shader.
 */
@Mixin(FogRenderer.class)
public abstract class FogRendererMixin {
    @ModifyReturnValue(
            method = "setupFog(Lnet/minecraft/client/Camera;ILnet/minecraft/client/DeltaTracker;FLnet/minecraft/client/multiplayer/ClientLevel;)Lnet/minecraft/client/renderer/fog/FogData;",
            at = @At("RETURN")
    )
    private FogData fogvisuals$applyCustomFog(FogData original, Camera camera, int renderDistance,
                                              DeltaTracker deltaTracker, float partialTicks, ClientLevel level) {
        FogManager.applyFog(original, camera, level, partialTicks);
        return original;
    }
}
