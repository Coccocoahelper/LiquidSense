/*
 * LiquidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/CCBlueX/LiquidBounce/
 */
package net.ccbluex.liquidbounce.features.module.modules.combat

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.utils.InventoryUtils
import net.ccbluex.liquidbounce.utils.PacketUtils.sendPacket
import net.ccbluex.liquidbounce.utils.timer.MSTimer
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.FloatValue
import net.ccbluex.liquidbounce.value.IntegerValue
import net.ccbluex.liquidbounce.value.ListValue
import net.minecraft.client.gui.inventory.GuiInventory
import net.minecraft.init.Items
import net.minecraft.network.play.client.*
import net.minecraft.network.play.client.C07PacketPlayerDigging.Action.DROP_ITEM
import net.minecraft.network.play.client.C16PacketClientStatus.EnumState.OPEN_INVENTORY_ACHIEVEMENT
import net.minecraft.util.BlockPos
import net.minecraft.util.EnumFacing

object AutoSoup : Module("AutoSoup", category = ModuleCategory.COMBAT) {

    private val healthValue = FloatValue("Health", 15f, 0f, 20f)
    private val delayValue = IntegerValue("Delay", 150, 0, 500)
    private val openInventoryValue = BoolValue("OpenInv", false)
    private val simulateInventoryValue = object : BoolValue("SimulateInventory", true) {
        override fun isSupported() = !openInventoryValue.get()
    }
    private val bowlValue = ListValue("Bowl", arrayOf("Drop", "Move", "Stay"), "Drop")

    private val timer = MSTimer()

    override val tag
        get() = healthValue.get().toString()

    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        if (!timer.hasTimePassed(delayValue.get()))
            return

        val thePlayer = mc.thePlayer ?: return

        val soupInHotbar = InventoryUtils.findItem(36, 45, Items.mushroom_stew)

        if (thePlayer.health <= healthValue.get() && soupInHotbar != -1) {
            sendPacket(C09PacketHeldItemChange(soupInHotbar - 36))
            sendPacket(C08PacketPlayerBlockPlacement(thePlayer.inventory.getStackInSlot(soupInHotbar)))

            if (bowlValue.get() == "Drop")
                sendPacket(C07PacketPlayerDigging(DROP_ITEM,
                    BlockPos.ORIGIN, EnumFacing.DOWN))

            sendPacket(C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem))
            timer.reset()
            return
        }

        val bowlInHotbar = InventoryUtils.findItem(36, 45, Items.bowl)
        if (bowlValue.get() == "Move" && bowlInHotbar != -1) {
            if (openInventoryValue.get() && mc.currentScreen !is GuiInventory)
                return

            var bowlMovable = false

            for (i in 9..36) {
                val itemStack = thePlayer.inventory.getStackInSlot(i)

                if (itemStack == null) {
                    bowlMovable = true
                    break
                } else if (itemStack.item == Items.bowl && itemStack.stackSize < 64) {
                    bowlMovable = true
                    break
                }
            }

            if (bowlMovable) {
                val openInventory = mc.currentScreen !is GuiInventory && simulateInventoryValue.get()

                if (openInventory)
                    sendPacket(C16PacketClientStatus(OPEN_INVENTORY_ACHIEVEMENT))

                mc.playerController.windowClick(0, bowlInHotbar, 0, 1, thePlayer)
            }
        }

        val soupInInventory = InventoryUtils.findItem(9, 36, Items.mushroom_stew)

        if (soupInInventory != -1 && InventoryUtils.hasSpaceHotbar()) {
            if (openInventoryValue.get() && mc.currentScreen !is GuiInventory)
                return

            val openInventory = mc.currentScreen !is GuiInventory && simulateInventoryValue.get()
            if (openInventory)
                sendPacket(C16PacketClientStatus(OPEN_INVENTORY_ACHIEVEMENT))

            mc.playerController.windowClick(0, soupInInventory, 0, 1, thePlayer)

            if (openInventory)
                sendPacket(C0DPacketCloseWindow())

            timer.reset()
        }
    }

}