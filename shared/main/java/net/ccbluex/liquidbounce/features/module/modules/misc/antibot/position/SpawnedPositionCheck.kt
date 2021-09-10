package net.ccbluex.liquidbounce.features.module.modules.misc.antibot.position

import net.ccbluex.liquidbounce.api.minecraft.client.entity.IEntity
import net.ccbluex.liquidbounce.api.minecraft.client.entity.player.IEntityPlayer
import net.ccbluex.liquidbounce.api.minecraft.client.multiplayer.IWorldClient
import net.ccbluex.liquidbounce.api.minecraft.util.WMathHelper
import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.event.Render3DEvent
import net.ccbluex.liquidbounce.features.module.modules.misc.AntiBot
import net.ccbluex.liquidbounce.features.module.modules.misc.antibot.BotCheck
import net.ccbluex.liquidbounce.utils.misc.StringUtils
import net.ccbluex.liquidbounce.utils.render.ColorUtils
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import kotlin.math.hypot
import kotlin.math.pow
import kotlin.math.sqrt

class SpawnedPositionCheck : BotCheck("position.spawnedPosition")
{
	override val isActive: Boolean
		get() = AntiBot.positionSpawnedPositionEnabledValue.get()

	companion object
	{
		val spawnPositionSuspects = mutableSetOf<Int>()
	}

	private val spawnPosition = mutableSetOf<Int>()

	override fun isBot(theWorld: IWorldClient, thePlayer: IEntity, target: IEntityPlayer): Boolean = target.entityId in spawnPosition

	override fun onPacket(event: PacketEvent)
	{
		val thePlayer = mc.thePlayer ?: return

		val packet = event.packet

		if (classProvider.isSPacketSpawnPlayer(packet))
		{
			val playerSpawnPacket = packet.asSPacketSpawnPlayer()

			val entityId = playerSpawnPacket.entityID

			val entityX: Double = playerSpawnPacket.x.toDouble() / 32.0
			val entityY: Double = playerSpawnPacket.y.toDouble() / 32.0
			val entityZ: Double = playerSpawnPacket.z.toDouble() / 32.0

			val serverLocation = getPingCorrectionAppliedLocation(thePlayer)

			val serverPos = serverLocation.position
			val serverYaw = serverLocation.rotation.yaw

			val yawRadians = WMathHelper.toRadians(serverYaw - 180.0F)

			val func = functions

			val deltaLimit = AntiBot.positionSpawnedPositionDeltaThresholdValue.get().pow(2)

			if (hypot(serverPos.xCoord - entityX, serverPos.zCoord - entityZ) <= 10 && entityY >= serverPos.yCoord) spawnPositionSuspects.add(entityId)

			for ((posIndex, back, y) in arrayOf(Triple(1, AntiBot.positionSpawnedPositionPosition1BackValue.get(), AntiBot.positionSpawnedPositionPosition1YValue.get()), Triple(2, AntiBot.positionPosition2BackValue.get(), AntiBot.positionSpawnedPositionPosition2YValue.get())))
			{
				val expectDeltaX = serverPos.xCoord - func.sin(yawRadians) * back - entityX
				val expectDeltaY = serverPos.yCoord + y - entityY
				val expectDeltaZ = serverPos.zCoord + func.cos(yawRadians) * back - entityZ

				val delta = expectDeltaX * expectDeltaX + expectDeltaY * expectDeltaY + expectDeltaZ * expectDeltaZ

				// Position Delta
				if (delta <= deltaLimit)
				{
					notification { "Suspicious spawn #$entityId (posIndex: $posIndex, dist: ${StringUtils.DECIMALFORMAT_6.format(sqrt(delta))})" }
					spawnPosition.add(entityId)
				}
			}
		}
	}

	override fun onRender3D(@Suppress("UNUSED_PARAMETER") event: Render3DEvent)
	{
		if (!AntiBot.positionMarkEnabledValue.get()) return

		val thePlayer = mc.thePlayer ?: return

		val partialTicks = event.partialTicks

		val serverLocation = getPingCorrectionAppliedLocation(thePlayer)
		val lastServerLocation = getPingCorrectionAppliedLocation(thePlayer, 1)

		val lastServerYaw = lastServerLocation.rotation.yaw

		val serverPos = serverLocation.position
		val lastServerPos = lastServerLocation.position

		val yaw = lastServerYaw + (serverLocation.rotation.yaw - lastServerYaw) * partialTicks

		val dir = WMathHelper.toRadians(yaw - 180.0F)

		val func = functions

		val sin = -func.sin(dir)
		val cos = func.cos(dir)

		val posX = lastServerPos.xCoord + (serverPos.xCoord - lastServerPos.xCoord) * partialTicks
		val posY = lastServerPos.yCoord + (serverPos.yCoord - lastServerPos.yCoord) * partialTicks
		val posZ = lastServerPos.zCoord + (serverPos.zCoord - lastServerPos.zCoord) * partialTicks

		val provider = classProvider

		val renderManager = mc.renderManager
		val renderPosX = renderManager.renderPosX
		val renderPosY = renderManager.renderPosY
		val renderPosZ = renderManager.renderPosZ

		val alpha = AntiBot.positionMarkAlphaValue.get()

		val deltaLimit = AntiBot.positionSpawnedPositionDeltaThresholdValue.get()

		val width = thePlayer.width + deltaLimit
		val height = thePlayer.height + deltaLimit

		val bb = provider.createAxisAlignedBB(-width - renderPosX, -renderPosY, -width - renderPosZ, width - renderPosX, height - renderPosY, width - renderPosZ)

		for ((back, y, color) in arrayOf(Triple(AntiBot.positionSpawnedPositionPosition1BackValue.get(), AntiBot.positionSpawnedPositionPosition1YValue.get(), 0x0088FF), Triple(AntiBot.positionSpawnedPositionPosition2BackValue.get(), AntiBot.positionSpawnedPositionPosition2YValue.get(), 0x0000FF))) RenderUtils.drawAxisAlignedBB(bb.offset(posX + sin * back, posY + y, posZ + cos * back), ColorUtils.applyAlphaChannel(color, alpha))
	}

	override fun clear()
	{
		spawnPositionSuspects.clear()
		spawnPosition.clear()
	}
}
