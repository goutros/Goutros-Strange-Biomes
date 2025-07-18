package net.goutros.goutrosstrangebiomes.block;

import net.goutros.goutrosstrangebiomes.GoutrosStrangeBiomes;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FireBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(Registries.BLOCK, GoutrosStrangeBiomes.MOD_ID);

    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(Registries.ITEM, GoutrosStrangeBiomes.MOD_ID);

    public static final Supplier<Block> PILLOW_GRASS_BLOCK =
            BLOCKS.register("pillow_grass_block", () ->
                    new PillowGrassBlock(BlockBehaviour.Properties.of()
                            .mapColor(MapColor.GRASS)
                            .instrument(NoteBlockInstrument.GUITAR)
                            .strength(0.6F)
                            .randomTicks() // to match grass behavior
                            .sound(SoundType.WOOL)
                            .ignitedByLava()
                    )
            );

    public static final Supplier<Block> PILLOW_DIRT =
            BLOCKS.register("pillow_dirt", () ->
                    new Block(BlockBehaviour.Properties.of()
                            .mapColor(MapColor.COLOR_BROWN)
                            .instrument(NoteBlockInstrument.GUITAR)
                            .strength(0.5F)
                            .sound(SoundType.WOOL)
                            .ignitedByLava()
                    )
            );

    public static final Supplier<Block> GOLDEN_PILLOW_GRASS_BLOCK =
            BLOCKS.register("golden_pillow_grass_block", () ->
                    new PillowGrassBlock(BlockBehaviour.Properties.of()
                            .mapColor(MapColor.COLOR_YELLOW)
                            .instrument(NoteBlockInstrument.GUITAR)
                            .strength(0.6F)
                            .randomTicks()
                            .sound(SoundType.WOOL)
                            .ignitedByLava()
                    )
            );

    public static final Supplier<Block> BUTTONS =
            BLOCKS.register("buttons", () ->
                    new ButtonsBlock(BlockBehaviour.Properties.of()
                            .mapColor(MapColor.COLOR_PINK) // or any color
                            .noCollission()
                            .strength(0.2F)
                            .sound(SoundType.CANDLE)
                    )
            );


    public static final Supplier<Item> PILLOW_GRASS_BLOCK_ITEM =
            ITEMS.register("pillow_grass_block", () ->
                    new BlockItem(PILLOW_GRASS_BLOCK.get(), new Item.Properties()));

    public static final Supplier<Item> PILLOW_DIRT_ITEM = ITEMS.register("pillow_dirt",
            () -> new BlockItem(PILLOW_DIRT.get(), new Item.Properties()));

    public static final Supplier<Item> GOLDEN_PILLOW_GRASS_ITEM = ITEMS.register("golden_pillow_grass_block",
            () -> new BlockItem(GOLDEN_PILLOW_GRASS_BLOCK.get(), new Item.Properties()));

    public static final Supplier<Item> BUTTONS_ITEM =
            ITEMS.register("buttons", () ->
                    new BlockItem(BUTTONS.get(), new Item.Properties()));


    public static void makeFlammable(Block block, int burnChance, int spreadChance) {
        FireBlock fireBlock = (FireBlock) Blocks.FIRE;
        fireBlock.setFlammable(block, burnChance, spreadChance);
    }

    public static void registerFlammableBlocks() {
        // Only register if blocks exist
        if (PILLOW_GRASS_BLOCK != null) {
            makeFlammable(PILLOW_GRASS_BLOCK.get(), 30, 60);
        }
        if (PILLOW_DIRT != null) {
            makeFlammable(PILLOW_DIRT.get(), 20, 30);
        }
        if (GOLDEN_PILLOW_GRASS_BLOCK != null) {
            makeFlammable(GOLDEN_PILLOW_GRASS_BLOCK.get(), 30, 60);
        }
    }
}
