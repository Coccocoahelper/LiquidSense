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
package net.ccbluex.liquidbounce.features.module.modules.render

import net.ccbluex.liquidbounce.config.Choice
import net.ccbluex.liquidbounce.config.ChoiceConfigurable
import net.ccbluex.liquidbounce.event.events.DrawGlowEvent
import net.ccbluex.liquidbounce.event.events.WorldRenderEvent
import net.ccbluex.liquidbounce.event.handler
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.render.GenericColorMode
import net.ccbluex.liquidbounce.render.GenericRainbowColorMode
import net.ccbluex.liquidbounce.render.GenericStaticColorMode
import net.ccbluex.liquidbounce.render.MultiColorBoxRenderer
import net.ccbluex.liquidbounce.render.engine.Color4b
import net.ccbluex.liquidbounce.render.renderEnvironmentForWorld
import net.ccbluex.liquidbounce.render.withPositionRelativeToCamera
import net.ccbluex.liquidbounce.utils.block.AbstractBlockLocationTracker
import net.ccbluex.liquidbounce.utils.block.ChunkScanner
import net.ccbluex.liquidbounce.utils.block.getState
import net.ccbluex.liquidbounce.utils.item.findBlocksEndingWith
import net.ccbluex.liquidbounce.utils.math.toBlockPos
import net.minecraft.block.BlockState
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Box
import net.minecraft.util.math.Vec3d

/**
 * BlockESP module
 *
 * Allows you to see selected blocks through walls.
 */

object ModuleBlockESP : Module("BlockESP", Category.RENDER) {

    private val modes = choices("Mode", Glow, arrayOf(Box, Glow))
    private val targets by blocks("Targets",
        findBlocksEndingWith("_BED", "DRAGON_EGG").toHashSet()).onChange {
        if (enabled) {
            disable()
            enable()
        }
        it
    }

    private val colorMode = choices(
        "ColorMode",
        { it.choices[0] },
        { arrayOf(MapColorMode, GenericStaticColorMode(it, Color4b(255, 179, 72, 50)), GenericRainbowColorMode(it)) }
    )

    private val fullBox = Box(0.0, 0.0, 0.0, 1.0, 1.0, 1.0)

    private object Box : Choice("Box") {
        override val parent: ChoiceConfigurable
            get() = modes

        private val outline by boolean("Outline", true)

        val renderHandler = handler<WorldRenderEvent> { event ->
            val matrixStack = event.matrixStack

            drawBoxMode(matrixStack, this.outline, false)
        }

        fun drawBoxMode(matrixStack: MatrixStack, drawOutline: Boolean, fullAlpha: Boolean): Boolean {
            val colorMode = colorMode.activeChoice as GenericColorMode<Pair<BlockPos, BlockState>>

            val boxRenderer = MultiColorBoxRenderer()

            var dirty = false

            renderEnvironmentForWorld(matrixStack) {
                synchronized(BlockTracker.trackedBlockMap) {
                    for (pos in BlockTracker.trackedBlockMap.keys) {
                        val vec3d = Vec3d(pos.x.toDouble(), pos.y.toDouble(), pos.z.toDouble())

                        val blockPos = vec3d.toBlockPos()
                        val blockState = blockPos.getState() ?: continue

                        if (blockState.isAir) {
                            continue
                        }

                        val outlineShape = blockState.getOutlineShape(world, blockPos)
                        val boundingBox = if (outlineShape.isEmpty) {
                            fullBox
                        } else {
                            outlineShape.boundingBox
                        }

                        var color = colorMode.getColor(blockPos to blockState)

                        if (fullAlpha) {
                            color = color.alpha(255)
                        }

                        withPositionRelativeToCamera(vec3d) {
                            boxRenderer.drawBox(
                                this,
                                boundingBox,
                                faceColor = color,
                                outlineColor = color.alpha(150).takeIf { drawOutline }
                            )
                        }

                        dirty = true
                    }
                }

                boxRenderer.draw()
            }

            return dirty
        }
    }

    private object Glow : Choice("Glow") {
        override val parent: ChoiceConfigurable
            get() = modes

        val renderHandler = handler<DrawGlowEvent> { event ->
            val dirty = Box.drawBoxMode(event.matrixStack, drawOutline = false, fullAlpha = true)

            if (dirty)
                event.markDirty()
        }
    }

    private object MapColorMode : Choice("MapColor"), GenericColorMode<Pair<BlockPos, BlockState>> {
        override val parent: ChoiceConfigurable
            get() = colorMode

        override fun getColor(param: Pair<BlockPos, BlockState>?): Color4b {
            val (pos, state) = param!!

            return Color4b(state.getMapColor(mc.world!!, pos).color).alpha(100)
        }
    }

    override fun enable() {
        ChunkScanner.subscribe(BlockTracker)
    }

    override fun disable() {
        ChunkScanner.unsubscribe(BlockTracker)
    }

    private object TrackedState

    private object BlockTracker : AbstractBlockLocationTracker<TrackedState>() {
        override fun getStateFor(pos: BlockPos, state: BlockState): TrackedState? {
            return if (!state.isAir && targets.contains(state.block)) {
                TrackedState
            } else {
                null
            }
        }

    }

}
