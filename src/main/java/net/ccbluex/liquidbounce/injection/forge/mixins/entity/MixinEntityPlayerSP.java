/*
 * LiquidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/CCBlueX/LiquidBounce/
 */
package net.ccbluex.liquidbounce.injection.forge.mixins.entity;

import net.ccbluex.liquidbounce.event.*;
import net.ccbluex.liquidbounce.features.module.modules.combat.KillAura;
import net.ccbluex.liquidbounce.features.module.modules.exploit.AntiHunger;
import net.ccbluex.liquidbounce.features.module.modules.exploit.PortalMenu;
import net.ccbluex.liquidbounce.features.module.modules.fun.Derp;
import net.ccbluex.liquidbounce.features.module.modules.movement.InventoryMove;
import net.ccbluex.liquidbounce.features.module.modules.movement.NoSlow;
import net.ccbluex.liquidbounce.features.module.modules.movement.Sneak;
import net.ccbluex.liquidbounce.features.module.modules.movement.Sprint;
import net.ccbluex.liquidbounce.features.module.modules.render.NoSwing;
import net.ccbluex.liquidbounce.features.module.modules.world.Scaffold;
import net.ccbluex.liquidbounce.utils.CooldownHelper;
import net.ccbluex.liquidbounce.utils.MovementUtils;
import net.ccbluex.liquidbounce.utils.Rotation;
import net.ccbluex.liquidbounce.utils.RotationUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockFence;
import net.minecraft.block.BlockFenceGate;
import net.minecraft.block.BlockWall;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemSword;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.network.play.client.C0APacketAnimation;
import net.minecraft.network.play.client.C0BPacketEntityAction;
import net.minecraft.potion.Potion;
import net.minecraft.util.*;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(EntityPlayerSP.class)
@SideOnly(Side.CLIENT)
public abstract class MixinEntityPlayerSP extends MixinAbstractClientPlayer {

    @Shadow
    public boolean serverSprintState;

    @Shadow
    public abstract void playSound(String name, float volume, float pitch);

    @Shadow
    public int sprintingTicksLeft;

    @Shadow
    protected int sprintToggleTimer;

    @Shadow
    public float timeInPortal;

    @Shadow
    public float prevTimeInPortal;

    @Shadow
    protected Minecraft mc;

    @Shadow
    public MovementInput movementInput;

    @Shadow
    public abstract void setSprinting(boolean sprinting);

    @Shadow
    protected abstract boolean pushOutOfBlocks(double x, double y, double z);

    @Shadow
    public abstract void sendPlayerAbilities();

    @Shadow
    public float horseJumpPower;

    @Shadow
    public int horseJumpPowerCounter;

    @Shadow
    protected abstract void sendHorseJump();

    @Shadow
    public abstract boolean isRidingHorse();

    @Shadow
    @Final
    public NetHandlerPlayClient sendQueue;

    @Shadow
    private boolean serverSneakState;

    @Shadow
    public abstract boolean isSneaking();

    @Shadow
    protected abstract boolean isCurrentViewEntity();

    @Shadow
    private double lastReportedPosX;

    @Shadow
    private int positionUpdateTicks;

    @Shadow
    private double lastReportedPosY;

    @Shadow
    private double lastReportedPosZ;

    @Shadow
    private float lastReportedYaw;

    @Shadow
    private float lastReportedPitch;

    /**
     * @author CCBlueX
     */
    @Inject(method = "onUpdateWalkingPlayer", at = @At("HEAD"), cancellable = true)
    private void onUpdateWalkingPlayer(CallbackInfo ci) {
        EventManager.INSTANCE.callEvent(new MotionEvent(EventState.PRE));

        final InventoryMove inventoryMove = InventoryMove.INSTANCE;
        final Sneak sneak = Sneak.INSTANCE;
        final boolean fakeSprint = (inventoryMove.getState() && inventoryMove.getAacAdditionProValue().get()) || AntiHunger.INSTANCE.getState() || (sneak.getState() && (!MovementUtils.INSTANCE.isMoving() || !sneak.getStopMoveValue().get()) && sneak.getModeValue().get().equals("MineSecure"));

        boolean sprinting = isSprinting() && !fakeSprint;

        if (sprinting != serverSprintState) {
            if (sprinting)
                sendQueue.addToSendQueue(new C0BPacketEntityAction((EntityPlayerSP) (Object) this, C0BPacketEntityAction.Action.START_SPRINTING));
            else
                sendQueue.addToSendQueue(new C0BPacketEntityAction((EntityPlayerSP) (Object) this, C0BPacketEntityAction.Action.STOP_SPRINTING));

            serverSprintState = sprinting;
        }

        boolean sneaking = isSneaking();

        if (sneaking != serverSneakState && (!sneak.getState() || sneak.getModeValue().get().equals("Legit"))) {
            if (sneaking)
                sendQueue.addToSendQueue(new C0BPacketEntityAction((EntityPlayerSP) (Object) this, C0BPacketEntityAction.Action.START_SNEAKING));
            else
                sendQueue.addToSendQueue(new C0BPacketEntityAction((EntityPlayerSP) (Object) this, C0BPacketEntityAction.Action.STOP_SNEAKING));

            serverSneakState = sneaking;
        }

        if (isCurrentViewEntity()) {
            float yaw = rotationYaw;
            float pitch = rotationPitch;

            final Rotation targetRotation = RotationUtils.INSTANCE.getTargetRotation();

            final Derp derp = Derp.INSTANCE;
            if (derp.getState()) {
                float[] rot = derp.getRotation();
                yaw = rot[0];
                pitch = rot[1];
            }

            if (targetRotation != null) {
                yaw = targetRotation.getYaw();
                pitch = targetRotation.getPitch();
            }

            double xDiff = posX - lastReportedPosX;
            double yDiff = getEntityBoundingBox().minY - lastReportedPosY;
            double zDiff = posZ - lastReportedPosZ;
            double yawDiff = yaw - this.lastReportedYaw;
            double pitchDiff = pitch - this.lastReportedPitch;
            boolean moved = xDiff * xDiff + yDiff * yDiff + zDiff * zDiff > 9.0E-4D || positionUpdateTicks >= 20;
            boolean rotated = yawDiff != 0.0D || pitchDiff != 0.0D;

            if (ridingEntity == null) {
                if (moved && rotated) {
                    sendQueue.addToSendQueue(new C03PacketPlayer.C06PacketPlayerPosLook(posX, getEntityBoundingBox().minY, posZ, yaw, pitch, onGround));
                } else if (moved) {
                    sendQueue.addToSendQueue(new C03PacketPlayer.C04PacketPlayerPosition(posX, getEntityBoundingBox().minY, posZ, onGround));
                } else if (rotated) {
                    sendQueue.addToSendQueue(new C03PacketPlayer.C05PacketPlayerLook(yaw, pitch, onGround));
                } else {
                    sendQueue.addToSendQueue(new C03PacketPlayer(onGround));
                }
            } else {
                sendQueue.addToSendQueue(new C03PacketPlayer.C06PacketPlayerPosLook(motionX, -999.0D, motionZ, yaw, pitch, onGround));
                moved = false;
            }

            ++positionUpdateTicks;

            if (moved) {
                lastReportedPosX = posX;
                lastReportedPosY = getEntityBoundingBox().minY;
                lastReportedPosZ = posZ;
                positionUpdateTicks = 0;
            }

            if (rotated) {
                this.lastReportedYaw = yaw;
                this.lastReportedPitch = pitch;
            }
        }

        EventManager.INSTANCE.callEvent(new MotionEvent(EventState.POST));

        ci.cancel();
    }

    @Inject(method = "swingItem", at = @At("HEAD"), cancellable = true)
    private void swingItem(CallbackInfo callbackInfo) {
        final NoSwing noSwing = NoSwing.INSTANCE;

        if (noSwing.getState()) {
            callbackInfo.cancel();

            if (!noSwing.getServerSideValue().get()) {
                sendQueue.addToSendQueue(new C0APacketAnimation());
                CooldownHelper.INSTANCE.resetLastAttackedTicks();
            }
        } else {
            CooldownHelper.INSTANCE.resetLastAttackedTicks();
        }
    }

    @Inject(method = "pushOutOfBlocks", at = @At("HEAD"), cancellable = true)
    private void onPushOutOfBlocks(CallbackInfoReturnable<Boolean> callbackInfoReturnable) {
        PushOutEvent event = new PushOutEvent();
        if (noClip) {
            event.cancelEvent();
        }
        EventManager.INSTANCE.callEvent(event);

        if (event.isCancelled()) {
            callbackInfoReturnable.setReturnValue(false);
        }
    }

    /**
     * @author CCBlueX
     */
    @Overwrite
    public void onLivingUpdate() {
        EventManager.INSTANCE.callEvent(new UpdateEvent());

        if (sprintingTicksLeft > 0) {
            --sprintingTicksLeft;

            if (sprintingTicksLeft == 0) {
                setSprinting(false);
            }
        }

        if (sprintToggleTimer > 0) {
            --sprintToggleTimer;
        }

        prevTimeInPortal = timeInPortal;

        if (inPortal) {
            if (mc.currentScreen != null && !mc.currentScreen.doesGuiPauseGame() && !PortalMenu.INSTANCE.getState()) {
                mc.displayGuiScreen(null);
            }

            if (timeInPortal == 0f) {
                mc.getSoundHandler().playSound(PositionedSoundRecord.create(new ResourceLocation("portal.trigger"), rand.nextFloat() * 0.4F + 0.8F));
            }

            timeInPortal += 0.0125F;

            if (timeInPortal >= 1f) {
                timeInPortal = 1f;
            }

            inPortal = false;
        } else if (isPotionActive(Potion.confusion) && getActivePotionEffect(Potion.confusion).getDuration() > 60) {
            timeInPortal += 0.006666667F;

            if (timeInPortal > 1f) {
                timeInPortal = 1f;
            }
        } else {
            if (timeInPortal > 0f) {
                timeInPortal -= 0.05F;
            }

            if (timeInPortal < 0f) {
                timeInPortal = 0f;
            }
        }

        if (timeUntilPortal > 0) {
            --timeUntilPortal;
        }

        boolean flag = movementInput.jump;
        boolean flag1 = movementInput.sneak;
        float f = 0.8F;
        boolean flag2 = movementInput.moveForward >= f;
        movementInput.updatePlayerMoveState();

        final NoSlow noSlow = NoSlow.INSTANCE;
        final KillAura killAura = KillAura.INSTANCE;

        if (getHeldItem() != null && (isUsingItem() || (getHeldItem().getItem() instanceof ItemSword && killAura.getBlockStatus())) && !isRiding()) {
            final SlowDownEvent slowDownEvent = new SlowDownEvent(0.2F, 0.2F);
            EventManager.INSTANCE.callEvent(slowDownEvent);
            movementInput.moveStrafe *= slowDownEvent.getStrafe();
            movementInput.moveForward *= slowDownEvent.getForward();
            sprintToggleTimer = 0;
        }

        pushOutOfBlocks(posX - (double) width * 0.35D, getEntityBoundingBox().minY + 0.5D, posZ + (double) width * 0.35D);
        pushOutOfBlocks(posX - (double) width * 0.35D, getEntityBoundingBox().minY + 0.5D, posZ - (double) width * 0.35D);
        pushOutOfBlocks(posX + (double) width * 0.35D, getEntityBoundingBox().minY + 0.5D, posZ - (double) width * 0.35D);
        pushOutOfBlocks(posX + (double) width * 0.35D, getEntityBoundingBox().minY + 0.5D, posZ + (double) width * 0.35D);

        final Sprint sprint = Sprint.INSTANCE;

        final boolean legitSprint = sprint.getModeValue().get().equals("Legit");

        boolean flag3 = !(sprint.getState() && !legitSprint && sprint.getFoodValue().get()) || (float) getFoodStats().getFoodLevel() > 6f || capabilities.allowFlying;

        if (onGround && !flag1 && !flag2 && movementInput.moveForward >= f && !isSprinting() && flag3 && !isUsingItem() && !isPotionActive(Potion.blindness)) {
            if (sprintToggleTimer <= 0 && !mc.gameSettings.keyBindSprint.isKeyDown()) {
                sprintToggleTimer = 7;
            } else {
                setSprinting(true);
            }
        }

        if (!isSprinting() && movementInput.moveForward >= f && flag3 && (noSlow.getState() || !isUsingItem()) && !isPotionActive(Potion.blindness) && mc.gameSettings.keyBindSprint.isKeyDown()) {
            setSprinting(true);
        }

        final Scaffold scaffold = Scaffold.INSTANCE;

        final Rotation targetRotation = RotationUtils.INSTANCE.getTargetRotation();

        boolean shouldStop = targetRotation != null && movementInput.moveForward * MathHelper.cos((rotationYaw - targetRotation.getYaw()) * 3.1415927F / 180.0F) + movementInput.moveStrafe * MathHelper.sin((rotationYaw - targetRotation.getYaw()) * 3.1415927F / 180.0F) < 0.8;

        if ((scaffold.getState() && !scaffold.getSprintValue().get()) || (sprint.getState() && !legitSprint && sprint.getCheckServerSide().get() && (onGround || !sprint.getCheckServerSideGround().get()) && !sprint.getAllDirectionsValue().get() && shouldStop))
            setSprinting(false);

        if (isSprinting() && ((!(sprint.getState() && !legitSprint && sprint.getAllDirectionsValue().get()) && movementInput.moveForward < f) || isCollidedHorizontally || !flag3)) {
            setSprinting(false);
        }

        if (capabilities.allowFlying) {
            if (mc.playerController.isSpectatorMode()) {
                if (!capabilities.isFlying) {
                    capabilities.isFlying = true;
                    sendPlayerAbilities();
                }
            } else if (!flag && movementInput.jump) {
                if (flyToggleTimer == 0) {
                    flyToggleTimer = 7;
                } else {
                    capabilities.isFlying = !capabilities.isFlying;
                    sendPlayerAbilities();
                    flyToggleTimer = 0;
                }
            }
        }

        if (capabilities.isFlying && isCurrentViewEntity()) {
            if (movementInput.sneak) {
                motionY -= capabilities.getFlySpeed() * 3f;
            }

            if (movementInput.jump) {
                motionY += capabilities.getFlySpeed() * 3f;
            }
        }

        if (isRidingHorse()) {
            if (horseJumpPowerCounter < 0) {
                ++horseJumpPowerCounter;

                if (horseJumpPowerCounter == 0) {
                    horseJumpPower = 0f;
                }
            }

            if (flag && !movementInput.jump) {
                horseJumpPowerCounter = -10;
                sendHorseJump();
            } else if (!flag && movementInput.jump) {
                horseJumpPowerCounter = 0;
                horseJumpPower = 0f;
            } else if (flag) {
                ++horseJumpPowerCounter;

                if (horseJumpPowerCounter < 10) {
                    horseJumpPower = (float) horseJumpPowerCounter * 0.1F;
                } else {
                    horseJumpPower = 0.8F + 2f / (float) (horseJumpPowerCounter - 9) * 0.1F;
                }
            }
        } else {
            horseJumpPower = 0f;
        }

        super.onLivingUpdate();

        if (onGround && capabilities.isFlying && !mc.playerController.isSpectatorMode()) {
            capabilities.isFlying = false;
            sendPlayerAbilities();
        }
    }

    @Override
    public void moveEntity(double x, double y, double z) {
        MoveEvent moveEvent = new MoveEvent(x, y, z);
        EventManager.INSTANCE.callEvent(moveEvent);

        if (moveEvent.isCancelled()) return;

        x = moveEvent.getX();
        y = moveEvent.getY();
        z = moveEvent.getZ();

        if (noClip) {
            setEntityBoundingBox(getEntityBoundingBox().offset(x, y, z));
            posX = (getEntityBoundingBox().minX + getEntityBoundingBox().maxX) / 2.0D;
            posY = getEntityBoundingBox().minY;
            posZ = (getEntityBoundingBox().minZ + getEntityBoundingBox().maxZ) / 2.0D;
        } else {
            worldObj.theProfiler.startSection("move");
            double d0 = posX;
            double d1 = posY;
            double d2 = posZ;

            if (isInWeb) {
                isInWeb = false;
                x *= 0.25D;
                y *= 0.05000000074505806D;
                z *= 0.25D;
                motionX = 0.0D;
                motionY = 0.0D;
                motionZ = 0.0D;
            }

            double d3 = x;
            double d4 = y;
            double d5 = z;
            boolean flag = onGround && isSneaking();

            if (flag || moveEvent.isSafeWalk()) {
                double d6;

                //noinspection ConstantConditions
                for (d6 = 0.05D; x != 0.0D && worldObj.getCollidingBoundingBoxes((Entity) (Object) this, getEntityBoundingBox().offset(x, -1.0D, 0.0D)).isEmpty(); d3 = x) {
                    if (x < d6 && x >= -d6) {
                        x = 0.0D;
                    } else if (x > 0.0D) {
                        x -= d6;
                    } else {
                        x += d6;
                    }
                }

                //noinspection ConstantConditions
                for (; z != 0.0D && worldObj.getCollidingBoundingBoxes((Entity) (Object) this, getEntityBoundingBox().offset(0.0D, -1.0D, z)).isEmpty(); d5 = z) {
                    if (z < d6 && z >= -d6) {
                        z = 0.0D;
                    } else if (z > 0.0D) {
                        z -= d6;
                    } else {
                        z += d6;
                    }
                }

                //noinspection ConstantConditions
                for (; x != 0.0D && z != 0.0D && worldObj.getCollidingBoundingBoxes((Entity) (Object) this, getEntityBoundingBox().offset(x, -1.0D, z)).isEmpty(); d5 = z) {
                    if (x < d6 && x >= -d6) {
                        x = 0.0D;
                    } else if (x > 0.0D) {
                        x -= d6;
                    } else {
                        x += d6;
                    }

                    d3 = x;

                    if (z < d6 && z >= -d6) {
                        z = 0.0D;
                    } else if (z > 0.0D) {
                        z -= d6;
                    } else {
                        z += d6;
                    }
                }
            }

            //noinspection ConstantConditions
            List<AxisAlignedBB> list1 = worldObj.getCollidingBoundingBoxes((Entity) (Object) this, getEntityBoundingBox().addCoord(x, y, z));
            AxisAlignedBB axisalignedbb = getEntityBoundingBox();

            for (AxisAlignedBB axisalignedbb1 : list1) {
                y = axisalignedbb1.calculateYOffset(getEntityBoundingBox(), y);
            }

            setEntityBoundingBox(getEntityBoundingBox().offset(0.0D, y, 0.0D));
            boolean flag1 = onGround || d4 != y && d4 < 0.0D;

            for (AxisAlignedBB axisalignedbb2 : list1) {
                x = axisalignedbb2.calculateXOffset(getEntityBoundingBox(), x);
            }

            setEntityBoundingBox(getEntityBoundingBox().offset(x, 0.0D, 0.0D));

            for (AxisAlignedBB axisalignedbb13 : list1) {
                z = axisalignedbb13.calculateZOffset(getEntityBoundingBox(), z);
            }

            setEntityBoundingBox(getEntityBoundingBox().offset(0.0D, 0.0D, z));

            if (stepHeight > 0f && flag1 && (d3 != x || d5 != z)) {
                StepEvent stepEvent = new StepEvent(stepHeight);
                EventManager.INSTANCE.callEvent(stepEvent);
                double d11 = x;
                double d7 = y;
                double d8 = z;
                AxisAlignedBB axisalignedbb3 = getEntityBoundingBox();
                setEntityBoundingBox(axisalignedbb);
                y = stepEvent.getStepHeight();
                //noinspection ConstantConditions
                List<AxisAlignedBB> list = worldObj.getCollidingBoundingBoxes((Entity) (Object) this, getEntityBoundingBox().addCoord(d3, y, d5));
                AxisAlignedBB axisalignedbb4 = getEntityBoundingBox();
                AxisAlignedBB axisalignedbb5 = axisalignedbb4.addCoord(d3, 0.0D, d5);
                double d9 = y;

                for (AxisAlignedBB axisalignedbb6 : list) {
                    d9 = axisalignedbb6.calculateYOffset(axisalignedbb5, d9);
                }

                axisalignedbb4 = axisalignedbb4.offset(0.0D, d9, 0.0D);
                double d15 = d3;

                for (AxisAlignedBB axisalignedbb7 : list) {
                    d15 = axisalignedbb7.calculateXOffset(axisalignedbb4, d15);
                }

                axisalignedbb4 = axisalignedbb4.offset(d15, 0.0D, 0.0D);
                double d16 = d5;

                for (AxisAlignedBB axisalignedbb8 : list) {
                    d16 = axisalignedbb8.calculateZOffset(axisalignedbb4, d16);
                }

                axisalignedbb4 = axisalignedbb4.offset(0.0D, 0.0D, d16);
                AxisAlignedBB axisalignedbb14 = getEntityBoundingBox();
                double d17 = y;

                for (AxisAlignedBB axisalignedbb9 : list) {
                    d17 = axisalignedbb9.calculateYOffset(axisalignedbb14, d17);
                }

                axisalignedbb14 = axisalignedbb14.offset(0.0D, d17, 0.0D);
                double d18 = d3;

                for (AxisAlignedBB axisalignedbb10 : list) {
                    d18 = axisalignedbb10.calculateXOffset(axisalignedbb14, d18);
                }

                axisalignedbb14 = axisalignedbb14.offset(d18, 0.0D, 0.0D);
                double d19 = d5;

                for (AxisAlignedBB axisalignedbb11 : list) {
                    d19 = axisalignedbb11.calculateZOffset(axisalignedbb14, d19);
                }

                axisalignedbb14 = axisalignedbb14.offset(0.0D, 0.0D, d19);
                double d20 = d15 * d15 + d16 * d16;
                double d10 = d18 * d18 + d19 * d19;

                if (d20 > d10) {
                    x = d15;
                    z = d16;
                    y = -d9;
                    setEntityBoundingBox(axisalignedbb4);
                } else {
                    x = d18;
                    z = d19;
                    y = -d17;
                    setEntityBoundingBox(axisalignedbb14);
                }

                for (AxisAlignedBB axisalignedbb12 : list) {
                    y = axisalignedbb12.calculateYOffset(getEntityBoundingBox(), y);
                }

                setEntityBoundingBox(getEntityBoundingBox().offset(0.0D, y, 0.0D));

                if (d11 * d11 + d8 * d8 >= x * x + z * z) {
                    x = d11;
                    y = d7;
                    z = d8;
                    setEntityBoundingBox(axisalignedbb3);
                } else {
                    EventManager.INSTANCE.callEvent(new StepConfirmEvent());
                }
            }

            worldObj.theProfiler.endSection();
            worldObj.theProfiler.startSection("rest");
            posX = (getEntityBoundingBox().minX + getEntityBoundingBox().maxX) / 2.0D;
            posY = getEntityBoundingBox().minY;
            posZ = (getEntityBoundingBox().minZ + getEntityBoundingBox().maxZ) / 2.0D;
            isCollidedHorizontally = d3 != x || d5 != z;
            isCollidedVertically = d4 != y;
            onGround = isCollidedVertically && d4 < 0.0D;
            isCollided = isCollidedHorizontally || isCollidedVertically;
            int i = MathHelper.floor_double(posX);
            int j = MathHelper.floor_double(posY - 0.20000000298023224D);
            int k = MathHelper.floor_double(posZ);
            BlockPos blockpos = new BlockPos(i, j, k);
            Block block1 = worldObj.getBlockState(blockpos).getBlock();

            if (block1.getMaterial() == Material.air) {
                Block block = worldObj.getBlockState(blockpos.down()).getBlock();

                if (block instanceof BlockFence || block instanceof BlockWall || block instanceof BlockFenceGate) {
                    block1 = block;
                    blockpos = blockpos.down();
                }
            }

            updateFallState(y, onGround, block1, blockpos);

            if (d3 != x) {
                motionX = 0.0D;
            }

            if (d5 != z) {
                motionZ = 0.0D;
            }

            if (d4 != y) {
                //noinspection ConstantConditions
                block1.onLanded(worldObj, (Entity) (Object) this);
            }

            if (canTriggerWalking() && !flag && ridingEntity == null) {
                double d12 = posX - d0;
                double d13 = posY - d1;
                double d14 = posZ - d2;

                if (block1 != Blocks.ladder) {
                    d13 = 0.0D;
                }

                if (onGround) {
                    //noinspection ConstantConditions
                    block1.onEntityCollidedWithBlock(worldObj, blockpos, (Entity) (Object) this);
                }

                distanceWalkedModified = (float) ((double) distanceWalkedModified + (double) MathHelper.sqrt_double(d12 * d12 + d14 * d14) * 0.6D);
                distanceWalkedOnStepModified = (float) ((double) distanceWalkedOnStepModified + (double) MathHelper.sqrt_double(d12 * d12 + d13 * d13 + d14 * d14) * 0.6D);

                if (distanceWalkedOnStepModified > (float) getNextStepDistance() && block1.getMaterial() != Material.air) {
                    setNextStepDistance((int) distanceWalkedOnStepModified + 1);

                    if (isInWater()) {
                        float f = MathHelper.sqrt_double(motionX * motionX * 0.20000000298023224D + motionY * motionY + motionZ * motionZ * 0.20000000298023224D) * 0.35F;

                        if (f > 1f) {
                            f = 1f;
                        }

                        playSound(getSwimSound(), f, 1f + (rand.nextFloat() - rand.nextFloat()) * 0.4F);
                    }

                    playStepSound(blockpos, block1);
                }
            }

            try {
                doBlockCollisions();
            } catch (Throwable throwable) {
                CrashReport crashreport = CrashReport.makeCrashReport(throwable, "Checking entity block collision");
                CrashReportCategory crashreportcategory = crashreport.makeCategory("Entity being checked for collision");
                addEntityCrashInfo(crashreportcategory);
                throw new ReportedException(crashreport);
            }

            boolean flag2 = isWet();

            if (worldObj.isFlammableWithin(getEntityBoundingBox().contract(0.001D, 0.001D, 0.001D))) {
                dealFireDamage(1);

                if (!flag2) {
                    setFire(getFire() + 1);

                    if (getFire() == 0) {
                        setFire(8);
                    }
                }
            } else if (getFire() <= 0) {
                setFire(-fireResistance);
            }

            if (flag2 && getFire() > 0) {
                playSound("random.fizz", 0.7F, 1.6F + (rand.nextFloat() - rand.nextFloat()) * 0.4F);
                setFire(-fireResistance);
            }

            worldObj.theProfiler.endSection();
        }
    }
}

