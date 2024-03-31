/*
 * This file is part of LiquidBounce (https://github.com/CCBlueX/LiquidBounce)
 *
 * Copyright (c) 2015 - 2024 CCBlueX
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
package net.ccbluex.liquidbounce.features.module.modules.player

import net.ccbluex.liquidbounce.event.repeatable
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.modules.player.invcleaner.OffHandSlot
import net.ccbluex.liquidbounce.utils.inventory.canCloseMainInventory
import net.ccbluex.liquidbounce.utils.inventory.isPlayerInventory
import net.ccbluex.liquidbounce.utils.inventory.performSwapToHotbar
import net.ccbluex.liquidbounce.utils.inventory.runWithOpenedInventory
import net.ccbluex.liquidbounce.utils.item.findInventorySlot
import net.minecraft.item.ItemStack
import net.minecraft.item.Items

/**
 * AutoTotem module
 *
 * Automatically places a totem in off-hand.
 */

object ModuleAutoTotem : Module("AutoTotem", Category.PLAYER) {

    val repeatable = repeatable {
        if (!player.currentScreenHandler.isPlayerInventory) {
            return@repeatable
        }

        val offHandStack = player.offHandStack

        if (isValidTotem(offHandStack)) {
            return@repeatable
        }

        val slot = findInventorySlot { isValidTotem(it) } ?: return@repeatable

        // todo: use inventory manager instead (?)
        runWithOpenedInventory {
            interaction.performSwapToHotbar(slot, OffHandSlot)

            canCloseMainInventory
        }
    }

    private fun isValidTotem(stack: ItemStack): Boolean {
        return !stack.isEmpty && stack.item == Items.TOTEM_OF_UNDYING
    }

}
