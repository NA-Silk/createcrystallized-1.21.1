package com.nasilk.createcrystallized.block.custom;

import com.simibubi.create.content.contraptions.AssemblyException;
import dev.ryanhcode.sable.Sable;
import dev.ryanhcode.sable.api.physics.handle.RigidBodyHandle;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import dev.simulated_team.simulated.util.SimAssemblyHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.TickTask;
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
    private static final int THRUST_DELAY = 10;

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
        // Compute thrust
        Vector3d thrust =
            new Vector3d(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) // Block position
            .sub(player.getX(), player.getY(), player.getZ()) // Player position
            .normalize(); // Reduce to unit vector

        // Adjust for recoil and scale magnitude TODO: Make a better y-transition
        Direction collisionDirection = Direction.getNearest(thrust.x, thrust.y, thrust.z);
        BlockPos collisionPos = pos.relative(collisionDirection);
        if (serverLevel.getBlockState(collisionPos).isSolidRender(serverLevel, collisionPos)) {
            switch (collisionDirection.getAxis()) {
                case X -> thrust.x = -thrust.x;
                case Y -> thrust.y = -thrust.y;
                case Z -> thrust.z = -thrust.z;
            }
        }
        if (serverLevel.getBlockState(pos.below()).isSolidRender(serverLevel, pos.below()) && thrust.y < 0.0d) {
            thrust.set(0.0d, 1.0d, 0.0d);
        }
        thrust.mul(THRUST);

        // Convert to sublevel
        SimAssemblyHelper.AssemblyResult result;
        try {
            result = SimAssemblyHelper.assembleFromSingleBlock(serverLevel, pos, pos, true, true);
        } catch (final AssemblyException ignoredError) {
            return;
        }
        if (!(result.subLevel() instanceof  ServerSubLevel subLevel)) return;

        // Apply thrust on a later tick
        MinecraftServer server = serverLevel.getServer();
        server.tell(new TickTask(
            server.getTickCount() + THRUST_DELAY,
            () -> {
                RigidBodyHandle handle = RigidBodyHandle.of(subLevel);
                if (handle.isValid()) handle.addLinearAndAngularVelocity(thrust, new Vector3d());
            }
        ));

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
            1.0F,1.0F
        );
    }
}
