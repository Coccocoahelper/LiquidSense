/*
 * LiquidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/CCBlueX/LiquidBounce/
 */
package net.ccbluex.liquidbounce.features.module.modules.movement

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.api.enums.EnumFacingType
import net.ccbluex.liquidbounce.api.minecraft.item.IItem
import net.ccbluex.liquidbounce.api.minecraft.network.play.client.ICPacketPlayerDigging
import net.ccbluex.liquidbounce.api.minecraft.util.WBlockPos
import net.ccbluex.liquidbounce.event.EventState
import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.MotionEvent
import net.ccbluex.liquidbounce.event.SlowDownEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.features.module.modules.combat.KillAura
import net.ccbluex.liquidbounce.features.module.modules.combat.TpAura
import net.ccbluex.liquidbounce.injection.backend.Backend
import net.ccbluex.liquidbounce.utils.extensions.isMoving
import net.ccbluex.liquidbounce.utils.timer.TickTimer
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.FloatValue
import net.ccbluex.liquidbounce.value.IntegerValue
import net.ccbluex.liquidbounce.value.ValueGroup

@ModuleInfo(name = "NoSlow", description = "Cancels slowness effects caused by soulsand and using items.", category = ModuleCategory.MOVEMENT)
class NoSlow : Module()
{
	private val blockForwardMultiplier = FloatValue("BlockForwardMultiplier", 1.0F, 0.2F, 1.0F)
	private val blockStrafeMultiplier = FloatValue("BlockStrafeMultiplier", 1.0F, 0.2F, 1.0F)

	private val consumeForwardMultiplier = FloatValue("ConsumeForwardMultiplier", 1.0F, 0.2F, 1.0F)
	private val consumeStrafeMultiplier = FloatValue("ConsumeStrafeMultiplier", 1.0F, 0.2F, 1.0F)

	private val bowForwardMultiplier = FloatValue("BowForwardMultiplier", 1.0F, 0.2F, 1.0F)
	private val bowStrafeMultiplier = FloatValue("BowStrafeMultiplier", 1.0F, 0.2F, 1.0F)

	val sneakForwardMultiplier = FloatValue("SneakForwardMultiplier", 0.3F, 0.3F, 1.0F)
	val sneakStrafeMultiplier = FloatValue("SneakStrafeMultiplier", 0.3F, 0.3F, 1.0F)

	val soulSandForwardMultiplier = FloatValue("SoulSandMultiplier", 0.4F, 0.4F, 1.0F)

	private val packetGroup = ValueGroup("Packet")
	private val packetEnabledValue = BoolValue("Enabled", true, "Packet")
	private val packetDelayValue = IntegerValue("Delay", 0, 0, 3, "Packet-PacketsDelay")
	private val packetBlockValue = BoolValue("Block", true, "Packet-Block")
	private val packetUnblock = BoolValue("Unblock", true, "Packet-Unblock")
	private val packetTimerValue = FloatValue("Timer", 1.0F, 0.1F, 1.0F, "Packet-Timer") // Set this 0.8 to bypass AAC NoSlowdown check

	// Blocks
	val liquidPushValue = BoolValue("LiquidPush", true)

	private var ncpDelay = TickTimer()

	init
	{
		packetGroup.addAll(packetEnabledValue, packetDelayValue, packetBlockValue, packetUnblock, packetTimerValue)
	}

	@EventTarget
	fun onMotion(event: MotionEvent)
	{
		val thePlayer = mc.thePlayer ?: return
		val heldItem = thePlayer.heldItem ?: return

		val provider = classProvider

		if (!provider.isItemSword(heldItem.item) || !thePlayer.isMoving) return

		val moduleManager = LiquidBounce.moduleManager

		val aura = moduleManager[KillAura::class.java] as KillAura
		val tpaura = moduleManager[TpAura::class.java] as TpAura

		val timer = mc.timer
		val packetTimer = packetTimerValue.get()

		if (timer.timerSpeed == packetTimer) timer.timerSpeed = 1.0F
		if (!thePlayer.isBlocking && !aura.serverSideBlockingStatus && !tpaura.serverSideBlockingStatus) return

		if (packetEnabledValue.get() && Backend.MINECRAFT_VERSION_MINOR == 8)
		{
			if (timer.timerSpeed != packetTimer) timer.timerSpeed = packetTimer

			if (ncpDelay.hasTimePassed(packetDelayValue.get()))
			{
				val netHandler = mc.netHandler

				when (event.eventState)
				{
					EventState.PRE -> if (packetUnblock.get()) netHandler.addToSendQueue(provider.createCPacketPlayerDigging(ICPacketPlayerDigging.WAction.RELEASE_USE_ITEM, WBlockPos(0, 0, 0), provider.getEnumFacing(EnumFacingType.DOWN))) // Un-block
					EventState.POST -> if (packetBlockValue.get()) netHandler.addToSendQueue(provider.createCPacketPlayerBlockPlacement(WBlockPos(-1, -1, -1), 255, thePlayer.inventory.getCurrentItemInHand(), 0.0F, 0.0F, 0.0F)) // (Re-)Block
				}

				ncpDelay.reset()
			}
		}
		ncpDelay.update()
	}

	@EventTarget
	fun onSlowDown(event: SlowDownEvent)
	{
		val heldItem = (mc.thePlayer ?: return).heldItem?.item

		event.forward = getMultiplier(heldItem, isForward = true)
		event.strafe = getMultiplier(heldItem, isForward = false)
	}

	private fun getMultiplier(item: IItem?, isForward: Boolean): Float
	{
		val provider = classProvider

		return when
		{
			provider.isItemFood(item) || provider.isItemPotion(item) || provider.isItemBucketMilk(item) -> if (isForward) consumeForwardMultiplier.get() else consumeStrafeMultiplier.get()
			provider.isItemSword(item) -> if (isForward) blockForwardMultiplier.get() else blockStrafeMultiplier.get()
			provider.isItemBow(item) -> if (isForward) bowForwardMultiplier.get() else bowStrafeMultiplier.get()
			else -> 0.2F
		}
	}

	override val tag: String?
		get() = if (packetEnabledValue.get()) "Packet" else null
}
