/*
 * LiquidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/CCBlueX/LiquidBounce/
 */
package net.ccbluex.liquidbounce.features.module.modules.movement

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.JumpEvent
import net.ccbluex.liquidbounce.event.MoveEvent
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.features.module.modules.exploit.Damage
import net.ccbluex.liquidbounce.features.module.modules.world.Scaffold
import net.ccbluex.liquidbounce.features.module.modules.world.Tower
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.Notification
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.NotificationIcon
import net.ccbluex.liquidbounce.utils.extensions.cos
import net.ccbluex.liquidbounce.utils.extensions.forward
import net.ccbluex.liquidbounce.utils.extensions.isMoving
import net.ccbluex.liquidbounce.utils.extensions.moveDirectionDegrees
import net.ccbluex.liquidbounce.utils.extensions.moveDirectionRadians
import net.ccbluex.liquidbounce.utils.extensions.sin
import net.ccbluex.liquidbounce.utils.extensions.speed
import net.ccbluex.liquidbounce.utils.extensions.strafe
import net.ccbluex.liquidbounce.utils.extensions.zeroXZ
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.FloatValue
import net.ccbluex.liquidbounce.value.IntegerValue
import net.ccbluex.liquidbounce.value.ListValue

// FIXME: Fix broken LongJump
@ModuleInfo(name = "LongJump", description = "Allows you to jump further.", category = ModuleCategory.MOVEMENT)
class LongJump : Module()
{
    /**
     * Options
     */
    private val modeValue = ListValue("Mode", arrayOf("NCP", "Teleport", "AAC3.0.1", "AAC3.0.5", "AAC3.1.0", "Mineplex", "Mineplex2", "Mineplex3", "RedeSky"), "NCP")

    /**
     * NCP mode - Boost motion multiplier
     */
    private val ncpBoostValue = FloatValue("NCPBoost", 4.25f, 1f, 10f)

    /**
     * Teleport mode - Teleport Distance
     */
    private val teleportDistanceValue = FloatValue("TeleportDistance", 2.5f, 1.0f, 10.0f)

    /**
     * Teleport mode - Air-ticks to teleport
     */
    private val teleportAtTicksValue = IntegerValue("TeleportAtTicks", 0, 0, 10)

    /**
     * Automatically jump when player is on ground
     */
    private val autoJumpValue = BoolValue("AutoJump", false)

    /**
     * Automatically disable LongJump after landing
     */
    private val autoDisableValue = BoolValue("AutoDisable", true)

    /**
     * Disable Scaffold and Tower to bypass fastplace (and scaffold) checks
     */
    private val autoDisableScaffoldValue = BoolValue("DisableScaffoldAndTower", true)

    /**
     * Damage on start (Warning: Only works when AutoJump option enabled)
     */
    private val damageOnStartValue = object : BoolValue("DamageOnStart", false)
    {
        override fun onChanged(oldValue: Boolean, newValue: Boolean)
        {
            if (newValue) autoJumpValue.set(true)
        }
    }
    private val damageModeValue = ListValue("DamageMode", arrayOf("NCP", "Hypixel"), "NCP")

    /**
     * Variables
     */
    private var jumped = false
    private var canBoost = false
    private var boosted = false
    private var canMineplex2Boost = false

    private var teleportTicks = 0

    override fun onEnable()
    {
        boosted = false
        jumped = false
        canBoost = false
        canMineplex2Boost = false

        if (autoDisableScaffoldValue.get())
        {
            val moduleManager = LiquidBounce.moduleManager

            val scaffold = moduleManager[Scaffold::class.java]
            val tower = moduleManager[Tower::class.java]

            val disableScaffold = scaffold.state
            val disableTower = tower.state

            if (disableScaffold) scaffold.state = false
            if (disableTower) tower.state = false

            if (disableScaffold || disableTower) LiquidBounce.hud.addNotification(Notification(NotificationIcon.INFORMATION, "LongJump", "Disabled ${if (disableScaffold && disableTower) "Scaffold and Tower" else if (disableScaffold) "Scaffold" else "Tower"}", 1000))
        }
    }

    @EventTarget
    fun onUpdate(@Suppress("UNUSED_PARAMETER") event: UpdateEvent)
    {
        val thePlayer = mc.thePlayer ?: return

        if (LadderJump.jumped) thePlayer.strafe(thePlayer.speed * 1.08f)

        val autoDisable = autoDisableValue.get()

        if (jumped)
        {
            val mode = modeValue.get()

            if (thePlayer.onGround || thePlayer.capabilities.isFlying)
            {
                jumped = false
                canMineplex2Boost = false

                if (mode.equals("NCP", ignoreCase = true)) thePlayer.zeroXZ()

                if (boosted && autoDisable) state = false
                return
            }

            run {
                when (mode.toLowerCase())
                {
                    "ncp" ->
                    {
                        thePlayer.strafe(thePlayer.speed * if (canBoost) ncpBoostValue.get() else 1f)
                        canBoost = false
                        if (autoDisable) state = false
                    }

                    "aac3.0.1" ->
                    {
                        thePlayer.motionY += 0.05999
                        thePlayer.strafe(thePlayer.speed * 1.08f)
                        boosted = true
                    }

                    "aac3.0.5", "mineplex3" ->
                    {
                        thePlayer.jumpMovementFactor = 0.09f
                        thePlayer.motionY += 0.0132099999999999999999999999999
                        thePlayer.jumpMovementFactor = 0.08f

                        thePlayer.strafe()

                        boosted = true
                    }

                    "aac3.1.0" ->
                    {
                        if (thePlayer.fallDistance > 0.5f && canBoost && !boosted)
                        {
                            thePlayer.forward(3.0, thePlayer.moveDirectionDegrees)
                            boosted = true
                            if (autoDisable) state = false
                        }
                    }

                    "mineplex" ->
                    {
                        thePlayer.motionY += 0.0132099999999999999999999999999
                        thePlayer.jumpMovementFactor = 0.08f
                        boosted = true
                        thePlayer.strafe()
                    }

                    "mineplex2" ->
                    {
                        if (!canMineplex2Boost) return@run

                        thePlayer.jumpMovementFactor = 0.1f

                        if (thePlayer.fallDistance > 1.5f)
                        {
                            thePlayer.jumpMovementFactor = 0f
                            thePlayer.motionY = -10.0
                        }

                        boosted = true
                        thePlayer.strafe()
                    }

                    "redesky" ->
                    {
                        thePlayer.jumpMovementFactor = 0.15f
                        thePlayer.motionY += 0.05f

                        boosted = true
                    }
                }
            }
        }

        if (autoJumpValue.get() && thePlayer.onGround && thePlayer.isMoving)
        {
            jumped = true

            if (damageOnStartValue.get()) when (damageModeValue.get().toLowerCase())
            {
                "ncp" -> Damage.ncpDamage()
                "hypixel" -> Damage.hypixelDamage()
            }

            thePlayer.jump()
        }
    }

    @EventTarget
    fun onMove(event: MoveEvent)
    {
        val thePlayer = mc.thePlayer ?: return
        val mode = modeValue.get().toLowerCase()

        if (mode == "mineplex3")
        {
            if (thePlayer.fallDistance != 0.0f) thePlayer.motionY += 0.037
        }
        else if (jumped)
        {
            val isTeleportMode = mode == "teleport"

            if (isTeleportMode && thePlayer.isMoving && canBoost)
            {
                if (teleportTicks >= teleportAtTicksValue.get())
                {
                    val dir = thePlayer.moveDirectionRadians
                    val teleportDistance = teleportDistanceValue.get()

                    event.x = (-dir.sin * teleportDistance).toDouble()
                    event.z = (dir.cos * teleportDistance).toDouble()

                    canBoost = false
                    boosted = true
                    teleportTicks = 0
                    if (autoDisableValue.get()) state = false
                }
                teleportTicks++
            }

            if (!thePlayer.isMoving && mode == "ncp")
            {
                event.zeroXZ()
                thePlayer.zeroXZ()
            }
        }
    }

    @EventTarget(ignoreCondition = true)
    fun onJump(event: JumpEvent)
    {
        val thePlayer = mc.thePlayer ?: return

        jumped = true
        canBoost = true

        if (state)
        {
            if (damageOnStartValue.get()) when (damageModeValue.get().toLowerCase())
            {
                "ncp" -> Damage.ncpDamage()
                "hypixel" -> Damage.hypixelDamage()
            }

            when (modeValue.get().toLowerCase())
            {
                "mineplex" -> event.motion = event.motion * 4.08f

                "mineplex2" -> if (thePlayer.isCollidedHorizontally)
                {
                    event.motion = 2.31f
                    canMineplex2Boost = true
                    thePlayer.onGround = false
                }
            }
        }
    }

    override val tag: String
        get() = when
        {
            modeValue.get().equals("NCP", ignoreCase = true) -> "NCP-${ncpBoostValue.get()}"
            modeValue.get().equals("Teleport", ignoreCase = true) -> "Teleport-${teleportDistanceValue.get()}-At ${teleportAtTicksValue.get()}"
            else -> modeValue.get()
        }
}
