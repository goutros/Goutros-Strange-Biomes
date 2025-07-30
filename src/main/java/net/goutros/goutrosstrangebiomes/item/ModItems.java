package net.goutros.goutrosstrangebiomes.item;

import net.goutros.goutrosstrangebiomes.GoutrosStrangeBiomes;
import net.goutros.goutrosstrangebiomes.entity.ModEntities;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.common.DeferredSpawnEggItem;

public class ModItems {

    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(net.minecraft.core.registries.Registries.ITEM, GoutrosStrangeBiomes.MOD_ID);

    public static final DeferredHolder<Item, Item> YARN_BALL =
            ITEMS.register("yarn_ball", () -> new Item(new Item.Properties()));

    // Custom spawn egg that will use our custom texture
    public static final DeferredHolder<Item, DeferredSpawnEggItem> YARN_CAT_SPAWN_EGG =
            ITEMS.register("yarn_cat_spawn_egg", () -> new DeferredSpawnEggItem(
                    ModEntities.YARN_CAT,
                    0xFFFFFF, // Primary color (these won't be used with custom texture)
                    0x8B4513, // Secondary color (these won't be used with custom texture)
                    new Item.Properties()
            ));
}