package com.nasilk.createfluidsandfixins.particle;

import com.nasilk.createfluidsandfixins.CreateFluidsAndFixins;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import java.util.function.Supplier;

public class ModParticles {
    public static final DeferredRegister<ParticleType<?>> PARTICLE_TYPES =
        DeferredRegister.create(BuiltInRegistries.PARTICLE_TYPE, CreateFluidsAndFixins.MOD_ID);

    public static final Supplier<SimpleParticleType> DENSITE_PARTICLES = PARTICLE_TYPES.register(
        "densite_particles",
        () -> new SimpleParticleType(true)
    );

    public static final Supplier<SimpleParticleType> PROPULSITE_PARTICLES = PARTICLE_TYPES.register(
        "propulsite_particles",
        () -> new SimpleParticleType(true)
    );

    public static final Supplier<SimpleParticleType> PROPULSITE_THRUSTER_FIRING_PARTICLES = PARTICLE_TYPES.register(
        "propulsite_thruster_firing_particles",
        () -> new SimpleParticleType(true)
    );

    public static final Supplier<SimpleParticleType> PROPULSITE_THRUSTER_CHARGING_PARTICLES = PARTICLE_TYPES.register(
        "propulsite_thruster_charging_particles",
        () -> new SimpleParticleType(true)
    );

    public static void register(IEventBus eventBus) {
        PARTICLE_TYPES.register(eventBus);
    }
}
