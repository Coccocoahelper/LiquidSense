/*
 * LiquidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/CCBlueX/LiquidBounce/
 */
package net.ccbluex.liquidbounce.features.module.modules.player

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.utils.extensions.fixedSensitivityPitch
import net.ccbluex.liquidbounce.utils.extensions.fixedSensitivityYaw
import net.ccbluex.liquidbounce.utils.misc.RandomUtils
import net.ccbluex.liquidbounce.utils.timer.MSTimer
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.FloatValue
import net.ccbluex.liquidbounce.value.IntegerValue
import net.ccbluex.liquidbounce.value.ListValue
import net.minecraft.client.settings.GameSettings

object AntiAFK : Module("AntiAFK", "Prevents you from getting kicked for being AFK.", ModuleCategory.PLAYER) {

    private val swingDelayTimer = MSTimer()
    private val delayTimer = MSTimer()

    private val modeValue = ListValue("Mode", arrayOf("Old", "Random", "Custom"), "Random")

    private val rotateValue = object : BoolValue("Rotate", true) {
        override fun isSupported() = modeValue.get() == "Custom"
    }
    private val rotationDelayValue = object : IntegerValue("RotationDelay", 100, 0, 1000) {
        override fun isSupported() = rotateValue.isActive()
    }
    private val rotationAngleValue = object : FloatValue("RotationAngle", 1f, -180F, 180F) {
        override fun isSupported() = rotateValue.isActive()
    }
    private val swingValue = object : BoolValue("Swing", true) {
        override fun isSupported() = modeValue.get() == "Custom"
    }
    private val swingDelayValue = object : IntegerValue("SwingDelay", 100, 0, 1000) {
        override fun isSupported() = swingValue.isActive()
    }
    private val jumpValue = object : BoolValue("Jump", true) {
        override fun isSupported() = modeValue.get() == "Custom"
    }
    private val moveValue = object : BoolValue("Move", true) {
        override fun isSupported() = modeValue.get() == "Custom"
    }

    private var shouldMove = false
    private var randomTimerDelay = 500L

    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        val thePlayer = mc.thePlayer ?: return

        when (modeValue.get().lowercase()) {
            "old" -> {
                mc.gameSettings.keyBindForward.pressed = true

                if (delayTimer.hasTimePassed(500)) {
                    thePlayer.fixedSensitivityYaw += 180F
                    delayTimer.reset()
                }
            }
            "random" -> {
                getRandomMoveKeyBind().pressed = shouldMove

                if (!delayTimer.hasTimePassed(randomTimerDelay)) return
                shouldMove = false
                randomTimerDelay = 500L
                when (RandomUtils.nextInt(0, 6)) {
                    0 -> {
                        if (thePlayer.onGround) thePlayer.jump()
                        delayTimer.reset()
                    }
                    1 -> {
                        if (!thePlayer.isSwingInProgress) thePlayer.swingItem()
                            delayTimer.reset()
                        }
                        2 -> {
                            randomTimerDelay = RandomUtils.nextInt(0, 1000).toLong()
                            shouldMove = true
                            delayTimer.reset()
                        }
                        3 -> {
                            thePlayer.inventory.currentItem = RandomUtils.nextInt(0, 9)
                            mc.playerController.updateController()
                            delayTimer.reset()
                        }
                        4 -> {
                            thePlayer.fixedSensitivityYaw += RandomUtils.nextFloat(-180f, 180f)
                            delayTimer.reset()
                        }
                        5 -> {
                            thePlayer.fixedSensitivityPitch += RandomUtils.nextFloat(-10f, 10f)
                            delayTimer.reset()
                        }
                    }
            }
            "custom" -> {
                if (moveValue.get())
                    mc.gameSettings.keyBindForward.pressed = true

                if (jumpValue.get() && thePlayer.onGround)
                    thePlayer.jump()

                if (rotateValue.get() && delayTimer.hasTimePassed(rotationDelayValue.get())) {
                    thePlayer.fixedSensitivityYaw += rotationAngleValue.get()
                    thePlayer.fixedSensitivityPitch += RandomUtils.nextFloat(0F, 1F) * 2 - 1
                    delayTimer.reset()
                }

                if (swingValue.get() && !thePlayer.isSwingInProgress && swingDelayTimer.hasTimePassed(swingDelayValue.get())) {
                    thePlayer.swingItem()
                    swingDelayTimer.reset()
                }
            }
        }
    }

    private val moveKeyBindings =
        setOf(mc.gameSettings.keyBindForward,mc.gameSettings.keyBindLeft, mc.gameSettings.keyBindBack, mc.gameSettings.keyBindRight)

    private fun getRandomMoveKeyBind() = moveKeyBindings.random()

    override fun onDisable() {
        if (!GameSettings.isKeyDown(mc.gameSettings.keyBindForward))
            mc.gameSettings.keyBindForward.pressed = false
    }
}