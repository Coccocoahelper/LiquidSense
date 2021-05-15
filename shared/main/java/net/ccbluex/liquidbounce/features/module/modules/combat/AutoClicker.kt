/*
 * LiquidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/CCBlueX/LiquidBounce/
 */
package net.ccbluex.liquidbounce.features.module.modules.combat

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.Render3DEvent
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.utils.misc.RandomUtils
import net.ccbluex.liquidbounce.utils.timer.TimeUtils
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.IntegerValue
import kotlin.random.Random

@ModuleInfo(name = "AutoClicker", description = "Constantly clicks when holding down a mouse button.", category = ModuleCategory.COMBAT)
class AutoClicker : Module()
{
	private val maxCPSValue: IntegerValue = object : IntegerValue("MaxCPS", 8, 1, 20)
	{

		override fun onChanged(oldValue: Int, newValue: Int)
		{
			val minCPS = minCPSValue.get()
			if (minCPS > newValue) set(minCPS)
		}
	}

	private val minCPSValue: IntegerValue = object : IntegerValue("MinCPS", 5, 1, 20)
	{

		override fun onChanged(oldValue: Int, newValue: Int)
		{
			val maxCPS = maxCPSValue.get()
			if (maxCPS < newValue) set(maxCPS)
		}
	}

	private val rightValue = BoolValue("Right", true)
	private val leftValue = BoolValue("Left", true)
	private val jitterValue = BoolValue("Jitter", false)

	private var rightDelay = TimeUtils.randomClickDelay(minCPSValue.get(), maxCPSValue.get())
	private var rightLastSwing = 0L
	private var leftDelay = TimeUtils.randomClickDelay(minCPSValue.get(), maxCPSValue.get())
	private var leftLastSwing = 0L

	@EventTarget
	fun onRender(@Suppress("UNUSED_PARAMETER") event: Render3DEvent)
	{
		val thePlayer = mc.thePlayer ?: return
		val gameSettings = mc.gameSettings

		// Left click
		if (gameSettings.keyBindAttack.isKeyDown && leftValue.get() && System.currentTimeMillis() - leftLastSwing >= leftDelay && mc.playerController.curBlockDamageMP == 0F)
		{
			gameSettings.keyBindAttack.onTick(gameSettings.keyBindAttack.keyCode) // Minecraft Click Handling

			leftLastSwing = System.currentTimeMillis()
			leftDelay = TimeUtils.randomClickDelay(minCPSValue.get(), maxCPSValue.get())
		}

		// Right click
		if (gameSettings.keyBindUseItem.isKeyDown && !thePlayer.isUsingItem && rightValue.get() && System.currentTimeMillis() - rightLastSwing >= rightDelay)
		{
			gameSettings.keyBindAttack.onTick(gameSettings.keyBindUseItem.keyCode) // Minecraft Click Handling

			rightLastSwing = System.currentTimeMillis()
			rightDelay = TimeUtils.randomClickDelay(minCPSValue.get(), maxCPSValue.get())
		}
	}

	@EventTarget
	fun onUpdate(@Suppress("UNUSED_PARAMETER") event: UpdateEvent)
	{
		val thePlayer = mc.thePlayer ?: return
		val gameSettings = mc.gameSettings

		if (jitterValue.get() && (leftValue.get() && gameSettings.keyBindAttack.isKeyDown && mc.playerController.curBlockDamageMP == 0F || rightValue.get() && gameSettings.keyBindUseItem.isKeyDown && !thePlayer.isUsingItem))
		{
			if (Random.nextBoolean()) thePlayer.rotationYaw += if (Random.nextBoolean()) -RandomUtils.nextFloat(0F, 1F) else RandomUtils.nextFloat(0F, 1F)

			if (Random.nextBoolean())
			{
				// Make sure pitch is not going into unlegit values
				thePlayer.rotationPitch = (thePlayer.rotationPitch + if (Random.nextBoolean()) -RandomUtils.nextFloat(0F, 1F) else RandomUtils.nextFloat(0F, 1F)).coerceIn(-90F, 90F)
			}
		}
	}

	override val tag: String
		get() = "${minCPSValue.get()} ~ ${maxCPSValue.get()}"
}
