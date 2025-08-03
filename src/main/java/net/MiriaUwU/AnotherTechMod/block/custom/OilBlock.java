package net.MiriaUwU.AnotherTechMod.block.custom;

import net.MiriaUwU.AnotherTechMod.Config;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FlowingFluid;


public class OilBlock extends LiquidBlock {

    public OilBlock(FlowingFluid fluid, BlockBehaviour.Properties properties) {
        super(fluid, properties);
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        super.onPlace(state, level, pos, oldState, isMoving);
        if (!level.isClientSide()) {
            level.scheduleTick(pos, this, 60); // Check after 3 seconds
        }
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        // Check config first: is oil flammable?
        if (!Config.OIL_FLAMMABLE.get()) {
            // If config says no, just reschedule the tick without igniting
            level.scheduleTick(pos, this, 60);
            return;
        }

        boolean ignite = false;


        // Check all directions as before
        for (Direction dir : Direction.values()) {
            BlockPos neighborPos = pos.relative(dir);
            BlockState neighborState = level.getBlockState(neighborPos);

            if (neighborState.is(Blocks.FIRE)) {
                ignite = true;
                break;
            }
        }

        if (!ignite) {
            BlockState aboveState = level.getBlockState(pos.above());
            if (aboveState.is(Blocks.FIRE)) {
                ignite = true;
            }
        }

        if (ignite) {
            combust(level, pos);
        } else {
            level.scheduleTick(pos, this, 60);
        }
    }

    private void combust(ServerLevel level, BlockPos pos) {
        // this removes the oil and replaces it with fire
        level.removeBlock(pos, false);
        level.setBlock(pos, Blocks.FIRE.defaultBlockState(), 3);

        // plays fire ignition sound
        level.playSound(null, pos, SoundEvents.FIRECHARGE_USE, SoundSource.BLOCKS, 1.0F, 1.0F);

        // explodes, can be turned off in configs
        level.explode(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                2.0F, Level.ExplosionInteraction.BLOCK);

        // spreads the fire to other oil blocks
        for (Direction dir : Direction.values()) {
            BlockPos adjacentPos = pos.relative(dir);
            BlockState adjacentState = level.getBlockState(adjacentPos);

            if (adjacentState.is(this)) {  // Checks if adjacent block is oil block
                // schedule a tick for the other oil block to ignite after some delay
                int delay = 20 + level.random.nextInt(20); // 1 to 2 seconds
                level.scheduleTick(adjacentPos, this, delay);
            }
        }
    }

    @Override
    public boolean canBeReplaced(BlockState state, BlockPlaceContext context) {
        return true; // allows block placement inside oil
    }
}