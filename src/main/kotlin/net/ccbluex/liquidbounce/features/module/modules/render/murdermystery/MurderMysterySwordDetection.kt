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
 *
 */
package net.ccbluex.liquidbounce.features.module.modules.render.murdermystery

import net.minecraft.block.Blocks
import net.minecraft.item.*

object MurderMysterySwordDetection {
    private val KNOWN_SWORD_ITEMS =
        hashSetOf(
            Items.GOLDEN_CARROT,
            Items.CARROT,
            Items.CARROT_ON_A_STICK,
            Items.BONE,
            Items.TROPICAL_FISH,
            Items.PUFFERFISH,
            Items.SALMON,
            Items.BLAZE_ROD,
            Items.PUMPKIN_PIE,
            Items.NAME_TAG,
            Items.APPLE,
            Items.FEATHER,
            Items.COOKIE,
            Items.SHEARS,
            Items.COOKED_SALMON,
            Items.STICK,
            Items.QUARTZ,
            Items.ROSE_BUSH,
            Items.ICE,
            Items.COOKED_BEEF,
            Items.NETHER_BRICK,
            Items.COOKED_CHICKEN,
            Items.MUSIC_DISC_BLOCKS,
            Items.RED_DYE,
            Items.OAK_BOAT,
            Items.BOOK,
            Items.GLISTERING_MELON_SLICE,
        )
    private val KNOWN_NON_SWORD_ITEMS =
        hashSetOf(
            Items.WOODEN_SHOVEL,
            Items.GOLDEN_SHOVEL,
        )
    private val KNOWN_SWORD_BLOCKS =
        hashSetOf(
            Blocks.SPONGE,
            Blocks.DEAD_BUSH,
            Blocks.REDSTONE_TORCH,
            Blocks.CHORUS_PLANT,
        )

    fun isSword(item: Item?): Boolean {
        return when (item) {
            in KNOWN_NON_SWORD_ITEMS -> false
            in KNOWN_SWORD_ITEMS -> true
            is SwordItem -> true
            is PickaxeItem -> true
            is ShovelItem -> true
            is AxeItem -> true
            is HoeItem -> true
            is BoatItem -> true
            is BlockItem -> this.KNOWN_SWORD_BLOCKS.contains(item.block)
            else -> false
        }
    }
}
