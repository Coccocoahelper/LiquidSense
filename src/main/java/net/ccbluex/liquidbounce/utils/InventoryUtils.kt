/*
 * LiquidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/CCBlueX/LiquidBounce/
 */
package net.ccbluex.liquidbounce.utils

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.Listenable
import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.utils.timer.MSTimer
import net.minecraft.block.BlockBush
import net.minecraft.init.Blocks
import net.minecraft.item.Item
import net.minecraft.item.ItemBlock
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement
import net.minecraft.network.play.client.C09PacketHeldItemChange
import net.minecraft.network.play.client.C0DPacketCloseWindow
import net.minecraft.network.play.client.C16PacketClientStatus
import net.minecraft.network.play.client.C16PacketClientStatus.EnumState.OPEN_INVENTORY_ACHIEVEMENT
import net.minecraft.network.play.server.S2EPacketCloseWindow

object InventoryUtils : MinecraftInstance(), Listenable {

    // What slot is selected on server-side?
    var slot = -1

    // Is inventory open on server-side?
    var openInventory = false

    var CLICK_TIMER = MSTimer()

    val BLOCK_BLACKLIST = listOf(
        Blocks.chest,
        Blocks.ender_chest,
        Blocks.trapped_chest,
        Blocks.anvil,
        Blocks.sand,
        Blocks.web,
        Blocks.torch,
        Blocks.crafting_table,
        Blocks.furnace,
        Blocks.waterlily,
        Blocks.dispenser,
        Blocks.stone_pressure_plate,
        Blocks.wooden_pressure_plate,
        Blocks.noteblock,
        Blocks.dropper,
        Blocks.tnt,
        Blocks.standing_banner,
        Blocks.wall_banner,
        Blocks.redstone_torch
    )

    fun findItem(startInclusive: Int, endInclusive: Int, item: Item): Int? {
        for (i in startInclusive..endInclusive)
            if (mc.thePlayer.inventoryContainer.getSlot(i).stack?.item == item)
                return i

        return null
    }

    fun hasSpaceHotbar(): Boolean {
        for (i in 36..44)
            mc.thePlayer.inventoryContainer.getSlot(i).stack ?: return true

        return false
    }

    fun findBlockInHotbar(): Int? {
        for (i in 36..44) {
            val itemStack = mc.thePlayer.inventoryContainer.getSlot(i).stack ?: continue

            if (itemStack.item is ItemBlock && itemStack.stackSize > 0) {
                val itemBlock = itemStack.item as ItemBlock
                val block = itemBlock.block

                if (block.isFullCube && block !in BLOCK_BLACKLIST && block !is BlockBush) return i
            }
        }

        for (i in 36..44) {
            val itemStack = mc.thePlayer.inventoryContainer.getSlot(i).stack ?: continue

            if (itemStack.item is ItemBlock && itemStack.stackSize > 0) {
                val itemBlock = itemStack.item as ItemBlock
                val block = itemBlock.block

                if (block !in BLOCK_BLACKLIST && block !is BlockBush) return i
            }
        }

        return null
    }

    @EventTarget
    fun onPacket(event: PacketEvent) {
        if (event.isCancelled) return

        val packet = event.packet

        when (packet) {
            is C08PacketPlayerBlockPlacement -> CLICK_TIMER.reset()

            is C16PacketClientStatus ->
                if (packet.status == OPEN_INVENTORY_ACHIEVEMENT)
                    openInventory = true

            is C0DPacketCloseWindow, is S2EPacketCloseWindow -> openInventory = false

            is C09PacketHeldItemChange ->
                if (packet.slotId == slot) event.cancelEvent()
                else slot = packet.slotId
        }
    }

    override fun handleEvents() = true
}
