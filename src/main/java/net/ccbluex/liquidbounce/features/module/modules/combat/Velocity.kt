/*
 * LiquidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/CCBlueX/LiquidBounce/
 */
package net.ccbluex.liquidbounce.features.module.modules.combat

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.event.*
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.features.module.modules.movement.Speed
import net.ccbluex.liquidbounce.utils.RotationUtils
import net.ccbluex.liquidbounce.utils.extensions.*
import net.ccbluex.liquidbounce.utils.timer.MSTimer
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.FloatValue
import net.ccbluex.liquidbounce.value.ListValue
import net.ccbluex.liquidbounce.value.ValueGroup
import net.minecraft.client.entity.EntityPlayerSP
import net.minecraft.network.play.client.C03PacketPlayer.C04PacketPlayerPosition
import net.minecraft.network.play.server.S12PacketEntityVelocity
import net.minecraft.util.BlockPos
import net.minecraft.util.Vec3

@ModuleInfo(name = "Velocity", description = "Allows you to modify the amount of knockback you take. (a.k.a. AntiKnockback)", category = ModuleCategory.COMBAT)
class Velocity : Module()
{

    /**
     * OPTIONS
     */
    val horizontalValue = FloatValue("Horizontal", 0F, 0F, 1F)
    val verticalValue = FloatValue("Vertical", 0F, 0F, 1F)
    val modeValue = ListValue("Mode", arrayOf("Simple", "AAC3.1.2", "AACPush", "AAC3.2.0-Reverse", "AAC3.3.4-Reverse", "AAC3.5.0-Zero", "Jump", "Glitch", "Phase", "PacketPhase", "Legit"), "Simple")

    // AAC Reverse
    private val reverseStrengthValue = object : FloatValue("AAC3.2.0-ReverseStrength", 1F, 0.1F, 1F, "AAC3.2.0-Reverse-Strength")
    {
        override fun showCondition() = modeValue.get().endsWith("AAC3.2.0-Reverse", ignoreCase = true)
    }

    private val reverse2StrengthValue = object : FloatValue("AAC3.3.4-ReverseStrength", 0.05F, 0.02F, 0.1F, "AAC3.3.4-Reverse-Strength")
    {
        override fun showCondition() = modeValue.get().endsWith("AAC3.3.4-Reverse", ignoreCase = true)
    }

    private val aacPushGroup = object : ValueGroup("AACPush")
    {
        override fun showCondition() = modeValue.get().equals("AACPush", ignoreCase = true)
    }
    private val aacPushXZReducerValue = FloatValue("XZReducer", 2F, 1F, 3F, "AACPushXZReducer")
    private val aacPushYReducerValue = BoolValue("YReducer", true, "AACPushYReducer")

    private val phaseGroup = object : ValueGroup("Phase")
    {
        override fun showCondition() = modeValue.get().endsWith("Phase", ignoreCase = true)
    }
    private val phaseHeightValue = FloatValue("Height", 0.5F, 0F, 1F, "PhaseHeight")
    private val phaseOnlyGround = BoolValue("OnlyGround", true, "PhaseOnlyGround")

    private val legitGroup = object : ValueGroup("Legit")
    {
        override fun showCondition() = modeValue.get().equals("Legiot", ignoreCase = true)
    }
    private val legitStrafeValue = BoolValue("Strafe", false, "LegitStrafe")
    private val legitFaceValue = BoolValue("Face", true, "LegitFace")

    init
    {
        aacPushGroup.addAll(aacPushXZReducerValue, aacPushYReducerValue)
        phaseGroup.addAll(phaseHeightValue, phaseOnlyGround)
        legitGroup.addAll(legitStrafeValue, legitFaceValue)
    }

    /**
     * VALUES
     */
    var velocityTimer = MSTimer()
    var velocityInput = false

    // SmoothReverse
    private var reverseHurt = false

    // AACPush
    private var jump = false

    // Legit
    private var pos: BlockPos? = null

    override val tag: String
        get() = modeValue.get()

    override fun onDisable()
    {
        mc.thePlayer?.speedInAir = 0.02F
    }

    @EventTarget
    fun onUpdate(@Suppress("UNUSED_PARAMETER") event: UpdateEvent)
    {
        val thePlayer = mc.thePlayer ?: return

        if (thePlayer.isInWater || thePlayer.isInLava || thePlayer.isInWeb) return

        when (modeValue.get().lowercase())
        {
            "jump" -> if (thePlayer.hurtTime > 0 && thePlayer.onGround)
            {
                thePlayer.motionY = 0.42

                thePlayer.boost(0.2F)
            }

            "glitch" ->
            {
                thePlayer.noClip = velocityInput

                if (thePlayer.hurtTime == 7) thePlayer.motionY = 0.4

                velocityInput = false
            }

            "aac3.1.2" -> if (velocityInput && velocityTimer.hasTimePassed(80L))
            {
                thePlayer.multiply(horizontalValue.get())

                //mc.thePlayer.motionY *= verticalValue.get() ?

                velocityInput = false
            }

            "aac3.2.0-reverse" ->
            {
                if (!velocityInput) return

                if (!thePlayer.onGround) thePlayer.strafe(thePlayer.speed_f * reverseStrengthValue.get())
                else if (velocityTimer.hasTimePassed(80L)) velocityInput = false
            }

            "aac3.3.4-reverse" ->
            {
                if (!velocityInput)
                {
                    thePlayer.speedInAir = 0.02F
                    return
                }

                if (thePlayer.hurtTime > 0) reverseHurt = true

                if (!thePlayer.onGround)
                {
                    if (reverseHurt) thePlayer.speedInAir = reverse2StrengthValue.get()
                }
                else if (velocityTimer.hasTimePassed(80L))
                {
                    velocityInput = false
                    reverseHurt = false
                }
            }

            "aacpush" ->
            {
                if (jump)
                {
                    if (thePlayer.onGround) jump = false
                }
                else
                {

                    // Strafe
                    if (thePlayer.hurtTime > 0 && thePlayer.motionX != 0.0 && thePlayer.motionZ != 0.0) thePlayer.onGround = true

                    // Reduce Y
                    if (thePlayer.hurtResistantTime > 0 && aacPushYReducerValue.get() && !LiquidBounce.moduleManager[Speed::class.java].state) thePlayer.motionY -= 0.014999993
                }

                // Reduce XZ
                if (thePlayer.hurtResistantTime >= 19) thePlayer.divide(aacPushXZReducerValue.get())
            }

            "aac3.5.0-zero" -> if (thePlayer.hurtTime > 0)
            {
                if (!velocityInput || thePlayer.onGround || thePlayer.fallDistance > 2F) return

                // Generate AAC Movement-check flag
                thePlayer.motionY -= 1.0
                thePlayer.isAirBorne = true
                thePlayer.onGround = true
            }
            else velocityInput = false
        }
    }

    @EventTarget
    fun onPacket(event: PacketEvent)
    {
        val thePlayer = mc.thePlayer ?: return

        val packet = event.packet

        if (packet is S12PacketEntityVelocity)
        {
            if ((mc.theWorld?.getEntityByID(packet.entityID) ?: return) != thePlayer) return

            velocityTimer.reset()

            when (modeValue.get().lowercase())
            {
                "simple" -> if (applySimpleVelocity(packet)) event.cancelEvent()

                "aac3.1.2", "aac3.2.0-reverse", "aac3.3.4-reverse", "aac3.5.0-zero" -> velocityInput = true

                "phase" -> if (applyPhaseVelocity(thePlayer)) event.cancelEvent()

                "packetphase" -> if (applyPacketPhaseVelocity(thePlayer, packet)) event.cancelEvent()

                "legit" -> pos = BlockPos(thePlayer.posX, thePlayer.posY, thePlayer.posZ)

                "glitch" ->
                {
                    if (!thePlayer.onGround) return

                    velocityInput = true
                    event.cancelEvent()
                }
            }
        }

        // Explosion packets are handled by MixinNetHandlerPlayClient
    }

    @EventTarget
    fun onJump(event: JumpEvent)
    {
        val thePlayer = mc.thePlayer ?: return

        if (thePlayer.isInWater || thePlayer.isInLava || thePlayer.isInWeb) return

        when (modeValue.get().lowercase())
        {
            "aacpush" ->
            {
                jump = true

                if (!thePlayer.isCollidedVertically) event.cancelEvent()
            }

            "aac3.5.0-zero" -> if (thePlayer.hurtTime > 0) event.cancelEvent()
        }
    }

    @EventTarget
    fun onStrafe(event: StrafeEvent)
    {
        if (modeValue.get().equals("Legit", ignoreCase = true)) return

        val thePlayer = mc.thePlayer ?: return
        if (pos == null || thePlayer.hurtTime <= 0) return

        val rotation = RotationUtils.toRotation(thePlayer, Vec3(pos!!.x.toDouble(), pos!!.y.toDouble(), pos!!.z.toDouble()), false, RotationUtils.MinMaxPair.ZERO)
        if (legitFaceValue.get()) RotationUtils.setTargetRotation(rotation)

        if (legitStrafeValue.get()) thePlayer.strafe(directionDegrees = rotation.yaw)
        else thePlayer.simulateStrafe(event.forward, event.strafe, event.friction, rotation.yaw)
    }

    private fun applySimpleVelocity(packet: S12PacketEntityVelocity): Boolean
    {
        val horizontal = horizontalValue.get()
        val vertical = verticalValue.get()

        if (horizontal == 0F && vertical == 0F) return true

        packet.motionX = (packet.motionX * horizontal).toInt()
        packet.motionY = (packet.motionY * vertical).toInt()
        packet.motionZ = (packet.motionZ * horizontal).toInt()
        return false;
    }

    private fun applyPacketPhaseVelocity(thePlayer: EntityPlayerSP, packet: S12PacketEntityVelocity): Boolean = if (!(!thePlayer.onGround && phaseOnlyGround.get() || packet.motionX < 500 && packet.motionY < 500))
    {
        mc.netHandler.addToSendQueue(C04PacketPlayerPosition(thePlayer.posX, thePlayer.posY - phaseHeightValue.get(), thePlayer.posZ, false))
        true
    }
    else false

    private fun applyPhaseVelocity(thePlayer: EntityPlayerSP): Boolean = if (thePlayer.onGround || !phaseOnlyGround.get())
    {
        velocityInput = true
        thePlayer.setPositionAndUpdate(thePlayer.posX, thePlayer.posY - phaseHeightValue.get(), thePlayer.posZ)
        true
    }
    else false
}
