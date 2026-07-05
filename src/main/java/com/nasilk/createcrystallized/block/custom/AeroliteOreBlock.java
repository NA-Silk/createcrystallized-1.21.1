package com.nasilk.createcrystallized.block.custom;

import com.nasilk.createcrystallized.event.TaskEventScheduler;
import com.simibubi.create.content.contraptions.AssemblyException;
import dev.ryanhcode.sable.Sable;
import dev.ryanhcode.sable.api.physics.handle.RigidBodyHandle;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import dev.simulated_team.simulated.util.SimAssemblyHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import org.joml.Vector3d;

public class AeroliteOreBlock extends Block {
    public static final BooleanProperty DISARMED = BlockStateProperties.DISARMED;
    private static final double THRUST = 10.0d;
    private static final int THRUST_DELAY = 2;

    public AeroliteOreBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(DISARMED, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(DISARMED);
    }

    @Override
    protected void attack(BlockState state, Level level, BlockPos pos, Player player) {
        if (!state.getValue(DISARMED)
            && level instanceof ServerLevel serverLevel
            && Sable.HELPER.getContaining(serverLevel, pos) == null
        ) {
            serverLevel.setBlockAndUpdate(pos, state.setValue(DISARMED, true));
            move(serverLevel, pos, player);
        }
        super.attack(state, level, pos, player);
    }

    private void move(ServerLevel serverLevel, BlockPos pos, Player player) {
        // Compute thrust (unnormalized)
        Vector3d thrust =
            new Vector3d(pos.getX() + 0.5d, pos.getY() + 0.5d, pos.getZ() + 0.5d) // Block position
            .sub(player.getX(), player.getY(), player.getZ()); // Player position

        // Handle reflections for each axis
        if (isColliding(serverLevel, pos.east()) && isColliding(serverLevel, pos.west())) {
            thrust.x = 0.0d;
        } else if ((thrust.x > 0.0d && isColliding(serverLevel, pos.east()))
            || (thrust.x < 0.0d && isColliding(serverLevel, pos.west()))
        ) thrust.x = -thrust.x;

        if (isColliding(serverLevel, pos.above()) && isColliding(serverLevel, pos.below())) {
            thrust.y = 0.0d;
        } else if ((thrust.y > 0.0d && isColliding(serverLevel, pos.above()))
            || (thrust.y < 0.0d && isColliding(serverLevel, pos.below()))
        ) thrust.y = -thrust.y;

        if (isColliding(serverLevel, pos.south()) && isColliding(serverLevel, pos.north())) {
            thrust.z = 0.0d;
        } else if ((thrust.z > 0.0d && isColliding(serverLevel, pos.south()))
            || (thrust.z < 0.0d && isColliding(serverLevel, pos.north()))
        ) thrust.z = -thrust.z;

        // Safely normalize and scale thrust
        if (thrust.lengthSquared() < 1e-3d) thrust.set(0.0d, 1.0d, 0.0d);
        thrust.normalize().mul(THRUST);

        // Convert to sublevel
        SimAssemblyHelper.AssemblyResult result;
        try {
            result = SimAssemblyHelper.assembleFromSingleBlock(serverLevel, pos, pos, true, true);
        } catch (final AssemblyException ignoredError) {
            return;
        }
        if (!(result.subLevel() instanceof  ServerSubLevel subLevel)) return;

        // Apply thrust on a later tick
        TaskEventScheduler.schedule(
            serverLevel.getServer(),
            THRUST_DELAY,
            () -> {
                RigidBodyHandle handle = RigidBodyHandle.of(subLevel);
                if (handle.isValid()) handle.addLinearAndAngularVelocity(thrust, new Vector3d());
            }
        );

        // Play effects
        serverLevel.sendParticles(
            ParticleTypes.PORTAL,
            pos.getX() + 0.5,
            pos.getY() + 0.5,
            pos.getZ() + 0.5,
            8,0.5,0.5,0.5,1.0
        );
        serverLevel.playSound(
            null,
            pos,
            SoundEvents.ENDERMAN_TELEPORT,
            SoundSource.BLOCKS,
            1.0f,1.0f
        );
    }

    private boolean isColliding(ServerLevel serverLevel, BlockPos pos) {
        return !serverLevel.getBlockState(pos).getCollisionShape(serverLevel, pos).isEmpty();
    }
}
