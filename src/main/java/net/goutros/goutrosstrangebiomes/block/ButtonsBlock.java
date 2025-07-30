package net.goutros.goutrosstrangebiomes.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.Collections;
import java.util.List;
import java.util.function.BiFunction;

public class ButtonsBlock extends BushBlock implements BonemealableBlock {
    public static final MapCodec<ButtonsBlock> CODEC = simpleCodec(ButtonsBlock::new);
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final IntegerProperty AMOUNT = IntegerProperty.create("amount", 1, 4);

    private static final BiFunction<Direction, Integer, VoxelShape> SHAPE_BY_PROPERTIES = Util.memoize(
            (facing, count) -> {
                VoxelShape[] shapeVariants = new VoxelShape[]{
                        Block.box(8.0, 0.0, 8.0, 16.0, 3.0, 16.0),
                        Block.box(8.0, 0.0, 0.0, 16.0, 3.0, 8.0),
                        Block.box(0.0, 0.0, 0.0, 8.0, 3.0, 8.0),
                        Block.box(0.0, 0.0, 8.0, 8.0, 3.0, 16.0)
                };
                VoxelShape shape = Shapes.empty();
                for (int i = 0; i < count; i++) {
                    int index = Math.floorMod(i - facing.get2DDataValue(), 4);
                    shape = Shapes.or(shape, shapeVariants[index]);
                }
                return shape.singleEncompassing();
            }
    );

    public ButtonsBlock(BlockBehaviour.Properties props) {
        super(props);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(AMOUNT, 1));
    }

    private static final VoxelShape[] SHAPES = new VoxelShape[]{
            Block.box(0, 0, 0, 8, 1, 8), // 1 button
            Shapes.or(Block.box(0, 0, 0, 8, 1, 8), Block.box(8, 0, 0, 16, 1, 8)), // 2
            Shapes.or(Block.box(0, 0, 0, 8, 1, 8), Block.box(8, 0, 0, 16, 1, 8), Block.box(0, 0, 8, 8, 1, 16)), // 3
            Shapes.block() // full square for 4
    };

    @Override
    protected MapCodec<? extends BushBlock> codec() {
        return CODEC;
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rot) {
        return state.setValue(FACING, rot.rotate(state.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    @Override
    public boolean canBeReplaced(BlockState state, BlockPlaceContext context) {
        return !context.isSecondaryUseActive() && context.getItemInHand().is(this.asItem()) && state.getValue(AMOUNT) < 4
                || super.canBeReplaced(state, context);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState existing = context.getLevel().getBlockState(context.getClickedPos());
        return existing.is(this)
                ? existing.setValue(AMOUNT, Math.min(4, existing.getValue(AMOUNT) + 1))
                : this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, AMOUNT);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        return SHAPES[state.getValue(AMOUNT) - 1];
    }

    @Override
    public boolean isValidBonemealTarget(LevelReader level, BlockPos pos, BlockState state) {
        return true;
    }

    @Override
    public boolean isBonemealSuccess(Level level, RandomSource rand, BlockPos pos, BlockState state) {
        return true;
    }

    @Override
    public void performBonemeal(ServerLevel level, RandomSource rand, BlockPos pos, BlockState state) {
        int amount = state.getValue(AMOUNT);
        if (amount < 4) {
            level.setBlock(pos, state.setValue(AMOUNT, amount + 1), 2);
        } else {
            popResource(level, pos, new ItemStack(this));
        }
    }
}
