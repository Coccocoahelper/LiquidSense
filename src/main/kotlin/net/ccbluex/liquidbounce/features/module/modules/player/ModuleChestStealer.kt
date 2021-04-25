/*
 * This file is part of LiquidBounce (https://github.com/CCBlueX/LiquidBounce)
 *
 * Copyright (c) 2016 - 2021 CCBlueX
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

import net.ccbluex.liquidbounce.event.*
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.utils.client.Chronometer
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen
import net.minecraft.screen.slot.SlotActionType

object ModuleChestStealer : Module("ChestStealer", Category.PLAYER) {

    var delay by intRange("Delay", 50..200, 0..2000)

    val timer = Chronometer()

    val repeatable = handler<GameRenderEvent> {
        if (!timer.hasElapsed())
            return@handler

        val screen = mc.currentScreen

        if (screen !is GenericContainerScreen) {
            return@handler
        }

        val itemsToCollect = ModuleInventoryCleaner.getUsefulItems(screen).shuffled()

        for (slotId in itemsToCollect) {
            mc.interactionManager!!.clickSlot(screen.screenHandler.syncId, slotId, 0, SlotActionType.QUICK_MOVE, player)

            val time = delay.random()

            if (time == 0)
                continue

            timer.waitFor(time.toLong())
            return@handler
        }

        if (itemsToCollect.isEmpty()) {
            player.closeHandledScreen()
        }
    }

}
