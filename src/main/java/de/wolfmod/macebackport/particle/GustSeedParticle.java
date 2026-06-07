package de.wolfmod.macebackport.particle;

import de.wolfmod.macebackport.registry.ModParticles;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.NoRenderParticle;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.core.particles.SimpleParticleType;

public final class GustSeedParticle extends NoRenderParticle {
    private final double scale;
    private final int tickDelayInBetween;

    private GustSeedParticle(ClientLevel level, double x, double y, double z, double scale, int lifetime, int tickDelayInBetween) {
        super(level, x, y, z);
        this.scale = scale;
        this.lifetime = lifetime;
        this.tickDelayInBetween = tickDelayInBetween;
    }

    @Override
    public void tick() {
        if (this.age % (this.tickDelayInBetween + 1) == 0) {
            for (int i = 0; i < 3; i++) {
                double x = this.x + ((this.random.nextDouble() - this.random.nextDouble()) * this.scale);
                double y = this.y + ((this.random.nextDouble() - this.random.nextDouble()) * this.scale);
                double z = this.z + ((this.random.nextDouble() - this.random.nextDouble()) * this.scale);
                this.level.addParticle(ModParticles.GUST.get(), x, y, z, (double) this.age / (double) this.lifetime, 0.0D, 0.0D);
            }
        }

        if (++this.age == this.lifetime) {
            this.remove();
        }
    }

    public static final class Provider implements ParticleProvider<SimpleParticleType> {
        private final double scale;
        private final int lifetime;
        private final int tickDelayInBetween;

        public Provider(double scale, int lifetime, int tickDelayInBetween) {
            this.scale = scale;
            this.lifetime = lifetime;
            this.tickDelayInBetween = tickDelayInBetween;
        }

        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z,
                                       double xSpeed, double ySpeed, double zSpeed) {
            return new GustSeedParticle(level, x, y, z, this.scale, this.lifetime, this.tickDelayInBetween);
        }
    }
}
