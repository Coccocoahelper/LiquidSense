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
package net.ccbluex.liquidbounce.features.module.modules.player

import net.ccbluex.liquidbounce.event.events.StateUpdateEvent
import net.ccbluex.liquidbounce.event.handler
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.utils.entity.isCloseToEdge

/**
 * An eagle module
 *
 * Legit trick to build faster.
 */
object ModuleEagle : Module("Eagle", Category.PLAYER) {

    val edgeDistance by float("EagleEdgeDistance", 0.01f, 0.01f..1.3f)

    val repeatable = handler<StateUpdateEvent> {
        // Check if player is on the edge and is NOT flying
        if (player.isCloseToEdge(edgeDistance.toDouble()) && !player.abilities.flying) {
            it.state.enforceEagle = true
        }
    }

}