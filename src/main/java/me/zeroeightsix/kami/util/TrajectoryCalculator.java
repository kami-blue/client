package me.zeroeightsix.kami.util;

import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.*;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;

import java.util.List;

/**
 * Created by Gebruiker on 27/02/2017.
 */
public class TrajectoryCalculator {

    /**
     * Check if the thePlayer is holding an item that can be thrown/shot.
     *
     * @param entity an entity
     * @return true if can shoot/throw, false otherwise
     */
    public static ThrowingType getThrowType(EntityLivingBase entity) {
        // Check if we're holding an item first
        if (entity.getHeldItem(EnumHand.MAIN_HAND).isEmpty()) {
            return ThrowingType.NONE;
        }

        ItemStack itemStack = entity.getHeldItem(EnumHand.MAIN_HAND);
        Item item = itemStack.getItem();
        // The potion is kind of special so we do it's own check
        if (item instanceof ItemPotion) {
            // Check if it's a splashable potion
            if (itemStack.getItem() instanceof ItemSplashPotion){
                return ThrowingType.POTION;
            }
        } else if (item instanceof ItemBow && entity.isHandActive()) {
            return ThrowingType.BOW;
        } else if (item instanceof ItemExpBottle) {
            return ThrowingType.EXPERIENCE;
        } else if (item instanceof ItemSnowball || item instanceof ItemEgg || item instanceof ItemEnderPearl) {
            return ThrowingType.NORMAL;
        }

        // Unknown type
        return ThrowingType.NONE;
    }

    public enum ThrowingType {
        NONE, BOW, EXPERIENCE, POTION, NORMAL
    }

    /**
     * A class used to mimic the flight of an entity.  Actual
     * implementation resides in multiple classes but the parent of all
     * of them is {@link net.minecraft.entity.projectile.EntityThrowable}
     */
    public static final class FlightPath {
        private EntityLivingBase shooter;
        public Vec3d position;
        private Vec3d motion;
        private float yaw;
        private float pitch;
        private AxisAlignedBB boundingBox;
        private boolean collided;
        private RayTraceResult target;
        private ThrowingType throwingType;

        public FlightPath(EntityLivingBase entityLivingBase, ThrowingType throwingType) {
            this.shooter = entityLivingBase;
            this.throwingType = throwingType;

            double[] ipos = interpolate(shooter);

            // Set the starting angles of the entity
            this.setLocationAndAngles(ipos[0] + Wrapper.getMinecraft().getRenderManager().renderPosX, ipos[1] + this.shooter.getEyeHeight() + Wrapper.getMinecraft().getRenderManager().renderPosY, ipos[2] + Wrapper.getMinecraft().getRenderManager().renderPosZ,
                    this.shooter.rotationYaw, this.shooter.rotationPitch);
            Vec3d startingOffset = new Vec3d(MathHelper.cos(this.yaw / 180.0F * (float) Math.PI) * 0.16F, 0.1d,
                    MathHelper.sin(this.yaw / 180.0F * (float) Math.PI) * 0.16F);
            this.position = this.position.subtract(startingOffset);
            // Update the entity's bounding box
            this.setPosition(this.position);

            // Set the entity's motion based on the shooter's rotations
            this.motion = new Vec3d(-MathHelper.sin(this.yaw / 180.0F * (float) Math.PI) * MathHelper.cos(this.pitch / 180.0F * (float) Math.PI),
                    -MathHelper.sin(this.pitch / 180.0F * (float) Math.PI),
                    MathHelper.cos(this.yaw / 180.0F * (float) Math.PI) * MathHelper.cos(this.pitch / 180.0F * (float) Math.PI));
            this.setThrowableHeading(this.motion, this.getInitialVelocity());
        }

        /**
         * Update the entity's data in the world.
         */
        public void onUpdate() {
            // Get the predicted positions in the world
            Vec3d prediction = this.position.add(this.motion);
            // Check if we've collided with a block in the same time
            RayTraceResult blockCollision = this.shooter.getEntityWorld().rayTraceBlocks(this.position, prediction, false, true, false);
            // Check if we got a block collision
            if (blockCollision != null) {
                prediction = blockCollision.hitVec;
            }

            // Check entity collision
            this.onCollideWithEntity(prediction, blockCollision);

            // Check if we had a collision
            if (this.target != null) {
                this.collided = true;
                // Update position
                this.setPosition(this.target.hitVec);
                return;
            }

            // Sanity check to see if we've gone below the world (if we have we will never collide)
            if (this.position.y <= 0.0d) {
                // Force this to true even though we haven't collided with anything
                this.collided = true;
                return;
            }

            // Update the entity's position based on velocity
            this.position = this.position.add(this.motion);
            float motionModifier = 0.99F;
            // Check if our path will collide with water
            if (this.shooter.getEntityWorld().isMaterialInBB(this.boundingBox, Material.WATER)) {
                motionModifier = this.throwingType == ThrowingType.BOW ? 0.6F : 0.8F;
            }

            // Slowly decay the velocity of the path
            this.motion = mult(this.motion, motionModifier);
            // Drop the motionY by the constant gravity
            this.motion = this.motion.subtract(0.0d, this.getGravityVelocity(), 0.0d);
            // Update the position and bounding box
            this.setPosition(this.position);
        }

        /**
         * Check if our path collides with an entity.
         *
         * @param prediction the predicted position
         * @param blockCollision block collision if we had one
         */
        private void onCollideWithEntity(Vec3d prediction, RayTraceResult blockCollision) {
            Entity collidingEntity = null;
            double currentDistance = 0.0d;
            // Get all possible collision entities disregarding the local thePlayer
            List<Entity> collisionEntities = this.shooter.world.getEntitiesWithinAABBExcludingEntity(this.shooter, this.boundingBox.expand(this.motion.x, this.motion.y, this.motion.z).expand(1.0D, 1.0D, 1.0D));;

            // Loop through every loaded entity in the world
            for (Entity entity : collisionEntities) {
                // Check if we can collide with the entity or it's ourself
                if (!entity.canBeCollidedWith() && entity != this.shooter) {
                    continue;
                }

                // Check if we collide with our bounding box
                float collisionSize = entity.getCollisionBorderSize();
                AxisAlignedBB expandedBox = entity.getEntityBoundingBox().expand(collisionSize, collisionSize, collisionSize);
                RayTraceResult objectPosition = expandedBox.calculateIntercept(this.position, prediction);
                // Check if we have a collision

                if (objectPosition != null) {
                    double distanceTo = this.position.distanceTo(objectPosition.hitVec);

                    // Check if we've gotten a closer entity
                    if (distanceTo < currentDistance || currentDistance == 0.0D) {
                        collidingEntity = entity;
                        currentDistance = distanceTo;
                    }
                }
            }

            // Check if we had an entity
            if (collidingEntity != null) {
                // Set our target to the result
                this.target = new RayTraceResult(collidingEntity);
            } else {
                // Fallback to the block collision
                this.target = blockCollision;
            }
        }

        /**
         * Return the initial velocity of the entity at it's exact starting
         * moment in flight.
         *
         * @return entity velocity in flight
         */
        private float getInitialVelocity() {
            Item item = this.shooter.getHeldItem(EnumHand.MAIN_HAND).getItem();
            switch (this.throwingType) {
                case BOW:
                    // A local instance of the bow we are holding
                    ItemBow bow = (ItemBow) item;
                    // Check how long we've been using the bow
                    int useDuration = bow.getMaxItemUseDuration(this.shooter.getHeldItem(EnumHand.MAIN_HAND)) - this.shooter.getItemInUseCount();
                    float velocity = (float) useDuration / 20.0F;
                    velocity = (velocity * velocity + velocity * 2.0F) / 3.0F;
                    if (velocity > 1.0F) {
                        velocity = 1.0F;
                    }

                    // When the arrow is spawned inside of ItemBow, they multiply it by 2
                    return (velocity * 2.0f) * 1.5f;
                case POTION:
                    return 0.5F;
                case EXPERIENCE:
                    return 0.7F;
                case NORMAL:
                    return 1.5f;
            }
            // The standard gravity
            return 1.5f;
        }

        /**
         * Get the constant gravity of the item in use.
         *
         * @return gravity relating to item
         */
        private float getGravityVelocity() {
            switch (this.throwingType) {
                case BOW:
                case POTION:
                    return 0.05f;
                case EXPERIENCE:
                    return 0.07f;
                case NORMAL:
                    return 0.03f;
            }

            // The standard gravity
            return 0.03f;
        }

        /**
         * Set the position and rotation of the entity in the world.
         *
         * @param x x position in world
         * @param y y position in world
         * @param z z position in world
         * @param yaw yaw rotation axis
         * @param pitch pitch rotation axis
         */
        private void setLocationAndAngles(double x, double y, double z, float yaw, float pitch) {
            this.position = new Vec3d(x, y, z);
            this.yaw = yaw;
            this.pitch = pitch;
        }

        /**
         * Sets the x,y,z of the entity from the given parameters. Also seems to set
         * up a bounding box.
         *
         * @param position position in world
         */
        private void setPosition(Vec3d position) {
            this.position = new Vec3d(position.x, position.y, position.z);
            // Usually this is this.width / 2.0f but throwables change
            double entitySize = (this.throwingType == ThrowingType.BOW ? 0.5d : 0.25d) / 2.0d;
            // Update the path's current bounding box
            this.boundingBox = new AxisAlignedBB(position.x - entitySize,
                    position.y - entitySize,
                    position.z - entitySize,
                    position.x + entitySize,
                    position.y + entitySize,
                    position.z + entitySize);
        }

        /**
         * Set the entity's velocity and position in the world.
         *
         * @param motion velocity in world
         * @param velocity starting velocity
         */
        private void setThrowableHeading(Vec3d motion, float velocity) {
            // Divide the current motion by the length of the vector
            this.motion = div(motion, (float) motion.length());
            // Multiply by the velocity
            this.motion = mult(this.motion, velocity);
        }

        /**
         * Check if the path has collided with an object.
         *
         * @return path collides with ground
         */
        public boolean isCollided() {
            return collided;
        }

        /**
         * Get the target we've collided with if it exists.
         *
         * @return moving object target
         */
        public RayTraceResult getCollidingTarget() {
            return target;
        }
    }

    public static double[] interpolate(Entity entity) {
        double posX = interpolate(entity.posX, entity.lastTickPosX) - Wrapper.getMinecraft().renderManager.renderPosX;
        double posY = interpolate(entity.posY, entity.lastTickPosY) - Wrapper.getMinecraft().renderManager.renderPosY;
        double posZ = interpolate(entity.posZ, entity.lastTickPosZ) - Wrapper.getMinecraft().renderManager.renderPosZ;
        return new double[] { posX, posY, posZ };
    }

    public static double interpolate(double now, double then) {
        return then + (now - then) * Wrapper.getMinecraft().getRenderPartialTicks();
    }

    public static Vec3d mult(Vec3d factor, float multiplier) {
        return new Vec3d(factor.x * multiplier, factor.y * multiplier, factor.z * multiplier);
    }

    public static Vec3d div(Vec3d factor, float divisor) {
        return new Vec3d(factor.x / divisor, factor.y / divisor, factor.z / divisor);
    }
}
