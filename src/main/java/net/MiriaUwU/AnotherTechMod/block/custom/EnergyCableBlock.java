package net.MiriaUwU.AnotherTechMod.block.custom;

import com.mojang.serialization.MapCodec;
import net.MiriaUwU.AnotherTechMod.entity.EnergyCableBlockEntity;
import net.MiriaUwU.AnotherTechMod.entity.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;

public class EnergyCableBlock extends Block implements EntityBlock {
    public static final MapCodec<EnergyCableBlock> CODEC = simpleCodec(EnergyCableBlock::new);

    // Connection properties for each direction
    public static final BooleanProperty NORTH = BlockStateProperties.NORTH;
    public static final BooleanProperty SOUTH = BlockStateProperties.SOUTH;
    public static final BooleanProperty EAST = BlockStateProperties.EAST;
    public static final BooleanProperty WEST = BlockStateProperties.WEST;
    public static final BooleanProperty UP = BlockStateProperties.UP;
    public static final BooleanProperty DOWN = BlockStateProperties.DOWN;

    public EnergyCableBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(NORTH, false)
                .setValue(SOUTH, false)
                .setValue(EAST, false)
                .setValue(WEST, false)
                .setValue(UP, false)
                .setValue(DOWN, false));
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(NORTH, SOUTH, EAST, WEST, UP, DOWN);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new EnergyCableBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide) {
            return null;
        }
        return type == ModBlockEntities.ENERGY_CABLE.get() ?
                (BlockEntityTicker<T>) (BlockEntityTicker<EnergyCableBlockEntity>) EnergyCableBlockEntity::serverTick : null;
    }

    @Override
    protected BlockState updateShape(BlockState state, Direction direction, BlockState neighborState,
                                     LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        return getStateWithConnections(state, level, pos);
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        if (!level.isClientSide) {
            BlockState newState = getStateWithConnections(state, level, pos);
            if (!newState.equals(state)) {
                level.setBlock(pos, newState, 3);
            }
        }
    }

    private BlockState getStateWithConnections(BlockState state, LevelAccessor level, BlockPos pos) {
        boolean north = canConnectTo(level, pos, Direction.NORTH);
        boolean south = canConnectTo(level, pos, Direction.SOUTH);
        boolean east = canConnectTo(level, pos, Direction.EAST);
        boolean west = canConnectTo(level, pos, Direction.WEST);
        boolean up = canConnectTo(level, pos, Direction.UP);
        boolean down = canConnectTo(level, pos, Direction.DOWN);

        return state.setValue(NORTH, north)
                .setValue(SOUTH, south)
                .setValue(EAST, east)
                .setValue(WEST, west)
                .setValue(UP, up)
                .setValue(DOWN, down);
    }

    private boolean canConnectTo(LevelAccessor level, BlockPos pos, Direction direction) {
        BlockPos neighborPos = pos.relative(direction);

        // Check if it's another cable
        if (level.getBlockState(neighborPos).getBlock() instanceof EnergyCableBlock) {
            return true;
        }

        // Check if neighbor has energy capability
        if (level instanceof Level realLevel) {
            IEnergyStorage energyStorage = realLevel.getCapability(Capabilities.EnergyStorage.BLOCK, neighborPos, direction.getOpposite());
            return energyStorage != null;
        }

        return false;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return createShape(state);
    }

    private VoxelShape createShape(BlockState state) {
        VoxelShape shape = Block.box(6, 6, 6, 10, 10, 10); // Center core

        if (state.getValue(NORTH)) shape = Shapes.or(shape, Block.box(6, 6, 0, 10, 10, 6));
        if (state.getValue(SOUTH)) shape = Shapes.or(shape, Block.box(6, 6, 10, 10, 10, 16));
        if (state.getValue(EAST)) shape = Shapes.or(shape, Block.box(10, 6, 6, 16, 10, 10));
        if (state.getValue(WEST)) shape = Shapes.or(shape, Block.box(0, 6, 6, 6, 10, 10));
        if (state.getValue(UP)) shape = Shapes.or(shape, Block.box(6, 10, 6, 10, 16, 10));
        if (state.getValue(DOWN)) shape = Shapes.or(shape, Block.box(6, 0, 6, 10, 6, 10));

        return shape;
    }
}
