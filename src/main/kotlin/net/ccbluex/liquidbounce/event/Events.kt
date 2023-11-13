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
package net.ccbluex.liquidbounce.event

import io.netty.channel.ChannelPipeline
import net.ccbluex.liquidbounce.config.Value
import net.ccbluex.liquidbounce.features.chat.client.packet.User
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.utils.client.ForcedState
import net.ccbluex.liquidbounce.utils.client.Nameable
import net.ccbluex.liquidbounce.utils.movement.DirectionalInput
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.option.KeyBinding
import net.minecraft.client.util.InputUtil
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.client.world.ClientWorld
import net.minecraft.entity.Entity
import net.minecraft.entity.MovementType
import net.minecraft.network.packet.Packet
import net.minecraft.text.Text
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import net.minecraft.util.shape.VoxelShape
import kotlin.reflect.KClass
import kotlin.reflect.full.findAnnotation

/**
 * Contains all classes of events. Used to create lookup tables ahead of time
 */
val ALL_EVENT_CLASSES: Array<KClass<out Event>> = arrayOf(
    GameTickEvent::class,
    BlockChangeEvent::class,
    ChunkLoadEvent::class,
    ChunkUnloadEvent::class,
    WorldDisconnectEvent::class,
    GameRenderEvent::class,
    WorldRenderEvent::class,
    EngineRenderEvent::class,
    OverlayRenderEvent::class,
    ScreenRenderEvent::class,
    WindowResizeEvent::class,
    WindowFocusEvent::class,
    MouseButtonEvent::class,
    MouseScrollEvent::class,
    MouseCursorEvent::class,
    KeyboardKeyEvent::class,
    KeyboardCharEvent::class,
    InputHandleEvent::class,
    MovementInputEvent::class,
    KeyEvent::class,
    MouseRotationEvent::class,
    KeyBindingEvent::class,
    AttackEvent::class,
    SessionEvent::class,
    ScreenEvent::class,
    ChatSendEvent::class,
    ChatReceiveEvent::class,
    UseCooldownEvent::class,
    BlockShapeEvent::class,
    BlockBreakingProgressEvent::class,
    BlockVelocityMultiplierEvent::class,
    BlockSlipperinessMultiplierEvent::class,
    EntityMarginEvent::class,
    HealthUpdateEvent::class,
    DeathEvent::class,
    PlayerTickEvent::class,
    PlayerMovementTickEvent::class,
    PlayerNetworkMovementTickEvent::class,
    PlayerPushOutEvent::class,
    PlayerMoveEvent::class,
    PlayerJumpEvent::class,
    PlayerUseMultiplier::class,
    PlayerVelocityStrafe::class,
    PlayerStrideEvent::class,
    PlayerSafeWalkEvent::class,
    CancelBlockBreakingEvent::class,
    PlayerStepEvent::class,
    FluidPushEvent::class,
    TickJumpEvent::class,
    PipelineEvent::class,
    PacketEvent::class,
    ClientStartEvent::class,
    ClientShutdownEvent::class,
    ValueChangedEvent::class,
    ToggleModuleEvent::class,
    NotificationEvent::class,
    ClientChatMessageEvent::class,
    ClientChatErrorEvent::class,
    StateUpdateEvent::class,
    WorldChangeEvent::class,
    AltManagerUpdateEvent::class
)

/**
 * Retrieves the name that the event is supposed to be associated with in JavaScript.
 */
val KClass<out Event>.liquidBounceEventJsName: String
    get() = this.findAnnotation<Nameable>()!!.name

// Game events

@Nameable("gameTick")
class GameTickEvent : Event()

// Render events
@Nameable("blockChange")
class BlockChangeEvent(val blockPos: BlockPos, val newState: BlockState) : Event()

@Nameable("chunkLoad")
class ChunkLoadEvent(val x: Int, val z: Int) : Event()

@Nameable("chunkUnload")
class ChunkUnloadEvent(val x: Int, val z: Int) : Event()

@Nameable("worldDisconnect")
class WorldDisconnectEvent : Event()

@Nameable("worldChange")
class WorldChangeEvent(val world: ClientWorld?) : Event()

@Nameable("gameRender")
class GameRenderEvent : Event()

@Nameable("worldRender")
class WorldRenderEvent(val matrixStack: MatrixStack, val partialTicks: Float) : Event()

@Nameable("engineRender")
class EngineRenderEvent(val tickDelta: Float) : Event()

@Nameable("overlayRender")
class OverlayRenderEvent(val context: DrawContext, val tickDelta: Float) : Event()

@Nameable("screenRender")
class ScreenRenderEvent(val screen: Screen, val context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) : Event()

@Nameable("windowResize")
class WindowResizeEvent(val window: Long, val width: Int, val height: Int) : Event()

@Nameable("windowFocus")
class WindowFocusEvent(val window: Long, val focused: Boolean) : Event()

@Nameable("mouseButton")
class MouseButtonEvent(val window: Long, val button: Int, val action: Int, val mods: Int) : Event()

@Nameable("mouseScroll")
class MouseScrollEvent(val window: Long, val horizontal: Double, val vertical: Double) : Event()

@Nameable("mouseCursor")
class MouseCursorEvent(val window: Long, val x: Double, val y: Double) : Event()

@Nameable("keyboardKey")
class KeyboardKeyEvent(val window: Long, val keyCode: Int, val scancode: Int, val action: Int, val mods: Int) : Event()

@Nameable("keyboardChar")
class KeyboardCharEvent(val window: Long, val codepoint: Int) : Event()

// Input events
@Nameable("inputHandle")
class InputHandleEvent : Event()

@Nameable("movementInput")
class MovementInputEvent(var directionalInput: DirectionalInput, var jumping: Boolean) : Event()

@Nameable("key")
class KeyEvent(val key: InputUtil.Key, val action: Int, val mods: Int) : Event()

@Nameable("mouseRotation")
class MouseRotationEvent(var cursorDeltaX: Double, var cursorDeltaY: Double) : CancellableEvent()

@Nameable("keyBinding")
class KeyBindingEvent(var key: KeyBinding) : Event()

// User action events
@Nameable("attack")
class AttackEvent(val enemy: Entity) : Event()

@Nameable("session")
class SessionEvent : Event()

@Nameable("screen")
class ScreenEvent(val screen: Screen?) : CancellableEvent()

@Nameable("chatSend")
class ChatSendEvent(val message: String) : CancellableEvent()

@Nameable("chatReceive")
class ChatReceiveEvent(val message: String, val textData: Text, val type: ChatType) : Event() {

    enum class ChatType {
        CHAT_MESSAGE, DISGUISED_CHAT_MESSAGE, GAME_MESSAGE
    }

}

@Nameable("useCooldown")
class UseCooldownEvent(var cooldown: Int) : Event()

// World events
@Nameable("blockShape")
class BlockShapeEvent(val state: BlockState, val pos: BlockPos, var shape: VoxelShape) : Event()

@Nameable("blockBreakingProgress")
class BlockBreakingProgressEvent(val pos: BlockPos) : Event()

@Nameable("blockVelocityMultiplier")
class BlockVelocityMultiplierEvent(val block: Block, var multiplier: Float) : Event()

@Nameable("blockSlipperinessMultiplier")
class BlockSlipperinessMultiplierEvent(val block: Block, var slipperiness: Float) : Event()

// Entity events
@Nameable("entityMargin")
class EntityMarginEvent(val entity: Entity, var margin: Float) : Event()

// Entity events bound to client-user entity
@Nameable("healthUpdate")
class HealthUpdateEvent(val health: Float, val food: Int, val saturation: Float, val previousHealth: Float) : Event()

@Nameable("death")
class DeathEvent : Event()

@Nameable("playerTick")
class PlayerTickEvent : Event()

@Nameable("playerMovementTick")
class PlayerMovementTickEvent : Event()

@Nameable("playerNetworkMovementTick")
class PlayerNetworkMovementTickEvent(val state: EventState) : Event()

@Nameable("playerPushOut")
class PlayerPushOutEvent : CancellableEvent()

@Nameable("playerMove")
class PlayerMoveEvent(val type: MovementType, val movement: Vec3d) : Event()

@Nameable("playerJump")
class PlayerJumpEvent(var motion: Float) : CancellableEvent()

@Nameable("playerUseMultiplier")
class PlayerUseMultiplier(var forward: Float, var sideways: Float) : Event()

@Nameable("playerStrafe")
class PlayerVelocityStrafe(val movementInput: Vec3d, val speed: Float, val yaw: Float, var velocity: Vec3d) : Event()

@Nameable("playerStride")
class PlayerStrideEvent(var strideForce: Float) : Event()

@Nameable("playerSafeWalk")
class PlayerSafeWalkEvent(var isSafeWalk: Boolean = false) : Event()

@Nameable("cancelBlockBreaking")
class CancelBlockBreakingEvent : CancellableEvent()

@Nameable("playerStep")
class PlayerStepEvent(var height: Float) : Event()

@Nameable("fluidPush")
class FluidPushEvent : CancellableEvent()

@Nameable("tickJump")
class TickJumpEvent : Event()

// Network events

@Nameable("pipeline")
class PipelineEvent(val channelPipeline: ChannelPipeline) : Event()

@Nameable("packet")
class PacketEvent(val origin: TransferOrigin, val packet: Packet<*>, val original: Boolean = true) : CancellableEvent()

enum class TransferOrigin {
    SEND, RECEIVE
}

// Client events
@Nameable("clientStart")
class ClientStartEvent : Event()

@Nameable("clientShutdown")
class ClientShutdownEvent : Event()

@Nameable("valueChanged")
class ValueChangedEvent(val value: Value<*>) : Event()

@Nameable("toggleModule")
class ToggleModuleEvent(val module: Module, val newState: Boolean, val ignoreCondition: Boolean = false) : Event()

@Nameable("notification")
class NotificationEvent(val title: String, val message: String, val severity: Severity) : Event() {
    enum class Severity {
        INFO, SUCCESS, ERROR, ENABLED, DISABLED
    }
}

@Nameable("clientChatMessage")
class ClientChatMessageEvent(val user: User, val message: String, val chatGroup: ChatGroup) : Event() {
    enum class ChatGroup {
        PUBLIC_CHAT, PRIVATE_CHAT
    }
}

@Nameable("clientChatError")
class ClientChatErrorEvent(val error: String) : Event()

@Nameable("stateUpdate")
class StateUpdateEvent : Event() {
    val state: ForcedState = ForcedState()
}

@Nameable("altManagerUpdate")
class AltManagerUpdateEvent(val success: Boolean, val message: String) : Event()