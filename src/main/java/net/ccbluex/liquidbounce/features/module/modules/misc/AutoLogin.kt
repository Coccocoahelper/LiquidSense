package net.ccbluex.liquidbounce.features.module.modules.misc

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.value.TextValue
import net.minecraft.network.play.server.S02PacketChat
import net.minecraft.network.play.server.S45PacketTitle

@ModuleInfo(name = "AutoLogin", description = "Automatically log-in or register with specified password.", category = ModuleCategory.MISC)
class AutoLogin : Module()
{
    private val password = TextValue("Password", "123123")

    @EventTarget
    fun onPacket(event: PacketEvent)
    {
        val thePlayer = mc.thePlayer ?: return
        val pw = password.get()

        if (event.packet is S02PacketChat)
        {
            val chat = event.packet.chatComponent.unformattedText
            if (chat matches loginPattern) thePlayer.sendChatMessage("/login $pw")
            if (chat matches registerPattern) thePlayer.sendChatMessage("/register $pw")
            if (chat matches registerPattern2) thePlayer.sendChatMessage("/register $pw $pw")
        }
        else if (event.packet is S45PacketTitle)
        {
            val title = event.packet.message?.unformattedText ?: return
            if (title matches loginPattern) thePlayer.sendChatMessage("/login $pw")
            if (title matches registerPattern) thePlayer.sendChatMessage("/register $pw")
            if (title matches registerPattern2) thePlayer.sendChatMessage("/register $pw $pw")
        }
    }

    companion object
    {
        private val loginPattern = Regex("/[lL]ogin [\\[<(].*[]>)]")
        private val registerPattern = Regex("/[rR]egister [\\[<(].*[]>)]")
        private val registerPattern2 = Regex("/[rR]egister [\\[<(].*[]>)] [\\[<(].*[]>)]")
    }
}
