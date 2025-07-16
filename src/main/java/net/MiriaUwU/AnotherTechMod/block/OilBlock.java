package net.MiriaUwU.AnotherTechMod.block;

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

        // Also explicitly check the block one above
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
        // Remove oil and replace with fire
        level.removeBlock(pos, false);
        level.setBlock(pos, Blocks.FIRE.defaultBlockState(), 3);

        // Play fire ignition sound
        level.playSound(null, pos, SoundEvents.FIRECHARGE_USE, SoundSource.BLOCKS, 1.0F, 1.0F);

        // Explosion (optional)
        level.explode(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                2.0F, Level.ExplosionInteraction.BLOCK);

        // Spread ignition to adjacent oil blocks
        for (Direction dir : Direction.values()) {
            BlockPos adjacentPos = pos.relative(dir);
            BlockState adjacentState = level.getBlockState(adjacentPos);

            if (adjacentState.is(this)) {  // Checks if adjacent block is oil block
                // Schedule a tick for the adjacent oil block to ignite after some delay
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