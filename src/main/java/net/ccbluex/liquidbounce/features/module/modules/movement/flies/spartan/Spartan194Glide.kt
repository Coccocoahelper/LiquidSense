package net.ccbluex.liquidbounce.features.module.modules.movement.flies.spartan

import net.ccbluex.liquidbounce.features.module.modules.movement.flies.FlyMode
import net.ccbluex.liquidbounce.utils.extensions.sendPacketWithoutEvent
import net.ccbluex.liquidbounce.utils.extensions.strafe
import net.minecraft.network.play.client.C03PacketPlayer.C04PacketPlayerPosition

class Spartan194Glide : FlyMode("Spartan194Glide")
{
    override fun onUpdate()
    {
        val thePlayer = mc.thePlayer ?: return
        val networkManager = mc.netHandler.networkManager

        val x = thePlayer.posX
        val y = thePlayer.posY
        val z = thePlayer.posZ

        thePlayer.strafe(0.264f)

        if (thePlayer.ticksExisted % 8 == 0) networkManager.sendPacketWithoutEvent(C04PacketPlayerPosition(x, y + 10, z, true))
    }
}
