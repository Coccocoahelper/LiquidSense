/*
 * This file is part of LiquidBounce (https://github.com/CCBlueX/LiquidBounce)
 *
 * Copyright (c) 2015 - 2023 CCBlueX
 *
 * LiquidBounce is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * LiquidBounce is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with LiquidBounce. If not, see <https://www.gnu.org/licenses/>.
 */
package net.ccbluex.liquidbounce.features.module.modules.combat

import net.ccbluex.liquidbounce.config.NamedChoice
import net.ccbluex.liquidbounce.event.repeatable
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.utils.client.SilentHotbar
import net.ccbluex.liquidbounce.utils.combat.pauseCombat
import net.ccbluex.liquidbounce.utils.item.InventoryConstraintsConfigurable
import net.ccbluex.liquidbounce.utils.item.findHotbarSlot
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen
import net.minecraft.client.gui.screen.ingame.InventoryScreen
import net.minecraft.item.Items
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket
import net.minecraft.screen.slot.SlotActionType
import net.minecraft.util.Hand

/**
 * AutoSoup module
 *
 * Automatically eats soup whenever your health is low.
 */

object ModuleAutoSoup : Module("AutoSoup", Category.COMBAT) {

    private val bowl by boolean("DropAfterUse", true)
    private val health by int("Health", 15, 1..20)
    private val delay by int("Delay", 0, 0..15)
    private val inventoryConstraints = tree(InventoryConstraintsConfigurable())
    private val swapPreviousDelay by int("SwapPreviousDelay", 5, 1..100)

    val repeatable = repeatable {
        val mushroomStewSlot = findHotbarSlot(Items.MUSHROOM_STEW)
        val bowlHotbarSlot = findHotbarSlot(Items.BOWL)

        if (interaction.hasRidingInventory()) {
            return@repeatable
        }
        if (player.health < health && mushroomStewSlot != null) {
            // we need to take some actions
            pauseCombat = true
            if (player.isBlocking) {
                waitUntil { !player.isBlocking }
            }
            wait { inventoryConstraints.delay.random() }

            if (mushroomStewSlot != player.inventory.selectedSlot) {
                SilentHotbar.selectSlotSilently(this, mushroomStewSlot, swapPreviousDelay)
            }

            // uses soup
            interaction.sendSequencedPacket(world) { sequence ->
                PlayerInteractItemC2SPacket(Hand.MAIN_HAND, sequence)
            }

            // checks if using it, succeeded, if so - drop an empty bowl
            if (bowl && player.inventory.getStack(mushroomStewSlot).item == Items.BOWL)
                mc.interactionManager!!.clickSlot(0, mushroomStewSlot, 1, SlotActionType.THROW, player)
            return@repeatable
        } else {
            wait { delay }
            pauseCombat = false
        }
    }

    override fun disable() {
        pauseCombat = false
    }

    enum class BowlMode(override val choiceName: String) : NamedChoice {
        DROP("Drop"), MOVE("Move")
    }
}
