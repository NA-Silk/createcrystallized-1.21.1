package com.nasilk.createcrystallized.ponder.custom;

import dev.eriksonn.aeronautics.config.AeroConfig;
import dev.eriksonn.aeronautics.index.AeroBlocks;
import dev.eriksonn.aeronautics.service.AeroLevititeService;
import dev.simulated_team.simulated.ponder.instructions.OffsetBreakParticlesInstruction;
import java.util.function.Consumer;
import net.createmod.catnip.math.Pointing;
import net.createmod.ponder.api.ParticleEmitter;
import net.createmod.ponder.api.PonderPalette;
import net.createmod.ponder.api.element.ElementLink;
import net.createmod.ponder.api.element.WorldSectionElement;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.createmod.ponder.api.scene.Selection;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class FluidScenes {
    public FluidScenes() {}

    // TODO remove plagiarism
    @SuppressWarnings("unchecked")
    public static void densiteEmulsion(SceneBuilder scene, SceneBuildingUtil util) {
        scene.title("levitite_blend", "Crystallizing Levitite Blend into Levitite");
        scene.configureBasePlate(1, 1, 5);

        scene.world().showSection(util.select().fromTo(1, 0, 1, 5, 0, 5), Direction.UP);
        scene.idle(10);
        ElementLink<WorldSectionElement> link1 = scene.world().showIndependentSection(util.select().position(3, 3, 3), Direction.DOWN);
        scene.world().moveSection(link1, new Vec3(0.0d, -2.0d, 0.0d), 0);
        scene.idle(20);
        scene.overlay().showText(60).pointAt(util.vector().topOf(3, 1, 3)).attachKeyFrame().placeNearTarget().text("Levitite Blend can be crystallized into Levitite");
        scene.idle(80);
        scene.overlay().showText(50).pointAt(util.vector().topOf(3, 1, 3)).attachKeyFrame().placeNearTarget().text("This process requires heat");
        scene.idle(20);
        scene.overlay().showControls(util.vector().topOf(3, 1, 3), Pointing.DOWN, 20).withItem(new ItemStack(Items.FLINT_AND_STEEL));
        scene.idle(5);
        scene.effects().emitParticles(new Vec3(3.5d, 1.5d, 3.5d), withinBlockSpace(ParticleTypes.SMOKE, 1.5f), 15.0f, 2);
        scene.idle(50);
        scene.world().replaceBlocks(util.select().position(3, 3, 3), AeroBlocks.LEVITITE.getDefaultState(), false);
        scene.effects().emitParticles(new Vec3(3.5d, 1.5d, 3.5d), withinBlockSpace(ParticleTypes.FLAME, 1.5f), 15.0f, 2);
        scene.idle(25);
        scene.overlay().showText(60).pointAt(util.vector().centerOf(3, 1, 3)).attachKeyFrame().placeNearTarget().colored(PonderPalette.RED).text("Once catalyzed, the Levitite cannot be recollected");
        scene.idle(20);

        for(int i = 0; i < 4; ++i) {
            scene.world().incrementBlockBreakingProgress(new BlockPos(3, 3, 3));
            scene.world().incrementBlockBreakingProgress(new BlockPos(3, 3, 3));
            scene.idle(5);
        }

        scene.world().setBlock(new BlockPos(3, 3, 3), Blocks.AIR.defaultBlockState(), false);
        scene.addInstruction(new OffsetBreakParticlesInstruction(AABB.unitCubeFromLowerCorner(new Vec3(3.0d, 1.0d, 3.0d)), AeroBlocks.LEVITITE.getDefaultState()));
        scene.idle(50);
        ElementLink<WorldSectionElement>[] link2 = new ElementLink[3];

        for(int i = 0; i < 3; ++i) {
            link2[i] = scene.world().showIndependentSection(util.select().position(5 - 2 * i, 2, 3), Direction.DOWN);
            scene.world().moveSection(link2[i], new Vec3(0.0d, -1.0d, 0.0d), 0);
            scene.idle(5);
        }

        scene.idle(5);
        ElementLink<WorldSectionElement>[] link3 = new ElementLink[3];
        Selection[] selections = new Selection[]{util.select().fromTo(1, 3, 3, 1, 4, 4), util.select().position(4, 3, 4), util.select().position(5, 3, 4)};

        for(int i = 0; i < 3; ++i) {
            link3[i] = scene.world().showIndependentSection(selections[i], Direction.DOWN);
            scene.world().moveSection(link3[i], new Vec3(((i == 1 ? -1.0d : 0.0d) + (1 - i) * 4.0d), -2.0d, 0.0d), 0);
            scene.idle(5);
        }

        scene.idle(10);
        scene.overlay().showText(70).pointAt(util.vector().topOf(3, 1, 4)).attachKeyFrame().placeNearTarget().text("Nearby heat sources can also start the crystallization process");

        for(int i = 0; i < 3; ++i) {
            scene.effects().emitParticles(new Vec3(5.5d - i * 2.0d, 1.5d, 3.5d), withinBlockSpace(ParticleTypes.SMOKE, 1.5f), 15.0f, 2);
            scene.idle(5);
        }

        scene.idle(60);

        for(int i = 0; i < 3; ++i) {
            scene.effects().emitParticles(new Vec3(5.5d - i * 2.0d, 1.5d, 3.5d), withinBlockSpace(ParticleTypes.FLAME, 1.5f), 15.0f, 2);
            scene.world().replaceBlocks(util.select().position(5 - i * 2, 2, 3), AeroBlocks.LEVITITE.getDefaultState(), false);
            scene.idle(5);
        }

        scene.idle(20);

        for(int i = 0; i < 3; ++i) {
            scene.world().hideIndependentSection(link2[i], Direction.UP);
            scene.idle(2);
        }

        for(int i = 0; i < 3; ++i) {
            scene.world().hideIndependentSection(link3[i], Direction.UP);
            scene.idle(2);
        }

        scene.idle(20);
        Selection pulley = util.select().fromTo(2, 2, 6, 3, 5, 6).add(util.select().fromTo(3, 5, 5, 3, 5, 6));
        ElementLink<WorldSectionElement> pulleySection = scene.world().showIndependentSection(pulley, Direction.DOWN);
        scene.world().moveSection(pulleySection, new Vec3(0.0d, -2.0d, 0.0d), 0);
        scene.idle(15);

        for(int i = 0; i < 7; ++i) {
            Selection sel = null;

            for(int x = 0; x < 5; ++x) {
                int z = 4 - i + Math.abs(x - 2);
                if (z >= 0 && z < 5) {
                    sel = addSelection(sel, util.select().position(x + 1, 1, z + 1));
                }
            }

            if (sel != null) {
                scene.world().showSection(sel, Direction.DOWN);
            }

            scene.idle(4);
        }

        scene.idle(3);
        scene.world().hideIndependentSection(pulleySection, Direction.UP);
        scene.idle(20);
        scene.overlay().showControls(util.vector().topOf(3, 1, 3), Pointing.DOWN, 20).withItem(new ItemStack(Items.FLINT_AND_STEEL));
        scene.addKeyframe();
        scene.idle(5);
        animateSpread(scene, util, 0L, new BlockPos(3, 1, 3), 2, 5, (k) -> {
            if (k == 3) {
                scene.overlay().showText(60).pointAt(util.vector().topOf(3, 1, 3)).placeNearTarget().text("After the reaction is started, the crystal will spread throughout the entire fluid");
            }

        });

        boolean pass;
        try {
            pass = AeroConfig.server().blocks.breakBlocksOnCrystallize.get();
        } catch (Exception var17) {
            pass = true;
        }

        if (pass) {
            long clayIndex = 152746674644658L;
            Selection claySelection = null;
            Selection levititeSelection = null;
            long x = 1L;

            for(int j = 0; j < 7; ++j) {
                for(int i = 0; i < 7; ++i) {
                    if ((x & clayIndex) > 0L) {
                        claySelection = addSelection(claySelection, util.select().position(i, 1, j));
                    } else if (i > 0 && j > 0 && i < 6 && j < 6) {
                        levititeSelection = addSelection(levititeSelection, util.select().position(i, 1, j));
                    }

                    x <<= 1;
                }
            }

            scene.idle(30);
            scene.world().hideSection(util.select().fromTo(1, 1, 1, 5, 1, 5), Direction.UP);
            scene.idle(20);
            if (claySelection != null && levititeSelection != null) {
                scene.world().replaceBlocks(claySelection, Blocks.CLAY.defaultBlockState(), false);
                scene.world().replaceBlocks(levititeSelection, AeroLevititeService.INSTANCE.getFluid().defaultFluidState().createLegacyBlock(), false);
                scene.world().showSection(claySelection, Direction.DOWN);
                scene.idle(20);
                scene.world().showSection(levititeSelection, Direction.DOWN);
                scene.idle(30);
            }
            scene.overlay().showText(60).pointAt(util.vector().topOf(1, 1, 3)).attachKeyFrame().placeNearTarget().text("When certain blocks are used to cast Levitite...");
            scene.idle(70);
            scene.overlay().showControls(util.vector().topOf(2, 1, 3), Pointing.DOWN, 20).withItem(new ItemStack(Items.FLINT_AND_STEEL));
            scene.idle(5);
            animateSpread(scene, util, clayIndex, new BlockPos(2, 1, 3), 2, 4, (k) -> {
            });
            scene.idle(10);
            scene.overlay().showText(60).pointAt(util.vector().topOf(1, 1, 3)).placeNearTarget().text("...they will be destroyed during the crystallization process");
        }

    }

    // TODO
    public static void propulsiteFlurry(SceneBuilder scene, SceneBuildingUtil util) {
        densiteEmulsion(scene, util);
    }

    // TODO
    public static void oscilliteSuspension(SceneBuilder scene, SceneBuildingUtil util) {
        densiteEmulsion(scene, util);
    }

    @SuppressWarnings("SameParameterValue")
    static void animateSpread(SceneBuilder scene, SceneBuildingUtil util, long clayIndex, BlockPos startPos, int minDelay, int maxDelay, Consumer<Integer> stepEvent) {
        int[] arr1 = new int[25];
        int[] arr2 = new int[25];

        for(int x = 0; x < 5; ++x) {
            for(int y = 0; y < 5; ++y) {
                int i = x + 5 * y;
                int i2 = 1 + x + (y + 1) * 7;
                long n = 1L << i2;
                if ((n & clayIndex) > 0L) {
                    arr1[i] = -2;
                    arr2[i] = -2;
                }
            }
        }

        arr1[startPos.getX() + startPos.getZ() * 5 - 6] = 4;
        scene.effects().emitParticles(new Vec3(0.5d + (double)startPos.getX(), 1.5d, 0.5d + (double)startPos.getZ()), withinBlockSpace(ParticleTypes.SMOKE, 1.5f), 10.0f, 2);

        for(int k = 0; k < 20; ++k) {
            int[] source = k % 2 == 0 ? arr1 : arr2;
            int[] target = k % 2 == 0 ? arr2 : arr1;

            for(int x = 0; x < 5; ++x) {
                for(int y = 0; y < 5; ++y) {
                    int i = x + 5 * y;
                    if (source[i] > 0) {
                        source[i]--;
                        if (source[i] == 0) {
                            source[i] = -1;
                            scene.world().replaceBlocks(util.select().position(x + 1, 1, y + 1), AeroBlocks.LEVITITE.getDefaultState(), false);
                            scene.effects().emitParticles(new Vec3(1.5d + (double)x, 1.5d, 1.5d + (double)y), withinBlockSpace(ParticleTypes.FLAME, 1.5f), 10.0f, 2);
                        }
                    }
                }
            }

            boolean done = true;

            for(int x = 0; x < 5; ++x) {
                for(int y = 0; y < 5; ++y) {
                    int i = x + 5 * y;
                    target[i] = source[i];
                    if (source[i] >= 0) {
                        done = false;
                    }

                    if (source[i] == 0 && (x > 0 && source[x - 1 + 5 * y] == -1 || x < 4 && source[x + 1 + 5 * y] == -1 || y > 0 && source[x + 5 * y - 5] == -1 || y < 4 && source[x + 5 * y + 5] == -1)) {
                        target[i] = scene.getScene().getWorld().random.nextInt(minDelay, maxDelay);
                        scene.effects().emitParticles(new Vec3(1.5d + (double)x, 1.5d, 1.5d + (double)y), withinBlockSpace(ParticleTypes.SMOKE, 1.5f), 10.0f, 1);
                    }
                }
            }

            long bit = 1L;

            for(int y = 0; y < 7; ++y) {
                for(int x = 0; x < 7; ++x) {
                    if ((bit & clayIndex) > 0L) {
                        boolean remove = true;

                        for(int i = 0; i < 4; ++i) {
                            Vec3i dir = Direction.from2DDataValue(i).getNormal();
                            int x2 = x + dir.getX() - 1;
                            int y2 = y + dir.getZ() - 1;
                            if (x2 >= 0 && y2 >= 0 && x2 < 5 && y2 < 5 && source[x2 + y2 * 5] >= 0) {
                                remove = false;
                                break;
                            }
                        }

                        if (remove) {
                            scene.world().destroyBlock(new BlockPos(x, 1, y));
                            clayIndex &= ~bit;
                        }
                    }

                    bit <<= 1;
                }
            }

            if (done) {
                return;
            }

            scene.idle(6);
            stepEvent.accept(k);
        }
    }

    static Selection addSelection(Selection source, Selection a) {
        return source == null ? a : source.add(a);
    }

    public static <T extends ParticleOptions> ParticleEmitter withinBlockSpace(T data, float range) {
        return (w, x, y, z) -> w.addParticle(
            data,
            Math.floor(x) + 0.5d + (w.random.nextDouble() - 0.5d) * range,
            Math.floor(y) + 0.5d + (w.random.nextDouble() - 0.5d) * range,
            Math.floor(z) + 0.5d + (w.random.nextDouble() - 0.5d) * range,
            0.0d, 0.0d, 0.0d
        );
    }
}
