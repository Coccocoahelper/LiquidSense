package net.ccbluex.liquidbounce.features.module.modules.misc

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.event.WorldEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.Notification
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.NotificationIcon
import net.ccbluex.liquidbounce.utils.ClientUtils
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.Item
import net.minecraft.item.ItemBow
import net.minecraft.item.ItemMap

/**
 * LiquidBounce Hacked Client A minecraft forge injection client using Mixin
 *
 * @author CCBlueX
 * @game   Minecraft
 */
@ModuleInfo(name = "MurderDetector", description = "Detects murder in murder mystery.", category = ModuleCategory.MISC)
class MurderDetector : Module()
{
    val murders = mutableSetOf<EntityPlayer>()

    override fun onEnable()
    {
        murders.clear()
    }

    @EventTarget
    fun onUpdate(@Suppress("UNUSED_PARAMETER") event: UpdateEvent)
    {
        val theWorld = mc.theWorld ?: return
        val thePlayer = mc.thePlayer ?: return

        theWorld.loadedEntityList.asSequence().filterIsInstance<EntityPlayer>().filter { it != thePlayer }.filter { it.currentEquippedItem?.item != null }.filter { !murders.contains(it) }.filter { isMurder(it.currentEquippedItem?.item!!) }.forEach {
            murders.add(it)
            ClientUtils.displayChatMessage(thePlayer, "\u00A7a\u00A7l${it.name}\u00A7r is the \u00A74\u00A7lmurderer\u00A7r!")
            LiquidBounce.hud.addNotification(Notification(NotificationIcon.MURDER_MYSTERY, "Murder Detector", "${it.name}\u00A7r is murder!", 5000L))
        }
    }

    @EventTarget
    fun onWorldChange(@Suppress("UNUSED_PARAMETER") event: WorldEvent)
    {
        murders.clear()
    }

    override val tag: String?
        get() = murders.firstOrNull()?.name

    companion object
    {
        fun isMurder(item: Item): Boolean
        {
            return item !is ItemMap && item !is ItemBow && arrayOf("item.ingotGold", "item.arrow", "item.potion", "item.paper", "tile.tnt", "item.web", "item.bed", "item.compass", "item.comparator", "item.shovelWood").none { item.unlocalizedName.equals(it, ignoreCase = true) }
        }
    }
}
