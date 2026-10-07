package com.nasilk.createcrystallized.particle;

import com.nasilk.createcrystallized.CreateCrystallized;
import com.simibubi.create.foundation.data.CreateRegistrate;
import com.tterrag.registrate.util.entry.RegistryEntry;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.Registries;

public class ModParticles {
    public static final CreateRegistrate REGISTRATE = CreateCrystallized.getRegistrate();

    public static final RegistryEntry<ParticleType<?>, SimpleParticleType> DENSITE_PARTICLES = registerParticle(
        "densite_particles"
    );

    public static final RegistryEntry<ParticleType<?>, SimpleParticleType> DENSITE_WELL_PARTICLES = registerParticle(
        "densite_well_particles"
    );

    public static final RegistryEntry<ParticleType<?>, SimpleParticleType> DENSITE_CORE_PARTICLES = registerParticle(
        "densite_core_particles"
    );

    public static final RegistryEntry<ParticleType<?>, SimpleParticleType> PROPULSITE_PARTICLES = registerParticle(
        "propulsite_particles"
    );

    public static final RegistryEntry<ParticleType<?>, SimpleParticleType> PROPULSITE_THRUSTER_FIRING_PARTICLES = registerParticle(
        "propulsite_thruster_firing_particles"
    );

    public static final RegistryEntry<ParticleType<?>, SimpleParticleType> PROPULSITE_THRUSTER_CHARGING_PARTICLES = registerParticle(
        "propulsite_thruster_charging_particles"
    );

    public static final RegistryEntry<ParticleType<?>, SimpleParticleType> OSCILLITE_CANNON_CHARGING_PARTICLES = registerParticle(
        "oscillite_cannon_charging_particles"
    );

    public static final RegistryEntry<ParticleType<?>, SimpleParticleType> OSCILLITE_CANNON_FIRING_PARTICLES = registerParticle(
        "oscillite_cannon_firing_particles"
    );

    private static RegistryEntry<ParticleType<?>, SimpleParticleType> registerParticle(String name) {
        return REGISTRATE.simple(
            name,
            Registries.PARTICLE_TYPE,
            () -> new SimpleParticleType(true)
        );
    }

    public static void register() {
        CreateCrystallized.LOGGER.info("Particles registered");
    }
}
