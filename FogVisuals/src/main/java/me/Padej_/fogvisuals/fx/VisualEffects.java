package me.Padej_.fogvisuals.fx;

import me.Padej_.fogvisuals.config.FogConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.ParticleTypes;

/**
 * Client-side particle visuals in the spirit of visual clients:
 * a trail behind the player and an aura orbiting around them.
 */
public final class VisualEffects {
    private static int tick = 0;

    private VisualEffects() {
    }

    public static void endClientTick(Minecraft client) {
        tick++;
        FogConfig cfg = FogConfig.get();
        if (client.player == null || client.level == null || client.isPaused()) {
            return;
        }

        if (cfg.trail) {
            // density 0..100 -> spawn every 1..6 ticks
            int interval = Math.max(1, 6 - cfg.trailDensity / 20);
            if (tick % interval == 0) {
                double x = client.player.getX();
                double y = client.player.getY() + 0.15;
                double z = client.player.getZ();
                double spread = 0.06;
                client.level.addParticle(ParticleTypes.END_ROD,
                        x + (Math.random() - 0.5) * spread,
                        y + Math.random() * 0.4,
                        z + (Math.random() - 0.5) * spread,
                        0.0, 0.01, 0.0);
            }
        }

        if (cfg.aura) {
            int interval = Math.max(2, 8 - cfg.auraDensity / 18);
            if (tick % interval == 0) {
                double x = client.player.getX();
                double y = client.player.getY() + 1.0;
                double z = client.player.getZ();
                // orbiting ring, phase shifts over time
                double phase = tick * 0.25;
                for (int i = 0; i < 2; i++) {
                    double a = phase + i * Math.PI;
                    client.level.addParticle(ParticleTypes.ENCHANT,
                            x + Math.cos(a) * 0.9,
                            y + Math.sin(tick * 0.12) * 0.35,
                            z + Math.sin(a) * 0.9,
                            0.0, 0.02, 0.0);
                }
            }
        }
    }
}
