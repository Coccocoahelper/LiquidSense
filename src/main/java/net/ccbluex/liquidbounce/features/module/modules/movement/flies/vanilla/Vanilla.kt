/*
 * LiquidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/CCBlueX/LiquidBounce/
 */
package net.ccbluex.liquidbounce.features.module.modules.movement.flies.vanilla

import net.ccbluex.liquidbounce.event.MoveEvent
import net.ccbluex.liquidbounce.features.module.modules.movement.Fly
import net.ccbluex.liquidbounce.features.module.modules.movement.flies.FlyMode
import net.ccbluex.liquidbounce.utils.MovementUtils.strafe
import net.ccbluex.liquidbounce.utils.PacketUtils.sendPacket
import net.minecraft.network.play.client.C00PacketKeepAlive

class Vanilla : FlyMode("Vanilla") {
    override fun onMotion() {}

    override fun onEnable() {}

    override fun onDisable() {
        super.onDisable()
    }

    override fun onUpdate() {
        val thePlayer = mc.thePlayer
        if (Fly.vanillaKeepAlive) sendPacket(C00PacketKeepAlive())
        thePlayer.capabilities.isFlying = false
        thePlayer.motionY = Fly.vanillaY
        thePlayer.motionX = 0.0
        thePlayer.motionZ = 0.0
        if (mc.gameSettings.keyBindJump.isKeyDown) thePlayer.motionY += Fly.vanillaUpwardsY
        if (mc.gameSettings.keyBindSneak.isKeyDown) thePlayer.motionY -= Fly.vanillaDownwardsY
        strafe(Fly.vanillaSpeed)
        mc.timer.timerSpeed = Fly.vanillaTimer
        //Fly.handleVanillaKickBypass()
    }

    override fun onMove(event: MoveEvent) {}
}
