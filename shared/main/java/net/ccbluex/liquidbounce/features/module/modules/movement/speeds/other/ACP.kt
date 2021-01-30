package net.ccbluex.liquidbounce.features.module.modules.movement.speeds.other

import net.ccbluex.liquidbounce.api.minecraft.potion.PotionType
import net.ccbluex.liquidbounce.event.EventState
import net.ccbluex.liquidbounce.event.MoveEvent
import net.ccbluex.liquidbounce.features.module.modules.movement.speeds.SpeedMode
import net.ccbluex.liquidbounce.utils.MovementUtils.isMoving
import net.ccbluex.liquidbounce.utils.MovementUtils.strafe

/**
 * LiquidBounce Hacked Client A minecraft forge injection client using Mixin
 *
 * @author CCBlueX
 * @game   Minecraft
 */
class ACP : SpeedMode("AntiCheatPlus")
{
	override fun onMotion(eventState: EventState)
	{
		val thePlayer = mc.thePlayer ?: return

		if (isMoving)
		{
			val amplifier = if (thePlayer.isPotionActive(classProvider.getPotionEnum(PotionType.MOVE_SPEED))) thePlayer.getActivePotionEffect(classProvider.getPotionEnum(PotionType.MOVE_SPEED))!!.amplifier else -1

			thePlayer.motionX *= 0.8
			thePlayer.motionZ *= 0.8

			val moveSpeed = if (thePlayer.onGround) when (amplifier)
			{
				0 -> 0.70F //0.31 +6 +6 +
				1 -> 0.75F // 0.37 - previous value
				2 -> 0.85F // 0.41
				3 -> 0.95F // 0.45
				4 -> 1.05F // 0.49
				5 -> 1.15F // 0.53
				else -> 0.5F
			}
			else when (amplifier)
			{
				0 -> 0.85F // 0.31 +6 +6 +
				1 -> 0.91F // 0.37 - previous value
				2 -> 1.01F // 0.41
				3 -> 1.12F // 0.45
				4 -> 1.23F // 0.49
				5 -> 1.35F // 0.53
				else -> 0.55F
			}

			strafe(moveSpeed)
		}
		else
		{
			thePlayer.motionX = 0.0
			thePlayer.motionZ = 0.0
		}
	}

	override fun onUpdate()
	{
	}

	override fun onMove(event: MoveEvent)
	{
	}
}
