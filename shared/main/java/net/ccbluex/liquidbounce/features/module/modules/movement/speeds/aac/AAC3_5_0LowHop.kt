/*
 * LiquidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/CCBlueX/LiquidBounce/
 */
package net.ccbluex.liquidbounce.features.module.modules.movement.speeds.aac

import net.ccbluex.liquidbounce.event.EventState
import net.ccbluex.liquidbounce.event.MoveEvent
import net.ccbluex.liquidbounce.features.module.modules.movement.speeds.SpeedMode
import net.ccbluex.liquidbounce.utils.MovementUtils

class AAC3_5_0LowHop : SpeedMode("AAC3.5.0-LowHop")
{
	private var firstJump = false
	private var waitForGround = false

	override fun onEnable()
	{
		firstJump = true
	}

	override fun onMotion(eventState: EventState)
	{
		val thePlayer = mc.thePlayer ?: return
		if (eventState != EventState.PRE) return

		if (MovementUtils.isMoving)
		{
			if (thePlayer.hurtTime <= 0)
			{
				if (thePlayer.onGround)
				{
					waitForGround = false
					if (!firstJump) firstJump = true

					jump(thePlayer)
					thePlayer.motionY = 0.41
				}
				else
				{
					if (waitForGround) return
					if (thePlayer.isCollidedHorizontally) return

					firstJump = false
					thePlayer.motionY -= 0.0149
				}
				if (!thePlayer.isCollidedHorizontally) MovementUtils.forward(if (firstJump) 0.0016 else 0.001799)
			}
			else
			{
				firstJump = true
				waitForGround = true
			}
		}
		else
		{
			thePlayer.motionZ = 0.0
			thePlayer.motionX = 0.0
		}

		MovementUtils.strafe()
	}

	override fun onUpdate()
	{
	}

	override fun onMove(event: MoveEvent)
	{
	}
}
