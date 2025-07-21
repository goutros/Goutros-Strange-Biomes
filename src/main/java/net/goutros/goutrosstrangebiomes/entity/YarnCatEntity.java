package net.goutros.goutrosstrangebiomes.entity;

import net.goutros.goutrosstrangebiomes.entity.ModEntities;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

public class YarnCatEntity extends TamableAnimal {

    // Animation states for the new 1.21.1 animation system
    public final AnimationState idleAnimationState = new AnimationState();
    public final AnimationState walkAnimationState = new AnimationState();
    public final AnimationState sitAnimationState = new AnimationState();

    // Data synchronizers
    private static final EntityDataAccessor<Boolean> SITTING =
            SynchedEntityData.defineId(YarnCatEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> DYE_COLOR =
            SynchedEntityData.defineId(YarnCatEntity.class, EntityDataSerializers.INT);

    private int idleAnimationTimeout = 0;

    public YarnCatEntity(EntityType<? extends TamableAnimal> entityType, Level level) {
        super(entityType, level);
        this.setTame(false, false);
    }

    @Override
    protected void registerGoals() {
        // Basic movement and behavior
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new SitWhenOrderedToGoal(this));

        // Tamed behavior
        this.goalSelector.addGoal(2, new FollowOwnerGoal(this, 1.0D, 10.0F, 2.0F));

        // Wild behavior
        this.goalSelector.addGoal(3, new AvoidEntityGoal<>(this, Player.class, 16.0F, 0.8D, 1.33D));
        this.goalSelector.addGoal(4, new WaterAvoidingRandomStrollGoal(this, 0.8D));
        this.goalSelector.addGoal(5, new LookAtPlayerGoal(this, Player.class, 10.0F));
        this.goalSelector.addGoal(6, new RandomLookAroundGoal(this));

        // Defensive behavior
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(SITTING, false);
        builder.define(DYE_COLOR, DyeColor.WHITE.getId());
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 10.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.3D)
                .add(Attributes.ATTACK_DAMAGE, 3.0D)
                .add(Attributes.FOLLOW_RANGE, 16.0D);
    }

    @Override
    public void tick() {
        super.tick();

        if (this.level().isClientSide()) {
            setupAnimationStates();
        }
    }

    private void setupAnimationStates() {
        // Idle animation management
        if (this.idleAnimationTimeout <= 0) {
            this.idleAnimationTimeout = this.random.nextInt(40) + 80;
            this.idleAnimationState.start(this.tickCount);
        } else {
            --this.idleAnimationTimeout;
        }

        // Manage animation states based on entity state
        if (this.isInSittingPose()) {
            // Stop other animations when sitting
            this.walkAnimationState.stop();
            this.sitAnimationState.startIfStopped(this.tickCount);
        } else {
            // Stop sitting animation when not sitting
            this.sitAnimationState.stop();

            // Check if moving for walk animation
            if (this.isMoving()) {
                this.walkAnimationState.startIfStopped(this.tickCount);
            } else {
                this.walkAnimationState.stop();
            }
        }
    }

    private boolean isMoving() {
        return this.getDeltaMovement().horizontalDistanceSqr() > 1.0E-6;
    }

    @Override
    protected void updateWalkAnimation(float partialTick) {
        float f = Math.min(partialTick * 6.0F, 1.0F);
        this.walkAnimation.update(f, 0.2F);
    }

    // Sitting functionality
    public boolean isSitting() {
        return this.entityData.get(SITTING);
    }

    public void setSitting(boolean sitting) {
        this.entityData.set(SITTING, sitting);
        this.setOrderedToSit(sitting);
    }

    @Override
    public boolean isInSittingPose() {
        return this.isSitting();
    }

    // Dye color functionality with full DyeColor support
    public DyeColor getDyeColor() {
        return DyeColor.byId(this.entityData.get(DYE_COLOR));
    }

    public void setDyeColor(DyeColor color) {
        this.entityData.set(DYE_COLOR, color.getId());
    }

    /**
     * Gets the texture diffuse color for rendering.
     * This is compatible with mod-added dye colors as it uses the DyeColor's built-in color value.
     */
    public int getTextureDiffuseColor() {
        return this.getDyeColor().getTextureDiffuseColor();
    }

    /**
     * Gets the firework color for particle effects or other color representations.
     */
    public int getFireworkColor() {
        return this.getDyeColor().getFireworkColor();
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);

        if (this.level().isClientSide) {
            if (this.isTame() && this.isOwnedBy(player)) {
                return InteractionResult.SUCCESS;
            } else if (this.isFood(itemstack) && !this.isTame()) {
                return InteractionResult.SUCCESS;
            } else if (itemstack.getItem() instanceof DyeItem && this.isTame() && this.isOwnedBy(player)) {
                return InteractionResult.SUCCESS;
            }
            return InteractionResult.PASS;
        }

        if (this.isTame()) {
            if (this.isOwnedBy(player)) {
                // Handle dye interaction for tamed cats
                if (itemstack.getItem() instanceof DyeItem dyeItem) {
                    DyeColor dyeColor = dyeItem.getDyeColor();

                    if (this.getDyeColor() != dyeColor) {
                        this.setDyeColor(dyeColor);

                        if (!player.getAbilities().instabuild) {
                            itemstack.shrink(1);
                        }

                        this.level().playSound(
                                null,
                                this.getX(),
                                this.getY(),
                                this.getZ(),
                                SoundEvents.DYE_USE,
                                SoundSource.AMBIENT,
                                1.0f,
                                1.0f
                        );

                        if (this.level() instanceof ServerLevel serverLevel) {
                            Vec3 pos = this.position().add(0, this.getBbHeight() * 0.5, 0);

                            int packed = dyeColor.getTextureDiffuseColor();
                            float r = ((packed >> 16) & 0xFF) / 255.0f;
                            float g = ((packed >> 8) & 0xFF) / 255.0f;
                            float b = (packed & 0xFF) / 255.0f;

                            for (int i = 0; i < 7; ++i) {
                                double dx = this.random.nextGaussian() * 0.12;
                                double dy = this.random.nextGaussian() * 0.12;
                                double dz = this.random.nextGaussian() * 0.12;

                                serverLevel.sendParticles(
                                        new DustParticleOptions(new Vector3f(r, g, b), 1.0f),
                                        pos.x, pos.y, pos.z,
                                        1,
                                        dx, dy, dz,
                                        0.05
                                );
                            }
                        }

                        return InteractionResult.CONSUME;
                    }

                    return InteractionResult.PASS;
                }

                // Handle sitting toggle
                if (!this.isBaby()) {
                    this.setOrderedToSit(!this.isOrderedToSit());
                    this.setSitting(!this.isSitting());
                    this.jumping = false;
                    this.navigation.stop();
                    this.setTarget(null);
                    return InteractionResult.SUCCESS;
                }
            }
        } else if (this.isFood(itemstack)) {
            // Taming attempt with food
            if (!player.getAbilities().instabuild) {
                itemstack.shrink(1);
            }

            // 33% chance to tame (same as vanilla cats)
            if (this.random.nextInt(3) == 0) {
                this.tame(player);
                this.navigation.stop();
                this.setTarget(null);
                this.setOrderedToSit(true);
                this.setSitting(true);
                this.level().broadcastEntityEvent(this, (byte) 7); // Heart particles
            } else {
                this.level().broadcastEntityEvent(this, (byte) 6); // Smoke particles
            }

            return InteractionResult.CONSUME;
        }

        return super.mobInteract(player, hand);
    }

    // Breeding
    @Override
    @Nullable
    public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob otherParent) {
        YarnCatEntity baby = ModEntities.YARN_CAT.get().create(level);
        if (baby != null && otherParent instanceof YarnCatEntity otherCat) {
            // Randomly inherit color from either parent
            DyeColor color = this.random.nextBoolean() ? this.getDyeColor() : otherCat.getDyeColor();
            baby.setDyeColor(color);

            // Baby inherits owner from this parent
            if (this.isTame()) {
                baby.setOwnerUUID(this.getOwnerUUID());
                baby.setTame(true, true);
            }
        }
        return baby;
    }

    @Override
    public boolean isFood(ItemStack stack) {
        return stack.is(Items.COD) ||
                stack.is(Items.SALMON) ||
                stack.is(Items.TROPICAL_FISH) ||
                stack.is(Items.COOKED_COD) ||
                stack.is(Items.COOKED_SALMON);
    }

    // NBT data saving/loading
    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putByte("DyeColor", (byte) this.getDyeColor().getId());
        compound.putBoolean("Sitting", this.isSitting());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        if (compound.contains("DyeColor")) {
            this.setDyeColor(DyeColor.byId(compound.getByte("DyeColor")));
        }
        if (compound.contains("Sitting")) {
            this.setSitting(compound.getBoolean("Sitting"));
            this.setOrderedToSit(compound.getBoolean("Sitting"));
        }
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty,
                                        MobSpawnType spawnType, @Nullable SpawnGroupData spawnGroupData) {
        // Randomly assign dye color on spawn with weighted probabilities
        RandomSource random = level.getRandom();

        // Weighted color selection (white and common colors more likely)
        DyeColor[] commonColors = {DyeColor.WHITE, DyeColor.ORANGE, DyeColor.GRAY, DyeColor.BROWN};
        DyeColor[] allColors = DyeColor.values();

        DyeColor selectedColor;
        if (random.nextFloat() < 0.6f) {
            // 60% chance for common colors
            selectedColor = commonColors[random.nextInt(commonColors.length)];
        } else {
            // 40% chance for any color
            selectedColor = allColors[random.nextInt(allColors.length)];
        }

        this.setDyeColor(selectedColor);

        return super.finalizeSpawn(level, difficulty, spawnType, spawnGroupData);
    }

    // Custom name handling - always "Yarn Cat" regardless of color
    @Override
    public boolean hasCustomName() {
        // Allow custom names from name tags but don't auto-generate color-based names
        return super.hasCustomName();
    }

    // Sounds
    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.CAT_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundEvents.CAT_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.CAT_DEATH;
    }

    @Override
    protected float getSoundVolume() {
        return 0.4F;
    }

    // Particle effects for client events
    @Override
    public void handleEntityEvent(byte eventId) {
        if (eventId == 7) {
            // Heart particles (successful taming)
            this.spawnTamingParticles(true);
        } else if (eventId == 6) {
            // Smoke particles (failed taming)
            this.spawnTamingParticles(false);
        } else {
            super.handleEntityEvent(eventId);
        }
    }

    public boolean isJebRainbow() {
        Component name = this.getCustomName();
        return name != null && name.getString().equalsIgnoreCase("jeb_") && !this.isSilent();
    }

    public static Vector3f fromPackedColor(int packed) {
        float r = ((packed >> 16) & 0xFF) / 255.0f;
        float g = ((packed >> 8) & 0xFF) / 255.0f;
        float b = (packed & 0xFF) / 255.0f;
        return new Vector3f(r, g, b);
    }

    public Vector3f getDynamicColor() {
        if (this.isJebRainbow()) {
            float hue = (this.tickCount % 200) / 200.0f;
            int rgb = java.awt.Color.HSBtoRGB(hue, 1.0f, 1.0f);
            return fromPackedColor(rgb);
        } else {
            return fromPackedColor(this.getTextureDiffuseColor());
        }
    }
}