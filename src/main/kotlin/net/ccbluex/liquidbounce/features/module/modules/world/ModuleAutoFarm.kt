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
package net.ccbluex.liquidbounce.features.module.modules.world

import net.ccbluex.liquidbounce.config.ToggleableConfigurable
import net.ccbluex.liquidbounce.event.repeatable
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.modules.player.ModuleBlink
import net.ccbluex.liquidbounce.utils.aiming.RotationManager
import net.ccbluex.liquidbounce.utils.aiming.RotationsConfigurable
import net.ccbluex.liquidbounce.utils.aiming.raycast
import net.ccbluex.liquidbounce.utils.block.getBlock
import net.ccbluex.liquidbounce.utils.block.getCenterDistanceSquared
import net.ccbluex.liquidbounce.utils.block.getState
import net.ccbluex.liquidbounce.utils.block.searchBlocksInCuboid
import net.ccbluex.liquidbounce.utils.client.SilentHotbar
import net.ccbluex.liquidbounce.utils.entity.eyes
import net.ccbluex.liquidbounce.utils.entity.getNearestPoint
import net.ccbluex.liquidbounce.utils.item.getEnchantment
import net.minecraft.block.*
import net.minecraft.client.gui.screen.ingame.HandledScreen
import net.minecraft.enchantment.Enchantments
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.util.ActionResult
import net.minecraft.util.Hand
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.hit.HitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Box
import kotlin.math.abs

/**
 * AutoFarm module
 *
 * Automatically farms stuff for you.
 */
object ModuleAutoFarm : Module("AutoFarm", Category.WORLD) {
    // TODO Fix this entire module-
    private val range by float("Range", 4.5F, 1F..6F)
    private val extraSearchRange by float("extraSearchRange", 0F, 0F..3F)
    private val throughWalls by boolean("ThroughWalls", false)


    private object AutoPlaceCrops : ToggleableConfigurable(this, "AutoPlaceCrops", true) {
        val swapBackDelay by intRange("swapBackDelay", 1..2, 1..20)
    }

    private val rotations = RotationsConfigurable()
    private val fortune by boolean("fortune", true)

    init {
        tree(AutoPlaceCrops)
        tree(rotations)
    }


    // Rotation

    private var currentTarget: BlockPos? = null

    val networkTickHandler = repeatable { _ ->
        if (mc.currentScreen is HandledScreen<*>) {
            return@repeatable
        }
        updateTarget()

        val rotation = RotationManager.currentRotation ?: return@repeatable

        val rayTraceResult = raycast(range.toDouble(), rotation) ?: return@repeatable


        if (ModuleBlink.enabled) {
            return@repeatable
        }

        if (rayTraceResult.type != HitResult.Type.BLOCK
        ) {
            return@repeatable
        }
        val blockPos = rayTraceResult.blockPos

        val state = rayTraceResult.blockPos.getState() ?: return@repeatable
        if(isTargeted(
                state,
                rayTraceResult.blockPos
            )){

            if (!state.isAir) {
                if(fortune){
                    findBestItem (1) { _, itemStack -> itemStack.getEnchantment(Enchantments.FORTUNE) }
                        ?.let { (slot, _) ->
                            SilentHotbar.selectSlotSilently(this, slot, 2)
                        }
                }
                val direction = rayTraceResult.side
                if (mc.interactionManager!!.updateBlockBreakingProgress(blockPos, direction)) {
                    player.swingHand(Hand.MAIN_HAND)
                }
            }
        } else if(isFarmBlock(
                state,
                rayTraceResult.blockPos.offset(rayTraceResult.side).down())){
            val item =
                findClosestItem(
                    if(state.block is FarmlandBlock) {
                        arrayOf(Items.WHEAT_SEEDS, Items.BEETROOT_SEEDS, Items.CARROT, Items.POTATO)
                    } else {
                    arrayOf(Items.NETHER_WART)}
                )

            if(item != null){
                SilentHotbar.selectSlotSilently(this, item, AutoPlaceCrops.swapBackDelay.random())
                placeCrop(rayTraceResult)
            }
        }


    }

    private fun findClosestItem(items: Array<Item>) = (0..8).filter { player.inventory.getStack(it).item in items }
        .minByOrNull { abs(player.inventory.selectedSlot - it) }
    private fun findBestItem(validator: (Int, ItemStack) -> Boolean,
                             sort: (Int, ItemStack) -> Int = { slot, _ -> abs(player.inventory.selectedSlot - slot) }) = (0..8)
        .map {slot -> Pair (slot, player.inventory.getStack(slot)) }
        .filter { (slot, itemStack) -> validator (slot, itemStack) }
        .maxByOrNull { (slot, itemStack) -> sort (slot, itemStack) }


    private fun findBestItem(min: Int, sort: (Int, ItemStack) -> Int) = (0..8)
        .map {slot -> Pair (slot, player.inventory.getStack(slot)) }
        .maxByOrNull { (slot, itemStack) -> sort (slot, itemStack) }
        ?.takeIf {  (slot, itemStack) -> sort(slot, itemStack) >= min }


    private fun placeCrop(rayTraceResult: BlockHitResult){
        val stack = player.mainHandStack
        val count = stack.count
        val interactBlock = interaction.interactBlock(player, Hand.MAIN_HAND, rayTraceResult)

        if (interactBlock.isAccepted) {
            if (interactBlock.shouldSwingHand()) {
                player.swingHand(Hand.MAIN_HAND)

                if (!stack.isEmpty && (stack.count != count || interaction.hasCreativeInventory())) {
                    mc.gameRenderer.firstPersonRenderer.resetEquipProgress(Hand.MAIN_HAND)
                }
            }

            return
        } else if (interactBlock == ActionResult.FAIL) {
            return
        }
    }

    private fun updateTarget() {
        this.currentTarget = null

        val radius = range + extraSearchRange
        val radiusSquared = radius * radius
        val eyesPos = mc.player!!.eyes

        // searches for any blocks within the radius that need to be destroyed, such as crops.
        // If there are no such blocks, it proceeds to check if there are any blocks suitable for placing crops or nether wart on
        val blockToProcess = searchBlocksInCuboid(radius.toInt()) { pos, state ->
            !state.isAir && getNearestPoint(
                eyesPos,
                Box(pos, pos.add(1, 1, 1))
            ).squaredDistanceTo(eyesPos) <= radiusSquared && isTargeted(state, pos)
        }.minByOrNull { it.first.getCenterDistanceSquared() }
            ?: if(AutoPlaceCrops.enabled) {searchBlocksInCuboid(radius.toInt()) { pos, state ->
            !state.isAir && getNearestPoint(
                eyesPos,
                Box(pos, pos.add(1, 1, 1))
            ).squaredDistanceTo(eyesPos) <= radiusSquared && isFarmBlock(state, pos)
        }.minByOrNull { it.first.getCenterDistanceSquared() } ?: return} else return

        val (pos, state) = blockToProcess

        val rt = raytraceBlock(
            player.eyes,
            pos,
            state,
            range = range.toDouble(),
            wallsRange = if (throughWalls) range.toDouble() else 0.0
        )

        // We got a free angle at the block? Cool.
        if (rt != null) {
            val (rotation, _) = rt
            this.currentTarget = pos
            RotationManager.aimAt(rotation, configurable = rotations)
        }
    }

    private fun isTargeted(state: BlockState, pos: BlockPos): Boolean {
        val block = state.block

        return when (block) {
            is GourdBlock -> true
            is CropBlock -> block.isMature(state)
            is NetherWartBlock -> state.get(NetherWartBlock.AGE) >= 3
            is CocoaBlock -> state.get(CocoaBlock.AGE) >= 2
            is SugarCaneBlock -> isAboveLast<SugarCaneBlock>(pos)
            is CactusBlock -> isAboveLast<CactusBlock>(pos)
            is KelpPlantBlock -> isAboveLast<KelpPlantBlock>(pos)
            is BambooBlock -> isAboveLast<BambooBlock>(pos)
            else -> false
        }
    }

    /**
     * checks if the block is either a farmland or soulsand block and has air above it
     */
    private fun isFarmBlock(state: BlockState, pos: BlockPos): Boolean {
        val block = state.block

        return if(when (block) {
            is FarmlandBlock -> true
            is SoulSandBlock -> true
            else -> false
        }) {pos.up().getState()?.isAir == true} else {false}
    }

    private inline fun <reified T : Block> isAboveLast(pos: BlockPos): Boolean {
        return pos.down().getBlock() is T && pos.down(2).getBlock() !is T
    }


}
