package net.goutros.goutrosstrangebiomes.entity;

import net.goutros.goutrosstrangebiomes.GoutrosStrangeBiomes;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;

@EventBusSubscriber(modid = GoutrosStrangeBiomes.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public class ModEntities {

    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(Registries.ENTITY_TYPE, GoutrosStrangeBiomes.MOD_ID);

    // Register YarnCatEntity
    public static final DeferredHolder<EntityType<?>, EntityType<YarnCatEntity>> YARN_CAT =
            ENTITY_TYPES.register("yarn_cat", () -> EntityType.Builder.of(YarnCatEntity::new, MobCategory.CREATURE)
                    .sized(0.4F, 0.6F)
                    .eyeHeight(0.68F) // Eye height for proper look-at behavior
                    .passengerAttachments(0.6875F)
                    .clientTrackingRange(8)
                    .updateInterval(3)
                    .build("yarn_cat"));

    // Register attributes after entity registration
    @SubscribeEvent
    public static void registerAttributes(EntityAttributeCreationEvent event) {
        // Ensure YARN_CAT is properly registered before accessing it
        event.put(YARN_CAT.get(), YarnCatEntity.createAttributes().build());
    }

    // Register spawn placements for YarnCatEntity
    @SubscribeEvent
    public static void registerSpawnPlacements(RegisterSpawnPlacementsEvent event) {
        event.register(YARN_CAT.get(),
                SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                Animal::checkAnimalSpawnRules,
                RegisterSpawnPlacementsEvent.Operation.AND);
    }
}
