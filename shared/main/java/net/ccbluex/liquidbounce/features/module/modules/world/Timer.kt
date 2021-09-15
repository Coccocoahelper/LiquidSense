/*
 * LiquidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/CCBlueX/LiquidBounce/
 */
package net.ccbluex.liquidbounce.features.module.modules.world

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.event.WorldEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.FloatValue

@ModuleInfo(name = "Timer", description = "Changes the speed of the entire game.", category = ModuleCategory.WORLD)
class Timer : Module()
{

	private val speedValue = FloatValue("Speed", 2F, 0.1F, 10F)
	private val onMoveValue = BoolValue("OnMove", true)

	override fun onDisable()
	{
		mc.timer.timerSpeed = 1F
	}

	@EventTarget
	fun onUpdate(@Suppress("UNUSED_PARAMETER") event: UpdateEvent)
	{
		val thePlayer = mc.thePlayer ?: return
		val timer = mc.timer

		if (MovementUtils.isMoving(thePlayer) || !onMoveValue.get())
		{
			timer.timerSpeed = speedValue.get()
			return
		}

		timer.timerSpeed = 1F
	}

	@EventTarget
	fun onWorld(event: WorldEvent)
	{
		if (event.worldClient != null) return

		state = false
	}

	override val tag: String
		get() = "${speedValue.get()}"
}
