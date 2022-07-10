package net.ccbluex.liquidbounce.features.module.modules.misc.antibot.movement

import net.ccbluex.liquidbounce.features.module.modules.misc.AntiBot
import net.ccbluex.liquidbounce.features.module.modules.misc.antibot.BotCheck
import net.ccbluex.liquidbounce.utils.misc.StringUtils
import net.minecraft.client.multiplayer.WorldClient
import net.minecraft.entity.Entity
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.util.Vec3
import kotlin.math.hypot

class HorizontalSpeedCheck : BotCheck("move.hspeed")
{
    override val isActive: Boolean
        get() = AntiBot.hspeedEnabledValue.get()

    private val vl = mutableMapOf<Int, Int>()

    override fun isBot(theWorld: WorldClient, thePlayer: Entity, target: EntityPlayer): Boolean
    {
        val entityId = target.entityId
        return entityId in vl && (!AntiBot.hspeedVLEnabledValue.get() || (vl[entityId] ?: 0) >= AntiBot.hspeedVLLimitValue.get())
    }

    override fun onEntityMove(theWorld: WorldClient, thePlayer: EntityPlayer, target: EntityPlayer, isTeleport: Boolean, newPos: Vec3, rotating: Boolean, newYaw: Float, newPitch: Float, onGround: Boolean)
    {
        val entityId = target.entityId
        val speed = hypot(target.posX - newPos.xCoord, target.posZ - newPos.zCoord)
        if (speed > AntiBot.hspeedLimitValue.get())
        {
            notification(target) { arrayOf("delta=${StringUtils.DECIMALFORMAT_6.format(speed)}") }
            vl[entityId] = (vl[entityId] ?: 0) + 2
        }
        else if (AntiBot.hspeedVLDecValue.get())
        {
            val currentVL = (vl[entityId] ?: 0) - 1
            if (currentVL <= 0) vl.remove(entityId) else vl[entityId] = currentVL
        }
    }

    override fun clear()
    {
        vl.clear()
    }
}
